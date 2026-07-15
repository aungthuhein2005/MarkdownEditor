package markdowneditor.controller;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author Aung Thu Hein
 */
public class PdfExportController {

    private final JFrame parentFrame;

    public PdfExportController(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public void exportToPdf(String bodyHtml, String suggestedName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        chooser.setSelectedFile(new File(suggestedName + ".pdf"));

        if (chooser.showSaveDialog(parentFrame) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File outputFile = chooser.getSelectedFile();
        if (!outputFile.getName().toLowerCase().endsWith(".pdf")) {
            outputFile = new File(outputFile.getParentFile(), outputFile.getName() + ".pdf");
        }

        try {
            String xhtml = toXhtml(bodyHtml);
            writePdf(xhtml, outputFile);
            javax.swing.JOptionPane.showMessageDialog(parentFrame,
                    "Exported to " + outputFile.getName(),
                    "Export Complete", javax.swing.JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(parentFrame,
                    "Could not export PDF: " + ex.getMessage(),
                    "Export Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // helpful while debugging
        }
    }

    private String toXhtml(String bodyHtml) {
        String css = loadCss();
        String html
                = "<html><head><style>"
                + css
                + "</style></head><body>"
                + bodyHtml
                + "</body></html>";
        Document doc = Jsoup.parse(html);
        doc.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)
                .escapeMode(Entities.EscapeMode.xhtml);
        return doc.html();
    }

    private void writePdf(String xhtml, File outputFile) throws Exception {
        try (OutputStream os = new FileOutputStream(outputFile)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(xhtml, null);
            builder.toStream(os);
            builder.run();
        }
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
