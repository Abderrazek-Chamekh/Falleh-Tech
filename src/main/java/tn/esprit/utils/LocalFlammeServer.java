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

public class LocalFlammeServer {

    private static HttpServer server;

    public static void startServer(Long userId) {
        try {
            server = HttpServer.create(new InetSocketAddress(8081), 0);
            server.createContext("/add-flamme", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    if ("GET".equals(exchange.getRequestMethod())) {

                        // ✅ Créer et ajouter la flamme
                        Flamme flamme = new Flamme();
                        flamme.setUserId(userId);
                        flamme.setCount(1);
                        FlammeService.getInstance().ajouter(flamme);

                        // ✅ Mettre à jour l'interface sur le bon thread JavaFX
                        Platform.runLater(() -> {
                            FrontViewController controller = FrontViewController.getInstance();
                            if (controller != null) {
                                controller.updateFlammeCount(userId);
                            }
                        });

                        String response = "🔥 Flamme ajoutée avec succès.";
                        exchange.sendResponseHeaders(200, response.getBytes().length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(response.getBytes());
                        }
                    }
                }
            });

            server.start();
            System.out.println("✅ Local HTTP Server démarré sur port 8081");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("🛑 Server arrêté.");
        }
    }
}
