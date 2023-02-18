package xyz.kyngs.librelogin.paper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.injector.temporary.TemporaryPlayerFactory;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedProfilePublicKey;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mojang.datafixers.util.Either;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.common.config.ConfigurationKeys;
import xyz.kyngs.librelogin.common.listener.AuthenticListeners;
import xyz.kyngs.librelogin.common.util.GeneralUtil;
import xyz.kyngs.librelogin.paper.protocollib.ClientPublicKey;
import xyz.kyngs.librelogin.paper.protocollib.EncryptionUtil;
import xyz.kyngs.librelogin.paper.protocollib.ProtocolListener;

import javax.crypto.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.comphenix.protocol.PacketType.Login.Client.START;
import static com.comphenix.protocol.PacketType.Login.Server.DISCONNECT;

public class PaperListeners extends AuthenticListeners<PaperLibreLogin, Player, World> implements Listener {

    private static final String ENCRYPTION_CLASS_NAME = "MinecraftEncryption";
    private static final Class<?> ENCRYPTION_CLASS;
    private static Method encryptMethod;
    private static Method cipherMethod;

    static {
        ENCRYPTION_CLASS = MinecraftReflection.getMinecraftClass(
                "util." + ENCRYPTION_CLASS_NAME, ENCRYPTION_CLASS_NAME
        );
    }

    private final KeyPair keyPair = EncryptionUtil.generateKeyPair();
    private final Random random = new SecureRandom();
    private final Cache<String, EncryptionData> encryptionDataCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();
    private final FloodgateHelper floodgateHelper;
    private final Cache<Player, String> ipCache;
    private final Cache<UUID, User> readOnlyUserCache;
    private final Cache<Player, Location> spawnLocationCache;

