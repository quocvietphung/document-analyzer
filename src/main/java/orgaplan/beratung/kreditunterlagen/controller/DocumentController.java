package orgaplan.beratung.kreditunterlagen.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orgaplan.beratung.kreditunterlagen.model.Document;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.service.DocumentService;
import orgaplan.beratung.kreditunterlagen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file, @RequestParam("type") String documentType, @RequestParam("userId") String userId) {
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
}
