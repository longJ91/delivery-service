package jjh.delivery.domain.customer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Customer Aggregate Root
 * 순수 도메인 객체 - 외부 프레임워크 의존성 없음
 */
public class Customer {

    private static final int MAX_ADDRESSES = 10;

    private final UUID id;
    private String email;
    private String name;
    private String phoneNumber;
    private String profileImageUrl;
    private CustomerStatus status;
    private final List<CustomerAddress> addresses;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    private Customer(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.email = builder.email;
        this.name = builder.name;
        this.phoneNumber = builder.phoneNumber;
        this.profileImageUrl = builder.profileImageUrl;
        this.status = builder.status != null ? builder.status : CustomerStatus.ACTIVE;
        this.addresses = new ArrayList<>(builder.addresses);
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.lastLoginAt = builder.lastLoginAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    // =====================================================
    // Domain Business Logic
    // =====================================================

    /**
     * 프로필 업데이트
     */
    public void updateProfile(String name, String phoneNumber, String profileImageUrl) {
        validateActiveStatus();
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 이메일 변경
     */
    public void changeEmail(String newEmail) {
        validateActiveStatus();
        if (newEmail == null || newEmail.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        this.email = newEmail;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 배송지 추가
     */
    public void addAddress(CustomerAddress address) {
        validateActiveStatus();
        if (addresses.size() >= MAX_ADDRESSES) {
            throw new IllegalStateException("Maximum " + MAX_ADDRESSES + " addresses allowed");
        }

        // 기본 배송지로 설정하면 기존 기본 배송지 해제
        if (address.isDefault()) {
            clearDefaultAddress();
        }

        // 첫 번째 주소는 자동으로 기본 배송지
        if (addresses.isEmpty()) {
            addresses.add(address.withDefault(true));
        } else {
            addresses.add(address);
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 배송지 삭제
     */
    public void removeAddress(UUID addressId) {
        validateActiveStatus();
        CustomerAddress toRemove = findAddress(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found: " + addressId));

        addresses.remove(toRemove);

        // 삭제된 주소가 기본 배송지였으면 첫 번째 주소를 기본으로 설정
        if (toRemove.isDefault() && !addresses.isEmpty()) {
            CustomerAddress first = addresses.get(0);
            addresses.set(0, first.withDefault(true));
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 기본 배송지 설정
     */
    public void setDefaultAddress(UUID addressId) {
        validateActiveStatus();
        CustomerAddress address = findAddress(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found: " + addressId));

        clearDefaultAddress();

        int index = addresses.indexOf(address);
        addresses.set(index, address.withDefault(true));
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 배송지 수정
     */
    public CustomerAddress updateAddress(
            UUID addressId,
            String name,
            String recipientName,
            String phoneNumber,
            String postalCode,
            String address1,
            String address2,
            boolean isDefault
    ) {
        validateActiveStatus();
        CustomerAddress existing = findAddress(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found: " + addressId));

        // 기본 배송지로 변경하면 기존 기본 배송지 해제
        if (isDefault && !existing.isDefault()) {
            clearDefaultAddress();
        }

        CustomerAddress updated = existing.withUpdated(
                name, recipientName, phoneNumber, postalCode, address1, address2, isDefault
        );

        int index = addresses.indexOf(existing);
        addresses.set(index, updated);
        this.updatedAt = LocalDateTime.now();

        return updated;
    }

    private void clearDefaultAddress() {
        for (int i = 0; i < addresses.size(); i++) {
            if (addresses.get(i).isDefault()) {
                addresses.set(i, addresses.get(i).withDefault(false));
            }
        }
    }

    /**
     * 로그인 기록
     */
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 계정 정지
     */
    public void suspend() {
        if (!status.canBeSuspended()) {
            throw new IllegalStateException("Cannot suspend customer in status: " + status);
        }
        this.status = CustomerStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 계정 활성화
     */
    public void activate() {
        if (this.status == CustomerStatus.WITHDRAWN) {
            throw new IllegalStateException("Cannot activate withdrawn customer");
        }
        this.status = CustomerStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 휴면 전환
     */
    public void makeDormant() {
        if (this.status != CustomerStatus.ACTIVE) {
            throw new IllegalStateException("Only active customers can become dormant");
        }
        this.status = CustomerStatus.DORMANT;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 탈퇴
     */
    public void withdraw() {
        if (!status.canWithdraw()) {
            throw new IllegalStateException("Already withdrawn");
        }
        this.status = CustomerStatus.WITHDRAWN;
        this.updatedAt = LocalDateTime.now();
    }

    // =====================================================
    // Query Methods
    // =====================================================

    /**
     * 기본 배송지 조회
     */
    public Optional<CustomerAddress> getDefaultAddress() {
        return addresses.stream()
                .filter(CustomerAddress::isDefault)
                .findFirst();
    }

    /**
     * 배송지 조회
     */
    public Optional<CustomerAddress> findAddress(UUID addressId) {
        return addresses.stream()
                .filter(addr -> addr.id().equals(addressId))
                .findFirst();
    }

    /**
     * 활성 상태 검증
     */
    private void validateActiveStatus() {
        if (!status.isActive()) {
            throw new IllegalStateException("Customer is not active: " + status);
        }
    }

    // =====================================================
    // Getters
    // =====================================================

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public List<CustomerAddress> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    // =====================================================
    // Builder
    // =====================================================

    public static class Builder {
        private UUID id;
        private String email;
        private String name;
        private String phoneNumber;
        private String profileImageUrl;
        private CustomerStatus status;
        private List<CustomerAddress> addresses = new ArrayList<>();
        private LocalDateTime createdAt;
        private LocalDateTime lastLoginAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder profileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
            return this;
        }

        public Builder status(CustomerStatus status) {
            this.status = status;
            return this;
        }

        public Builder addresses(List<CustomerAddress> addresses) {
            this.addresses = new ArrayList<>(addresses);
            return this;
        }

        public Builder addAddress(CustomerAddress address) {
            this.addresses.add(address);
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder lastLoginAt(LocalDateTime lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }

        public Customer build() {
            validateRequired();
            return new Customer(this);
        }

        private void validateRequired() {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("email is required");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("name is required");
            }
        }
    }
}
