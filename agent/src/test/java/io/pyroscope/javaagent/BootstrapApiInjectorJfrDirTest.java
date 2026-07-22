package io.pyroscope.javaagent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class BootstrapApiInjectorJfrDirTest {

    @TempDir
    Path tempDir;

    @Test
    void testBootstrapJarCanBeCreatedInConfiguredDirectory() {
        String jfrDir = tempDir.toString();
        Instrumentation instrumentation = mock(Instrumentation.class);

        assertDoesNotThrow(() -> {
            BootstrapApiInjector.class
                .getDeclaredMethod("inject", Instrumentation.class, String.class)
                .invoke(null, instrumentation, jfrDir);
        });
    }

    @Test
    void testBootstrapJarCanBeCreatedWithNullDirectory() {
        Instrumentation instrumentation = mock(Instrumentation.class);

        assertDoesNotThrow(() -> {
            BootstrapApiInjector.class
                .getDeclaredMethod("inject", Instrumentation.class, String.class)
                .invoke(null, instrumentation, null);
        });
    }

    @Test
    void testBootstrapJarCanBeCreatedWithEmptyDirectory() {
        Instrumentation instrumentation = mock(Instrumentation.class);

        assertDoesNotThrow(() -> {
            BootstrapApiInjector.class
                .getDeclaredMethod("inject", Instrumentation.class, String.class)
                .invoke(null, instrumentation, "");
        });
    }

    @Test
    void testConfiguredDirectoryIsCreatedForBootstrapJar() throws Exception {
        Path jfrDir = tempDir.resolve("bootstrap/jar/dir");
        assertFalse(Files.exists(jfrDir), "Test directory should not exist initially");

        testCreateBootstrapJarMethod(jfrDir.toString());

        assertTrue(Files.exists(jfrDir),
            "Configured directory should be created for bootstrap JAR");
        assertTrue(Files.isDirectory(jfrDir),
            "Created path should be a directory");
    }

    @Test
    void testBootstrapJarNameStartsWithPyroscope() throws Exception {
        Path jfrDir = tempDir.resolve("bootstrap");
        Files.createDirectories(jfrDir);

        testCreateBootstrapJarMethod(jfrDir.toString());

        boolean foundBootstrapJar = Files.walk(jfrDir)
            .map(Path::getFileName)
            .map(Path::toString)
            .anyMatch(name -> name.startsWith("pyroscope-bootstrap-") && name.endsWith(".jar"));

        assertTrue(foundBootstrapJar,
            "Bootstrap JAR file should start with 'pyroscope-bootstrap-' and end with '.jar'");
    }

    private void testCreateBootstrapJarMethod(String jfrDir) throws Exception {
        java.lang.reflect.Method method = BootstrapApiInjector.class.getDeclaredMethod("createBootstrapJar", String.class);
        method.setAccessible(true);
        Path result = (Path) method.invoke(null, jfrDir);

        assertNotNull(result);
        assertTrue(result.toString().contains(jfrDir),
            "Bootstrap JAR should be created in the configured directory");
    }
}
