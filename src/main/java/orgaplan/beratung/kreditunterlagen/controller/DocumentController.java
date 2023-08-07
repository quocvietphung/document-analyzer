package orgaplan.beratung.kreditunterlagen.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orgaplan.beratung.kreditunterlagen.model.Document;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.request.FileDownloadRequest;
import orgaplan.beratung.kreditunterlagen.service.DocumentService;
import orgaplan.beratung.kreditunterlagen.service.UserService;
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

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file,
                                            @RequestParam("type") String documentType,
                                            @RequestParam("userId") String userId) {
        User user = userService.getUserById(userId.toString());
        if (user == null) {
            logger.error("User not found with ID: {}", userId);
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        Document document = null;
        try {
            document = documentService.save(file, documentType, user);
        } catch (Exception e) {
            logger.error("Error occurred while saving document", e);
            return new ResponseEntity<>("Error occurred while saving document", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(document, HttpStatus.OK);
    }

    @PostMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestBody FileDownloadRequest fileRequest, HttpServletRequest request) {
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
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
