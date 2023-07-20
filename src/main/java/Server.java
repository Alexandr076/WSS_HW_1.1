import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int COUNT_OF_CONNECTIONS = 64;
    private static final int CONNECTION_PORT = 9999;
    public static ExecutorService executorService = Executors.newFixedThreadPool(COUNT_OF_CONNECTIONS);
    private static Socket socket = new Socket();

    public static List<String> initializeFiles() {
        List<String> fileNames = new ArrayList<>();
        File folder = new File(System.getProperty("user.dir")+"\\public");
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles.length > 0) {
            for (File file: listOfFiles) {
                fileNames.add("/" + file.getName());
            }
        }
        return fileNames;
    }

    public static void startServer(List<String> validPaths) {
        try (final var serverSocket = new ServerSocket(CONNECTION_PORT)) {
            while (true) {
                socket = serverSocket.accept();
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            final var out = new BufferedOutputStream(socket.getOutputStream());
                            newConnectionHandler(socket, in, out, validPaths);
                        } catch (IOException e) {
                            System.out.println("Error inside thread");
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void newConnectionHandler(Socket socket, BufferedReader in,
                                            BufferedOutputStream out, List<String> validPaths) throws IOException {
        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            // just close socket
            return;
        }

        final var path = parts[1];
        if (!validPaths.contains(path)) {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
            return;
        }

        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);

        // special case for classic
        if (path.equals("/classic.html")) {
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.write(content);
            out.flush();
            return;
        }

        final var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }

    public static void main(String[] args) {
        List<String> validPaths = initializeFiles();
        startServer(validPaths);
    }
}
