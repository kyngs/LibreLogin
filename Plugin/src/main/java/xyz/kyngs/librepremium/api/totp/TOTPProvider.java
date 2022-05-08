package xyz.kyngs.librepremium.api.totp;

import xyz.kyngs.librepremium.api.database.User;

public interface TOTPProvider {

    TOTPData generate(User user);

    TOTPData generate(User user, String secret);

    boolean verify(Integer code, String secret);

}
