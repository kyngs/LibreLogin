package xyz.kyngs.librepremium.api.util;

public record SemanticVersion(int major, int minor, int patch, boolean dev) {

    public static SemanticVersion parse(String version) {
        String[] split = version.replace("-DEVELOPMENT", "").split("\\.");
        return new SemanticVersion(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), version.endsWith("-DEVELOPMENT"));
    }

    /**
     * Compares this version with the specified version.
     *
     * @param other the version to compare to
     * @return -1 if this version is less than the specified version, 0 if they are equal, and 1 if this version is greater
     */
    public int compare(SemanticVersion other) {
        if (major != other.major) {
            return major < other.major ? -1 : 1;
        }
        if (minor != other.minor) {
            return minor < other.minor ? -1 : 1;
        }
        if (patch != other.patch) {
            return patch < other.patch ? -1 : 1;
        }
        return dev ?
                (other.dev ? 0 : -1)
                :
                (other.dev ? 1 : 0);
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch + (dev ? "-DEVELOPMENT" : "");
    }
}
