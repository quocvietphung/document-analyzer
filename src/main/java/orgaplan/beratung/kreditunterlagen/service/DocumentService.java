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
import orgaplan.beratung.kreditunterlagen.util.Types;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            // Handle the case where the document type string is not valid
            throw new IllegalArgumentException("Invalid document type: " + documentType);
        }

        document.setUser(user);

        // Save the file locally
        String userFolder = UPLOADED_FOLDER + user.getId() + "/";
        Path path = Paths.get(userFolder + file.getOriginalFilename());
        try {
            // Create directory if not exist
            Files.createDirectories(path.getParent());
            // Write the file
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
            Path filePath = Paths.get(UPLOADED_FOLDER, fileRequest.getUserId(), fileRequest.getFileName()).normalize();
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

        Map<String, List<Document>> docMap = new HashMap<>();
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
}
