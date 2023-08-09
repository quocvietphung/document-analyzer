package orgaplan.beratung.kreditunterlagen.service;

import orgaplan.beratung.kreditunterlagen.exception.UserExceptions;
import orgaplan.beratung.kreditunterlagen.model.Document;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.repository.DocumentRepository;
import orgaplan.beratung.kreditunterlagen.request.FileDownloadRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import orgaplan.beratung.kreditunterlagen.response.DocumentResponse;
import orgaplan.beratung.kreditunterlagen.Types;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class DocumentService {

    private static String UPLOADED_FOLDER = "uploads/";

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserService userService;

    public Document save(MultipartFile file, String documentType, User user) {
        Document document = new Document();

        try {
            Types.DocumentType docType = Types.DocumentType.valueOf(documentType);
            document.setDocumentType(docType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid document type: " + documentType);
        }

        document.setUser(user);

        String docTypeFolder = documentType + "/";
        String userFolder = UPLOADED_FOLDER + user.getId() + "/" + docTypeFolder;
        Path path = Paths.get(userFolder + file.getOriginalFilename());

        try {
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }

        document.setFileName(file.getOriginalFilename());
        document.setFilePath(path.toString());

        return documentRepository.save(document);
    }

    public Resource loadFileAsResource(FileDownloadRequest fileRequest) {
        try {
            String normalizedDocumentType = fileRequest.getDocumentType();
            Path filePath = Paths.get(UPLOADED_FOLDER, fileRequest.getUserId(), normalizedDocumentType, fileRequest.getFileName()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + fileRequest.getFileName());
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + fileRequest.getFileName(), ex);
        }
    }

    public List<Document> getDocumentsByUserId(String userId) {
        return documentRepository.findByUserId(userId);
    }

    public DocumentResponse getUserDocumentsByUserId(String userId) {
        List<Document> documents = getDocumentsByUserId(userId);

        User user = userService.getUserById(userId);
        if (user == null) {
            throw new UserExceptions.UserNotFoundException("User not found with ID: " + userId);
        }

        List<String> requiredDocuments = user.getRole().equals("PRIVAT_KUNDEN")
                ? Types.PRIVAT_KUNDEN_DOCUMENTS
                : Types.FIRMEN_KUNDEN_DOCUMENTS;

        Map<String, List<Document>> docMap = new LinkedHashMap<>();
        for (String docType : requiredDocuments) {
            docMap.put(docType, new ArrayList<>());
        }

        for (Document document : documents) {
            String docTypeName = document.getDocumentType().name();
            if (docMap.containsKey(docTypeName)) {
                docMap.get(docTypeName).add(document);
            }
        }

        return new DocumentResponse(userId, docMap);
    }

    public boolean deleteDocumentByUserIdAndDocumentId(String userId, String documentId) {
        Optional<Document> documentOptional = documentRepository.findById(documentId);

        if (documentOptional.isPresent()) {
            Document document = documentOptional.get();

            // Check if the document belongs to the user
            if (document.getUser().getId().equals(userId)) {
                try {
                    // Delete the file from the file system
                    Path path = Paths.get(document.getFilePath());
                    Files.deleteIfExists(path);

                    // Delete the document record from the database
                    documentRepository.deleteById(documentId);

                    return true;
                } catch (IOException e) {
                    throw new RuntimeException("Error deleting the document file. " + e.getMessage(), e);
                }
            }
        }
        return false;
    }
}