    public PaperListeners(PaperLibreLogin plugin) {
        super(plugin);

        floodgateHelper = this.plugin.floodgateEnabled() ? new FloodgateHelper() : null;

        new ProtocolListener(this, this.plugin);

        ipCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();

        readOnlyUserCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();

        spawnLocationCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    public Cache<Player, Location> getSpawnLocationCache() {
        return spawnLocationCache;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        GeneralUtil.runAsync(() -> onPlayerDisconnect(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPostLogin(PlayerLoginEvent event) {
        ipCache.put(event.getPlayer(), event.getAddress().getHostName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        var data = readOnlyUserCache.getIfPresent(event.getPlayer().getUniqueId());
        if (data == null) {
            event.getPlayer().kick(Component.text("Internal error, please try again later."));
            return;
        }
        readOnlyUserCache.invalidate(event.getPlayer().getUniqueId());
        onPostLogin(event.getPlayer(), data);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        var existing = event.getPlayerProfile();

        if (plugin.fromFloodgate(existing.getId())) return;

        var profile = plugin.getDatabaseProvider().getByName(event.getName());

        //Going to solve it when they remove it
        //noinspection removal
        existing.setId(profile.getUuid());

        readOnlyUserCache.put(profile.getUuid(), profile);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void chooseWorld(PlayerSpawnLocationEvent event) {
        var world = chooseServer(event.getPlayer(), ipCache.getIfPresent(event.getPlayer()), readOnlyUserCache.getIfPresent(event.getPlayer().getUniqueId()));
        ipCache.invalidate(event.getPlayer());
        spawnLocationCache.invalidate(event.getPlayer());
        if (world == null) {
            event.getPlayer().kick(Component.text("Internal error"));
        } else {
            var playedBefore = event.getPlayer().hasPlayedBefore();
            //This is terrible, but should work
            if (event.getPlayer().hasPlayedBefore() && !plugin.getConfiguration().get(ConfigurationKeys.LIMBO).contains(event.getSpawnLocation().getWorld().getName())) {
                if (plugin.getConfiguration().get(ConfigurationKeys.LIMBO).contains(world.getName())) {
                    spawnLocationCache.put(event.getPlayer(), event.getSpawnLocation());
                } else {
                    return;
                }
            }

            event.setSpawnLocation(world.getSpawnLocation());

        }
    }

    //Unused, might be useful in the future
    public void setUUID(Player player, String username) {
        var profile = plugin.getDatabaseProvider().getByName(username);

        try {
            var network = getNetworkManager(player);

            var clazz = network.getClass();
            var accessor = Accessors.getFieldAccessorOrNull(clazz, "spoofedUUID", UUID.class);
            accessor.set(network, profile.getUuid());
        } catch (Exception e) {
            e.printStackTrace();
            kickPlayer("Internal error", player);
        }
    }

    public void onPacketReceive(PacketEvent event) {
        var sender = event.getPlayer();
        var type = event.getPacketType();
        var packet = event.getPacket();

        if (type == PacketType.Login.Client.START) {
            var sessionKey = sender.getAddress().toString();

            encryptionDataCache.invalidate(sessionKey);

            if (plugin.floodgateEnabled()) {
                var success = floodgateHelper.processFloodgateTasks(event);
                // don't continue execution if the player was kicked by Floodgate
                if (!success) {
                    return;
                }
            }
            var username = getUsername(packet);

            Optional<ClientPublicKey> clientKey;

            if (MinecraftVersion.atOrAbove(new MinecraftVersion(1, 19, 3))) {
                clientKey = Optional.empty();
            } else {
                var profileKey = packet.getOptionals(BukkitConverters.getWrappedPublicKeyDataConverter())
                        .optionRead(0);

                clientKey = profileKey.flatMap(Function.identity()).flatMap(data -> {
                    var expires = data.getExpireTime();
                    var key = data.getKey();
                    var signature = data.getSignature();
                    return Optional.of(new ClientPublicKey(expires, key, signature));
                });
            }

            try {
                if (plugin.fromFloodgate(username)) return; //Floodgate player, won't handle it
                var preLoginResult = onPreLogin(username);
                switch (preLoginResult.state()) {
                    case DENIED -> {
                        assert preLoginResult.message() != null;
                        kickPlayer(LegacyComponentSerializer.legacySection().serialize(preLoginResult.message()), sender);
                    }
                    case FORCE_ONLINE -> {
                        byte[] token;
                        try {
                            token = EncryptionUtil.generateVerifyToken(random);

                            var newPacket = new PacketContainer(PacketType.Login.Server.ENCRYPTION_BEGIN);

                            newPacket.getStrings().write(0, "");

                            var keyModifier = newPacket.getSpecificModifier(PublicKey.class);

                            var verifyField = 0;
                            if (keyModifier.getFields().isEmpty()) {
                                verifyField++;
                                newPacket.getByteArrays().write(0, keyPair.getPublic().getEncoded());
                            } else {
                                keyModifier.write(0, keyPair.getPublic());
                            }

                            newPacket.getByteArrays().write(verifyField, token);

                            encryptionDataCache.put(sender.getAddress().toString(), new EncryptionData(username, token, clientKey.orElse(null)));

                            ProtocolLibrary.getProtocolManager().sendServerPacket(sender, newPacket);
                        } catch (Exception e) {
                            plugin.getLogger().error("Failed to send encryption begin packet for player " + username + "! Falling back to offline mode.");
                            e.printStackTrace();
                            return;
                        }

                        synchronized (event.getAsyncMarker().getProcessingLock()) {
                            event.setCancelled(true);
                        }
                    }
                }
            } finally {
                ProtocolLibrary.getProtocolManager().getAsynchronousManager().signalPacketTransmission(event);
            }
        } else {
            var sharedSecret = packet.getByteArrays().read(0);

            var data = encryptionDataCache.getIfPresent(sender.getAddress().toString());

            if (data == null) {
                kickPlayer("Illegal encryption state", sender);
                return;
            }

            var expectedToken = data.token().clone();

            if (!verifyNonce(packet, data.publicKey(), expectedToken)) {
                kickPlayer("Invalid nonce", sender);
            }

            //Verify session

            try {
                var privateKey = keyPair.getPrivate();

                SecretKey loginKey;

                try {
                    loginKey = EncryptionUtil.decryptSharedKey(privateKey, sharedSecret);
                } catch (GeneralSecurityException securityEx) {
                    kickPlayer("Cannot decrypt shared secret", sender);
                    return;
                }

                try {
                    if (!enableEncryption(loginKey, sender)) {
                        return;
                    }
                } catch (Exception e) {
                    kickPlayer("Cannot decrypt shared secret", sender);
                    return;
                }

                var serverId = EncryptionUtil.getServerIdHashString("", loginKey, keyPair.getPublic());
                var username = data.username();
                var address = sender.getAddress();

                try {
                    if (hasJoined(username, serverId, address.getAddress())) {
                        receiveFakeStartPacket(username, data.publicKey(), sender);
                    } else {
                        kickPlayer("Invalid session", sender);
                    }
                } catch (IOException e) {
                    kickPlayer("Cannot verify session", sender);
                }
            } finally {
                //this is a fake packet; it shouldn't be sent to the server
                synchronized (event.getAsyncMarker().getProcessingLock()) {
                    event.setCancelled(true);
                }

                ProtocolLibrary.getProtocolManager().getAsynchronousManager().signalPacketTransmission(event);
            }
        }
    }

    /**
     * fake a new login packet in order to let the server handle all the other stuff
     *
     * @author games647 and FastLogin contributors
     */
    private void receiveFakeStartPacket(String username, ClientPublicKey clientKey, Player player) {
        PacketContainer startPacket;
        if (MinecraftVersion.atOrAbove(new MinecraftVersion(1, 19, 0))) {
            startPacket = new PacketContainer(START);
            startPacket.getStrings().write(0, username);

            EquivalentConverter<WrappedProfilePublicKey.WrappedProfileKeyData> converter = BukkitConverters.getWrappedPublicKeyDataConverter();
            var wrappedKey = Optional.ofNullable(clientKey).map(key ->
                    new WrappedProfilePublicKey.WrappedProfileKeyData(clientKey.getExpire(), clientKey.getKey(), clientKey.getSignature())
            );

            startPacket.getOptionals(converter).write(0, wrappedKey);
        } else {
            //uuid is ignored by the packet definition
            WrappedGameProfile fakeProfile = new WrappedGameProfile(UUID.randomUUID(), username);

            Class<?> profileHandleType = fakeProfile.getHandleType();
            Class<?> packetHandleType = PacketRegistry.getPacketClassFromType(START);
            ConstructorAccessor startCons = Accessors.getConstructorAccessorOrNull(packetHandleType, profileHandleType);
            startPacket = new PacketContainer(START, startCons.invoke(BukkitUnwrapper.getInstance().unwrapItem(fakeProfile)));
        }

        //we don't want to handle our own packets so ignore filters
        ProtocolLibrary.getProtocolManager().receiveClientPacket(player, startPacket, false);
    }

    public boolean hasJoined(String username, String serverHash, InetAddress hostIp) throws IOException {
        String url;
        if (hostIp instanceof Inet6Address) {
            url = String.format("https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s", username, serverHash);
        } else {
            var encodedIP = URLEncoder.encode(hostIp.getHostAddress(), StandardCharsets.UTF_8.name());
            url = String.format("https://sessionserver.mojang.com/session/minecraft/hasJoined?username=%s&serverId=%s&ip=%s", username, serverHash, encodedIP);
        }

        var conn = (HttpURLConnection) new URL(url).openConnection();
        int responseCode = conn.getResponseCode();
        return responseCode != 204;
    }

    /**
     * @author games647 and FastLogin contributors
     */
    private boolean enableEncryption(SecretKey loginKey, Player player) throws IllegalArgumentException {
        // Initialize method reflections
        if (encryptMethod == null) {
            Class<?> networkManagerClass = MinecraftReflection.getNetworkManagerClass();

            try {
                // Try to get the old (pre MC 1.16.4) encryption method
                encryptMethod = FuzzyReflection.fromClass(networkManagerClass)
                        .getMethodByParameters("a", SecretKey.class);
            } catch (IllegalArgumentException exception) {
                // Get the new encryption method
                encryptMethod = FuzzyReflection.fromClass(networkManagerClass)
                        .getMethodByParameters("a", Cipher.class, Cipher.class);

                // Get the needed Cipher helper method (used to generate ciphers from login key)
                cipherMethod = FuzzyReflection.fromClass(ENCRYPTION_CLASS)
                        .getMethodByParameters("a", int.class, Key.class);
            }
        }

        try {
            Object networkManager = this.getNetworkManager(player);

            // If cipherMethod is null - use old encryption (pre MC 1.16.4), otherwise use the new cipher one
            if (cipherMethod == null) {
                // Encrypt/decrypt packet flow, this behaviour is expected by the client
                encryptMethod.invoke(networkManager, loginKey);
            } else {
                // Create ciphers from login key
                Object decryptionCipher = cipherMethod.invoke(null, Cipher.DECRYPT_MODE, loginKey);
                Object encryptionCipher = cipherMethod.invoke(null, Cipher.ENCRYPT_MODE, loginKey);

                // Encrypt/decrypt packet flow, this behaviour is expected by the client
                encryptMethod.invoke(networkManager, decryptionCipher, encryptionCipher);
            }
        } catch (Exception ex) {
            kickPlayer("Couldn't enable encryption", player);
            return false;
        }

        return true;
    }

    /**
     * Try to get network manager from protocollib
     *
     * @author games647 and FastLogin contributors
     */
    private Object getNetworkManager(Player player) throws ClassNotFoundException {
        Object injectorContainer = TemporaryPlayerFactory.getInjectorFromPlayer(player);

        // ChannelInjector
        Class<?> injectorClass = Class.forName("com.comphenix.protocol.injector.netty.Injector");
        Object rawInjector = FuzzyReflection.getFieldValue(injectorContainer, injectorClass, true);

        Class<?> rawInjectorClass = rawInjector.getClass();
        FieldAccessor accessor = Accessors.getFieldAccessorOrNull(rawInjectorClass, "networkManager", Object.class);
        return accessor.get(rawInjector);
    }

    /**
     * @author games647 and FastLogin contributors
     */
    private void kickPlayer(String reason, Player player) {
        PacketContainer kickPacket = new PacketContainer(DISCONNECT);
        kickPacket.getChatComponents().write(0, WrappedChatComponent.fromText(reason));
        //send kick packet at login state
        //the normal event.getPlayer.kickPlayer(String) method does only work at play state
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, kickPacket);
        //tell the server that we want to close the connection
        player.kickPlayer("Disconnect");
    }

    /**
     * @author games647 and FastLogin contributors
     */
    private String getUsername(PacketContainer packet) {
        WrappedGameProfile profile = packet.getGameProfiles().readSafely(0);
        if (profile == null) {
            return packet.getStrings().read(0);
        }

        //player.getName() won't work at this state
        return profile.getName();
    }

    /**
     * @author games647 and FastLogin contributors
     */
    private boolean verifyNonce(PacketContainer packet,
                                ClientPublicKey clientPublicKey, byte[] expectedToken) {
        try {
            if (MinecraftVersion.atOrAbove(new MinecraftVersion(1, 19, 0))
                    && !MinecraftVersion.atOrAbove(new MinecraftVersion(1, 19, 3))) {
                Either<byte[], ?> either = packet.getSpecificModifier(Either.class).read(0);
                if (clientPublicKey == null) {
                    Optional<byte[]> left = either.left();
                    if (left.isEmpty()) {
                        return false;
                    }

                    return EncryptionUtil.verifyNonce(expectedToken, keyPair.getPrivate(), left.get());
                } else {
                    Optional<?> optSignatureData = either.right();
                    if (optSignatureData.isEmpty()) {
                        return false;
                    }

                    Object signatureData = optSignatureData.get();
                    long salt = FuzzyReflection.getFieldValue(signatureData, Long.TYPE, true);
                    byte[] signature = FuzzyReflection.getFieldValue(signatureData, byte[].class, true);

                    PublicKey publicKey = clientPublicKey.getKey();
                    return EncryptionUtil.verifySignedNonce(expectedToken, publicKey, salt, signature);
                }
            } else {
                byte[] nonce = packet.getByteArrays().read(1);
                return EncryptionUtil.verifyNonce(expectedToken, keyPair.getPrivate(), nonce);
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchPaddingException
                 | IllegalBlockSizeException | BadPaddingException signatureEx) {
            return false;
        }
    }
}
