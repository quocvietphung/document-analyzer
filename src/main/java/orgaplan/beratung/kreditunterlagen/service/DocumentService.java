package orgaplan.beratung.kreditunterlagen.service;

import orgaplan.beratung.kreditunterlagen.model.Document;
import orgaplan.beratung.kreditunterlagen.model.User;
import orgaplan.beratung.kreditunterlagen.repository.DocumentRepository;
import orgaplan.beratung.kreditunterlagen.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID; // Import UUID

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Autowired
    public DocumentService(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Document uploadDocument(MultipartFile file, String userId, String documentType) throws IOException {
        String uploadDir = System.getProperty("user.dir") + "/uploads";
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdir();
        }

        String fileName = file.getOriginalFilename();
        File convertedFile = new File(uploadDir + "/" + fileName);
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();

        Document document = new Document();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {

            return null;
        }
        document.setUser(user);
        document.setDocumentType(documentType);
        document.setDocument(fileName);
        return documentRepository.save(document);
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    public InputStreamResource getDocumentResource(Document document) {
        String filePath = System.getProperty("user.dir") + "/uploads/" + document.getDocument();
        Path path = Paths.get(filePath);

        try {
            return new InputStreamResource(Objects.requireNonNull(Files.newInputStream(path)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
