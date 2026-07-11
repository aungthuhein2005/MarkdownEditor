/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package markdowneditor.controller;

import java.awt.CardLayout;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTree;
import java.io.File;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import markdowneditor.model.FileTreeNode;
import java.util.prefs.Preferences;

/**
 *
 * @author Aung Thu Hein
 */
public class FileTreeController {
    
    private static  final String LAST_FOLDER_KEY = "lastOpenedFolder";
    private final Preferences prefs = Preferences.userNodeForPackage(FileTreeController.class);
    
    private static final String CARD_TREE = "card2";   // match whatever NetBeans generated
    private static final String CARD_EMPTY = "card3";  // match whatever NetBeans generated
    private final Container sidebarPanel;
    
    private final JTree tree;
    private final EditorController editorController;
    private final JFrame parentFrame;
    
    public FileTreeController(JFrame parentFrame, JTree tree, EditorController editorController, Container sidebarPanel){
        this.parentFrame = parentFrame;
        this.tree = tree;
        this.editorController = editorController;
        this.sidebarPanel =sidebarPanel;
        attachedDoubleClickListener();
    }
    
    public void openFolderDialog(){
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(chooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION){
            loadFolder(chooser.getSelectedFile());
        }
    }
    
    public void loadFolder(File rootFolder){
        FileTreeNode root = new FileTreeNode(rootFolder, null);
        tree.setModel(new DefaultTreeModel(root));
        tree.setRootVisible(true);
        prefs.put(LAST_FOLDER_KEY, rootFolder.getAbsolutePath());
        showCard(CARD_TREE);
    }
    
    public void restoreLastFolder(){
        String path = prefs.get(LAST_FOLDER_KEY, null);
        if(path != null){
            File folder = new File(path);
            if(folder.exists() && folder.isDirectory()){
                loadFolder(folder);
                return;
            }
        }
        showCard(CARD_EMPTY);
    }
    
    private void showCard(String cardName){
        ((CardLayout)sidebarPanel.getLayout()).show(sidebarPanel, cardName);
    }
    
    private void attachedDoubleClickListener(){
        tree.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2){
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if(path != null){
                        FileTreeNode node = (FileTreeNode) path.getLastPathComponent();
                        if(!node.getFiel().isDirectory()){
                            editorController.openFile(node.getFiel());
                        }
                    }
                }
            }
            
        });
    }
    
}
