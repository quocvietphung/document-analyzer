package orgaplan.beratung.kreditunterlagen.controller;

import orgaplan.beratung.kreditunterlagen.model.Document;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.service.DocumentService;
import orgaplan.beratung.kreditunterlagen.service.FileStorageService;
import orgaplan.beratung.kreditunterlagen.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public DocumentController(DocumentService documentService, UserRepository userRepository, FileStorageService fileStorageService) {
        this.documentService = documentService;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/uploadFile")
    public Document saveDocument(@RequestParam("file") MultipartFile file,
                                 @RequestParam("userId") String userId,
                                 @RequestParam("docType") String docType) throws IOException {
        String fileName = fileStorageService.storeFile(file);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/documents/downloadFile/")
                .path(fileName).toUriString();

        Document doc = new Document();
        doc.setUser(user);
        doc.setDocumentType(docType);
        doc.setFileName(fileName);
        doc.setFilePath(fileDownloadUri);

        return documentService.save(doc);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Document>> getDocumentsByUserId(@PathVariable String userId) {
        List<Document> docs = documentService.findByUserId(userId);
        return ResponseEntity.ok().body(docs);
    }

    @GetMapping("/downloadFile/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            System.out.println("Could not determine file type.");
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
