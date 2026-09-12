package xyz.kyngs.librelogin.paper;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.kyngs.librarian.PaperLibraryManager;

public class PaperLoader implements PluginLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PaperLoader.class);

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        var libraryManager = new PaperLibraryManager();

        LOGGER.info("Loading libraries...");

        try {
            libraryManager.configureFromJSON();

            libraryManager.classloader(classpathBuilder);
        } catch (Exception e) {
            LOGGER.error("Failed to load libraries, stopping server to prevent damage", e);
            System.exit(1);
        }
    }
}
