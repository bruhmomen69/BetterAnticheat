package better.anticheat.paper;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class BetterAnticheatPaperLoader implements PluginLoader {
    public static final String[] dependencies = new String[]{
            "it.unimi.dsi:fastutil:8.5.16",
            "org.yaml:snakeyaml:2.4",
            "io.github.revxrsal:lamp.bukkit:4.0.0-rc.12",
            "io.github.revxrsal:lamp.brigadier:4.0.0-rc.12",
            "io.github.revxrsal:lamp.common:4.0.0-rc.12",
            "com.github.haifengl:smile-core:4.4.0",
            "com.github.haifengl:smile-plot:4.4.0",
            "com.alibaba.fastjson2:fastjson2:2.0.57",
            "com.github.luben:zstd-jni:1.5.6-7"
    };

    @Override
    public void classloader(final PluginClasspathBuilder classpathBuilder) {
        final var resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder("atlassian-3rdp-mirror", "default", "https://packages.atlassian.com/maven-3rdparty/").build());
        resolver.addRepository(new RemoteRepository.Builder("jcenter", "default", "https://jcenter.bintray.com/").build());
        resolver.addRepository(new RemoteRepository.Builder("atlassian-external-mirror", "default", "https://packages.atlassian.com/mvn/maven-atlassian-external/").build());
        resolver.addRepository(new RemoteRepository.Builder("wso2-mirror", "default", "https://maven.wso2.org/nexus/content/repositories/releases/").build());
        resolver.addRepository(new RemoteRepository.Builder("spigot", "default", "https://hub.spigotmc.org/nexus/content/repositories/snapshots/").build());
        resolver.addRepository(new RemoteRepository.Builder("gcs-central-mirror", "default", "https://maven-central.storage-download.googleapis.com/maven2/").build());
        resolver.addRepository(new RemoteRepository.Builder("mulesoft", "default", "https://repository.mulesoft.org/nexus/content/repositories/public/").build());
        resolver.addRepository(new RemoteRepository.Builder("thanks-my-friend-mirror", "default", "https://german-code-repo.haldor.xyz/repository/maven-central/").build());
        resolver.addRepository(new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build());
        resolver.addRepository(new RemoteRepository.Builder("sonatype-mirror", "default", "https://oss.sonatype.org/content/groups/public/").build());

        for (final var dependencyString : dependencies) {
            resolver.addDependency(new Dependency(new DefaultArtifact(dependencyString), null));
        }

        classpathBuilder.addLibrary(resolver);
    }
}