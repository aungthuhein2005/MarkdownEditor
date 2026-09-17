package markdowneditor.ai;

import java.util.prefs.Preferences;

public class AiSettings {
    private static final String PROVIDER_KEY = "ai.provider";
    private static final String MODEL_KEY_PREFIX = "ai.model.";
    private static final String OLLAMA_URL_KEY = "ai.ollama.url";
    private static final String MAX_TOKENS_KEY = "ai.maxOutputTokens";
    private static final int DEFAULT_MAX_TOKENS = 2000;

    private final Preferences preferences = Preferences.userNodeForPackage(AiSettings.class);
    private AiProviderType provider;
    private String model;
    private String baseUrl;
    private String apiKey;
    private int maxOutputTokens;

    public AiSettings() {
        provider = readProvider();
        model = preferences.get(MODEL_KEY_PREFIX + provider.name(), provider.getDefaultModel());
        baseUrl = provider == AiProviderType.OLLAMA
                ? preferences.get(OLLAMA_URL_KEY, provider.getDefaultBaseUrl())
                : provider.getDefaultBaseUrl();
        apiKey = readEnvironmentKey(provider);
        maxOutputTokens = preferences.getInt(MAX_TOKENS_KEY, DEFAULT_MAX_TOKENS);
    }

    private AiProviderType readProvider() {
        try {
            return AiProviderType.valueOf(preferences.get(PROVIDER_KEY, AiProviderType.OLLAMA.name()));
        } catch (IllegalArgumentException ex) {
            return AiProviderType.OLLAMA;
        }
    }

    private String readEnvironmentKey(AiProviderType selectedProvider) {
        String name = switch (selectedProvider) {
            case OPENAI -> "OPENAI_API_KEY";
            case GEMINI -> "GEMINI_API_KEY";
            case OLLAMA -> null;
        };
        return name == null ? "" : System.getenv().getOrDefault(name, "");
    }

    public void selectProvider(AiProviderType newProvider) {
        if (provider != newProvider) {
            provider = newProvider;
            model = preferences.get(MODEL_KEY_PREFIX + provider.name(), provider.getDefaultModel());
            baseUrl = provider == AiProviderType.OLLAMA
                    ? preferences.get(OLLAMA_URL_KEY, provider.getDefaultBaseUrl())
                    : provider.getDefaultBaseUrl();
            apiKey = readEnvironmentKey(provider);
        }
    }

    public void saveNonSecretSettings() {
        preferences.put(PROVIDER_KEY, provider.name());
        preferences.put(MODEL_KEY_PREFIX + provider.name(), model);
        preferences.putInt(MAX_TOKENS_KEY, maxOutputTokens);
        if (provider == AiProviderType.OLLAMA) {
            preferences.put(OLLAMA_URL_KEY, baseUrl);
        }
    }

    public AiProviderType getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getMaxOutputTokens() {
        return maxOutputTokens;
    }

    public void setMaxOutputTokens(int maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
    }
}
