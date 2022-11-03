package xyz.kyngs.librepremium.api.database;

import xyz.kyngs.librepremium.api.crypto.HashedPassword;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * A user in the database.
 *
 * @author kyngs
 */
public interface User {

    String getSecret();

    void setSecret(String secret);

    String getIp();

    void setIp(String ip);

    String getLastServer();

    void setLastServer(String lastServer);

    Timestamp getLastAuthentication();

    void setLastAuthentication(Timestamp lastAuthentication);

    Timestamp getJoinDate();

    void setJoinDate(Timestamp joinDate);

    Timestamp getLastSeen();

    void setLastSeen(Timestamp lastSeen);

    HashedPassword getHashedPassword();

    void setHashedPassword(HashedPassword hashedPassword);

    UUID getUuid();

    UUID getPremiumUUID();

    void setPremiumUUID(UUID premiumUUID);

    String getLastNickname();

    void setLastNickname(String lastNickname);

    boolean isRegistered();

    boolean autoLoginEnabled();

}
