package better.anticheat.paper;

import better.anticheat.core.util.dependencies.DependencyList;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class BetterAnticheatPaperLoader implements PluginLoader {
    @Override
    public void classloader(final PluginClasspathBuilder classpathBuilder) {
        final var resolver = new MavenLibraryResolver();

        // Add all the repositories
        for (final var repo : DependencyList.REPOSITORIES) {
            resolver.addRepository(new RemoteRepository.Builder(repo[0], "default", repo[1]).build());
        }

        // Add all the dependencies
        for (final var dependencyString : DependencyList.DEPENDENCIES) {
            resolver.addDependency(new Dependency(new DefaultArtifact(dependencyString), null));
        }

        classpathBuilder.addLibrary(resolver);
    }
}