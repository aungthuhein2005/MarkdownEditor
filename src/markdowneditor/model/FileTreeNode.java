/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package markdowneditor.model;

import java.util.List;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

/**
 *
 * @author Aung Thu Hein
 */
public class FileTreeNode implements TreeNode{
    
    private final File file;
    private final FileTreeNode parent;
    private List<FileTreeNode> children;
    
    public FileTreeNode(File file, FileTreeNode parent){
        this.file = file;
        this.parent = parent;
    }
    
    public File getFiel(){return file;}
    
    private void loadChildren(){
        if(children != null) return;
        children = new ArrayList<>();
        File[] files = file.listFiles();
        if(files != null){
            Arrays.sort(files, (a, b) -> {
                if (a.isDirectory() != b.isDirectory()) return a.isDirectory() ? -1 : 1;
                return a.getName().compareToIgnoreCase(b.getName());
            });
            for(File f : files){
                if(f.isDirectory() || f.getName().toLowerCase().endsWith(".md")){
                    children.add(new FileTreeNode(f, this));
                }
            }
        }
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        loadChildren(); return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        loadChildren(); return children.size();
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
        loadChildren(); return children.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return file.isDirectory();
    }

    @Override
    public boolean isLeaf() {
        return !file.isDirectory();
    }

    @Override
    public Enumeration<? extends TreeNode> children() {
        loadChildren(); return Collections.enumeration(children);
    }

    @Override
    public String toString() {
        String name = file.getName();
        return name.isEmpty() ? file.getPath(): name;
    }
    
    
}
