package orgaplan.beratung.kreditunterlagen.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orgaplan.beratung.kreditunterlagen.model.Document;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.request.FileDownloadRequest;
import orgaplan.beratung.kreditunterlagen.response.DocumentResponse;
import orgaplan.beratung.kreditunterlagen.service.DocumentService;
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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentValidation documentValidation;

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadDocument(@RequestParam("file") MultipartFile file,
                                                 @RequestParam("type") String documentType,
                                                 @RequestParam("userId") String userId) {
        User user = userService.getUserById(userId.toString());
        if (user == null) {
            logger.error("User not found with ID: {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        try {
            documentValidation.validateDocumentTypeForUserRole(documentType, user);
            Document document = documentService.save(file, documentType, user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Document uploaded successfully");
            response.put("document", document);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error occurred while saving document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestBody FileDownloadRequest fileRequest, HttpServletRequest request) {
        try {
            // Load file as Resource
            Resource resource = documentService.loadFileAsResource(fileRequest);

            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                logger.info("Could not determine file type.");
            }

            // Fallback to the default content type if type could not be determined
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error occurred while retrieving the document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public DocumentResponse getUserDocuments(@PathVariable String userId) {
        return documentService.getUserDocumentsByUserId(userId);
    }
}
