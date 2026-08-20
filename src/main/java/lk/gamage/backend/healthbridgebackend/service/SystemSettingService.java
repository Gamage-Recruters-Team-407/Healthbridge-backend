package lk.gamage.backend.healthbridgebackend.service;

import lk.gamage.backend.healthbridgebackend.dto.SystemSettingDto;
import lk.gamage.backend.healthbridgebackend.model.SystemSetting;
import lk.gamage.backend.healthbridgebackend.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemSettingService {

    private final SystemSettingRepository repository;

    public SystemSetting createSystemSetting(SystemSettingDto dto) {
        if (repository.findBySettingKey(dto.getSettingKey()).isPresent()) {
            throw new RuntimeException("Setting already exists with key: " + dto.getSettingKey());
        }

        SystemSetting systemSetting = new SystemSetting();
        systemSetting.setCategory(dto.getCategory());
        systemSetting.setSettingKey(dto.getSettingKey());
        systemSetting.setSettingValue(dto.getSettingValue());
        systemSetting.setDescription(dto.getDescription());
        systemSetting.setCreatedAt(LocalDateTime.now());
        systemSetting.setUpdatedAt(LocalDateTime.now());
        systemSetting.setLastModifiedBy("SYSTEM_ADMIN");

        return repository.save(systemSetting);
    }

    public List<SystemSetting> getAllSystemSettings() {
        return repository.findAll();
    }

    public SystemSetting getSystemSetting(String settingKey) {
        return repository.findBySettingKey(settingKey)
                .orElseThrow(() -> new RuntimeException("Setting not found with key: " + settingKey));
    }

    public SystemSetting updateSystemSetting(String settingKey, SystemSettingDto dto) {
        SystemSetting systemSetting = getSystemSetting(settingKey);
        systemSetting.setSettingValue(dto.getSettingValue());
        systemSetting.setDescription(dto.getDescription());
        systemSetting.setCreatedAt(LocalDateTime.now());
        systemSetting.setUpdatedAt(LocalDateTime.now());
        systemSetting.setLastModifiedBy("SYSTEM_ADMIN");

        return repository.save(systemSetting);
    }

    public void resetToDefaultSystemSetting() {

    }
}
