/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package markdowneditor.controller;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import org.commonmark.Extension;
import java.util.Arrays;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import java.nio.charset.StandardCharsets;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.embed.swing.JFXPanel;
/**
 *
 * @author Aung Thu Hein
 */
public class PreviewController {

    private final List<Extension> extensions = Arrays.asList(TablesExtension.create());
    private final Parser parser = Parser.builder().extensions(extensions).build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();

    private final JFXPanel previewPane;
    private final JTextArea sourceArea;
    private WebView webView;
    public PreviewController(JFXPanel previewPane, JTextArea sourceArea) {
        this.previewPane = previewPane;
        this.sourceArea = sourceArea;
        
        Platform.runLater(() -> {

            webView = new WebView();
    
            Scene scene = new Scene(webView);
    
            previewPane.setScene(scene);
    
        });
    }

    public void updatePreview() {
        String markdown = sourceArea.getText();
        String bodyHtml = renderer.render(parser.parse(markdown));
        String css = loadCss();
        String html
                = "<html><head><style>"
                + css
                + "</style></head><body>"
                + bodyHtml
                + "</body></html>";
                Platform.runLater(() -> {
                    webView.getEngine().loadContent(html);
                });
    }

    private String loadCss() {
        try (InputStream in = getClass().getResourceAsStream("/css/style.css")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
