import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;

public class NotesServer {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        Path notesDir = Path.of(System.getProperty("user.home"), ".notes");

        // 🔥 LIST NOTES
        server.createContext("/api/notes", exchange -> {
            String response = Notes1.listNotesAsString(notesDir);

            sendResponse(exchange, response);
        });

        // 🔥 CREATE NOTE (with image)
        server.createContext("/api/create", exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());

            // title | content | tags | image
            String[] parts = body.split("\\|", 4);

            String title = parts.length > 0 ? parts[0] : "";
            String content = parts.length > 1 ? parts[1] : "";
            String tags = parts.length > 2 ? parts[2] : "";
            String image = parts.length > 3 ? parts[3] : "";

            String response = Notes1.createNoteFromUI(notesDir, title, content, tags, image);

            sendResponse(exchange, response);
        });

        // 🔥 READ NOTE
        server.createContext("/api/note", exchange -> {

            String query = exchange.getRequestURI().getQuery();
            String fileName = "";

            if (query != null && query.startsWith("file=")) {
                fileName = query.substring(5);
            }

            String response = Notes1.readNoteAsString(notesDir, fileName);

            sendResponse(exchange, response);
        });

        // 🔥 UPDATE NOTE (with image)
        server.createContext("/api/update", exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());

            // fileName | title | tags | image | content
            String[] parts = body.split("\\|", 5);

            String fileName = parts.length > 0 ? parts[0] : "";
            String title = parts.length > 1 ? parts[1] : "";
            String tags = parts.length > 2 ? parts[2] : "";
            String image = parts.length > 3 ? parts[3] : "";
            String content = parts.length > 4 ? parts[4] : "";

            String response = Notes1.updateNoteFromUI(notesDir, fileName, title, tags, image, content);

            sendResponse(exchange, response);
        });

        // 🔥 DELETE NOTE
        server.createContext("/api/delete", exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String fileName = new String(exchange.getRequestBody().readAllBytes());

            String response = Notes1.deleteNote(notesDir, fileName);

            sendResponse(exchange, response);
        });

        // 🔥 SEARCH
        server.createContext("/api/search", exchange -> {

            String query = exchange.getRequestURI().getQuery();

            final String keyword = (query != null && query.startsWith("q="))
                    ? query.substring(2).toLowerCase()
                    : "";

            String response = Notes1.searchNotes(notesDir, keyword);

            sendResponse(exchange, response);
        });

        // 🔥 RANDOM MEMORY
        server.createContext("/api/random", exchange -> {

            String response = Notes1.getRandomNote(notesDir);

            sendResponse(exchange, response);
        });

        server.start();
        System.out.println("Server running at http://localhost:8080");
    }

    // 🔥 Helper method
    private static void sendResponse(HttpExchange exchange, String response) throws IOException {

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "text/plain");

        exchange.sendResponseHeaders(200, response.getBytes().length);

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}