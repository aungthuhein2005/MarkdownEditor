package markdowneditor.controller;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import markdowneditor.ai.AiOperation;
import markdowneditor.ai.AiSettings;
import markdowneditor.ai.LlmProvider;
import markdowneditor.ai.ProviderFactory;
import markdowneditor.ui.AiResultDialog;
import markdowneditor.ui.AiSettingsDialog;

public class AiAssistantController {
    private final JFrame parentFrame;
    private final JTextArea editorArea;
    private final AiSettings settings = new AiSettings();
    private LlmProvider activeProvider;
    private CompletableFuture<String> activeRequest;
    private boolean cloudDisclosureAccepted;
    private markdowneditor.ai.AiProviderType disclosedProvider;

    public AiAssistantController(JFrame parentFrame, JTextArea editorArea) {
        this.parentFrame = parentFrame;
        this.editorArea = editorArea;
    }

    public void showSettings() {
        markdowneditor.ai.AiProviderType previousProvider = settings.getProvider();
        boolean saved = new AiSettingsDialog(parentFrame, settings).showDialog();
        if (saved && previousProvider != settings.getProvider()) {
            cloudDisclosureAccepted = false;
            disclosedProvider = null;
        }
    }

    public void run(AiOperation operation) {
        if (activeRequest != null && !activeRequest.isDone()) {
            JOptionPane.showMessageDialog(parentFrame,
                    "An AI request is already running.",
                    "AI Assistant", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!validateSettings()) {
            return;
        }

        int start = editorArea.getSelectionStart();
        int end = editorArea.getSelectionEnd();
        if (start == end) {
            start = 0;
            end = editorArea.getText().length();
        }
        String source = editorArea.getText().substring(start, end);
        if (source.isBlank()) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Select some Markdown or enter text in the editor first.",
                    "AI Assistant", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!confirmCloudTransfer(source.length())) {
            return;
        }

        final int sourceStart = start;
        final int sourceEnd = end;
        activeProvider = ProviderFactory.create(settings);
        AiResultDialog[] holder = new AiResultDialog[1];
        AiResultDialog dialog = new AiResultDialog(parentFrame,
                operation + " with " + settings.getProvider(),
                this::cancelActiveRequest,
                result -> replaceSource(sourceStart, sourceEnd, source, result, holder[0]),
                result -> insertBelow(sourceStart, sourceEnd, source, result, holder[0]));
        holder[0] = dialog;
        dialog.setVisible(true);

        try {
            activeRequest = activeProvider.generate(operation, source);
        } catch (RuntimeException ex) {
            dialog.showError(messageFor(ex));
            return;
        }
        activeRequest.whenComplete((result, error) -> SwingUtilities.invokeLater(() -> {
            if (!dialog.isDisplayable()) {
                return;
            }
            if (error != null) {
                if (!(unwrap(error) instanceof CancellationException)) {
                    dialog.showError(messageFor(error));
                }
            } else {
                dialog.showResult(result);
            }
        }));
    }

    private boolean validateSettings() {
        if (settings.getModel() == null || settings.getModel().isBlank()
                || (settings.getProvider().isCloud()
                && (settings.getApiKey() == null || settings.getApiKey().isBlank()))) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Configure the AI provider, model, and API key first.",
                    "AI Assistant", JOptionPane.INFORMATION_MESSAGE);
            showSettings();
        }
        return settings.getModel() != null && !settings.getModel().isBlank()
                && (!settings.getProvider().isCloud()
                || (settings.getApiKey() != null && !settings.getApiKey().isBlank()));
    }

    private boolean confirmCloudTransfer(int characterCount) {
        if (!settings.getProvider().isCloud()) {
            return true;
        }
        if (cloudDisclosureAccepted && disclosedProvider == settings.getProvider()) {
            return true;
        }
        int result = JOptionPane.showConfirmDialog(parentFrame,
                "This will send " + characterCount + " characters of Markdown to "
                + settings.getProvider() + ".\n\nContinue for this application session?",
                "Send Text to Cloud Provider", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            cloudDisclosureAccepted = true;
            disclosedProvider = settings.getProvider();
            return true;
        }
        return false;
    }

    private void replaceSource(int start, int end, String original, String result, AiResultDialog dialog) {
        if (!sourceIsUnchanged(start, end, original)) {
            showSourceChanged();
            return;
        }
        editorArea.replaceRange(result, start, end);
        editorArea.requestFocusInWindow();
        dialog.dispose();
    }

    private void insertBelow(int start, int end, String original, String result, AiResultDialog dialog) {
        if (!sourceIsUnchanged(start, end, original)) {
            showSourceChanged();
            return;
        }
        String separator = end > 0 && editorArea.getText().charAt(end - 1) == '\n' ? "\n" : "\n\n";
        editorArea.insert(separator + result, end);
        editorArea.requestFocusInWindow();
        dialog.dispose();
    }

    private boolean sourceIsUnchanged(int start, int end, String original) {
        return start >= 0 && end <= editorArea.getText().length()
                && editorArea.getText().substring(start, end).equals(original);
    }

    private void showSourceChanged() {
        JOptionPane.showMessageDialog(parentFrame,
                "The source Markdown changed while the AI was working. Run the action again to avoid overwriting newer edits.",
                "Source Changed", JOptionPane.WARNING_MESSAGE);
    }

    private void cancelActiveRequest() {
        if (activeProvider != null) {
            activeProvider.cancel();
        }
        if (activeRequest != null) {
            activeRequest.cancel(true);
        }
    }

    private Throwable unwrap(Throwable error) {
        Throwable current = error;
        while ((current instanceof CompletionException) && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private String messageFor(Throwable error) {
        Throwable cause = unwrap(error);
        String message = cause.getMessage();
        return message == null || message.isBlank()
                ? "The AI provider request failed. Check the provider settings and connection."
                : message;
    }
}
