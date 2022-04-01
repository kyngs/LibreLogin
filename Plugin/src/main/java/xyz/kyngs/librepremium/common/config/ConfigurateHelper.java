package xyz.kyngs.librepremium.common.config;

import com.google.common.base.Splitter;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import xyz.kyngs.easydb.scheduler.ThrowableFunction;
import xyz.kyngs.librepremium.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librepremium.common.config.key.ConfigurationKey;

import java.util.List;

public record ConfigurateHelper(CommentedConfigurationNode configuration) {

    public String getString(String path) {
        return get(String.class, path);
    }

    public Integer getInt(String path) {
        return get(Integer.class, path);
    }

    public Boolean getBoolean(String path) {
        return get(Boolean.class, path);
    }

    public <T> T get(Class<T> clazz, String path) {
        return configurationFunction(path, node -> {
            if (node.isList()) throw new CorruptedConfigurationException("Node is a list!");
            return node.get(clazz);
        });
    }

    public List<String> getStringList(String path) {
        return getList(String.class, path);
    }

    public <T> List<T> getList(Class<T> clazz, String path) {
        return configurationFunction(path, node -> {
            if (!node.isList()) throw new CorruptedConfigurationException("Node is not a list!");
            return node.getList(clazz);
        });
    }

    public void set(String path, Object value) {
        try {
            resolve(path)
                    .set(value);
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    public CommentedConfigurationNode resolve(String key) {
        return configuration.node(Splitter.on('.').splitToList(key).toArray());
    }

    public <T> T configurationFunction(String path, ThrowableFunction<CommentedConfigurationNode, T, Exception> function) {
        try {
            var node = resolve(path);
            if (node == null || node.virtual()) return null;
            return function.run(resolve(path));
        } catch (Exception e) {
            return null;
        }
    }

    public void setDefault(ConfigurationKey<?> key) {
        try {
            resolve(key.key())
                    .comment(key.comment())
                    .set(key.defaultValue());
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T get(ConfigurationKey<T> key) {
        return key.compute(this);
    }

}
