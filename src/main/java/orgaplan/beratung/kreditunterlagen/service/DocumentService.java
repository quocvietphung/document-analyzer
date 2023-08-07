package orgaplan.beratung.kreditunterlagen.service;

import orgaplan.beratung.kreditunterlagen.model.Document;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class DocumentService {

    private static String UPLOADED_FOLDER = "uploads";

    @Autowired
    private DocumentRepository documentRepository;

    public Document save(MultipartFile file, String documentType, User user) {
        Document document = new Document();
        document.setDocumentType(documentType);
        document.setUser(user);

        // Save the file locally
        Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
        try {
            Files.write(path, file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }

        document.setFileName(file.getOriginalFilename());
        document.setFilePath(path.toString());

        return documentRepository.save(document);
    }
}
