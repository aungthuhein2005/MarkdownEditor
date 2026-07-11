/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package markdowneditor;

import markdowneditor.ui.MainFrame;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


/**
 *
 * @author Aung Thu Hein
 */
public class MarkdownEditor {

    /**
     * @param args the command line arguments
     */
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MainFrame.class.getName());
    
    public static void main(String[] args) {
        // TODO code application logic here
        installFlatLaf();
        java.awt.EventQueue.invokeLater(()->new MainFrame().setVisible(true));
        
    }
    
    private static void installFlatLaf(){
        try{
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
        }catch(Exception e){
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { }
        }
    }
    
}
