package markdowneditor.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class AiResultDialog extends JDialog {
    private final JLabel statusLabel = new JLabel("Generating...", SwingConstants.LEFT);
    private final JTextArea resultArea = new JTextArea();
    private final JButton replaceButton = new JButton("Replace Source");
    private final JButton insertButton = new JButton("Insert Below");
    private final JButton copyButton = new JButton("Copy");
    private final JButton closeButton = new JButton("Cancel");
    private final Runnable cancelAction;
    private boolean loading = true;

    public AiResultDialog(JFrame parent, String title, Runnable cancelAction,
            Consumer<String> replaceAction, Consumer<String> insertAction) {
        super(parent, title, false);
        this.cancelAction = cancelAction;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setEditable(false);
        resultArea.setText("Waiting for the model...");

        replaceButton.setEnabled(false);
        insertButton.setEnabled(false);
        copyButton.setEnabled(false);
        replaceButton.addActionListener(e -> replaceAction.accept(resultArea.getText()));
        insertButton.addActionListener(e -> insertAction.accept(resultArea.getText()));
        copyButton.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(resultArea.getText()), null));
        closeButton.addActionListener(e -> closeDialog());

        JPanel buttons = new JPanel();
        buttons.add(replaceButton);
        buttons.add(insertButton);
        buttons.add(copyButton);
        buttons.add(closeButton);

        add(statusLabel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(720, 520));
        pack();
        setLocationRelativeTo(parent);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeDialog();
            }
        });
    }

    public void showResult(String result) {
        loading = false;
        statusLabel.setText("Review the generated Markdown before applying it.");
        resultArea.setText(result);
        resultArea.setCaretPosition(0);
        replaceButton.setEnabled(true);
        insertButton.setEnabled(true);
        copyButton.setEnabled(true);
        closeButton.setText("Close");
    }

    public void showError(String message) {
        loading = false;
        statusLabel.setText("The request failed.");
        resultArea.setText(message);
        resultArea.setCaretPosition(0);
        closeButton.setText("Close");
    }

    private void closeDialog() {
        if (loading) {
            cancelAction.run();
        }
        dispose();
    }
}
