package orgaplan.beratung.kreditunterlagen.service;

import orgaplan.beratung.kreditunterlagen.dto.SelbstauskunftDocumentDTO;
import orgaplan.beratung.kreditunterlagen.model.Company;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

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

    @Autowired
    private SelbstauskunftRepository selbstauskunftRepository;

    @Autowired
    private CompanyRepository companyRepository;

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
            e.printStackTrace();
            throw new RuntimeException("Error converting image to PDF: " + e.getMessage());
        }
    }

    public Document save(MultipartFile file, String documentType, User user) throws IOException {
        Types.DocumentType docType = Types.DocumentType.valueOf(documentType);
        Document document = new Document();
        document.setDocumentType(docType);
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
        User user = userService.findUserById(userId);

        List<String> mainUserDocumentTypes = new ArrayList<>();
        List<String> spouseDocumentTypes = new ArrayList<>();
        List<String> optionDocumentsTypes = Arrays.asList(Types.DocumentType.SONSTIGE_DOKUMENTE.name());

        Boolean isSelfEmployed = null;

        if (user.getRole() == Types.UserRole.PRIVAT_KUNDE) {
            mainUserDocumentTypes.addAll(Types.getPrivatKundenDocuments());
        } else if (user.getRole() == Types.UserRole.FIRMEN_KUNDE) {
            mainUserDocumentTypes.addAll(Types.getFirmenKundenDocuments());

            Company company = companyRepository.findByUserId(userId);
            if (company != null) {
                isSelfEmployed = company.getIsSelfEmployed();
            }
        }

        if (user.getWithSecondPartner()) {
            spouseDocumentTypes.addAll(Types.getEhepartnerDocuments());
        }

        Map<String, List<Document>> docMap = initializeDocumentMap(mainUserDocumentTypes);
        Map<String, List<Document>> spouseDocMap = initializeDocumentMap(spouseDocumentTypes);
        Map<String, List<Document>> optionDocMap = initializeDocumentMap(optionDocumentsTypes);

        int completedDocumentTypes = categorizeDocuments(documents, docMap, spouseDocMap, optionDocMap);

        Optional<SelbstauskunftDocumentDTO> selbstauskunftDocumentDTOOptional = selbstauskunftRepository.findSelbstauskunftDocumentByUserId(userId);
        SelbstauskunftDocumentDTO selbstauskunftDocumentDTO = null;
        if (selbstauskunftDocumentDTOOptional.isPresent()) {
            selbstauskunftDocumentDTO = selbstauskunftDocumentDTOOptional.get();
            if (selbstauskunftDocumentDTO.getStatus() == Types.SelbstauskunftStatus.COMPLETED) {
                completedDocumentTypes++;
            }
        }

        int totalDocumentTypes = mainUserDocumentTypes.size() + spouseDocumentTypes.size() + 1;
        BigDecimal percentageUploaded = calculatePercentageUploaded(totalDocumentTypes, completedDocumentTypes);

        return DocumentResponse.builder()
                .regularDocuments(docMap)
                .ehepartnerDocuments(spouseDocMap.isEmpty() ? null : spouseDocMap)
                .selbstauskunftDocument(selbstauskunftDocumentDTO)
                .optionDocuments(optionDocMap)
                .percentageUploaded(percentageUploaded)
                .isSelfEmployed(isSelfEmployed)
                .build();
    }

    private Map<String, List<Document>> initializeDocumentMap(List<String> documentTypes) {
        Map<String, List<Document>> docMap = new LinkedHashMap<>();
        documentTypes.forEach(docType -> docMap.put(docType, new ArrayList<>()));
        return docMap;
    }

    private int categorizeDocuments(List<Document> documents, Map<String, List<Document>> docMap, Map<String, List<Document>> spouseDocMap, Map<String, List<Document>> optionDocMap) {
        Set<String> completedTypes = new HashSet<>();

        documents.forEach(document -> {
            String docTypeName = document.getDocumentType().name();
            if (docMap.containsKey(docTypeName)) {
                docMap.get(docTypeName).add(document);
                completedTypes.add(docTypeName);
            } else if (spouseDocMap.containsKey(docTypeName)) {
                spouseDocMap.get(docTypeName).add(document);
                completedTypes.add(docTypeName);
            } else if (optionDocMap.containsKey(docTypeName)) {
                optionDocMap.get(docTypeName).add(document);
            }
        });

        return completedTypes.size();
    }

    private BigDecimal calculatePercentageUploaded(int totalDocumentTypes, int completedDocumentTypes) {
        if (totalDocumentTypes == 0) return BigDecimal.ZERO;
        return new BigDecimal(completedDocumentTypes)
                .divide(new BigDecimal(totalDocumentTypes), 2, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP);
    }

    public boolean deleteDocument(String documentId, String userId) {
        User user = userService.findUserById(userId);

        Optional<Document> documentOptional = documentRepository.findByIdAndUserId(documentId, userId);
        if (!documentOptional.isPresent()) {
            throw new IllegalArgumentException("Document not found or does not belong to the user with ID: " + userId);
        }

        if (user.getForwardedBanks()) {
            throw new RuntimeException("Banken Weiterleitung wurde aktiviert, Sie können dieses Dokument nicht löschen");
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