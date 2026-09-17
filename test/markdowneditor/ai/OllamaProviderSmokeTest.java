package markdowneditor.ai;

/**
 * Manual local integration check. Start Ollama, then run this class with an
 * installed model name as the first argument.
 */
public final class OllamaProviderSmokeTest {
    private OllamaProviderSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: OllamaProviderSmokeTest <installed-model>");
        }

        AiSettings settings = new AiSettings();
        settings.selectProvider(AiProviderType.OLLAMA);
        settings.setBaseUrl("http://localhost:11434");
        settings.setModel(args[0]);
        settings.setMaxOutputTokens(64);

        String result = new OllamaProvider(settings)
                .generate(AiOperation.SUMMARIZE,
                        "# Test\n\nMarkdownEditor now supports local AI.")
                .get();
        if (result.isBlank()) {
            throw new AssertionError("Ollama returned no text.");
        }
        System.out.println(result);
    }
}
