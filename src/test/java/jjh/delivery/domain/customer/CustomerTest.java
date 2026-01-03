package jjh.delivery.domain.customer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Customer Aggregate Root Unit Tests
 */
@DisplayName("Customer 도메인 테스트")
class CustomerTest {

    // =====================================================
    // Test Fixtures
    // =====================================================

    private Customer.Builder createValidCustomerBuilder() {
        return Customer.builder()
                .email("customer@example.com")
                .name("홍길동")
                .phoneNumber("010-1234-5678");
    }

    private CustomerAddress createAddress(String name, boolean isDefault) {
        return CustomerAddress.of(
                name,
                "수령인",
                "010-1234-5678",
                "12345",
                "서울시 강남구",
                "상세주소",
                isDefault
        );
    }

    // =====================================================
    // 고객 생성 테스트
    // =====================================================

    @Nested
    @DisplayName("고객 생성")
    class CustomerCreation {

        @Test
        @DisplayName("필수 필드로 고객 생성 성공")
        void createCustomerWithRequiredFields() {
            // given & when
            Customer customer = createValidCustomerBuilder().build();

            // then
            assertThat(customer.getId()).isNotNull();
            assertThat(customer.getEmail()).isEqualTo("customer@example.com");
            assertThat(customer.getName()).isEqualTo("홍길동");
            assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
            assertThat(customer.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("email 없이 생성 시 예외 발생")
        void createWithoutEmailThrowsException() {
            assertThatThrownBy(() ->
                    Customer.builder()
                            .name("홍길동")
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("email");
        }

        @Test
        @DisplayName("name 없이 생성 시 예외 발생")
        void createWithoutNameThrowsException() {
            assertThatThrownBy(() ->
                    Customer.builder()
                            .email("customer@example.com")
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");
        }
    }

    // =====================================================
    // 프로필 업데이트 테스트
    // =====================================================

    @Nested
    @DisplayName("프로필 업데이트")
    class ProfileUpdate {

        @Test
        @DisplayName("프로필 정보 업데이트")
        void updateProfile() {
            // given
            Customer customer = createValidCustomerBuilder().build();

            // when
            customer.updateProfile("김철수", "010-9999-9999");

            // then
            assertThat(customer.getName()).isEqualTo("김철수");
            assertThat(customer.getPhoneNumber()).isEqualTo("010-9999-9999");
        }

        @Test
        @DisplayName("이메일 변경")
        void changeEmail() {
            // given
            Customer customer = createValidCustomerBuilder().build();

            // when
            customer.changeEmail("newemail@example.com");

            // then
            assertThat(customer.getEmail()).isEqualTo("newemail@example.com");
        }

        @Test
        @DisplayName("빈 이메일로 변경 시 예외")
        void changeEmailWithBlankThrowsException() {
            // given
            Customer customer = createValidCustomerBuilder().build();

            // when & then
            assertThatThrownBy(() -> customer.changeEmail("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email");
        }

        @Test
        @DisplayName("SUSPENDED 상태에서 프로필 업데이트 불가")
        void cannotUpdateProfileWhenSuspended() {
            // given
            Customer customer = createValidCustomerBuilder()
                    .status(CustomerStatus.SUSPENDED)
                    .build();

            // when & then
            assertThatThrownBy(() -> customer.updateProfile("새이름", "010-1111-1111"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not active");
        }
    }

    // =====================================================
    // 배송지 관리 테스트
    // =====================================================

    @Nested
    @DisplayName("배송지 관리")
    class AddressManagement {

        @Test
        @DisplayName("첫 번째 배송지는 자동으로 기본 배송지")
        void firstAddressIsDefault() {
            // given
            Customer customer = createValidCustomerBuilder().build();
            CustomerAddress address = createAddress("집", false);

            // when
            customer.addAddress(address);

            // then
            assertThat(customer.getAddresses()).hasSize(1);
            assertThat(customer.getDefaultAddress()).isPresent();
            assertThat(customer.getDefaultAddress().get().isDefault()).isTrue();
        }

        @Test
        @DisplayName("배송지 추가")
        void addAddress() {
            // given
            Customer customer = createValidCustomerBuilder().build();
            customer.addAddress(createAddress("집", false));

            // when
            customer.addAddress(createAddress("회사", false));

            // then
            assertThat(customer.getAddresses()).hasSize(2);
        }

        @Test
        @DisplayName("새 기본 배송지 추가 시 기존 기본 배송지 해제")
        void newDefaultAddressClearsOldDefault() {
            // given
            Customer customer = createValidCustomerBuilder().build();
            customer.addAddress(createAddress("집", false));
            assertThat(customer.getDefaultAddress().get().name()).isEqualTo("집");

            // when
            customer.addAddress(createAddress("회사", true));

            // then
            assertThat(customer.getDefaultAddress()).isPresent();
            assertThat(customer.getDefaultAddress().get().name()).isEqualTo("회사");
        }

        @Test
        @DisplayName("배송지 삭제")
        void removeAddress() {
            // given
            Customer customer = createValidCustomerBuilder().build();
            customer.addAddress(createAddress("집", false));
            customer.addAddress(createAddress("회사", false));
            UUID addressId = customer.getAddresses().get(1).id();

            // when
            customer.removeAddress(addressId);

            // then
            assertThat(customer.getAddresses()).hasSize(1);
        }

        @Test
        @DisplayName("기본 배송지 삭제 시 다음 배송지가 기본으로 설정")
        void removeDefaultAddressSetsNextAsDefault() {
            // given
            Customer customer = createValidCustomerBuilder().build();
            customer.addAddress(createAddress("집", false));
            customer.addAddress(createAddress("회사", false));
            UUID defaultAddressId = customer.getDefaultAddress().get().id();

            // when
            customer.removeAddress(defaultAddressId);

            // then
            assertThat(customer.getDefaultAddress()).isPresent();
            assertThat(customer.getDefaultAddress().get().name()).isEqualTo("회사");
        }

        @Test
        @DisplayName("기본 배송지 설정")
        void setDefaultAddress() {
            // given
            Customer customer = createValidCustomerBuilder().build();
            customer.addAddress(createAddress("집", false));
            customer.addAddress(createAddress("회사", false));
            UUID secondAddressId = customer.getAddresses().get(1).id();

            // when
            customer.setDefaultAddress(secondAddressId);

            // then
            assertThat(customer.getDefaultAddress().get().id()).isEqualTo(secondAddressId);
        }

        @Test
        @DisplayName("최대 10개 배송지 제한")
        void maxAddressesLimit() {
            // given
            Customer customer = createValidCustomerBuilder().build();
            for (int i = 0; i < 10; i++) {
                customer.addAddress(createAddress("주소" + i, false));
            }

            // when & then
            assertThatThrownBy(() -> customer.addAddress(createAddress("추가주소", false)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Maximum");
        }

        @Test
        @DisplayName("존재하지 않는 배송지 삭제 시 예외")
        void removeNonExistentAddressThrowsException() {
            // given
            Customer customer = createValidCustomerBuilder().build();
            UUID nonExistentId = UUID.fromString("00000000-0000-0000-0000-000000000999");

            // when & then
            assertThatThrownBy(() -> customer.removeAddress(nonExistentId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not found");
        }
    }

    // =====================================================
    // 고객 상태 관리 테스트
    // =====================================================

    @Nested
    @DisplayName("고객 상태 관리")
    class CustomerStatusManagement {

        @Test
        @DisplayName("로그인 기록")
        void recordLogin() {
            // given
            Customer customer = createValidCustomerBuilder().build();
            assertThat(customer.getLastLoginAt()).isNull();

            // when
            customer.recordLogin();

            // then
            assertThat(customer.getLastLoginAt()).isNotNull();
        }

        @Test
        @DisplayName("계정 정지")
        void suspend() {
            // given
            Customer customer = createValidCustomerBuilder().build();

            // when
            customer.suspend();

            // then
            assertThat(customer.getStatus()).isEqualTo(CustomerStatus.SUSPENDED);
        }

        @Test
        @DisplayName("WITHDRAWN 상태에서 정지 불가")
        void cannotSuspendWithdrawn() {
            // given
            Customer customer = createValidCustomerBuilder()
                    .status(CustomerStatus.WITHDRAWN)
                    .build();

            // when & then
            assertThatThrownBy(customer::suspend)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("계정 활성화")
        void activate() {
            // given
            Customer customer = createValidCustomerBuilder()
                    .status(CustomerStatus.SUSPENDED)
                    .build();

            // when
            customer.activate();

            // then
            assertThat(customer.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        }

        @Test
        @DisplayName("WITHDRAWN 상태에서 활성화 불가")
        void cannotActivateWithdrawn() {
            // given
            Customer customer = createValidCustomerBuilder()
                    .status(CustomerStatus.WITHDRAWN)
                    .build();

            // when & then
            assertThatThrownBy(customer::activate)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("휴면 전환")
        void makeDormant() {
            // given
            Customer customer = createValidCustomerBuilder().build();

            // when
            customer.makeDormant();

            // then
            assertThat(customer.getStatus()).isEqualTo(CustomerStatus.DORMANT);
        }

        @Test
        @DisplayName("ACTIVE 외 상태에서 휴면 전환 불가")
        void cannotMakeDormantFromNonActive() {
            // given
            Customer customer = createValidCustomerBuilder()
                    .status(CustomerStatus.SUSPENDED)
                    .build();

            // when & then
            assertThatThrownBy(customer::makeDormant)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only active customers");
        }

        @Test
        @DisplayName("탈퇴")
        void withdraw() {
            // given
            Customer customer = createValidCustomerBuilder().build();

            // when
            customer.withdraw();

            // then
            assertThat(customer.getStatus()).isEqualTo(CustomerStatus.WITHDRAWN);
        }

        @Test
        @DisplayName("이미 탈퇴한 상태에서 탈퇴 불가")
        void cannotWithdrawTwice() {
            // given
            Customer customer = createValidCustomerBuilder()
                    .status(CustomerStatus.WITHDRAWN)
                    .build();

            // when & then
            assertThatThrownBy(customer::withdraw)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("withdrawn");
        }
    }

    // =====================================================
    // 조회 메서드 테스트
    // =====================================================

    @Nested
    @DisplayName("조회 메서드")
    class QueryMethods {

        @Test
        @DisplayName("배송지 조회")
        void findAddress() {
            // given
            Customer customer = createValidCustomerBuilder().build();
            customer.addAddress(createAddress("집", false));
            UUID addressId = customer.getAddresses().get(0).id();
            UUID nonExistentId = UUID.fromString("00000000-0000-0000-0000-000000000999");

            // then
            assertThat(customer.findAddress(addressId)).isPresent();
            assertThat(customer.findAddress(nonExistentId)).isEmpty();
        }

        @Test
        @DisplayName("배송지 목록은 불변")
        void addressesIsImmutable() {
            // given
            Customer customer = createValidCustomerBuilder().build();
            customer.addAddress(createAddress("집", false));

            // when & then
            assertThatThrownBy(() -> customer.getAddresses().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
