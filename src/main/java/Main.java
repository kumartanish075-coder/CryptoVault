




import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Main {

    interface MultiSignable {
        void approve();
    }

    static class CryptographicPinMismatchException extends Exception {
        CryptographicPinMismatchException(String message) {
            super(message);
        }
    }

    static class VaultWipedException extends Exception {
        VaultWipedException(String message) {
            super(message);
        }
    }

    abstract static class CryptographicVault {
        String vaultName = "CryptoVault";
        boolean active = true;

        abstract boolean unlock(String pin)
                throws CryptographicPinMismatchException,
                VaultWipedException;
    }

    static class HardwareWallet extends CryptographicVault
            implements MultiSignable {

        private int attempts = 0;
        private int approvals = 0;

        @Override
        public boolean unlock(String pin)
                throws CryptographicPinMismatchException,
                VaultWipedException {

            if (!active) {
                throw new VaultWipedException(
                        "Vault is inactive.");
            }

            if (pin.equals("1234")) {
                attempts = 0;
                return true;
            }

            attempts++;

            int delay = (int) Math.pow(2, attempts);

            try {
                Thread.sleep(delay * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CryptographicPinMismatchException(
                        "Verification interrupted.");
            }

            if (attempts >= 3) {
                active = false;
                throw new VaultWipedException(
                        "Vault locked after 3 failed attempts.");
            }

            throw new CryptographicPinMismatchException(
                    "Incorrect PIN. Attempt " + attempts + " of 3.");
        }

        @Override
        public void approve() {
            if (active && approvals < 3) {
                approvals++;
            }
        }

        public int getApprovals() {
            return approvals;
        }
    }

    static HardwareWallet wallet = new HardwareWallet();

    static String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    static String dashboard(String message) {

        String html = """
            <!DOCTYPE html>
            <html>
            <head>
            <title>CryptoVault | Security Dashboard</title>
            <meta name="viewport" content="width=device-width, initial-scale=1">

            <style>
            * {
                box-sizing: border-box;
                margin: 0;
                padding: 0;
            }

            body {
                background: #080d19;
                color: #f0f4ff;
                font-family: Arial, sans-serif;
                line-height: 1.6;
            }

            html {
                scroll-behavior: smooth;
            }

            .navbar {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 22px 8%;
                border-bottom: 1px solid #202b40;
                background: #0b1220;
            }

            .logo {
                color: #68e5c3;
                font-size: 23px;
                font-weight: bold;
                letter-spacing: 2px;
            }

            .navlinks a {
                color: #aebbd0;
                text-decoration: none;
                margin-left: 25px;
                font-size: 13px;
            }

            .navlinks a:hover {
                color: #68e5c3;
            }

            .hero {
                padding: 75px 8% 55px;
                background:
                    radial-gradient(ellipse at 80% 20%,
                    #174c4c55, transparent 40%),
                    radial-gradient(ellipse at 10% 80%,
                    #42308044, transparent 40%);
            }

            .eyebrow {
                color: #68e5c3;
                letter-spacing: 3px;
                font-size: 11px;
                font-weight: bold;
            }

            .hero h1 {
                font-size: clamp(34px, 5vw, 55px);
                line-height: 1.15;
                margin: 18px 0;
            }

            .gradient {
                background: linear-gradient(90deg,
                    #68e5c3, #70aaff);
                background-clip: text;
                -webkit-background-clip: text;
                color: transparent;
            }

            .hero p {
                color: #9baac0;
                max-width: 600px;
                font-size: 15px;
            }

            .container {
                max-width: 1150px;
                margin: auto;
                padding: 20px 25px 70px;
            }

            .section-heading {
                margin: 38px 0 20px;
            }

            .section-heading h2 {
                font-size: 24px;
            }

            .section-heading p {
                color: #8493a9;
                font-size: 13px;
                margin-top: 5px;
            }

            .grid {
                display: grid;
                grid-template-columns: repeat(2, 1fr);
                gap: 22px;
            }

            .card {
                background: linear-gradient(145deg,
                    #17243a, #101a2c);
                border: 1px solid #293b53;
                border-radius: 18px;
                padding: 27px;
                transition: 0.3s;
                box-shadow: 0 15px 35px #00000030;
            }

            .card:hover {
                transform: translateY(-5px);
                border-color: #68e5c3;
                box-shadow: 0 15px 40px #68e5c315;
            }

            .icon {
                font-size: 28px;
                margin-bottom: 12px;
            }

            .card h3 {
                font-size: 19px;
                margin-bottom: 8px;
            }

            .muted {
                color: #91a0b7;
                font-size: 13px;
            }

            .status {
                color: #68e5c3;
                font-size: 12px;
                margin: 17px 0;
            }

            .big {
                font-size: 36px;
                color: #68e5c3;
                font-weight: bold;
                margin: 15px 0;
            }

            input {
                width: 100%;
                padding: 14px;
                background: #0a1425;
                border: 1px solid #354762;
                border-radius: 9px;
                color: white;
                margin: 16px 0;
                outline: none;
            }

            input:focus {
                border-color: #68e5c3;
            }

            button {
                padding: 13px 22px;
                border: none;
                border-radius: 9px;
                background: linear-gradient(90deg,
                    #68e5c3, #65bdf0);
                color: #07151c;
                font-weight: bold;
                cursor: pointer;
                transition: 0.25s;
            }

            button:hover {
                transform: scale(1.04);
                box-shadow: 0 0 20px #68e5c355;
            }

            .message {
                background: #142e32;
                border: 1px solid #286052;
                color: #79ebce;
                padding: 15px 20px;
                border-radius: 10px;
                margin: 15px 0 25px;
                font-size: 14px;
            }

            .progress {
                height: 7px;
                background: #28364b;
                border-radius: 10px;
                margin: 15px 0;
                overflow: hidden;
            }

            .progress span {
                display: block;
                height: 100%;
                background: linear-gradient(90deg,
                    #68e5c3, #70aaff);
                border-radius: 10px;
            }

            .concepts {
                display: grid;
                grid-template-columns: repeat(2, 1fr);
                gap: 15px;
            }

            .concept {
                background: #121e30;
                border: 1px solid #293b53;
                padding: 21px;
                border-radius: 13px;
                transition: 0.25s;
            }

            .concept:hover {
                border-color: #68e5c3;
                transform: translateY(-3px);
            }

            .number {
                color: #68e5c3;
                font-size: 11px;
                letter-spacing: 2px;
            }

            .concept h3 {
                font-size: 15px;
                margin: 8px 0;
            }

            .concept p {
                color: #91a0b7;
                font-size: 12px;
            }

            .footer {
                text-align: center;
                padding: 35px 15px;
                border-top: 1px solid #202b40;
                color: #708096;
                font-size: 12px;
            }

            @media(max-width: 700px) {
                .grid, .concepts {
                    grid-template-columns: 1fr;
                }

                .navbar {
                    padding: 18px 5%;
                }

                .navlinks a {
                    margin-left: 10px;
                    font-size: 11px;
                }

                .hero {
                    padding: 55px 7%;
                }
            }
            </style>
            </head>

            <body>

            <nav class="navbar">
                <div class="logo">◈ CRYPTOVAULT</div>
                <div class="navlinks">
                    <a href="#dashboard">Dashboard</a>
                    <a href="#security">Security</a>
                    <a href="#concepts">Java Concepts</a>
                </div>
            </nav>

            <section class="hero">
                <div class="eyebrow">DIGITAL ASSET SECURITY PLATFORM</div>
                <h1>Secure your assets.<br>
                <span class="gradient">Protect your future.</span></h1>
                <p>
                    A Java-based educational simulation of hardware
                    wallet protection and multi-signature authorization.
                    Designed around security, authentication and
                    object-oriented programming.
                </p>
            </section>

            <main class="container">

                <div id="dashboard" class="section-heading">
                    <h2>Security Dashboard</h2>
                    <p>Monitor your wallet and authorization system.</p>
                </div>

                <div class="message">__MESSAGE__</div>

                <div class="grid">

                    <div class="card">
                        <div class="icon">◈</div>
                        <h3>Hardware Wallet</h3>
                        <p class="muted">
                            Protected access through PIN authentication.
                        </p>
                        <div class="status">
                            ● SECURITY AUTHENTICATION
                        </div>

                        <form action="/unlock" method="post">
                            <input type="password" name="pin"
                            placeholder="Enter wallet PIN" required>
                            <button type="submit">
                                Authenticate Wallet →
                            </button>
                        </form>
                    </div>

                    <div class="card">
                        <div class="icon">⬡</div>
                        <h3>Multi-Signature Vault</h3>
                        <p class="muted">
                            Demonstration of multiple approval requests.
                        </p>
                        <div class="big">__APPROVALS__ / 3</div>
                        <p class="muted">Approval threshold: 2 of 3</p>

                        <div class="progress">
                            <span style="width:__PROGRESS__%"></span>
                        </div>

                        <form action="/approve" method="post">
                            <button type="submit">
                                Submit Approval →
                            </button>
                        </form>
                    </div>

                </div>

                <section id="security">
                    <div class="section-heading">
                        <h2>Security Architecture</h2>
                        <p>Core protection mechanisms used in CryptoVault.</p>
                    </div>

                    <div class="grid">
                        <div class="card">
                            <h3>01 / PIN Authentication</h3>
                            <p class="muted">
                                Verifies the user's PIN before granting
                                access to the simulated wallet.
                            </p>
                        </div>

                        <div class="card">
                            <h3>02 / Attempt Protection</h3>
                            <p class="muted">
                                Applies increasing delays and locks
                                the simulated vault after three
                                incorrect attempts.
                            </p>
                        </div>
                    </div>
                </section>

                <section id="concepts">
                    <div class="section-heading">
                        <h2>Core Java Concepts</h2>
                        <p>
                            Object-oriented programming concepts
                            implemented in this project.
                        </p>
                    </div>

                    <div class="concepts">

                        <div class="concept">
                            <div class="number">CONCEPT 01</div>
                            <h3>Interface – MultiSignable</h3>
                            <p>
                                Defines the approval operation required
                                for multi-signature authorization.
                            </p>
                        </div>

                        <div class="concept">
                            <div class="number">CONCEPT 02</div>
                            <h3>Abstract Class – CryptographicVault</h3>
                            <p>
                                Provides the common structure and
                                abstract unlock operation for vaults.
                            </p>
                        </div>

                        <div class="concept">
                            <div class="number">CONCEPT 03</div>
                            <h3>Inheritance</h3>
                            <p>
                                HardwareWallet inherits properties
                                and methods from CryptographicVault.
                            </p>
                        </div>

                        <div class="concept">
                            <div class="number">CONCEPT 04</div>
                            <h3>Interface Implementation</h3>
                            <p>
                                HardwareWallet implements MultiSignable
                                to provide the approval functionality.
                            </p>
                        </div>

                        <div class="concept">
                            <div class="number">CONCEPT 05</div>
                            <h3>Custom Exception – PIN Mismatch</h3>
                            <p>
                                Handles incorrect PIN entries during
                                wallet authentication.
                            </p>
                        </div>

                        <div class="concept">
                            <div class="number">CONCEPT 06</div>
                            <h3>Custom Exception – Vault Wiped</h3>
                            <p>
                                Handles access attempts when the
                                simulated vault becomes inactive.
                            </p>
                        </div>

                        <div class="concept">
                            <div class="number">CONCEPT 07</div>
                            <h3>Thread.sleep()</h3>
                            <p>
                                Introduces increasing delays between
                                unsuccessful PIN verification attempts.
                            </p>
                        </div>

                        <div class="concept">
                            <div class="number">CONCEPT 08</div>
                            <h3>PIN Retry Mechanism</h3>
                            <p>
                                Limits incorrect PIN attempts to
                                demonstrate basic access protection.
                            </p>
                        </div>

                    </div>
                </section>

            </main>

            <footer class="footer">
                CRYPTOVAULT © 2026<br>
                Educational Hardware Wallet Security Simulation
            </footer>

            </body>
            </html>
            """;

        int approvals = wallet.getApprovals();

        return html.replace("__MESSAGE__", escapeHtml(message))
                .replace("__APPROVALS__", String.valueOf(approvals))
                .replace("__PROGRESS__",
                        String.valueOf(approvals * 100 / 3));
    }

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "8080"));

        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/", exchange -> {

            String path = exchange.getRequestURI().getPath();
            String message = "Welcome to your CryptoVault dashboard.";

            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

                if (path.equals("/unlock")) {

                    byte[] body = exchange.getRequestBody().readAllBytes();
                    String input = new String(
                            body, StandardCharsets.UTF_8);

                    String pin = "";

                    for (String part : input.split("&")) {
                        if (part.startsWith("pin=")) {
                            pin = URLDecoder.decode(
                                    part.substring(4), "UTF-8");
                        }
                    }

                    try {
                        if (wallet.unlock(pin)) {
                            message = "Vault unlocked successfully!";
                        }
                    } catch (CryptographicPinMismatchException
                             | VaultWipedException e) {
                        message = e.getMessage();
                    }

                } else if (path.equals("/approve")) {

                    if (wallet.active
                            && wallet.getApprovals() < 3) {
                        wallet.approve();
                        message = "Approval recorded successfully!";
                    } else {
                        message = "Approval unavailable. Check vault status.";
                    }

                } else {
                    message = "Unknown request.";
                }
            }

            String html = dashboard(message);
            byte[] data = html.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                    "Content-Type", "text/html; charset=UTF-8");

            exchange.sendResponseHeaders(200, data.length);

            try (OutputStream out = exchange.getResponseBody()) {
                out.write(data);
            }
        });

        server.start();

        System.out.println(
                "CryptoVault server started on port " + port);
    }
}
