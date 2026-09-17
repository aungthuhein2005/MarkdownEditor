package markdowneditor.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import markdowneditor.ai.AiProviderType;
import markdowneditor.ai.AiSettings;

public class AiSettingsDialog extends JDialog {
    private final AiSettings settings;
    private final JComboBox<AiProviderType> providerBox = new JComboBox<>(AiProviderType.values());
    private final JTextField modelField = new JTextField(28);
    private final JTextField baseUrlField = new JTextField(28);
    private final JPasswordField apiKeyField = new JPasswordField(28);
    private final JSpinner maxTokensSpinner = new JSpinner(new SpinnerNumberModel(2000, 128, 32000, 128));
    private final JLabel keyHint = new JLabel();
    private boolean saved;
    private AiProviderType displayedProvider;

    public AiSettingsDialog(JFrame parent, AiSettings settings) {
        super(parent, "AI Settings", true);
        this.settings = settings;
        buildUi();
        loadSettings();
        pack();
        setMinimumSize(new Dimension(540, getPreferredSize().height));
        setLocationRelativeTo(parent);
    }

    public boolean showDialog() {
        setVisible(true);
        return saved;
    }

    private void buildUi() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 14, 8, 14));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, constraints, 0, "Provider:", providerBox);
        addRow(form, constraints, 1, "Model:", modelField);
        addRow(form, constraints, 2, "Base URL:", baseUrlField);
        addRow(form, constraints, 3, "API key:", apiKeyField);
        addRow(form, constraints, 4, "Maximum output:", maxTokensSpinner);

        constraints.gridx = 1;
        constraints.gridy = 5;
        constraints.weightx = 1;
        form.add(keyHint, constraints);

        JLabel privacy = new JLabel("Cloud text is sent only after confirmation. API keys are kept in memory and are not saved.");
        privacy.setBorder(BorderFactory.createEmptyBorder(4, 19, 10, 19));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> save());
        JPanel buttons = new JPanel();
        buttons.add(cancelButton);
        buttons.add(saveButton);

        providerBox.addActionListener(e -> providerChanged());
        getRootPane().setDefaultButton(saveButton);
        add(form, BorderLayout.CENTER);
        add(privacy, BorderLayout.NORTH);
        add(buttons, BorderLayout.SOUTH);
    }

    private void addRow(JPanel panel, GridBagConstraints constraints, int row, String label, Component component) {
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.weightx = 0;
        panel.add(new JLabel(label), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(component, constraints);
    }

    private void loadSettings() {
        displayedProvider = settings.getProvider();
        providerBox.setSelectedItem(displayedProvider);
        modelField.setText(settings.getModel());
        baseUrlField.setText(settings.getBaseUrl());
        apiKeyField.setText(settings.getApiKey());
        maxTokensSpinner.setValue(settings.getMaxOutputTokens());
        updateProviderFields();
    }

    private void providerChanged() {
        AiProviderType selected = (AiProviderType) providerBox.getSelectedItem();
        if (selected == null || selected == displayedProvider) {
            return;
        }
        displayedProvider = selected;
        modelField.setText(selected.getDefaultModel());
        baseUrlField.setText(selected.getDefaultBaseUrl());
        String environmentName = switch (selected) {
            case OPENAI -> "OPENAI_API_KEY";
            case GEMINI -> "GEMINI_API_KEY";
            case OLLAMA -> null;
        };
        apiKeyField.setText(environmentName == null ? ""
                : System.getenv().getOrDefault(environmentName, ""));
        updateProviderFields();
    }

    private void updateProviderFields() {
        boolean local = displayedProvider == AiProviderType.OLLAMA;
        baseUrlField.setEnabled(local);
        apiKeyField.setEnabled(!local);
        keyHint.setText(local
                ? "Local Ollama does not require an API key."
                : "You may also use the provider's API-key environment variable.");
    }

    private void save() {
        String model = modelField.getText().strip();
        String baseUrl = baseUrlField.getText().strip();
        char[] keyCharacters = apiKeyField.getPassword();
        String apiKey = new String(keyCharacters).strip();
        Arrays.fill(keyCharacters, '\0');

        if (model.isEmpty()) {
            showValidation("Enter a model name.");
            return;
        }
        if (displayedProvider == AiProviderType.OLLAMA && baseUrl.isEmpty()) {
            showValidation("Enter the local Ollama URL.");
            return;
        }
        if (displayedProvider.isCloud() && apiKey.isEmpty()) {
            showValidation("Enter an API key or set the provider's API-key environment variable.");
            return;
        }

        settings.selectProvider(displayedProvider);
        settings.setModel(model);
        settings.setBaseUrl(baseUrl);
        settings.setApiKey(apiKey);
        settings.setMaxOutputTokens((Integer) maxTokensSpinner.getValue());
        settings.saveNonSecretSettings();
        saved = true;
        dispose();
    }

    private void showValidation(String message) {
        JOptionPane.showMessageDialog(this, message, "AI Settings", JOptionPane.WARNING_MESSAGE);
    }
}
