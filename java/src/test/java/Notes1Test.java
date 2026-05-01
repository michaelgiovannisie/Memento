import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class Notes1Test {

    private Path tempDir;

    @BeforeEach
    void setup() throws Exception {
        tempDir = Files.createTempDirectory("notes-test");
        Path notesSubdir = tempDir.resolve("notes");
        Files.createDirectories(notesSubdir);
        Path note = notesSubdir.resolve("test.note");
        Files.writeString(note, """
            ---
            title: Test Note
            created: 2026-04-30
            tags: [java]
            ---
            
            Hello world
            """);
    }

    @AfterEach
    void cleanup() throws Exception {
        Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try { Files.delete(path); } catch (Exception ignored) {}
                });
    }

    @Test
    void testListCommand() {
        String output = runMain("list");

        assertTrue(output.contains("Test Note"));
        assertTrue(output.contains("test.note"));
    }

    @Test
    void testReadCommand() {
        String output = runMain("read", "test.note");

        assertTrue(output.contains("Hello world"));
    }

    @Test
    void testDeleteCommand() throws Exception {
        runMain("delete", "test.note");

        Path notePath = tempDir.resolve("notes/test.note");
        assertFalse(Files.exists(notePath));
    }

    @Test
    void testSearchCommand() {
        String output = runMain("search", "java");
        assertTrue(output.toLowerCase().contains("test.note"));
    }
}