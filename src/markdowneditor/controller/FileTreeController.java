/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package markdowneditor.controller;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import markdowneditor.model.FileTreeNode;
import java.util.prefs.Preferences;

/**
 *
 * @author Aung Thu Hein
 */
public class FileTreeController {

    private static final String LAST_FOLDER_KEY = "lastOpenedFolder";
    private final Preferences prefs = Preferences.userNodeForPackage(FileTreeController.class);
    private File currentRootFolder;
    private static final String CARD_TREE = "card2";   // match whatever NetBeans generated
    private static final String CARD_EMPTY = "card3";  // match whatever NetBeans generated
    private final Container sidebarPanel;

    private final JTree tree;
    private final EditorController editorController;
    private final JFrame parentFrame;

    public FileTreeController(JFrame parentFrame, JTree tree, EditorController editorController, Container sidebarPanel) {
        this.parentFrame = parentFrame;
        this.tree = tree;
        this.editorController = editorController;
        this.sidebarPanel = sidebarPanel;
        attachTreeListeners();
    }

    public void openFolderDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            loadFolder(chooser.getSelectedFile());
        }
    }


    public void restoreLastFolder() {
        String path = prefs.get(LAST_FOLDER_KEY, null);
        if (path != null) {
            File folder = new File(path);
            if (folder.exists() && folder.isDirectory()) {
                loadFolder(folder);
                return;
            }
        }
        showCard(CARD_EMPTY);
    }

    public File getCurrentRootFolder() {
        return currentRootFolder;
    }

    public void loadFolder(File rootFolder) {
        this.currentRootFolder = rootFolder;
        FileTreeNode root = new FileTreeNode(rootFolder, null);
        tree.setModel(new DefaultTreeModel(root));
        tree.setRootVisible(true);
        prefs.put(LAST_FOLDER_KEY, rootFolder.getAbsolutePath());
        showCard(CARD_TREE);
    }

    public void refresh() {
        if (currentRootFolder != null) {
            loadFolder(currentRootFolder); // rebuilds the node tree with fresh directory listing
        }
    }

    private void showCard(String cardName) {
        ((CardLayout) sidebarPanel.getLayout()).show(sidebarPanel, cardName);
    }

    private void attachTreeListeners() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete File");
        deleteItem.addActionListener(e -> deleteSelectedFile());
        popupMenu.add(deleteItem);

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
                        if (!node.getFiel().isDirectory()) {
                            editorController.openFile(node.getFiel());
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                showPopupIfNeeded(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopupIfNeeded(e);
            }

            private void showPopupIfNeeded(MouseEvent e) {
                if (!e.isPopupTrigger()) {
                    return;
                }

                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path == null) {
                    return;
                }

                tree.setSelectionPath(path);
                FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
                if (node.getFiel().isFile()) {
                    popupMenu.show(tree, e.getX(), e.getY());
                }
            }
        });

        tree.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteFile");
        tree.getActionMap().put("deleteFile", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                deleteSelectedFile();
            }
        });
    }

    private void deleteSelectedFile() {
        TreePath selectedPath = tree.getSelectionPath();
        if (selectedPath == null) {
            return;
        }

        FileTreeNode node = (FileTreeNode) selectedPath.getLastPathComponent();
        File file = node.getFiel();
        if (!file.isFile()) {
            return;
        }

        String message = "Permanently delete \"" + file.getName() + "\"?";
        if (editorController.hasUnsavedChanges(file)) {
            message += "\n\nThis file has unsaved changes that will be lost.";
        }

        int result = JOptionPane.showConfirmDialog(parentFrame, message,
                "Delete File", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Files.delete(file.toPath());
            editorController.closeFile(file);
            refresh();
        } catch (IOException | SecurityException ex) {
            JOptionPane.showMessageDialog(parentFrame,
                    "Could not delete file: " + ex.getMessage(),
                    "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

}
