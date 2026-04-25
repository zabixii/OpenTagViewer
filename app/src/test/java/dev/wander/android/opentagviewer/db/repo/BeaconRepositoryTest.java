package dev.wander.android.opentagviewer.db.repo;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class BeaconRepositoryTest {
    @Test
    public void sourceTree_noLongerContainsLegacyFindMyCompatHooks() throws Exception {
        String databaseSource = readProjectFile("src/main/java/dev/wander/android/opentagviewer/db/room/OpenTagViewerDatabase.java");
        String repositorySource = readProjectFile("src/main/java/dev/wander/android/opentagviewer/db/repo/BeaconRepository.java");
        String pythonBridgeSource = readProjectFile("src/main/python/main.py");

        assertFalse(databaseSource.contains("MIGRATION_1_2"));
        assertFalse(databaseSource.contains("addMigrations("));
        assertFalse(repositorySource.contains("Python.getInstance()"));
        assertFalse(repositorySource.contains("Lazy backfill"));
        assertFalse(pythonBridgeSource.contains("anisetteServerUrl: str = None"));
        assertFalse(pythonBridgeSource.contains("keep the existing Java callsite compiling"));
    }

    private static String readProjectFile(String appRelativePath) throws IOException {
        Path modulePath = Paths.get(appRelativePath);
        if (Files.exists(modulePath)) {
            return new String(Files.readAllBytes(modulePath), StandardCharsets.UTF_8);
        }

        Path repoRootPath = Paths.get("app").resolve(appRelativePath);
        return new String(Files.readAllBytes(repoRootPath), StandardCharsets.UTF_8);
    }
}
