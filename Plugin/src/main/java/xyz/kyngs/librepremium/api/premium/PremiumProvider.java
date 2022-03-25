package xyz.kyngs.librepremium.api.premium;

public interface PremiumProvider {

    PremiumUser getUserForName(String name) throws PremiumException;

}
