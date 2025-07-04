package com.example.usercontent.controller;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import com.example.usercontent.model.UserContent;
import com.example.usercontent.service.UserContentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import java.io.ByteArrayOutputStream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
// removed UserContentRequest import

@RestController
@RequestMapping("/content")
public class UserContentController {

    @Autowired
    private UserContentService service;

    @PostMapping
    public ResponseEntity<UserContent> save(@RequestBody(required = false) String content,
                                            @RequestParam(value = "content", required = false) String contentParam) {
        // Accept content from either raw body (text/plain, application/json) or as a form param
        String finalContent = content != null ? content : contentParam;
        if (finalContent == null) {
            return ResponseEntity.badRequest().build();
        }
        UserContent saved = service.saveContent(finalContent);
        return ResponseEntity.ok(saved);
    }


    @GetMapping("/{id}")
    public ResponseEntity<String> getContent(@PathVariable Long id) {
        Optional<UserContent> contentOpt = service.getContent(id);
        if (contentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        String contentStr = contentOpt.get().getContent();
        return ResponseEntity.ok(contentStr);
    }

    // Add new content (already handled by POST /content)

    // Delete content by id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        Optional<UserContent> contentOpt = service.getContent(id);
        if (contentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        service.deleteContent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadContent(
            @PathVariable Long id,
            @RequestParam(defaultValue = "json") String type) throws IOException {

        Optional<UserContent> contentOpt = service.getContent(id);
        if (contentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String content = contentOpt.get().getContent();
        String filename = "content_" + id + "." + type.toLowerCase();

        switch (type.toLowerCase()) {
            case "pdf": {
                // Generate a PDF file using iText
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    PdfWriter writer = new PdfWriter(out);
                    PdfDocument pdf = new PdfDocument(writer);
                    Document document = new Document(pdf);
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        JsonNode jsonNode = mapper.readTree(content);
                        if (jsonNode.isObject()) {
                            jsonNode.fieldNames().forEachRemaining(fieldName -> {
                                JsonNode value = jsonNode.get(fieldName);
                                document.add(new Paragraph(fieldName + ": " + value.asText()));
                            });
                        } else {
                            document.add(new Paragraph(content));
                        }
                    } catch (Exception e) {
                        // Not JSON, treat as plain text
                        for (String line : content.split("\\r?\\n")) {
                            document.add(new Paragraph(line));
                        }
                    }
                    document.close();
                    ByteArrayResource resource = new ByteArrayResource(out.toByteArray());
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename.replace(".pdf", "") + ".pdf")
                            .contentType(MediaType.APPLICATION_PDF)
                            .contentLength(out.size())
                            .body(resource);
                }
            }
            case "js":
            case "css":
            case "py": {
                String mimeType = "text/plain";
                if (type.equals("js")) mimeType = "application/javascript";
                if (type.equals("css")) mimeType = "text/css";
                if (type.equals("py")) mimeType = "text/x-python";
                ByteArrayResource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(MediaType.parseMediaType(mimeType))
                        .contentLength(content.getBytes(StandardCharsets.UTF_8).length)
                        .body(resource);
            }
            case "svg": {
                ByteArrayResource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(MediaType.parseMediaType("image/svg+xml"))
                        .contentLength(content.getBytes(StandardCharsets.UTF_8).length)
                        .body(resource);
            }
            case "jpg":
            case "jpeg":
            case "png": {
                // Render text as an image and return as image file, with word wrapping
                int width = 800;
                int fontSize = 24;
                Font font = new Font("Arial", Font.PLAIN, fontSize);
                // Prepare to wrap text
                java.util.List<String> wrappedLines = new java.util.ArrayList<>();
                BufferedImage tempImg = new BufferedImage(width, 100, BufferedImage.TYPE_INT_RGB);
                Graphics2D tempG = tempImg.createGraphics();
                tempG.setFont(font);
                FontMetrics fm = tempG.getFontMetrics();
                int lineHeight = fm.getHeight();
                for (String line : content.split("\\r?\\n")) {
                    StringBuilder sb = new StringBuilder();
                    for (String word : line.split(" ")) {
                        int lineWidth = fm.stringWidth(sb + (sb.length() > 0 ? " " : "") + word);
                        if (lineWidth > width - 40) {
                            wrappedLines.add(sb.toString());
                            sb = new StringBuilder(word);
                        } else {
                            if (sb.length() > 0) sb.append(" ");
                            sb.append(word);
                        }
                    }
                    if (sb.length() > 0) wrappedLines.add(sb.toString());
                }
                tempG.dispose();
                int height = Math.max(400, 40 + wrappedLines.size() * lineHeight + 40);
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, width, height);
                g2d.setColor(Color.BLACK);
                g2d.setFont(font);
                int y = 40;
                for (String line : wrappedLines) {
                    g2d.drawString(line, 20, y);
                    y += lineHeight;
                }
                g2d.dispose();
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    String format = type.equals("png") ? "png" : "jpg";
                    ImageIO.write(image, format, out);
                    ByteArrayResource resource = new ByteArrayResource(out.toByteArray());
                    String mimeType = type.equals("png") ? "image/png" : "image/jpeg";
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                            .contentType(MediaType.parseMediaType(mimeType))
                            .contentLength(out.size())
                            .body(resource);
                }
            }
            case "gif": {
                // For GIF, fallback to text bytes (or implement similar logic if needed)
                ByteArrayResource resource = new ByteArrayResource(content.getBytes(StandardCharsets.ISO_8859_1));
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(MediaType.parseMediaType("image/gif"))
                        .contentLength(content.getBytes(StandardCharsets.ISO_8859_1).length)
                        .body(resource);
            }
            case "xml": {
                String processedContent = convertToXml(content);
                ByteArrayResource resource = new ByteArrayResource(processedContent.getBytes(StandardCharsets.UTF_8));
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(MediaType.APPLICATION_XML)
                        .contentLength(processedContent.getBytes(StandardCharsets.UTF_8).length)
                        .body(resource);
            }
            case "txt": {
                ByteArrayResource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(MediaType.TEXT_PLAIN)
                        .contentLength(content.getBytes(StandardCharsets.UTF_8).length)
                        .body(resource);
            }
            case "json": {
                ByteArrayResource resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentLength(content.getBytes(StandardCharsets.UTF_8).length)
                        .body(resource);
            }
            case "docx": {
                // Generate a Word document using Apache POI
                try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        JsonNode jsonNode = mapper.readTree(content);
                        // Remove outermost brackets if present (object node)
                        if (jsonNode.isObject()) {
                            jsonNode.fieldNames().forEachRemaining(fieldName -> {
                                JsonNode value = jsonNode.get(fieldName);
                                XWPFParagraph paragraph = doc.createParagraph();
                                XWPFRun run = paragraph.createRun();
                                run.setText(fieldName + ": " + value.asText());
                            });
                        } else {
                            // If not an object, fallback to original string
                            for (String line : content.split("\\r?\\n")) {
                                XWPFParagraph paragraph = doc.createParagraph();
                                XWPFRun run = paragraph.createRun();
                                run.setText(line);
                            }
                        }
                    } catch (Exception e) {
                        // Not JSON, treat as plain text (multi-line supported)
                        for (String line : content.split("\\r?\\n")) {
                            XWPFParagraph paragraph = doc.createParagraph();
                            XWPFRun run = paragraph.createRun();
                            run.setText(line);
                        }
                    }
                    doc.write(out);
                    ByteArrayResource resource = new ByteArrayResource(out.toByteArray());
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                            .contentLength(out.size())
                            .body(resource);
                }
            }
            default: {
                return ResponseEntity.badRequest().body("Unsupported file type: " + type);
            }
        }
    }

    private String convertToXml(String content) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(content);
            StringBuilder xml = new StringBuilder();
            xml.append("<content>\n");
            jsonNode.fieldNames().forEachRemaining(fieldName -> {
                JsonNode value = jsonNode.get(fieldName);
                xml.append("  <").append(fieldName).append(">")
                        .append(value.asText())
                        .append("</").append(fieldName).append(">\n");
            });
            xml.append("</content>");
            return xml.toString();
        } catch (Exception e) {
            // Not JSON, treat as plain text (multi-line supported)
            StringBuilder xml = new StringBuilder();
            xml.append("<content>\n");
            for (String line : content.split("\\r?\\n")) {
                xml.append("  <line>").append(line).append("</line>\n");
            }
            xml.append("</content>");
            return xml.toString();
        }
    }
    // Download any file by filename (e.g., Word, PDF, image, etc.)
    @GetMapping("/download-file")
    public ResponseEntity<Resource> downloadAnyFile(@RequestParam String filename) {
        try {
            // Set your file storage directory here
            Path fileStorageLocation = Paths.get("files").toAbsolutePath().normalize();
            Path filePath = fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            // Try to determine file's content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
