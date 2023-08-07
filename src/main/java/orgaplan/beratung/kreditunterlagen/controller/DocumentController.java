package orgaplan.beratung.kreditunterlagen.controller;

import orgaplan.beratung.kreditunterlagen.model.Document;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.service.DocumentService;
import orgaplan.beratung.kreditunterlagen.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;
    private final String uploadDir = "uploads/"; // Đường dẫn thư mục uploads

    @Autowired
    public DocumentController(DocumentService documentService, UserRepository userRepository) {
        this.documentService = documentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/uploadFile")
    public ResponseEntity<Map<String, Object>> saveDocument(@RequestParam("file") MultipartFile file,
                                                            @RequestParam("userId") String userId,
                                                            @RequestParam("docType") String docType) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/documents/downloadFile/")
                .path(uniqueFileName).toUriString();

        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            if (!uploadDirFile.mkdirs()) {
                throw new IOException("Failed to create upload directory.");
            }
        }

        File dest = new File(uploadDir + uniqueFileName);
        file.transferTo(dest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));

        Document doc = new Document();
        doc.setUser(user);
        doc.setDocumentType(docType);
        doc.setFileName(uniqueFileName); // Lưu tên tệp duy nhất
        doc.setFilePath(fileDownloadUri);

        documentService.save(doc);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Document saved successfully");
        response.put("fileName", uniqueFileName); // Trả về tên tệp duy nhất
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Document>> getDocumentsByUserId(@PathVariable String userId) {
        List<Document> docs = documentService.findByUserId(userId);
        return ResponseEntity.ok().body(docs);
    }

    @GetMapping("/downloadFile/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        File file = new File(uploadDir + fileName);
        if (!file.exists()) {
            throw new RuntimeException("File not found: " + fileName);
        }

        Resource resource;
        try {
            Path filePath = file.toPath();
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found: " + fileName, ex);
        }

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
