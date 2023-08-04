package orgaplan.beratung.kreditunterlagen.controller;

import orgaplan.beratung.kreditunterlagen.model.Document;
import orgaplan.beratung.kreditunterlagen.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(@RequestParam("userId") String userId,
                                                              @RequestParam("documentType") String documentType,
                                                              @RequestParam("document") MultipartFile[] files) {
        try {
            Document document = documentService.uploadDocument(files, userId, documentType);
            if (document == null) {
                return ResponseEntity.status(500).body(Map.of("message", "Failed to upload document."));
            }

            ObjectMapper objectMapper = new ObjectMapper();
            // Chuyển chuỗi JSON thành một đối tượng Java có thể hiển thị được
            List<Map<String, String>> documentList = objectMapper.readValue(document.getDocument(), List.class);

            Map<String, Object> response = Map.of(
                    "message", "Documents uploaded and saved successfully",
                    "document", documentList // Sử dụng danh sách đã chuyển đổi
            );

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Failed to upload document."));
        }
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<InputStreamResource> downloadDocument(@PathVariable Long documentId) {
        Document document = documentService.getDocumentById(documentId);
        if (document == null) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = documentService.getDocumentResource(document);
        if (resource == null) {
            return ResponseEntity.status(500).body(null);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + document.getDocument());
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}
