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
    
    
    
    public static void main(String[] args) {
        // TODO code application logic here
        installFlatLaf();
        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("FlatLaf Application");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(400, 300);
//            frame.setVisible(true);
              MainFrame frame = new MainFrame();
        });
        
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
