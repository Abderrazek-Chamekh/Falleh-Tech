package tn.esprit.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import tn.esprit.Controllers.front.FrontViewController;
import tn.esprit.entities.Flamme;
import tn.esprit.services.FlammeService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalFlammeServer {
    private static final int DEFAULT_PORT = 8081;
    private static final int MAX_PORT_ATTEMPTS = 10;
    private static final int SERVER_SHUTDOWN_DELAY = 0; // Immediate shutdown

    private static HttpServer server;
    private static ExecutorService executorService;

    public static synchronized void startServer(Long userId) {
        // Don't start if already running
        if (server != null) {
            System.out.println("⚠️ Server is already running");
            return;
        }

        int port = DEFAULT_PORT;
        int attempts = 0;

        while (attempts < MAX_PORT_ATTEMPTS) {
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);

                // Create a proper thread pool instead of null (which uses system default)
                executorService = Executors.newFixedThreadPool(4);
                server.setExecutor(executorService);

                server.createContext("/add-flamme", new FlammeHandler(userId));

                server.start();
                System.out.println("✅ Local HTTP Server started on port " + port);

                // Add shutdown hook for proper cleanup
                Runtime.getRuntime().addShutdownHook(new Thread(LocalFlammeServer::stopServer));
                return;

            } catch (IOException e) {
                if (e.getMessage().contains("Address already in use")) {
                    System.out.println("⚠️ Port " + port + " in use, trying next port...");
                    port++;
                    attempts++;
                } else {
                    System.err.println("❌ Failed to start server: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }
        }

        System.err.println("❌ Failed to start server after " + MAX_PORT_ATTEMPTS + " attempts");
    }

    public static synchronized void stopServer() {
        if (server != null) {
            server.stop(SERVER_SHUTDOWN_DELAY);
            System.out.println("🛑 Server stopped");
            server = null;
        }

        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
    }

    private static class FlammeHandler implements HttpHandler {
        private final Long userId;

        public FlammeHandler(Long userId) {
            this.userId = userId;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!"GET".equals(exchange.getRequestMethod())) {
                    sendResponse(exchange, 405, "Method Not Allowed");
                    return;
                }

                // Create and add flamme
                Flamme flamme = new Flamme();
                flamme.setUserId(userId);
                flamme.setCount(1);
                FlammeService.getInstance().ajouter(flamme);

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    FrontViewController controller = FrontViewController.getInstance();
                    if (controller != null) {
                        controller.updateFlammeCount(userId);
                    }
                });

                sendResponse(exchange, 200, "🔥 Flamme ajoutée avec succès.");

            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "Internal Server Error");
            }
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}