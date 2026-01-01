package jjh.delivery.adapter.in.web.customer;

import jakarta.validation.Valid;
import jjh.delivery.adapter.in.web.auth.dto.CustomerResponse;
import jjh.delivery.adapter.in.web.customer.dto.AddAddressRequest;
import jjh.delivery.adapter.in.web.customer.dto.AddressListResponse;
import jjh.delivery.adapter.in.web.customer.dto.AddressResponse;
import jjh.delivery.adapter.in.web.customer.dto.UpdateProfileRequest;
import jjh.delivery.application.port.in.ManageAddressUseCase;
import jjh.delivery.application.port.in.ManageAddressUseCase.AddAddressCommand;
import jjh.delivery.application.port.in.UpdateCustomerProfileUseCase;
import jjh.delivery.application.port.in.UpdateCustomerProfileUseCase.UpdateProfileCommand;
import jjh.delivery.application.port.out.LoadCustomerPort;
import jjh.delivery.domain.customer.Customer;
import jjh.delivery.domain.customer.CustomerAddress;
import jjh.delivery.domain.customer.exception.CustomerNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer REST Controller - Driving Adapter (Inbound)
 * 고객 프로필 및 배송지 관리 API
 */
@RestController
@RequestMapping("/api/v2/customers")
public class CustomerController {

    private final LoadCustomerPort loadCustomerPort;
    private final UpdateCustomerProfileUseCase updateCustomerProfileUseCase;
    private final ManageAddressUseCase manageAddressUseCase;

    public CustomerController(
            LoadCustomerPort loadCustomerPort,
            UpdateCustomerProfileUseCase updateCustomerProfileUseCase,
            ManageAddressUseCase manageAddressUseCase
    ) {
        this.loadCustomerPort = loadCustomerPort;
        this.updateCustomerProfileUseCase = updateCustomerProfileUseCase;
        this.manageAddressUseCase = manageAddressUseCase;
    }

    /**
     * 내 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Customer customer = getCustomerFromAuth(userDetails);
        return ResponseEntity.ok(CustomerResponse.from(customer));
    }

    /**
     * 내 정보 수정
     */
    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        String customerId = getCustomerIdFromAuth(userDetails);
        UpdateProfileCommand command = new UpdateProfileCommand(request.name(), request.phone());
        Customer updated = updateCustomerProfileUseCase.updateProfile(customerId, command);
        return ResponseEntity.ok(CustomerResponse.from(updated));
    }

    /**
     * 배송지 목록 조회
     */
    @GetMapping("/me/addresses")
    public ResponseEntity<AddressListResponse> getMyAddresses(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String customerId = getCustomerIdFromAuth(userDetails);
        List<CustomerAddress> addresses = manageAddressUseCase.getAddresses(customerId);
        return ResponseEntity.ok(AddressListResponse.from(addresses));
    }

    /**
     * 배송지 등록
     */
    @PostMapping("/me/addresses")
    public ResponseEntity<AddressResponse> addAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddAddressRequest request
    ) {
        String customerId = getCustomerIdFromAuth(userDetails);

        AddAddressCommand command = new AddAddressCommand(
                request.name(),
                request.recipientName(),
                request.recipientPhone(),
                request.postalCode(),
                request.roadAddress(),
                request.detailAddress(),
                request.isDefault()
        );

        CustomerAddress added = manageAddressUseCase.addAddress(customerId, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(AddressResponse.from(added));
    }

    /**
     * 배송지 삭제
     */
    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<Void> removeAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String addressId
    ) {
        String customerId = getCustomerIdFromAuth(userDetails);
        manageAddressUseCase.removeAddress(customerId, addressId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 기본 배송지 설정
     */
    @PatchMapping("/me/addresses/{addressId}/default")
    public ResponseEntity<Void> setDefaultAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String addressId
    ) {
        String customerId = getCustomerIdFromAuth(userDetails);
        manageAddressUseCase.setDefaultAddress(customerId, addressId);
        return ResponseEntity.ok().build();
    }

    // ==================== Private Methods ====================

    private String getCustomerIdFromAuth(UserDetails userDetails) {
        // UserDetails.getUsername()은 customerId로 설정되어 있다고 가정
        return userDetails.getUsername();
    }

    private Customer getCustomerFromAuth(UserDetails userDetails) {
        String customerId = getCustomerIdFromAuth(userDetails);
        return loadCustomerPort.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }
}
