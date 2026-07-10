/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package markdowneditor.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Aung Thu Hein
 */
public class DocumentModel {
    
     public interface ModelListener {
        void onModelChanged();
    }
    
    private File currentFile;
    private boolean modified;
    private final List<ModelListener> listners = new ArrayList<>();
    
    public void addListener(ModelListener l){listners.add(l);};
    
    private void notifyChanged(){
        for(ModelListener l: listners) l.onModelChanged();
    }
    
    public File getcurrentFile(){
        return currentFile;
    }
    
    public void setcurrentFile(File file){
        this.currentFile = file;
        notifyChanged();
    }
    
    public boolean isModified(){
        return modified;
    }
    
    public void setModified(boolean modified){
        this.modified = modified;
        notifyChanged();
    }
    
    public String getDisplayName(){
        return currentFile != null ? currentFile.getName() : "Untitled";
    }
}
