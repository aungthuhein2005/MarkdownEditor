package markdowneditor.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class GeminiProvider extends AbstractHttpLlmProvider {
    private final AiSettings settings;

    public GeminiProvider(AiSettings settings) {
        this.settings = settings;
    }

    @Override
    public CompletableFuture<String> generate(AiOperation operation, String markdown) {
        String model = URLEncoder.encode(settings.getModel(), StandardCharsets.UTF_8).replace("+", "%20");
        URI uri = URI.create("https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent");

        JsonObject body = new JsonObject();
        body.add("systemInstruction", content(operation.getInstructions(), null));
        JsonArray contents = new JsonArray();
        contents.add(content(operation.createInput(markdown), "user"));
        body.add("contents", contents);
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("maxOutputTokens", settings.getMaxOutputTokens());
        generationConfig.addProperty("temperature", 0.3);
        body.add("generationConfig", generationConfig);

        return requireText(postJson(uri, body, "x-goog-api-key", settings.getApiKey())
                .thenApply(this::extractOutputText));
    }

    private JsonObject content(String text, String role) {
        JsonObject content = new JsonObject();
        if (role != null) {
            content.addProperty("role", role);
        }
        JsonObject part = new JsonObject();
        part.addProperty("text", text);
        JsonArray parts = new JsonArray();
        parts.add(part);
        content.add("parts", parts);
        return content;
    }

    private String extractOutputText(JsonObject json) {
        StringBuilder text = new StringBuilder();
        JsonArray candidates = json.has("candidates") && json.get("candidates").isJsonArray()
                ? json.getAsJsonArray("candidates") : new JsonArray();
        for (JsonElement candidateElement : candidates) {
            JsonObject candidate = candidateElement.getAsJsonObject();
            if (!candidate.has("content")) {
                continue;
            }
            JsonArray parts = candidate.getAsJsonObject("content").getAsJsonArray("parts");
            for (JsonElement partElement : parts) {
                JsonObject part = partElement.getAsJsonObject();
                if (part.has("text")) {
                    text.append(part.get("text").getAsString());
                }
            }
        }
        return text.toString();
    }
}
