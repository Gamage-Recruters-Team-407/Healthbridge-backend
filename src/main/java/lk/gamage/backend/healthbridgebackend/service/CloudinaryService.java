package lk.gamage.backend.healthbridgebackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map<String, Object> uploadFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return null;
        }

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
}