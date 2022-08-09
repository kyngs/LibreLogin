package xyz.kyngs.librepremium.common.totp;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.totp.TOTPData;
import xyz.kyngs.librepremium.api.totp.TOTPProvider;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class AuthenticTOTPProvider implements TOTPProvider {

    private final AuthenticLibrePremium<?, ?> plugin;
    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;
    private final CodeVerifier verifier;

    public AuthenticTOTPProvider(AuthenticLibrePremium<?, ?> plugin) {
        this.plugin = plugin;
        secretGenerator = new DefaultSecretGenerator();
        qrGenerator = new ZxingPngQrGenerator();

        verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
    }

    @Override
    public TOTPData generate(User user) {
        return generate(user, secretGenerator.generate());
    }

    @Override
    public TOTPData generate(User user, String secret) {
        var data = new QrData.Builder()
                .label(user.getLastNickname())
                .issuer(plugin.getConfiguration().getTotpLabel())
                .secret(secret)
                .build();

        byte[] code;

        try {
            code = qrGenerator.generate(data);
        } catch (QrGenerationException e) {
            throw new RuntimeException(e);
        }

        try {
            return new TOTPData(ImageIO.read(new ByteArrayInputStream(code)), secret);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean verify(Integer code, String secret) {
        return verifier.isValidCode(secret, code.toString());
    }
}
