
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "8080")
        );

        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", port), 0
        );

        server.createContext("/", exchange -> {
            String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>CryptoVault</title>
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1">
                    <style>
                        body {
                            background: #101522;
                            color: white;
                            font-family: Arial, sans-serif;
                            text-align: center;
                            padding: 60px 20px;
                        }
                        .card {
                            max-width: 450px;
                            margin: auto;
                            padding: 30px;
                            background: #202a3c;
                            border-radius: 16px;
                        }
                        button {
                            padding: 12px 22px;
                            background: #65e0bd;
                            border: 0;
                            border-radius: 8px;
                            cursor: pointer;
                        }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <h1>CryptoVault</h1>
                        <p>Hardware Wallet Security & Multi-Sig Vault</p>
                        <p>Java server is running successfully.</p>
                        <button onclick="alert('CryptoVault demo is working!')">
                            Test Demo
                        </button>
                    </div>
                </body>
                </html>
                """;

            byte[] data = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set(
                    "Content-Type", "text/html; charset=UTF-8"
            );
            exchange.sendResponseHeaders(200, data.length);

            try (OutputStream out = exchange.getResponseBody()) {
                out.write(data);
            }
        });

        server.start();
        System.out.println("CryptoVault server started on port " + port);
    }
}
