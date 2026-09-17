package markdowneditor.ai;

public final class ProviderFactory {
    private ProviderFactory() {
    }

    public static LlmProvider create(AiSettings settings) {
        return switch (settings.getProvider()) {
            case OPENAI -> new OpenAiProvider(settings);
            case GEMINI -> new GeminiProvider(settings);
            case OLLAMA -> new OllamaProvider(settings);
        };
    }
}
