package xyz.kyngs.librepremium.common.crypto;

import at.favre.lib.crypto.bcrypt.BCrypt;
import xyz.kyngs.librepremium.api.crypto.CryptoProvider;
import xyz.kyngs.librepremium.api.crypto.HashedPassword;
import xyz.kyngs.librepremium.common.util.CryptoUtil;

public class BCrypt2ACryptoProvider implements CryptoProvider {

    public static final BCrypt.Hasher HASHER = BCrypt
            .with(BCrypt.Version.VERSION_2A);
    public static final BCrypt.Verifyer VERIFIER = BCrypt
            .verifyer(BCrypt.Version.VERSION_2A);

    @Override
    public HashedPassword createHash(String password) {
        return CryptoUtil.convertFromBCryptRaw(
                HASHER.hashToString(10, password.toCharArray())
        );
    }

    @Override
    public boolean matches(String input, HashedPassword password) {
        var raw = CryptoUtil.rawFromHashed(password).toCharArray();
        var result = VERIFIER.verify(input.toCharArray(),
                raw
        );

        return result.verified;
    }

    @Override
    public String getIdentifier() {
        return "BCrypt-2A";
    }

}
