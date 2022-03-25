package xyz.kyngs.librepremium.common.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class GeneralUtil {

    public static String readInput(InputStream inputStream) throws IOException {
        var input = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        inputStream.close();
        return input;
    }

    public static Throwable getFurthestCause(Throwable throwable) {
        while (true) {
            var cause = throwable.getCause();

            if (cause == null) return throwable;

            throwable = cause;
        }
    }

    public static UUID fromUnDashedUUID(String id) {
        return new UUID(
                new BigInteger(id.substring(0, 16), 16).longValue(),
                new BigInteger(id.substring(16, 32), 16).longValue()
        );
    }

    @Nullable
    public static TextComponent formatComponent(@Nullable TextComponent component, Map<String, String> replacements) {
        if (component == null) return null;

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            //noinspection UnstableApiUsage
            component = (TextComponent) component.replaceText(entry.getKey(), Component.text(entry.getValue()));
        }
        return component;
    }

}
