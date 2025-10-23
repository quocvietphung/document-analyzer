package ai.document.analyzer.service;

import ai.document.analyzer.config.AzureBlobStorageConfig;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class AzureBlobStorageService {

    private static final Logger logger = LoggerFactory.getLogger(AzureBlobStorageService.class);

    private final BlobServiceClient blobServiceClient;
    private final AzureBlobStorageConfig config;

    public AzureBlobStorageService(BlobServiceClient blobServiceClient, AzureBlobStorageConfig config) {
        this.blobServiceClient = blobServiceClient;
        this.config = config;
    }

    /**
     * Check if Azure Blob Storage is configured and available
     */
    public boolean isEnabled() {
        return blobServiceClient != null && config.getContainerName() != null && !config.getContainerName().isEmpty();
    }

    /**
     * Upload a file to Azure Blob Storage
     * @param file The file to upload
     * @param fileName The name to give the blob
     * @param documentId The document ID to use in the blob name
     * @return The blob URL
     */
    public String uploadFile(MultipartFile file, String fileName, String documentId) throws IOException {
        if (!isEnabled()) {
            throw new IllegalStateException("Azure Blob Storage is not configured");
        }

        try {
            BlobContainerClient containerClient = getOrCreateContainer();
            
            // Create a unique blob name using document ID
            String blobName = documentId + "/" + fileName;
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            // Set content type
            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");

            // Upload the file
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(headers);

            logger.info("Uploaded file to blob storage: {}", blobName);
            return blobClient.getBlobUrl();
        } catch (Exception e) {
            logger.error("Error uploading file to Azure Blob Storage", e);
            throw new IOException("Failed to upload file to Azure Blob Storage", e);
        }
    }

    /**
     * Upload bytes to Azure Blob Storage
     * @param bytes The bytes to upload
     * @param fileName The name to give the blob
     * @param documentId The document ID to use in the blob name
     * @return The blob URL
     */
    public String uploadBytes(byte[] bytes, String fileName, String documentId) throws IOException {
        if (!isEnabled()) {
            throw new IllegalStateException("Azure Blob Storage is not configured");
        }

        try {
            BlobContainerClient containerClient = getOrCreateContainer();
            
            String blobName = documentId + "/" + fileName;
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType("application/pdf");
            
            java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(bytes);
            blobClient.upload(inputStream, bytes.length, true);
            blobClient.setHttpHeaders(headers);

            logger.info("Uploaded bytes to blob storage: {}", blobName);
            return blobClient.getBlobUrl();
        } catch (Exception e) {
            logger.error("Error uploading bytes to Azure Blob Storage", e);
            throw new IOException("Failed to upload bytes to Azure Blob Storage", e);
        }
    }

    /**
     * Download a file from Azure Blob Storage
     * @param blobName The name of the blob to download
     * @return The file content as byte array
     */
    public byte[] downloadFile(String blobName) throws IOException {
        if (!isEnabled()) {
            throw new IllegalStateException("Azure Blob Storage is not configured");
        }

        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(config.getContainerName());
            BlobClient blobClient = containerClient.getBlobClient(blobName);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                blobClient.downloadStream(outputStream);
                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            logger.error("Error downloading file from Azure Blob Storage: {}", blobName, e);
            throw new IOException("Failed to download file from Azure Blob Storage", e);
        }
    }

    /**
     * Get an InputStream for a blob
     * @param blobName The name of the blob
     * @return InputStream for the blob
     */
    public InputStream getBlobInputStream(String blobName) {
        if (!isEnabled()) {
            throw new IllegalStateException("Azure Blob Storage is not configured");
        }

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(config.getContainerName());
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        return blobClient.openInputStream();
    }

    /**
     * Delete a file from Azure Blob Storage
     * @param blobName The name of the blob to delete
     */
    public void deleteFile(String blobName) {
        if (!isEnabled()) {
            throw new IllegalStateException("Azure Blob Storage is not configured");
        }

        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(config.getContainerName());
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.delete();
            logger.info("Deleted blob from storage: {}", blobName);
        } catch (Exception e) {
            logger.error("Error deleting file from Azure Blob Storage: {}", blobName, e);
        }
    }

    /**
     * Get or create the blob container
     */
    private BlobContainerClient getOrCreateContainer() {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(config.getContainerName());
        if (!containerClient.exists()) {
            containerClient.create();
            logger.info("Created blob container: {}", config.getContainerName());
        }
        return containerClient;
    }

    /**
     * Check if a blob exists
     */
    public boolean blobExists(String blobName) {
        if (!isEnabled()) {
            return false;
        }

        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(config.getContainerName());
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            return blobClient.exists();
        } catch (Exception e) {
            logger.error("Error checking if blob exists: {}", blobName, e);
            return false;
        }
    }
}
