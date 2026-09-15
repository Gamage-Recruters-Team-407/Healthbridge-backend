package lk.gamage.backend.healthbridgebackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public CloudinaryService(
            Cloudinary cloudinary,
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret
    ) {
        this.cloudinary = cloudinary;
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public Map<String, Object> uploadFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return null;
        }

        requireConfiguration();

        try {

            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "folder", "healthbridge/support-tickets",
                    "resource_type", "auto"
            );

            return cloudinary.uploader().upload(
                    file.getBytes(),
                    uploadOptions
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to upload file to Cloudinary",
                    e
            );
        }
    }

    public void deleteFile(String publicId, String resourceType) {

        if (publicId == null || publicId.isBlank()) {
            return;
        }

        requireConfiguration();

        try {

            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap(
                            "resource_type", resourceType
                    )
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to delete file from Cloudinary",
                    e
            );
        }
    }

    private void requireConfiguration() {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new IllegalStateException(
                    "Cloudinary is not configured. Set cloudinary.cloud-name, cloudinary.api-key, and cloudinary.api-secret before uploading or deleting files."
            );
        }
    }
}