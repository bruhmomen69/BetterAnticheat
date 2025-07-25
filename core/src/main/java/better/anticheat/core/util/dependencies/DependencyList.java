package better.anticheat.core.util.dependencies;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DependencyList {
    public static final String[] DEPENDENCIES = new String[]{
            "wtf.spare:sparej:1.0.0",
            "it.unimi.dsi:fastutil:8.5.16",
            "org.yaml:snakeyaml:2.4",
            "io.github.revxrsal:lamp.bukkit:4.0.0-rc.12",
            "io.github.revxrsal:lamp.velocity:4.0.0-rc.12",
            "io.github.revxrsal:lamp.brigadier:4.0.0-rc.12",
            "io.github.revxrsal:lamp.common:4.0.0-rc.12",
            "com.github.haifengl:smile-core:4.4.0",
            "com.github.haifengl:smile-plot:4.4.0",
            "com.alibaba.fastjson2:fastjson2:2.0.57",
            "com.github.luben:zstd-jni:1.5.6-7"
    };

    public static final String[][] REPOSITORIES = new String[][]{
            {"mirror", "https://german-code-repo.haldor.xyz/mirror"},
            {"cloudera", "https://repository.cloudera.com/content/repositories/public/"},
            {"atlassian-3rdp-mirror", "https://packages.atlassian.com/maven-3rdparty/"},
            {"gcs-central-mirror", "https://maven-central.storage-download.googleapis.com/maven2/"},
            {"atlassian-external-mirror", "https://packages.atlassian.com/mvn/maven-atlassian-external/"},
            {"wso2-releases", "https://maven.wso2.org/nexus/content/repositories/releases/"},
            {"wso2-public", "https://maven.wso2.org/nexus/content/repositories/public/"},
            {"spigot", "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"},
            {"mulesoft", "https://repository.mulesoft.org/nexus/content/repositories/public/"},
            {"jcenter", "https://jcenter.bintray.com/"},
            {"paper", "https://repo.papermc.io/repository/maven-public/"},
            {"sonatype-mirror", "https://oss.sonatype.org/content/groups/public/"}
    };
}
