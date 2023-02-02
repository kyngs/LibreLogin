package xyz.kyngs.librelogin.paper;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.kyngs.librelogin.api.provider.LibrePremiumProvider;

public class PaperBootstrap extends JavaPlugin implements LibrePremiumProvider<Player, World> {

    private PaperLibrePremium librePremium;

    @Override
    public void onEnable() {
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
        getLogger().info("Bootstrapping LibrePremium...");

        librePremium = new PaperLibrePremium(this);
        librePremium.enable();
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
        librePremium.disable();
    }

    @Override
    public PaperLibrePremium getLibrePremium() {
        return librePremium;
    }

    protected void disable() {
        setEnabled(false);
    }

}
