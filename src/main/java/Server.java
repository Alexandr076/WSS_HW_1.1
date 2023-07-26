import org.w3c.dom.ls.LSOutput;

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
    private static ExecutorService executorService = Executors.newFixedThreadPool(COUNT_OF_CONNECTIONS);
    private static Socket socket = new Socket();

    public Server(List<String> fileNames) {
        startServer(fileNames);
    }


    private void startServer(List<String> validPaths) {
        try (final var serverSocket = new ServerSocket(CONNECTION_PORT)) {
            while (true) {
                socket = serverSocket.accept();
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            final var out = new BufferedOutputStream(socket.getOutputStream());
                            Handler.newConnectionHandler(socket, in, out, validPaths);
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
}