package orgaplan.beratung.kreditunterlagen.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import orgaplan.beratung.kreditunterlagen.model.Document;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.repository.DocumentRepository;
import orgaplan.beratung.kreditunterlagen.request.DeleteDocumentRequest;
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
import java.security.Principal;
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
                                                 Principal principal) throws Exception {

        String userId = principal.getName();
        User user = userService.findUserById(userId);
        documentValidation.validateDocumentTypeForUserRole(documentType, user);
        Document document = documentService.save(file, documentType, user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Document uploaded successfully");
        response.put("document", document);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('kreditvermittler') || hasRole('privat_kunde') || hasRole('firmen_kunde')")
    @GetMapping("/getUserDocuments")
    public DocumentResponse getUserDocuments(Authentication authentication) {
        String userId = authentication.getName();
        return documentService.getUserDocumentsByUserId(userId);
    }

    @PreAuthorize("hasRole('kreditvermittler') || hasRole('privat_kunde') || hasRole('firmen_kunde')")
    @GetMapping("/view")
    public ResponseEntity<?> viewDocument(Authentication authentication,
                                          @RequestParam String documentId,
                                          @RequestParam(required = false) String userId,
                                          HttpServletRequest request) {
        String currentUserId = authentication.getName();
        boolean isKreditvermittler = kreditvermittlerService.isKreditvermittler(authentication);

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

    @PreAuthorize("hasRole('privat_kunde') || hasRole('firmen_kunde')")
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteDocument(@RequestParam String documentId , Principal principal) {
        String userId = principal.getName();
        boolean deleted = documentService.deleteDocument(documentId, userId);
        if (deleted) {
            return ResponseEntity.ok().body("Document deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Document not found or user is not authorized");
        }
    }
}
