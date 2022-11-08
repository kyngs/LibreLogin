package xyz.kyngs.librepremium.paper;

import org.bukkit.World;
import org.bukkit.entity.Player;
import xyz.kyngs.librepremium.common.listener.AuthenticListeners;

public class PaperListeners extends AuthenticListeners<PaperLibrePremium, Player, World> {
    public PaperListeners(PaperLibrePremium paperLibrePremium) {
        super(paperLibrePremium);
    }
}
