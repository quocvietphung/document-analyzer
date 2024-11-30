package orgaplan.beratung.kreditunterlagen.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import orgaplan.beratung.kreditunterlagen.Types;
import orgaplan.beratung.kreditunterlagen.model.*;
import orgaplan.beratung.kreditunterlagen.repository.*;
import orgaplan.beratung.kreditunterlagen.request.KreditvermittlerForm;
import orgaplan.beratung.kreditunterlagen.response.*;
import orgaplan.beratung.kreditunterlagen.util.Util;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static orgaplan.beratung.kreditunterlagen.Types.UPLOADED_FOLDER_KREDITVERMITTLER_LOGO;
import static orgaplan.beratung.kreditunterlagen.Types.UPLOADED_FOLDER_KREDITVERMITTLER_PROFILE;

@Service
public class KreditvermittlerService {
    private static final Logger logger = LoggerFactory.getLogger(KreditvermittlerService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private KreditvermittlerRepository kreditvermittlerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CreditRequestRepository creditRequestRepository;

    @Autowired
    private SelbstauskunftRepository selbstauskunftRepository;

    @Autowired
    private WebClient webClient;

    @Autowired
    private ChildrenRepository childrenRepository;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private CompanyService companyService;

    @Transactional(readOnly = true)
    public User findUserByVermittler(String vermittlerId, String userId) {
        return userRepository.findByVermittlerIdAndId(vermittlerId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Beziehung zwischen vermittlerId: " + vermittlerId + " und userId: " + userId + " nicht gefunden"));
    }

    public KreditvermittlerInfo getKreditvermittlerInfoById(String vermittlerId) {
        return kreditvermittlerRepository.findById(vermittlerId)
                .map(this::convertToKreditvermittlerInfo)
                .orElseThrow(() -> new IllegalArgumentException("Kreditvermittler not found with id: " + vermittlerId));
    }

    private KreditvermittlerInfo convertToKreditvermittlerInfo(Kreditvermittler kreditvermittler) {
        return KreditvermittlerInfo.builder()
                .firstName(kreditvermittler.getFirstName())
                .lastName(kreditvermittler.getLastName())
                .role(kreditvermittler.getRole())
                .email(kreditvermittler.getEmail())
                .phoneNumber(kreditvermittler.getPhoneNumber())
                .build();
    }

    public Kreditvermittler createKreditvermittler(Kreditvermittler kreditvermittler) {
        String url = "http://127.0.0.1:8500/user/create";
        String startPassword = Util.generateStartPassword();
        String responseUUID;

        kreditvermittler.setRole(Types.UserRole.KREDIT_VERMITTLER);

        responseUUID = UUID.randomUUID().toString();

        kreditvermittler.setId(responseUUID);
        kreditvermittler.setFirstName(kreditvermittler.getFirstName());
        kreditvermittler.setLastName(kreditvermittler.getLastName());
        kreditvermittler.setPhoneNumber(kreditvermittler.getPhoneNumber());
        kreditvermittler.setTermsAndConditionsAccepted(true);
        kreditvermittler.setPrivacyPolicyAccepted(true);
        kreditvermittler.setUsageTermsAccepted(true);
        kreditvermittler.setConsentTermsAccepted(true);
        kreditvermittler.setCreatedAt(LocalDateTime.now());
        kreditvermittler.setUpdatedAt(LocalDateTime.now());

        return kreditvermittlerRepository.save(kreditvermittler);
    }

    public List<ClientResponse> getClientsByVermittlerId(String vermittlerId) {
        List<User> listClients = userRepository.findByVermittlerId(vermittlerId);
        List<ClientResponse> clientResponses = new ArrayList<>();

        for (User user : listClients) {
            User detailedUser = userService.findUserById(user.getId());
            if (detailedUser != null) {
                ClientResponse response = buildClientResponse(detailedUser);
                clientResponses.add(response);
            }
        }

        return clientResponses;
    }

    @Transactional(readOnly = true)
    public ClientDetail getClientResponseByVermittlerIdAndClientId(String vermittlerId, String clientId) {
        User user = findUserByVermittler(vermittlerId, clientId);

        ClientResponse clientResponse = buildClientResponse(user);

        DocumentResponse documentResponse = documentService.getUserDocumentsByUserId(clientId);

        List<CreditRequestResponse> creditRequestResponses = creditRequestRepository.findByUserId(clientId)
                .stream()
                .map(this::buildCreditRequestResponse)
                .collect(Collectors.toList());

        Company company = null;
        if (user.getRole() == Types.UserRole.FIRMEN_KUNDE) {
            company = companyService.getCompanyByUserId(clientId);
        }

        return ClientDetail.builder()
                .client(clientResponse)
                .document(documentResponse)
                .creditRequests(creditRequestResponses)
                .company(company)
                .build();
    }

    private CreditRequestResponse buildCreditRequestResponse(CreditRequest creditRequest) {
        return CreditRequestResponse.builder()
                .id(creditRequest.getId())
                .kreditTyp(creditRequest.getKreditTyp())
                .kreditLink(creditRequest.getKreditLink())
                .betrag(creditRequest.getBetrag())
                .laufzeit(creditRequest.getLaufzeit())
                .build();
    }

    private ClientResponse buildClientResponse(User user) {
        Long count = creditRequestRepository.countByUserId(user.getId());

        return ClientResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .active(user.getIsActive())
                .withSecondPartner(user.getWithSecondPartner())
                .createdAt(user.getCreatedAt())
                .documentProgress(user.getDocumentUploadPercentage().doubleValue())
                .forwardedBanks(user.getForwardedBanks())
                .forwardedBanksAt(user.getForwardedBanksAt())
                .hasKreditanfrage(count > 0)
                .build();
    }

    public void activateForwardBanksRequest(String vermittlerId, String userId, Boolean forwardedBanks) {
        User user = findUserByVermittler(vermittlerId, userId);

        if (user.getDocumentUploadPercentage().compareTo(new BigDecimal("100.0")) < 0) {
            throw new RuntimeException("Der Status kann nicht geändert werden, da der Dokumentenupload noch nicht bei 100% liegt.");
        }

        Long countCreditRequest = creditRequestRepository.countByUserId(userId);
        if (countCreditRequest == 0) {
            throw new RuntimeException("Cannot set forwardedBanks because count credit request is 0.");
        }

        try {
            String url = "http://127.0.0.1:8500/creditrequest/" + userId + "/publish";
            webClient.post()
                    .uri(url)
                    .retrieve()
                    .onStatus(status -> status.isError(), response -> Mono.error(new RuntimeException("API call failed")))
                    .onStatus(status -> status.value() == 204, response -> Mono.empty())
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();
        } catch (Exception e) {
            logger.error("API call to " + userId + " failed: " + e.getMessage(), e);
        }

        user.setForwardedBanks(forwardedBanks);

        if (forwardedBanks) {
            user.setForwardedBanksAt(LocalDateTime.now());
        } else {
            user.setForwardedBanksAt(null);
        }

        userRepository.save(user);
    }

    public Kreditvermittler editKreditvermittler(String vermittlerId, KreditvermittlerForm kreditvermittlerForm) throws Exception {
        Kreditvermittler kreditvermittler = kreditvermittlerRepository.findById(vermittlerId)
                .orElseThrow(() -> new IllegalArgumentException("Kreditvermittler not found"));

        if (kreditvermittlerForm.getFirstName() != null) {
            kreditvermittler.setFirstName(kreditvermittlerForm.getFirstName());
        }
        if (kreditvermittlerForm.getLastName() != null) {
            kreditvermittler.setLastName(kreditvermittlerForm.getLastName());
        }
        if (kreditvermittlerForm.getPhoneNumber() != null) {
            kreditvermittler.setPhoneNumber(kreditvermittlerForm.getPhoneNumber());
        }
        if (kreditvermittlerForm.getEmail() != null) {
            kreditvermittler.setEmail(kreditvermittlerForm.getEmail());
        }
        if (kreditvermittlerForm.getPrivacyPolicyAccepted() != null) {
            kreditvermittler.setPrivacyPolicyAccepted(kreditvermittlerForm.getPrivacyPolicyAccepted());
        }
        if (kreditvermittlerForm.getTermsAndConditionsAccepted() != null) {
            kreditvermittler.setTermsAndConditionsAccepted(kreditvermittlerForm.getTermsAndConditionsAccepted());
        }

        if (kreditvermittlerForm.getProfileImage() != null && !kreditvermittlerForm.getProfileImage().isEmpty()) {
            saveProfileImage(kreditvermittlerForm.getProfileImage(), kreditvermittler);
        }
        if (kreditvermittlerForm.getLogo() != null && !kreditvermittlerForm.getLogo().isEmpty()) {
            saveLogo(kreditvermittlerForm.getLogo(), kreditvermittler);
        }

        return kreditvermittlerRepository.save(kreditvermittler);
    }

    private void saveProfileImage(MultipartFile profileImage, Kreditvermittler kreditvermittler) throws Exception {
        String originalFilename = saveImage(profileImage, UPLOADED_FOLDER_KREDITVERMITTLER_PROFILE, kreditvermittler.getId());
        kreditvermittler.setProfileImage(originalFilename);
    }

    private void saveLogo(MultipartFile logo, Kreditvermittler kreditvermittler) throws Exception {
        String originalFilename = saveImage(logo, UPLOADED_FOLDER_KREDITVERMITTLER_LOGO, kreditvermittler.getId());
        kreditvermittler.setLogo(originalFilename);
    }

    private String saveImage(MultipartFile image, String folderName, String vermittlerId) throws Exception {
        String originalFilename = image.getOriginalFilename();

        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new Exception("File name cannot be empty");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFilename = vermittlerId + fileExtension;
        Path imagePath = Paths.get(folderName, savedFilename);

        Files.deleteIfExists(imagePath);
        Files.createDirectories(imagePath.getParent());
        Files.copy(image.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
        return originalFilename;
    }

    public Resource findImage(String vermittlerId, String type) throws IOException {
        String directoryPath = type.equals("logo") ?
                UPLOADED_FOLDER_KREDITVERMITTLER_LOGO : UPLOADED_FOLDER_KREDITVERMITTLER_PROFILE;
        Path directory = Paths.get(directoryPath);
        try (var paths = Files.walk(directory)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(vermittlerId))
                    .findFirst()
                    .map(path -> {
                        try {
                            return new UrlResource(path.toUri());
                        } catch (MalformedURLException e) {
                            return null;
                        }
                    }).orElse(null);
        }
    }

    public ClientStatisticsResponse getClientStatistics(String vermittlerId) {
        List<Object[]> statisticsResult = userRepository.getClientStatistics(vermittlerId);
        Object[] statisticsArray = statisticsResult.get(0);

        long totalNumberOfClients = ((Number) statisticsArray[0]).longValue();
        long numberOfActiveClients = ((Number) statisticsArray[1]).longValue();
        long numberOfPrivateClients = ((Number) statisticsArray[2]).longValue();
        long numberOfBusinessClients = ((Number) statisticsArray[3]).longValue();
        long clientsWithCompletedDocumentProcess = ((Number) statisticsArray[4]).longValue();
        long clientsWithKreditanfrage = ((Number) statisticsArray[5]).longValue();
        long clientsForwardedToBank = ((Number) statisticsArray[6]).longValue();

        return ClientStatisticsResponse.builder()
                .totalNumberOfClients(totalNumberOfClients)
                .numberOfActiveClients(numberOfActiveClients)
                .numberOfPrivateClients(numberOfPrivateClients)
                .numberOfBusinessClients(numberOfBusinessClients)
                .clientsWithCompletedDocumentProcess(clientsWithCompletedDocumentProcess)
                .clientsWithKreditanfrage(clientsWithKreditanfrage)
                .clientsForwardedToBank(clientsForwardedToBank)
                .build();
    }

    @Transactional
    public void deleteUser(String vermittlerId, String userId) {
        User user = findUserByVermittler(vermittlerId, userId);

        if (user.getRole() == Types.UserRole.FIRMEN_KUNDE) {
            companyRepository.deleteByUserId(userId);
        }

        Optional<Selbstauskunft> selbstauskunftOptional = selbstauskunftRepository.findByUser(user);
        if (selbstauskunftOptional.isPresent()) {
            Selbstauskunft selbstauskunft = selbstauskunftOptional.get();
            String selbstauskunftId = selbstauskunft.getId();

            childrenRepository.deleteBySelbstauskunftId(selbstauskunftId);
        }

        List<Document> userDocuments = documentRepository.findByUserId(userId);
        for (Document document : userDocuments) {
            deleteFile(Types.UPLOADED_FOLDER_DOCUMENT, document.getId() + ".pdf");
        }

        documentRepository.deleteByUserId(userId);
        selbstauskunftRepository.deleteByUserId(userId);
        creditRequestRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }

    private void deleteFile(String directory, String fileName) {
        try {
            Path filePath = Paths.get(directory, fileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + fileName, e);
        }
    }
}