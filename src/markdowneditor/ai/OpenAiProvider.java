package markdowneditor.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class OpenAiProvider extends AbstractHttpLlmProvider {
    private static final URI RESPONSES_URI = URI.create("https://api.openai.com/v1/responses");
    private final AiSettings settings;

    public OpenAiProvider(AiSettings settings) {
        this.settings = settings;
    }

    @Override
    public CompletableFuture<String> generate(AiOperation operation, String markdown) {
        JsonObject body = new JsonObject();
        body.addProperty("model", settings.getModel());
        body.addProperty("instructions", operation.getInstructions());
        body.addProperty("input", operation.createInput(markdown));
        body.addProperty("max_output_tokens", settings.getMaxOutputTokens());
        body.addProperty("store", false);

        return requireText(postJson(RESPONSES_URI, body, "Authorization", "Bearer " + settings.getApiKey())
                .thenApply(this::extractOutputText));
    }

    private String extractOutputText(JsonObject json) {
        StringBuilder text = new StringBuilder();
        JsonArray output = json.has("output") && json.get("output").isJsonArray()
                ? json.getAsJsonArray("output") : new JsonArray();
        for (JsonElement outputItem : output) {
            if (!outputItem.isJsonObject()) {
                continue;
            }
            JsonObject item = outputItem.getAsJsonObject();
            JsonArray content = item.has("content") && item.get("content").isJsonArray()
                    ? item.getAsJsonArray("content") : new JsonArray();
            for (JsonElement contentItem : content) {
                if (contentItem.isJsonObject() && contentItem.getAsJsonObject().has("text")) {
                    text.append(contentItem.getAsJsonObject().get("text").getAsString());
                }
            }
        }
        return text.toString();
    }
}
