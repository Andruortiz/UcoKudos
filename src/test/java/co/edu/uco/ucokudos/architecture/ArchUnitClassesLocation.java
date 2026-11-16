package co.edu.uco.ucokudos.architecture;

import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.LocationProvider;

import java.nio.file.Path;
import java.util.Set;

/** ArchUnit 1.4.1 cannot read Java 27 class files, so it analyses the copy the build compiles for Java 25. */
public class ArchUnitClassesLocation implements LocationProvider {

    @Override
    public Set<Location> get(final Class<?> testClass) {
        return Set.of(Location.of(Path.of("target", "archunit-classes")));
    }
}
