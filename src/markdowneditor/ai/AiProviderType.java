package markdowneditor.ai;

public enum AiProviderType {
    OPENAI("OpenAI", true, "gpt-5.6-luna", "https://api.openai.com/v1"),
    GEMINI("Gemini", true, "gemini-3.8-flash", "https://generativelanguage.googleapis.com/v1beta"),
    OLLAMA("Ollama (local)", false, "llama3.2", "http://localhost:11434");

    private final String displayName;
    private final boolean cloud;
    private final String defaultModel;
    private final String defaultBaseUrl;

    AiProviderType(String displayName, boolean cloud, String defaultModel, String defaultBaseUrl) {
        this.displayName = displayName;
        this.cloud = cloud;
        this.defaultModel = defaultModel;
        this.defaultBaseUrl = defaultBaseUrl;
    }

    public boolean isCloud() {
        return cloud;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public String getDefaultBaseUrl() {
        return defaultBaseUrl;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
