import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ElevatorWebServer {
    private static final ElevatorScheduler SCHEDULER = new ElevatorScheduler();
    private static final Path WEB_ROOT = Path.of("public").toAbsolutePath().normalize();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/api/schedule", exchange -> handleApi(exchange, false));
        server.createContext("/api/compare", exchange -> handleApi(exchange, true));
        server.createContext("/", ElevatorWebServer::serveStatic);
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        System.out.println("Intelligent Elevator Scheduler is running on port " + port);
    }

    private static void handleApi(HttpExchange exchange, boolean compare) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"error\":\"Use POST for this endpoint.\"}");
            return;
        }
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            int startFloor = readInt(body, "startFloor", 0);
            List<Integer> requests = readRequests(body);
            if (compare) {
                StringBuilder json = new StringBuilder("{\"results\":[");
                String[] algorithms = {"FCFS", "SSTF", "SCAN", "LOOK"};
                for (int i = 0; i < algorithms.length; i++) {
                    if (i > 0) json.append(',');
                    json.append(resultJson(algorithms[i], startFloor, requests));
                }
                json.append("]}");
                sendJson(exchange, 200, json.toString());
            } else {
                String algorithm = readString(body, "algorithm", "FCFS").toUpperCase(Locale.ROOT);
                sendJson(exchange, 200, resultJson(algorithm, startFloor, requests));
            }
        } catch (IllegalArgumentException ex) {
            sendJson(exchange, 400, "{\"error\":\"" + jsonEscape(ex.getMessage()) + "\"}");
        } catch (Exception ex) {
            sendJson(exchange, 500, "{\"error\":\"The server could not process this request.\"}");
            ex.printStackTrace();
        }
    }

    private static String resultJson(String algorithm, int startFloor, List<Integer> requests) {
        ElevatorScheduler.Result result = SCHEDULER.schedule(startFloor, requests, algorithm);
        List<Integer> movement = new ArrayList<>();
        int current = startFloor;
        int total = 0;
        for (int floor : result.stops) {
            int distance = Math.abs(floor - current);
            movement.add(distance);
            total += distance;
            current = floor;
        }
        StringBuilder json = new StringBuilder("{\"algorithm\":\"").append(algorithm)
                .append("\",\"startFloor\":").append(startFloor)
                .append(",\"stops\":").append(intArray(result.stops))
                .append(",\"movementPerStop\":").append(intArray(movement))
                .append(",\"totalMovement\":").append(result.totalMovement)
                .append(",\"averageMovementBeforeStop\":").append(String.format(Locale.US, "%.2f", result.averageMovementBeforeStop))
                .append(",\"requestsServed\":").append(result.requestsServed).append('}');
        return json.toString();
    }

    private static int readInt(String body, String key, int fallback) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(-?\\d+)").matcher(body);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : fallback;
    }

    private static String readString(String body, String key, String fallback) {
        Matcher matcher = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"").matcher(body);
        return matcher.find() ? matcher.group(1) : fallback;
    }

    private static List<Integer> readRequests(String body) {
        Matcher matcher = Pattern.compile("\\\"requests\\\"\\s*:\\s*\\[([^]]*)]").matcher(body);
        List<Integer> requests = new ArrayList<>();
        if (!matcher.find()) return requests;
        String values = matcher.group(1).trim();
        if (values.isEmpty()) return requests;
        for (String value : values.split(",")) {
            try {
                requests.add(Integer.parseInt(value.trim()));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Floor requests must be whole numbers.");
            }
        }
        return requests;
    }

    private static String intArray(List<Integer> values) {
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) out.append(',');
            out.append(values.get(i));
        }
        return out.append(']').toString();
    }

    private static String jsonEscape(String value) {
        if (value == null) return "Invalid request.";
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", " ");
    }

    private static void serveStatic(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method not allowed", "text/plain; charset=utf-8");
            return;
        }
        String requestPath = exchange.getRequestURI().getPath();
        if (requestPath.equals("/")) requestPath = "/index.html";
        Path file = WEB_ROOT.resolve(requestPath.substring(1)).normalize();
        if (!file.startsWith(WEB_ROOT) || !Files.isRegularFile(file)) {
            sendText(exchange, 404, "Not found", "text/plain; charset=utf-8");
            return;
        }
        String type = file.toString().endsWith(".html") ? "text/html; charset=utf-8"
                : file.toString().endsWith(".css") ? "text/css; charset=utf-8"
                : file.toString().endsWith(".js") ? "text/javascript; charset=utf-8"
                : "application/octet-stream";
        byte[] bytes = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(200, bytes.length);
        try (var out = exchange.getResponseBody()) { out.write(bytes); }
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        sendText(exchange, status, body, "application/json; charset=utf-8");
    }

    private static void sendText(HttpExchange exchange, int status, String body, String type) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var out = exchange.getResponseBody()) { out.write(bytes); }
    }
}
