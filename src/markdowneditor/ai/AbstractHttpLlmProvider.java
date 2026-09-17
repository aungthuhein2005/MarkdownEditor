package markdowneditor.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

abstract class AbstractHttpLlmProvider implements LlmProvider {
    protected static final Gson GSON = new Gson();
    private volatile CompletableFuture<JsonObject> activeRequest;
    private volatile HttpURLConnection activeConnection;

    protected CompletableFuture<JsonObject> postJson(URI uri, JsonObject body, String headerName, String headerValue) {
        activeRequest = CompletableFuture.supplyAsync(() -> {
            HttpURLConnection connection = null;
            int statusCode = -1;
            JsonObject json;
            try {
                connection = (HttpURLConnection) uri.toURL().openConnection();
                activeConnection = connection;
                connection.setConnectTimeout(15_000);
                connection.setReadTimeout(180_000);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                if (headerName != null && headerValue != null && !headerValue.isBlank()) {
                    connection.setRequestProperty(headerName, headerValue);
                }
                connection.setDoOutput(true);
                byte[] requestBody = GSON.toJson(body).getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(requestBody.length);
                try (var output = connection.getOutputStream()) {
                    output.write(requestBody);
                }

                statusCode = connection.getResponseCode();
                InputStream responseStream = statusCode >= 200 && statusCode < 300
                        ? connection.getInputStream() : connection.getErrorStream();
                String responseBody = responseStream == null ? "{}"
                        : new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);
                if (responseStream != null) {
                    responseStream.close();
                }
                json = JsonParser.parseString(responseBody).getAsJsonObject();
            } catch (IOException ex) {
                throw new CompletionException(new IllegalStateException(
                        "Could not connect to the provider: " + ex.getMessage(), ex));
            } catch (RuntimeException ex) {
                throw new CompletionException(new IllegalStateException(
                        "The provider returned an invalid response (HTTP " + statusCode + ").", ex));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                activeConnection = null;
            }
            if (statusCode < 200 || statusCode >= 300) {
                throw new CompletionException(new IllegalStateException(extractError(json, statusCode)));
            }
            return json;
        });
        return activeRequest;
    }

    private String extractError(JsonObject json, int statusCode) {
        if (json.has("error")) {
            if (json.get("error").isJsonObject()) {
                JsonObject error = json.getAsJsonObject("error");
                if (error.has("message")) {
                    return "Provider error: " + error.get("message").getAsString();
                }
            } else if (json.get("error").isJsonPrimitive()) {
                return "Provider error: " + json.get("error").getAsString();
            }
        }
        return "The provider request failed with HTTP " + statusCode + ".";
    }

    protected CompletableFuture<String> requireText(CompletableFuture<String> result) {
        return result.thenApply(text -> {
            if (text == null || text.isBlank()) {
                throw new CompletionException(new IllegalStateException("The provider returned no text."));
            }
            return text.strip();
        });
    }

    @Override
    public void cancel() {
        HttpURLConnection connection = activeConnection;
        if (connection != null) {
            connection.disconnect();
        }
        CompletableFuture<JsonObject> request = activeRequest;
        if (request != null) {
            request.cancel(true);
        }
    }
}
