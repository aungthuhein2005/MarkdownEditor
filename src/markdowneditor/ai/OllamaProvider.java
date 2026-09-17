package markdowneditor.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class OllamaProvider extends AbstractHttpLlmProvider {
    private final AiSettings settings;

    public OllamaProvider(AiSettings settings) {
        this.settings = settings;
    }

    @Override
    public CompletableFuture<String> generate(AiOperation operation, String markdown) {
        URI uri = createLocalUri(settings.getBaseUrl(), "/api/chat");
        JsonObject body = new JsonObject();
        body.addProperty("model", settings.getModel());
        body.addProperty("stream", false);
        body.addProperty("think", false);
        JsonArray messages = new JsonArray();
        messages.add(message("system", operation.getInstructions()));
        messages.add(message("user", operation.createInput(markdown)));
        body.add("messages", messages);
        JsonObject options = new JsonObject();
        options.addProperty("temperature", 0.3);
        options.addProperty("num_predict", settings.getMaxOutputTokens());
        body.add("options", options);

        return requireText(postJson(uri, body, null, null).thenApply(json -> {
            if (json.has("message") && json.getAsJsonObject("message").has("content")) {
                return json.getAsJsonObject("message").get("content").getAsString();
            }
            return "";
        }));
    }

    private JsonObject message(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        return message;
    }

    private URI createLocalUri(String baseUrl, String path) {
        URI base;
        try {
            base = URI.create(baseUrl.strip());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("The Ollama URL is invalid.");
        }
        String host = base.getHost() == null ? "" : base.getHost().toLowerCase(Locale.ROOT);
        boolean loopback = host.equals("localhost") || host.equals("127.0.0.1") || host.equals("::1")
                || host.equals("[::1]");
        if (!loopback || base.getUserInfo() != null
                || !("http".equalsIgnoreCase(base.getScheme()) || "https".equalsIgnoreCase(base.getScheme()))) {
            throw new IllegalArgumentException("For safety, the Ollama URL must use localhost or a loopback address.");
        }
        String root = base.toString().replaceAll("/+$", "");
        return URI.create(root + path);
    }
}
