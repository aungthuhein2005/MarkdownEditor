package markdowneditor.ai;

import java.util.concurrent.CompletableFuture;

public interface LlmProvider {
    CompletableFuture<String> generate(AiOperation operation, String markdown);

    void cancel();
}
