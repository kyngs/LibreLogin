package xyz.kyngs.librepremium.common.database;

import xyz.kyngs.librepremium.api.crypto.HashedPassword;
import xyz.kyngs.librepremium.api.database.User;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

public class AuthenticUser implements User {

    private final UUID uuid;
    private UUID premiumUUID;
    private HashedPassword hashedPassword;
    private String lastNickname;
    private Timestamp joinDate;
    private Timestamp lastSeen;
    private String secret;
    private String ip;
    private Timestamp lastAuthentication;
    private String lastServer;

    public AuthenticUser(UUID uuid, UUID premiumUUID, HashedPassword hashedPassword, String lastNickname, Timestamp joinDate, Timestamp lastSeen, String secret, String ip, Timestamp lastAuthentication, String lastServer) {
        this.uuid = uuid;
        this.premiumUUID = premiumUUID;
        this.hashedPassword = hashedPassword;
        this.lastNickname = lastNickname;
        this.joinDate = joinDate;
        this.lastSeen = lastSeen;
        this.secret = secret;
        this.ip = ip;
        this.lastAuthentication = lastAuthentication;
        this.lastServer = lastServer;
    }

    public Timestamp getLastAuthentication() {
        return lastAuthentication;
    }

    public void setLastAuthentication(Timestamp lastAuthentication) {
        this.lastAuthentication = lastAuthentication;
    }

    public Timestamp getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Timestamp joinDate) {
        this.joinDate = joinDate;
    }

    public Timestamp getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Timestamp lastSeen) {
        this.lastSeen = lastSeen;
    }

    public HashedPassword getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(HashedPassword hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getPremiumUUID() {
        return premiumUUID;
    }

    public void setPremiumUUID(UUID premiumUUID) {
        this.premiumUUID = premiumUUID;
    }

    public String getLastNickname() {
        return lastNickname;
    }

    public void setLastNickname(String lastNickname) {
        this.lastNickname = lastNickname;
    }

    public boolean isRegistered() {
        return hashedPassword != null;
    }

    public boolean autoLoginEnabled() {
        return premiumUUID != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var user = (AuthenticUser) o;
        return uuid.equals(user.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getLastServer() {
        return lastServer;
    }

    public void setLastServer(String lastServer) {
        this.lastServer = lastServer;
    }

}
