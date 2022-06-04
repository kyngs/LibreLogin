package xyz.kyngs.librepremium.api.image;

import java.awt.image.BufferedImage;

public interface ImageProjector<P> {

    void project(BufferedImage image, P player);

    boolean canProject(P player);

}
