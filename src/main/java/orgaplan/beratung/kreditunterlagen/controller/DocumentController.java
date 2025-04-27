package orgaplan.beratung.kreditunterlagen.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orgaplan.beratung.kreditunterlagen.model.Document;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.repository.DocumentRepository;
import orgaplan.beratung.kreditunterlagen.request.FileDownloadRequest;
import orgaplan.beratung.kreditunterlagen.response.DocumentResponse;
import orgaplan.beratung.kreditunterlagen.service.DocumentService;
import orgaplan.beratung.kreditunterlagen.service.KreditvermittlerService;
import orgaplan.beratung.kreditunterlagen.service.UserService;
import orgaplan.beratung.kreditunterlagen.validation.DocumentValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private KreditvermittlerService kreditvermittlerService;

    @Autowired
    private DocumentValidation documentValidation;

    @Autowired
    private DocumentRepository documentRepository;

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadDocument(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("type") String documentType,
                                                 @RequestParam String userId) throws Exception {

        User user = userService.findUserById(userId);
        documentValidation.validateDocumentTypeForUserRole(documentType, user);
        Document document = documentService.save(file, documentType, user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Document uploaded successfully");
        response.put("document", document);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/getUserDocuments")
    public DocumentResponse getUserDocuments(@RequestParam String userId) {
        return documentService.getUserDocumentsByUserId(userId);
    }

    @GetMapping("/view")
    public ResponseEntity<?> viewDocument(@RequestParam String documentId,
                                          @RequestParam String userId,
                                          HttpServletRequest request) {
        String currentUserId = userId;
        boolean isKreditvermittler = false;

        Optional<Document> documentOptional;

        if (isKreditvermittler && userId != null) {
            kreditvermittlerService.findUserByVermittler(currentUserId, userId);
            documentOptional = documentRepository.findByIdAndUserId(documentId, userId);
        } else {
            documentOptional = documentRepository.findByIdAndUserId(documentId, currentUserId);
        }

        if (!documentOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Sie haben keine Berechtigung, dieses Dokument anzuzeigen oder das Dokument existiert nicht");
        }

        FileDownloadRequest fileRequest = new FileDownloadRequest();
        fileRequest.setDocumentId(documentId);

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

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteDocument(@RequestParam String documentId,
                                                 @RequestParam String userId) {
        boolean deleted = documentService.deleteDocument(documentId, userId);
        if (deleted) {
            return ResponseEntity.ok().body("Document deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document not found or user is not authorized");
        }
    }
}
