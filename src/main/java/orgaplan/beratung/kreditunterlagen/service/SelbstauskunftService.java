package orgaplan.beratung.kreditunterlagen.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import orgaplan.beratung.kreditunterlagen.Types;
import orgaplan.beratung.kreditunterlagen.dto.ApplicantDTO;
import orgaplan.beratung.kreditunterlagen.dto.ChildDTO;
import orgaplan.beratung.kreditunterlagen.dto.SelbstauskunftDTO;
import orgaplan.beratung.kreditunterlagen.model.*;
import orgaplan.beratung.kreditunterlagen.model.Document;
import orgaplan.beratung.kreditunterlagen.repository.*;
import reactor.core.publisher.Mono;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SelbstauskunftService {

    private static final Logger logger = LoggerFactory.getLogger(SelbstauskunftService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private SelbstauskunftRepository selbstauskunftRepository;

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private ChildrenRepository childrenRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CoordinatesService coordinatesService;

    @Autowired
    private WebClient webClient;

    @Autowired
    private KreditvermittlerService kreditvermittlerService;

    public Mono<SelbstauskunftDTO> createSelbstauskunftFromAI(String userId) {
        String apiUrl = "https://dochorizon.klippa.com/api/services/document_capturing/v1/prompt_builder/configurations/kd";
        String apiKey = "wkVsEzpbWoSfE3CopHKCTutPXVXY7Qya";

        try {
            User user = userService.findUserById(userId);
            String documentBase64 = findAndConvertDocumentToBase64(userId);

            List<Map<String, String>> documents = new ArrayList<>();
            Map<String, String> documentMap = new HashMap<>();
            documentMap.put("data", documentBase64);
            documents.add(documentMap);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("documents", documents);

            return webClient.post()
                    .uri(apiUrl)
                    .header("x-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .flatMap(response -> {
                        logger.info("Response from AI: {}", response);
                        SelbstauskunftDTO dto = convertToSelbstauskunftDTO(userId,response, Types.DocumentType.GEHALTSABRECHNUNG);
                        if (user.getWithSecondPartner()) {
                            return callPartnerApiAndSave(userId, dto, apiUrl, apiKey);
                        } else {
                            Selbstauskunft savedSelbstauskunft = saveSelbstauskunftAI(userId, dto);
                            saveApplicantsAndChildren(savedSelbstauskunft, dto);
                            return Mono.just(convertToDTO(savedSelbstauskunft));
                        }
                    })
                    .doOnError(e -> logger.error("Error occurred while creating Selbstauskunft from AI", e));
        } catch (Exception e) {
            logger.error("Error in createSelbstauskunftFromAI", e);
            return Mono.error(new RuntimeException("Error in createSelbstauskunftFromAI", e));
        }
    }

    private Mono<SelbstauskunftDTO> callPartnerApiAndSave(String userId, SelbstauskunftDTO dto, String apiUrl, String apiKey) {
        String partnerDocumentBase64 = findAndConvertDocumentToBase64(userId, Types.DocumentType.GEHALTSABRECHNUNG_VON_EHEPARTNER);
        List<Map<String, String>> partnerDocuments = new ArrayList<>();
        Map<String, String> partnerDocumentMap = new HashMap<>();
        partnerDocumentMap.put("data", partnerDocumentBase64);
        partnerDocuments.add(partnerDocumentMap);

        Map<String, Object> partnerRequestBody = new HashMap<>();
        partnerRequestBody.put("documents", partnerDocuments);

        return webClient.post()
                .uri(apiUrl)
                .header("x-api-key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(partnerRequestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(partnerResponse -> {
                    logger.info("Partner response from AI: {}", partnerResponse);
                    ApplicantDTO partnerDto = convertToApplicantDTO(userId, partnerResponse, Types.ApplicantRole.LEBENPARTNER);
                    List<ApplicantDTO> applicants = new ArrayList<>(dto.getApplicants());
                    applicants.add(partnerDto);
                    dto.setApplicants(applicants);
                    Selbstauskunft savedSelbstauskunft = saveSelbstauskunftAI(userId, dto);
                    saveApplicantsAndChildren(savedSelbstauskunft, dto);
                    return Mono.just(convertToDTO(savedSelbstauskunft));
                });
    }

    private void saveApplicantsAndChildren(Selbstauskunft savedSelbstauskunft, SelbstauskunftDTO dto) {
        List<Applicant> applicants = dto.getApplicants().stream()
                .map(applicantDTO -> {
                    Applicant applicant = new Applicant();
                    applicant.setSelbstauskunft(savedSelbstauskunft);
                    applicant.setRole(Types.ApplicantRole.valueOf(applicantDTO.getRole()));
                    applicant.setFirstName(applicantDTO.getFirstName());
                    applicant.setLastName(applicantDTO.getLastName());
                    applicant.setSalutation(applicantDTO.getSalutation());
                    applicant.setTitle(applicantDTO.getTitle());
                    applicant.setDateOfBirth(applicantDTO.getDateOfBirth());
                    applicant.setPlaceOfBirth(applicantDTO.getPlaceOfBirth());
                    applicant.setCountryOfBirth(applicantDTO.getCountryOfBirth());
                    applicant.setNationality(applicantDTO.getNationality());
                    applicant.setSecondNationality(applicantDTO.getSecondNationality());
                    applicant.setPhone(applicantDTO.getPhone());
                    applicant.setEmail(applicantDTO.getEmail());
                    applicant.setStreetHouseNumber(applicantDTO.getStreetHouseNumber());
                    applicant.setPostalCode(applicantDTO.getPostalCode());
                    applicant.setCity(applicantDTO.getCity());
                    applicant.setCountry(applicantDTO.getCountry());
                    applicant.setResidenceSince(applicantDTO.getResidenceSince());
                    applicant.setMaritalStatus(applicantDTO.getMaritalStatus());
                    applicant.setTaxId(applicantDTO.getTaxId());
                    applicant.setEmploymentType(applicantDTO.getEmploymentType());
                    applicant.setNetIncome(applicantDTO.getNetIncome());
                    applicant.setDisposableIncome(applicantDTO.getDisposableIncome());
                    applicant.setStatutoryPension(applicantDTO.getStatutoryPension());
                    applicant.setPrivatePension(applicantDTO.getPrivatePension());
                    applicant.setOtherIncome(applicantDTO.getOtherIncome());
                    applicant.setEmployerName(applicantDTO.getEmployerName());
                    applicant.setJobTitle(applicantDTO.getJobTitle());
                    applicant.setEmployerBasedInGermany(applicantDTO.getEmployerBasedInGermany());
                    applicant.setEmploymentFixedUntil(applicantDTO.getEmploymentFixedUntil());
                    applicant.setProbationUntil(applicantDTO.getProbationUntil());
                    applicant.setRetirementDate(applicantDTO.getRetirementDate());
                    applicant.setCreditInstitute(applicantDTO.getCreditInstitute());
                    applicant.setIban(applicantDTO.getIban());
                    applicant.setBic(applicantDTO.getBic());
                    applicant.setCreatedAt(LocalDateTime.now());
                    applicant.setUpdatedAt(LocalDateTime.now());
                    return applicant;
                })
                .collect(Collectors.toList());

        List<Child> children = dto.getChildren().stream()
                .map(childReq -> {
                    Child child = new Child();
                    child.setSelbstauskunft(savedSelbstauskunft);
                    child.setName(childReq.getName());
                    child.setDateOfBirth(childReq.getDateOfBirth());
                    child.setChildBenefit(childReq.getChildBenefit());
                    child.setAlimonyPayments(childReq.getAlimonyPayments());
                    child.setMonthlyAmount(childReq.getMonthlyAmount());
                    child.setCreatedAt(LocalDateTime.now());
                    child.setUpdatedAt(LocalDateTime.now());
                    return child;
                })
                .collect(Collectors.toList());

        // Save applicants and children
        applicants.forEach(applicantRepository::save);
        children.forEach(childrenRepository::save);
    }

    private ApplicantDTO convertToApplicantDTO(String userId, Map<String, Object> response, Types.ApplicantRole role) {
        User user = userService.findUserById(userId);
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        Map<String, Object> employee = (Map<String, Object>) data.get("employee");
        Map<String, Object> employeeAddress = (Map<String, Object>) employee.get("address");

        String salutation = (String) employee.get("salutation");
        if ("Herrn".equals(salutation)) {
            salutation = "Herr";
        }
        String employerName = (String) ((Map<String, Object>) data.get("company")).get("name");

        BigDecimal netIncome = new BigDecimal(((Number) data.get("Netto")).doubleValue());

        return ApplicantDTO.builder()
                .firstName((String) employee.get("first_name"))
                .lastName((String) employee.get("last_name"))
                .role(role.name())
                .salutation(salutation)
                .dateOfBirth(convertStringToDate((String) employee.get("date_of_birth")))
                .streetHouseNumber(employeeAddress.get("street_name") + " " + employeeAddress.get("house_number"))
                .postalCode((String) employeeAddress.get("postal_code"))
                .city((String) employeeAddress.get("city"))
                .country("Deutschland")
                .taxId((String) employee.get("steuer_id"))
                .netIncome(netIncome)
                .disposableIncome(netIncome)
                .employerName(employerName)
                .jobTitle((String) employee.get("job_title"))
                .creditInstitute((String) employee.get("credit_institute"))
                .iban((String) employee.get("bank_account_number"))
                .bic((String) employee.get("bank_bic"))
                .maritalStatus("")
                .email(role == Types.ApplicantRole.ANTRAGSTELLER ? user.getEmail() : "")
                .phone("")
                .countryOfBirth("")
                .employmentType("")
                .nationality("")
                .placeOfBirth("")
                .employerBasedInGermany(true)
                .build();
    }

    private SelbstauskunftDTO convertToSelbstauskunftDTO(String userId, Map<String, Object> response, Types.DocumentType documentType) {
        Types.ApplicantRole role = documentType == Types.DocumentType.GEHALTSABRECHNUNG ?
                Types.ApplicantRole.ANTRAGSTELLER : Types.ApplicantRole.LEBENPARTNER;

        ApplicantDTO applicantDTO = convertToApplicantDTO(userId,response, role);

        int numberOfChildren = 0;
        if (role == Types.ApplicantRole.ANTRAGSTELLER) {
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            Map<String, Object> employee = (Map<String, Object>) data.get("employee");
            numberOfChildren = (int) employee.get("number_of_children");
        }

        return SelbstauskunftDTO.builder()
                .applicants(Collections.singletonList(applicantDTO))
                .children(Collections.emptyList())
                .numberOfChildren(numberOfChildren)
                .build();
    }

    private Date convertStringToDate(String dateStr) {
        List<String> formats = Arrays.asList("dd-MM-yyyy", "dd.MM.yyyy");
        for (String format : formats) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat(format);
                return formatter.parse(dateStr);
            } catch (ParseException e) {
                // Continue to next format
            }
        }
        throw new RuntimeException("Error parsing date: " + dateStr);
    }

    public String findAndConvertDocumentToBase64(String userId) {
        return findAndConvertDocumentToBase64(userId, Types.DocumentType.GEHALTSABRECHNUNG);
    }

    public String findAndConvertDocumentToBase64(String userId, Types.DocumentType documentType) {
        Optional<Document> documentOptional = documentRepository.findByUserIdAndDocumentType(userId, documentType)
                .stream()
                .findFirst();

        if (!documentOptional.isPresent()) {
            throw new IllegalArgumentException("No " + documentType + " document found for user ID " + userId);
        }

        Document document = documentOptional.get();
        String filePath = Types.UPLOADED_FOLDER_DOCUMENT + document.getId() + ".pdf";
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(filePath));
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            logger.error("Error reading document file at path: " + filePath, e);
            throw new IllegalArgumentException("Error reading document file at path: " + filePath, e);
        }
    }

    private Selbstauskunft saveSelbstauskunftAI(String userId, SelbstauskunftDTO dto) {
        User user = userService.findUserById(userId);
        Selbstauskunft selbstauskunft = new Selbstauskunft();
        selbstauskunft.setUser(user);
        selbstauskunft.setNumberOfChildren(dto.getNumberOfChildren());
        selbstauskunft.setStatus(Types.SelbstauskunftStatus.AI_GENERATED);
        selbstauskunft.setCreatedAt(LocalDateTime.now());
        selbstauskunft.setUpdatedAt(LocalDateTime.now());
        return selbstauskunftRepository.save(selbstauskunft);
    }

    public SelbstauskunftDTO saveSelbstauskunft(String userId, SelbstauskunftDTO request, boolean temporarySave) {
        User user = userService.findUserById(userId);

        if (user.getForwardedBanks()) {
            throw new RuntimeException("Die Weiterleitung zu den Banken wurde aktiviert, daher können Sie diese Selbstauskunft nicht speichern.");
        }

        Selbstauskunft selbstauskunft = selbstauskunftRepository.findByUser(user)
                .orElse(new Selbstauskunft());

        selbstauskunft.setUser(user);
        selbstauskunft.setNumberOfChildren(request.getNumberOfChildren());
        selbstauskunft.setApplicationDate(LocalDateTime.now());

        if (selbstauskunft.getId() == null) {
            selbstauskunft.setCreatedAt(LocalDateTime.now());
        }

        selbstauskunft.setUpdatedAt(LocalDateTime.now());

        if (selbstauskunft.getStatus() == null || temporarySave) {
            selbstauskunft.setStatus(temporarySave ? Types.SelbstauskunftStatus.TEMPORARILY_SAVED : Types.SelbstauskunftStatus.COMPLETED);
        }

        selbstauskunftRepository.save(selbstauskunft);

        List<Applicant> existingApplicants = applicantRepository.findBySelbstauskunft(selbstauskunft);

        if (user.getWithSecondPartner()) {
            if (request.getApplicants().size() != 2) {
                throw new IllegalArgumentException("Die Anzahl der Antragsteller muss genau 2 sein, da dieser Benutzer einen gemeinsamen Kreditantrag mit einem Lebenspartner stellt.");
            }
        } else {
            if (request.getApplicants().size() > 1) {
                throw new IllegalArgumentException("Die maximale Anzahl von Antragstellern ist 1, wenn kein zweiter Partner vorhanden ist.");
            }

            for (Applicant existingApplicant : existingApplicants) {
                if (existingApplicant.getRole() == Types.ApplicantRole.LEBENPARTNER) {
                    applicantRepository.delete(existingApplicant);
                }
            }
        }

        Map<String, Applicant> applicantMap = existingApplicants.stream()
                .collect(Collectors.toMap(Applicant::getId, Function.identity()));

        Map<Integer, Types.ApplicantRole> roleMap = new HashMap<>();
        roleMap.put(0, Types.ApplicantRole.ANTRAGSTELLER);
        roleMap.put(1, Types.ApplicantRole.LEBENPARTNER);

        List<Applicant> applicants = request.getApplicants().stream()
                .map(applicantDTO -> {
                    Applicant applicant = applicantMap.getOrDefault(applicantDTO.getId(), new Applicant());
                    applicant.setSelbstauskunft(selbstauskunft);

                    if (applicant.getId() == null) {
                        applicant.setCreatedAt(LocalDateTime.now());
                        applicant.setRole(roleMap.getOrDefault(request.getApplicants().indexOf(applicantDTO), Types.ApplicantRole.LEBENPARTNER));
                    }

                    applicant.setFirstName(temporarySave && applicantDTO.getFirstName() == null ? "" : applicantDTO.getFirstName());
                    applicant.setLastName(temporarySave && applicantDTO.getLastName() == null ? "" : applicantDTO.getLastName());
                    applicant.setSalutation(temporarySave && applicantDTO.getSalutation() == null ? "" : applicantDTO.getSalutation());
                    applicant.setTitle(applicantDTO.getTitle());
                    applicant.setDateOfBirth(temporarySave && applicantDTO.getDateOfBirth() == null ? new Date() : applicantDTO.getDateOfBirth());
                    applicant.setPlaceOfBirth(temporarySave && applicantDTO.getPlaceOfBirth() == null ? "" : applicantDTO.getPlaceOfBirth());
                    applicant.setCountryOfBirth(temporarySave && applicantDTO.getCountryOfBirth() == null ? "" : applicantDTO.getCountryOfBirth());
                    applicant.setNationality(temporarySave && applicantDTO.getNationality() == null ? "" : applicantDTO.getNationality());
                    applicant.setSecondNationality(applicantDTO.getSecondNationality());
                    applicant.setPhone(temporarySave && applicantDTO.getPhone() == null ? "" : applicantDTO.getPhone());
                    applicant.setEmail(temporarySave && applicantDTO.getEmail() == null ? "" : applicantDTO.getEmail());
                    applicant.setStreetHouseNumber(temporarySave && applicantDTO.getStreetHouseNumber() == null ? "" : applicantDTO.getStreetHouseNumber());
                    applicant.setPostalCode(temporarySave && applicantDTO.getPostalCode() == null ? "" : applicantDTO.getPostalCode());
                    applicant.setCity(temporarySave && applicantDTO.getCity() == null ? "" : applicantDTO.getCity());
                    applicant.setCountry(temporarySave && applicantDTO.getCountry() == null ? "" : applicantDTO.getCountry());
                    applicant.setResidenceSince(applicantDTO.getResidenceSince());
                    applicant.setMaritalStatus(temporarySave && applicantDTO.getMaritalStatus() == null ? "" : applicantDTO.getMaritalStatus());
                    applicant.setTaxId(temporarySave && applicantDTO.getTaxId() == null ? "" : applicantDTO.getTaxId());
                    applicant.setEmploymentType(temporarySave && applicantDTO.getEmploymentType() == null ? "" : applicantDTO.getEmploymentType());
                    applicant.setNetIncome(temporarySave && applicantDTO.getNetIncome() == null ? null : applicantDTO.getNetIncome());
                    applicant.setDisposableIncome(temporarySave && applicantDTO.getDisposableIncome() == null ? null : applicantDTO.getDisposableIncome());
                    applicant.setStatutoryPension(applicantDTO.getStatutoryPension());
                    applicant.setPrivatePension(applicantDTO.getPrivatePension());
                    applicant.setOtherIncome(applicantDTO.getOtherIncome());
                    applicant.setEmployerName(temporarySave && applicantDTO.getEmployerName() == null ? "" : applicantDTO.getEmployerName());
                    applicant.setJobTitle(applicantDTO.getJobTitle());
                    applicant.setEmployerBasedInGermany(applicantDTO.getEmployerBasedInGermany());
                    applicant.setEmploymentFixedUntil(applicantDTO.getEmploymentFixedUntil());
                    applicant.setProbationUntil(applicantDTO.getProbationUntil());
                    applicant.setRetirementDate(applicantDTO.getRetirementDate());
                    applicant.setCreditInstitute(applicantDTO.getCreditInstitute());
                    applicant.setIban(applicantDTO.getIban());
                    applicant.setBic(applicantDTO.getBic());
                    applicant.setUpdatedAt(LocalDateTime.now());

                    String location = coordinatesService.fetchCoordinates(
                            applicant.getStreetHouseNumber(),
                            applicant.getPostalCode(),
                            applicant.getCity(),
                            applicant.getCountry());

                    if (location != null) {
                        applicant.setLocation(location);
                    }

                    return applicant;
                })
                .collect(Collectors.toList());

        applicants.forEach(applicantRepository::save);

        List<Child> existingChildren = childrenRepository.findBySelbstauskunft(selbstauskunft);
        Map<String, Child> childMap = existingChildren.stream()
                .collect(Collectors.toMap(Child::getId, Function.identity()));

        List<Child> childrenToSave = new ArrayList<>();
        Set<String> requestChildIds = request.getChildren().stream()
                .map(ChildDTO::getId)
                .collect(Collectors.toSet());

        for (Child existingChild : existingChildren) {
            if (!requestChildIds.contains(existingChild.getId())) {
                childrenRepository.delete(existingChild);
            }
        }

        for (ChildDTO childReq : request.getChildren()) {
            Child child = childMap.getOrDefault(childReq.getId(), new Child());
            child.setSelbstauskunft(selbstauskunft);
            if (child.getId() == null) {
                child.setCreatedAt(LocalDateTime.now());
            }
            child.setName(temporarySave && childReq.getName() == null ? "" : childReq.getName());
            child.setDateOfBirth(temporarySave && childReq.getDateOfBirth() == null ? null : childReq.getDateOfBirth());
            child.setChildBenefit(temporarySave && childReq.getChildBenefit() == null ? false : childReq.getChildBenefit());
            child.setAlimonyPayments(temporarySave && childReq.getAlimonyPayments() == null ? false : childReq.getAlimonyPayments());
            child.setMonthlyAmount(childReq.getMonthlyAmount());
            child.setUpdatedAt(LocalDateTime.now());
            childrenToSave.add(child);
        }

        childrenToSave.forEach(childrenRepository::save);

        selbstauskunft.setStatus(temporarySave ? Types.SelbstauskunftStatus.TEMPORARILY_SAVED : Types.SelbstauskunftStatus.COMPLETED);
        selbstauskunft.setUpdatedAt(LocalDateTime.now());
        selbstauskunftRepository.save(selbstauskunft);

        SelbstauskunftDTO dto = convertToDTO(selbstauskunft);

        if (!temporarySave) {
            try {
                saveSelbstauskunftPdf(dto, userId);
            } catch (Exception e) {
                logger.error("Error converting to PDF", e);
            }
        }

        return dto;
    }

    public Document saveSelbstauskunftPdf(SelbstauskunftDTO selbstauskunftDTO, String userId) throws IOException, DocumentException {
        User user = userService.findUserById(userId);

        Selbstauskunft selbstauskunft = selbstauskunftRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Selbstauskunft not found for user ID: " + userId));

        Document document = selbstauskunft.getDocument();
        if (document == null) {
            document = new Document();
            document.setCreatedAt(LocalDateTime.now());
        } else {
            // Load the Document to avoid LazyInitializationException
            document = documentRepository.findById(document.getId()).orElseThrow(() -> new IllegalArgumentException("Document not found"));
        }

        document.setDocumentType(Types.DocumentType.SELBSTAUSKUNFT);
        document.setUser(user);
        document.setUpdatedAt(LocalDateTime.now());
        document = documentRepository.save(document);

        String docNameWithId = document.getId().toString() + ".pdf";
        Path path = Paths.get(Types.UPLOADED_FOLDER_DOCUMENT, docNameWithId);

        Files.createDirectories(path.getParent());

        if (Files.exists(path)) {
            Files.delete(path);
        }

        String filePath = path.toString();
        convertSelbstauskunftToPdf(selbstauskunftDTO, filePath, user);

        String fileName = "Selbstauskunft_" + user.getFirstName() + "_" + user.getLastName() + ".pdf";
        document.setFileName(fileName);
        document = documentRepository.save(document);

        selbstauskunft.setDocument(document);
        selbstauskunftRepository.save(selbstauskunft);

        return document;
    }

    private void convertSelbstauskunftToPdf(SelbstauskunftDTO dto, String filePath, User user) {
        com.itextpdf.text.Document document = new com.itextpdf.text.Document();
        PdfWriter writer = null;
        try {
            writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            String vermittlerId = user.getVermittlerId();
            if (vermittlerId != null) {
                try {
                    Resource logoResource = kreditvermittlerService.findImage(vermittlerId, "logo");
                    if (logoResource != null && logoResource.exists()) {
                        Image logo = Image.getInstance(logoResource.getURL());
                        logo.scaleToFit(100, 100);
                        logo.setAlignment(Element.ALIGN_RIGHT);
                        document.add(logo);
                    }
                } catch (Exception e) {
                    System.out.println("Logo not found or could not be added: " + e.getMessage());
                }
            }

            Font headerFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font subHeaderFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font bodyFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

            Paragraph title = new Paragraph("Selbstauskunft", headerFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

            for (ApplicantDTO applicant : dto.getApplicants()) {
                String applicantTitle = "Antragsteller:";
                if (user.getWithSecondPartner() && applicant.getRole().equals(Types.ApplicantRole.LEBENPARTNER.name())) {
                    applicantTitle = "Lebenspartner:";
                }

                document.add(new Paragraph(applicantTitle, subHeaderFont));

                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);

                addTableCell(table, "Vorname:", applicant.getFirstName(), bodyFont);
                addTableCell(table, "Nachname:", applicant.getLastName(), bodyFont);
                addTableCell(table, "Anrede:", applicant.getSalutation(), bodyFont);
                addTableCell(table, "Titel:", applicant.getTitle(), bodyFont);
                addTableCell(table, "Geburtsdatum:", dateFormat.format(applicant.getDateOfBirth()), bodyFont);
                addTableCell(table, "Geburtsort:", applicant.getPlaceOfBirth(), bodyFont);
                addTableCell(table, "Geburtsland:", applicant.getCountryOfBirth(), bodyFont);
                addTableCell(table, "Nationalität:", applicant.getNationality(), bodyFont);
                addTableCell(table, "Zweite Nationalität:", applicant.getSecondNationality(), bodyFont);
                addTableCell(table, "Telefon:", applicant.getPhone(), bodyFont);
                addTableCell(table, "E-Mail:", applicant.getEmail(), bodyFont);
                addTableCell(table, "Straße und Hausnummer:", applicant.getStreetHouseNumber(), bodyFont);
                addTableCell(table, "Postleitzahl:", applicant.getPostalCode(), bodyFont);
                addTableCell(table, "Stadt:", applicant.getCity(), bodyFont);
                addTableCell(table, "Wohnhaft seit:", applicant.getResidenceSince() != null ? dateFormat.format(applicant.getResidenceSince()) : "Keine Angaben", bodyFont);
                addTableCell(table, "Familienstand:", applicant.getMaritalStatus(), bodyFont);
                addTableCell(table, "Steuer-ID:", applicant.getTaxId(), bodyFont);
                addTableCell(table, "Beschäftigungsart:", applicant.getEmploymentType(), bodyFont);
                addTableCell(table, "Nettoeinkommen:", applicant.getNetIncome().toString(), bodyFont);
                addTableCell(table, "Verfügbares Einkommen:", applicant.getDisposableIncome().toString(), bodyFont);
                addTableCell(table, "Gesetzliche Rente:", applicant.getStatutoryPension() != null ? applicant.getStatutoryPension().toString() : "Keine Angaben", bodyFont);
                addTableCell(table, "Private Rente:", applicant.getPrivatePension() != null ? applicant.getPrivatePension().toString() : "Keine Angaben", bodyFont);
                addTableCell(table, "Sonstige Einkommen:", applicant.getOtherIncome() != null ? applicant.getOtherIncome().toString() : "Keine Angaben", bodyFont);
                addTableCell(table, "Arbeitgeber:", applicant.getEmployerName(), bodyFont);
                addTableCell(table, "Berufsbezeichnung:", applicant.getJobTitle(), bodyFont);
                addTableCell(table, "Arbeitgeber in Deutschland:", applicant.getEmployerBasedInGermany(), bodyFont);
                addTableCell(table, "Befristet bis:", applicant.getEmploymentFixedUntil() != null ? dateFormat.format(applicant.getEmploymentFixedUntil()) : "Keine Angaben", bodyFont);
                addTableCell(table, "Probezeit bis:", applicant.getProbationUntil() != null ? dateFormat.format(applicant.getProbationUntil()) : "Keine Angaben", bodyFont);
                addTableCell(table, "Ruhestand ab:", applicant.getRetirementDate() != null ? dateFormat.format(applicant.getRetirementDate()) : "Keine Angaben", bodyFont);
                addTableCell(table, "Kreditinstitut:", applicant.getCreditInstitute(), bodyFont);
                addTableCell(table, "IBAN:", applicant.getIban(), bodyFont);
                addTableCell(table, "BIC:", applicant.getBic(), bodyFont);

                document.add(table);
            }

            document.add(new Paragraph("Anzahl der Kinder: " + dto.getNumberOfChildren(), bodyFont));

            for (ChildDTO child : dto.getChildren()) {
                document.add(new Paragraph("Kind:", subHeaderFont));
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10f);
                table.setSpacingAfter(10f);

                addTableCell(table, "Name:", child.getName(), bodyFont);
                addTableCell(table, "Geburtsdatum:", dateFormat.format(child.getDateOfBirth()), bodyFont);
                addTableCell(table, "Kindergeld:", child.getChildBenefit(), bodyFont);
                addTableCell(table, "Unterhaltszahlungen:", child.getAlimonyPayments(), bodyFont);
                addTableCell(table, "Monatlicher Betrag:", child.getMonthlyAmount() != null ? child.getMonthlyAmount().toString() : "Keine Angaben", bodyFont);

                document.add(table);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating PDF document", e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void addTableCell(PdfPTable table, String header, String value, Font font) {
        table.addCell(new PdfPCell(new Paragraph(header, font)));
        table.addCell(new PdfPCell(new Paragraph(value != null ? value : "Keine Angaben", font)));
    }

    private void addTableCell(PdfPTable table, String header, Boolean value, Font font) {
        table.addCell(new PdfPCell(new Paragraph(header, font)));
        table.addCell(new PdfPCell(new Paragraph(value != null ? (value ? "Ja" : "Nein") : "Keine Angaben", font)));
    }


    public SelbstauskunftDTO getSelbstauskunftByUser(String userId) {
        User user = userService.findUserById(userId);
        Selbstauskunft selbstauskunft = selbstauskunftRepository.findByUser(user)
                .orElse(null);

        if (selbstauskunft == null) {
            return new SelbstauskunftDTO();
        }

        return convertToDTO(selbstauskunft);
    }

    private SelbstauskunftDTO convertToDTO(Selbstauskunft selbstauskunft) {
        List<ApplicantDTO> applicants = applicantRepository.findBySelbstauskunft(selbstauskunft).stream()
                .map(applicant -> new ApplicantDTO(
                        applicant.getId(),
                        applicant.getFirstName(),
                        applicant.getLastName(),
                        applicant.getRole().name(),
                        applicant.getSalutation(),
                        applicant.getTitle(),
                        applicant.getDateOfBirth(),
                        applicant.getPlaceOfBirth(),
                        applicant.getCountryOfBirth(),
                        applicant.getNationality(),
                        applicant.getSecondNationality(),
                        applicant.getPhone(),
                        applicant.getEmail(),
                        applicant.getStreetHouseNumber(),
                        applicant.getPostalCode(),
                        applicant.getCity(),
                        applicant.getCountry(),
                        applicant.getLocation(),
                        applicant.getResidenceSince(),
                        applicant.getMaritalStatus(),
                        applicant.getTaxId(),
                        applicant.getEmploymentType(),
                        applicant.getNetIncome(),
                        applicant.getDisposableIncome(),
                        applicant.getStatutoryPension(),
                        applicant.getPrivatePension(),
                        applicant.getOtherIncome(),
                        applicant.getEmployerName(),
                        applicant.getJobTitle(),
                        applicant.getEmployerBasedInGermany(),
                        applicant.getEmploymentFixedUntil(),
                        applicant.getProbationUntil(),
                        applicant.getRetirementDate(),
                        applicant.getCreditInstitute(),
                        applicant.getIban(),
                        applicant.getBic()
                ))
                .sorted(Comparator.comparing(applicantDTO -> applicantDTO.getRole().equals(Types.ApplicantRole.ANTRAGSTELLER.name()) ? 0 : 1))
                .collect(Collectors.toList());

        List<ChildDTO> children = childrenRepository.findBySelbstauskunft(selbstauskunft).stream()
                .map(child -> new ChildDTO(
                        child.getId(),
                        child.getName(),
                        child.getDateOfBirth(),
                        child.getChildBenefit(),
                        child.getAlimonyPayments(),
                        child.getMonthlyAmount(),
                        child.getCreatedAt(),
                        child.getUpdatedAt()))
                .sorted(Comparator.comparing(ChildDTO::getCreatedAt))
                .collect(Collectors.toList());

        return SelbstauskunftDTO.builder()
                .id(selbstauskunft.getId())
                .numberOfChildren(selbstauskunft.getNumberOfChildren())
                .status(selbstauskunft.getStatus().name())
                .applicationDate(selbstauskunft.getApplicationDate())
                .applicants(applicants)
                .children(children)
                .build();
    }
}