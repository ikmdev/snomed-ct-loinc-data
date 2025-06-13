package dev.ikm.tinkar.snomedctloinc.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public final class IntegrationTestUtils {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestUtils.class);

    private IntegrationTestUtils() {
    }

    public static Path findOriginPath(Path rootPath) {
        try (Stream<Path> stream = Files.walk(rootPath)
                .filter(Files::isDirectory)
                .filter(path -> path.toFile().getPath().contains("SnomedCT_"))) {
            Path directory = stream.findFirst().orElseThrow();
            LOG.info("found origin directory [{}]", directory);
            return directory;
        } catch (Exception ex) {
            LOG.error("findOriginPath failed to locate directory", ex);
            throw new RuntimeException(ex);
        }
    }

    public static String findEditionFromOrigin(Path origin) {
        File file = findMatchingFile(origin, "sct2_Concept_Snapshot").toFile();
        String[] nameParts = file.getName().split("_");
        return nameParts[3];
    }

    public static String findVersionFromOrigin(Path origin) {
        File file =  findMatchingFile(origin, "sct2_Concept_Snapshot").toFile();
        String[] nameParts = file.getName().split("_");
        return nameParts[4].substring(0, 8);
    }

    public static Path findMatchingFile(Path directory, String fileName) {
        try (Stream<Path> stream = Files.list(directory)
                .filter(path -> path.toFile().getName().startsWith(fileName))) {
            return stream.findFirst().orElseThrow(() -> new RuntimeException("unable to locate file: " + fileName));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
