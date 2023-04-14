/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.paper;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.provider.LibreLoginProvider;

public class PaperBootstrap extends JavaPlugin implements LibreLoginProvider<Player, World> {

    private PaperLibreLogin libreLogin;

    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        return id == null ?
                null
                : id.equals("void") ? new VoidWorldGenerator() : null;
    }

    @Override
    public void onLoad() {
        getLogger().info("Analyzing server setup...");

        try {
            var adventureClass = Class.forName("net.kyori.adventure.audience.Audience");

            if (!adventureClass.isAssignableFrom(Player.class)) {
                throw new ClassNotFoundException();
            }
        } catch (ClassNotFoundException e) {
            unsupportedSetup();
        }

        getLogger().info("Detected Adventure-compatible server distribution - " + getServer().getName() + " " + getServer().getVersion());

        libreLogin = new PaperLibreLogin(this);
    }

    @Override
    public void onEnable() {
        getLogger().info("Bootstrapping LibreLogin...");
        libreLogin.enable();
    }

    private void unsupportedSetup() {
        getLogger().severe("***********************************************************");

        getLogger().severe("Detected an unsupported server distribution. Please use Paper or its forks. SPIGOT IS NOT SUPPORTED!");

        getLogger().severe("***********************************************************");

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        }

        System.exit(1);

    }

    @Override
    public void onDisable() {
        libreLogin.disable();
    }

    @Override
    public PaperLibreLogin getLibreLogin() {
        return libreLogin;
    }

    protected void disable() {
        setEnabled(false);
    }

}
