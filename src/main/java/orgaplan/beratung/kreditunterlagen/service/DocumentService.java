package orgaplan.beratung.kreditunterlagen.service;

import orgaplan.beratung.kreditunterlagen.model.Document;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.repository.DocumentRepository;
import orgaplan.beratung.kreditunterlagen.request.FileDownloadRequest;
import orgaplan.beratung.kreditunterlagen.response.DocumentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import orgaplan.beratung.kreditunterlagen.Types;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import static orgaplan.beratung.kreditunterlagen.Types.UPLOADED_FOLDER_DOCUMENT;
import static orgaplan.beratung.kreditunterlagen.util.Util.changeExtensionToPdf;

@Service
public class DocumentService {

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentRepository documentRepository;

    private void convertImageToPdf(MultipartFile file, Path outputPath) {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(outputPath.toFile()));
            document.open();
            Image image = Image.getInstance(file.getBytes());
            image.scaleToFit(document.getPageSize());
            image.setAlignment(Image.ALIGN_CENTER);
            document.add(image);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error converting image to PDF: " + e.getMessage(), e);
        }
    }

    public Document save(MultipartFile file, String documentType, User user) throws IOException {
        Document document = new Document();
        document.setDocumentType(documentType);
        document.setUser(user);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        document = documentRepository.save(document);

        String docNameWithId = document.getId().toString() + ".pdf";
        String originalFilename = file.getOriginalFilename();
        Path path = Paths.get(Types.UPLOADED_FOLDER_DOCUMENT, docNameWithId);

        Files.createDirectories(path.getParent());

        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
            convertImageToPdf(file, path);
            document.setFileName(changeExtensionToPdf(originalFilename));
        } else {
            Files.copy(file.getInputStream(), path);
            if (!originalFilename.toLowerCase().endsWith(".pdf")) {
                originalFilename += ".pdf";
            }
            document.setFileName(originalFilename);
        }

        return documentRepository.save(document);
    }

    public Resource loadFileAsResource(FileDownloadRequest fileRequest) {
        try {
            String storedFileName = fileRequest.getDocumentId() + ".pdf";
            Path filePath = Paths.get(Types.UPLOADED_FOLDER_DOCUMENT, storedFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new IllegalArgumentException("File not found " + storedFileName);
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("File not found " + fileRequest.getDocumentId(), ex);
        }
    }

    public Optional<String> getOriginalFilenameById(String documentId) {
        return documentRepository.findById(documentId).map(Document::getFileName);
    }

    public List<Document> getDocumentsByUserId(String userId) {
        return documentRepository.findByUserId(userId);
    }

    public DocumentResponse getUserDocumentsByUserId(String userId) {
        List<Document> documents = getDocumentsByUserId(userId);

        return DocumentResponse.builder()
                .documents(documents)
                .isSelfEmployed(null)
                .build();
    }

    public boolean deleteDocument(String documentId, String userId) {
        Optional<Document> documentOptional = documentRepository.findByIdAndUserId(documentId, userId);
        if (!documentOptional.isPresent()) {
            throw new IllegalArgumentException("Document not found or does not belong to the user with ID: " + userId);
        }

        Document document = documentOptional.get();
        try {
            String docNameWithId = document.getId().toString() + ".pdf";
            Path path = Paths.get(UPLOADED_FOLDER_DOCUMENT, docNameWithId);

            Files.deleteIfExists(path);
            documentRepository.deleteById(documentId);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Error deleting the document file. " + e.getMessage(), e);
        }
    }
}