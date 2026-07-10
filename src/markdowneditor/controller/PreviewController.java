/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package markdowneditor.controller;

import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import org.commonmark.Extension;
import java.util.Arrays;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 *
 * @author Aung Thu Hein
 */
public class PreviewController {
    
    private final List<Extension> extensions = Arrays.asList(TablesExtension.create());
    private final Parser parser = Parser.builder().extensions(extensions).build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
    
    
    private final JEditorPane previewPane;
    private final JTextArea sourceArea;
    
    public PreviewController(JEditorPane previewPane, JTextArea sourceArea){
        this.previewPane = previewPane;
        this.sourceArea = sourceArea;
    }
    
    public void updatePreview(){
        String markdown = sourceArea.getText();
        String bodyHtml = renderer.render(parser.parse(markdown));
        String styledHtml = "<html><head><style>"
            + "body { font-family: sans-serif; font-size: 10pt; }"
            + "table { border-collapse: collapse; margin: 8px 0; }"
            + "table, th, td { border: 1px solid #999; }"
            + "th, td { padding: 4px 8px; }"
            + "</style></head><body>" + bodyHtml + "</body></html>";
        previewPane.setText(styledHtml);
    }
    
}
