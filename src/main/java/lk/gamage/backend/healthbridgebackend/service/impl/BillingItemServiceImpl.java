package lk.gamage.backend.healthbridgebackend.service.impl;

import lk.gamage.backend.healthbridgebackend.dto.request.BillingItemRequest;
import lk.gamage.backend.healthbridgebackend.dto.response.BillingItemResponse;
import lk.gamage.backend.healthbridgebackend.model.BillingItem;
import lk.gamage.backend.healthbridgebackend.repository.BillingItemRepository;
import lk.gamage.backend.healthbridgebackend.service.BillingItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillingItemServiceImpl
        implements BillingItemService {

    private final BillingItemRepository billingItemRepository;

    @Override
    public BillingItemResponse createBillingItem(
            BillingItemRequest request) {

        BillingItem item = new BillingItem();

        item.setInvoiceId(request.getInvoiceId());
        item.setPatientId(request.getPatientId());
        item.setCategory(request.getCategory());
        item.setDescription(request.getDescription());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());

        BigDecimal amount =
                request.getUnitPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        request.getQuantity()
                                )
                        );

        item.setAmount(amount);

        BillingItem saved =
                billingItemRepository.save(item);

        return mapToResponse(saved);
    }

    @Override
    public List<BillingItemResponse> getAllBillingItems() {

        return billingItemRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BillingItemResponse getBillingItem(String id) {

        BillingItem item =
                billingItemRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Billing item not found: " + id
                                )
                        );

        return mapToResponse(item);
    }

    @Override
    public List<BillingItemResponse> getInvoiceItems(
            String invoiceId) {

        return billingItemRepository
                .findByInvoiceId(invoiceId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BillingItemResponse updateBillingItem(
            String id,
            BillingItemRequest request) {

        BillingItem item =
                billingItemRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Billing item not found: " + id
                                )
                        );

        item.setInvoiceId(request.getInvoiceId());
        item.setPatientId(request.getPatientId());
        item.setCategory(request.getCategory());
        item.setDescription(request.getDescription());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(request.getUnitPrice());

        BigDecimal amount =
                request.getUnitPrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        request.getQuantity()
                                )
                        );

        item.setAmount(amount);

        return mapToResponse(
                billingItemRepository.save(item)
        );
    }

    @Override
    public void deleteBillingItem(String id) {

        if (!billingItemRepository.existsById(id)) {
            throw new RuntimeException(
                    "Billing item not found: " + id
            );
        }

        billingItemRepository.deleteById(id);
    }

    private BillingItemResponse mapToResponse(
            BillingItem item) {

        return BillingItemResponse.builder()
                .id(item.getId())
                .invoiceId(item.getInvoiceId())
                .patientId(item.getPatientId())
                .category(item.getCategory())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .amount(item.getAmount())
                .build();
    }
}