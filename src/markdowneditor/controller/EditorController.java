/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package markdowneditor.controller;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import markdowneditor.model.DocumentModel;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 *
 * @author Aung Thu Hein
 */
public class EditorController {

    private final JTextArea editorArea;
    private final DocumentModel model;
    private final JFrame parentFrame;

    public EditorController(JFrame parentFrame, JTextArea editorArea, DocumentModel model) {
        this.parentFrame = parentFrame;
        this.editorArea = editorArea;
        this.model = model;
    }

    public void newFile() {
        if (!confirmDiscardChange()) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Markdown Files (*.md)", "md"));
        if (chooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            openFile(chooser.getSelectedFile());
        }
    }

        public void openFile() {
        if (!confirmDiscardChange()) return;
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Markdown Files (*.md)", "md"));
        if (chooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            openFile(chooser.getSelectedFile());
        }
    }

    public void openFile(File file) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            editorArea.setText(content);
            model.setcurrentFile(file);
            model.setModified(false);
        } catch (IOException ex) {
            showError("Could not open file: " + ex.getMessage());
        }
    }

    public void saveFile() {
        if (model.getcurrentFile() == null) {
            saveFileAs();
        } else {
            writeToFile(model.getcurrentFile());
        }
    }

    public void saveFileAs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Markdown Files (*.md)", "md"));
        if (chooser.showSaveDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".md")) {
                file = new File(file.getParentFile(), file.getName() + ".md");
            }
            writeToFile(file);
        }
    }

    public void writeToFile(File file) {
        try {
            Files.write(file.toPath(), editorArea.getText().getBytes(StandardCharsets.UTF_8));
            model.setcurrentFile(file);
            model.setModified(false);
        } catch (IOException e) {
            showError("Could not save file " + e.getMessage());
        }
    }

    private boolean confirmDiscardChange() {
        if (!model.isModified()) {
            return true;
        }
        int result = JOptionPane.showConfirmDialog(parentFrame,
                "You have unsaved changes. Save before continuing?",
                "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            saveFile();
            return !model.isModified();
        }
        return result == JOptionPane.NO_OPTION;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(parentFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
