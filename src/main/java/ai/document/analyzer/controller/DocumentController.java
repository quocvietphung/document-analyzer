package ai.document.analyzer.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ai.document.analyzer.model.Document;
import ai.document.analyzer.model.User;
import ai.document.analyzer.repository.DocumentRepository;
import ai.document.analyzer.request.FileDownloadRequest;
import ai.document.analyzer.response.DocumentResponse;
import ai.document.analyzer.service.AzureFormRecognizerService;
import ai.document.analyzer.service.DocumentService;
import ai.document.analyzer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AzureFormRecognizerService azureFormRecognizerService;

    /**
     * Get authenticated user ID from JWT token
     */
    private String getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        return authentication.getName(); // This is the user ID from JWT
    }

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadDocument(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("type") String documentType) throws Exception {

        String userId = getAuthenticatedUserId();
        User user = userService.findUserById(userId);
        Document document = documentService.save(file, documentType, user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Document uploaded successfully");
        response.put("document", document);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getUserDocuments")
    public DocumentResponse getUserDocuments() {
        String userId = getAuthenticatedUserId();
        return documentService.getUserDocumentsByUserId(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocument(@PathVariable String id) {
        String userId = getAuthenticatedUserId();
        Optional<Document> documentOptional = documentRepository.findByIdAndUserId(id, userId);

        if (!documentOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(documentOptional.get());
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<?> viewDocument(@PathVariable String id, HttpServletRequest request) {
        String userId = getAuthenticatedUserId();
        Optional<Document> documentOptional = documentRepository.findByIdAndUserId(id, userId);

        if (!documentOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You do not have permission to view this document or the document does not exist");
        }

        FileDownloadRequest fileRequest = new FileDownloadRequest();
        fileRequest.setDocumentId(id);

        try {
            Resource resource = documentService.loadFileAsResource(fileRequest);

            String originalFilename = documentService.getOriginalFilenameById(fileRequest.getDocumentId())
                    .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + fileRequest.getDocumentId()));

            String contentType;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                logger.info("Could not determine file type.");
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFilename + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving the document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/process")
    public ResponseEntity<?> processDocument(@PathVariable String id) {
        try {
            String userId = getAuthenticatedUserId();
            Optional<Document> documentOptional = documentRepository.findByIdAndUserId(id, userId);

            if (!documentOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Document not found or access denied"));
            }

            Document document = documentOptional.get();
            FileDownloadRequest fileRequest = new FileDownloadRequest();
            fileRequest.setDocumentId(id);

            Resource resource = documentService.loadFileAsResource(fileRequest);
            
            // Convert Resource to MultipartFile-like object for Azure processing
            // For now, we'll return a placeholder response
            Map<String, Object> response = new HashMap<>();
            response.put("documentId", id);
            response.put("status", "processing");
            response.put("message", "Document processing initiated");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDocument(@PathVariable String id) {
        String userId = getAuthenticatedUserId();
        boolean deleted = documentService.deleteDocument(id, userId);
        if (deleted) {
            return ResponseEntity.ok().body("Document deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document not found or user is not authorized");
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeDocument(@RequestParam("file") MultipartFile file) {
        try {
            JsonNode result = azureFormRecognizerService.analyzeDocument(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", e.getMessage());
            logger.error("❌ Analyze failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDocuments(@RequestParam String q) {
        try {
            String userId = getAuthenticatedUserId();
            // Basic search implementation - search in file names and document types
            var documents = documentRepository.findByUserId(userId).stream()
                    .filter(doc -> doc.getFileName().toLowerCase().contains(q.toLowerCase()) ||
                                   doc.getDocumentType().toLowerCase().contains(q.toLowerCase()))
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("query", q);
            response.put("results", documents);
            response.put("count", documents.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error searching documents", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
