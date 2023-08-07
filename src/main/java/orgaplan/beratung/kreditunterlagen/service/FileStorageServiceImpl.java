package orgaplan.beratung.kreditunterlagen.service;

import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Override
    public String storeFile(MultipartFile file) {
        // Logic to store file
        return null;
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        // Logic to load file
        return null;
    }
}
