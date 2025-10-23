package ai.document.analyzer.service;

import ai.document.analyzer.model.Document;
import ai.document.analyzer.model.User;
import ai.document.analyzer.repository.DocumentRepository;
import ai.document.analyzer.request.FileDownloadRequest;
import ai.document.analyzer.response.DocumentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ai.document.analyzer.Types;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import static ai.document.analyzer.Types.UPLOADED_FOLDER_DOCUMENT;
import static ai.document.analyzer.util.Util.changeExtensionToPdf;

@Service
public class DocumentService {

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentRepository documentRepository;

    private void convertImageToPdf(MultipartFile file, Path outputPath) {
        try (PDDocument document = new PDDocument()) {
            PDImageXObject image = PDImageXObject.createFromByteArray(document, file.getBytes(), file.getOriginalFilename());
            
            // Create a page with the same dimensions as the image
            PDRectangle pageSize = new PDRectangle(image.getWidth(), image.getHeight());
            PDPage page = new PDPage(pageSize);
            document.addPage(page);
            
            // Add the image to the page
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(image, 0, 0, image.getWidth(), image.getHeight());
            }
            
            document.save(outputPath.toFile());
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