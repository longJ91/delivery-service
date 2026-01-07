package jjh.delivery.application.service;

import jjh.delivery.application.port.in.ManageAddressUseCase.AddAddressCommand;
import jjh.delivery.application.port.in.UpdateCustomerProfileUseCase.UpdateProfileCommand;
import jjh.delivery.application.port.out.LoadCustomerPort;
import jjh.delivery.application.port.out.SaveCustomerPort;
import jjh.delivery.domain.customer.Customer;
import jjh.delivery.domain.customer.CustomerAddress;
import jjh.delivery.domain.customer.exception.CustomerNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * CustomerService Unit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService 테스트")
class CustomerServiceTest {

    @Mock
    private LoadCustomerPort loadCustomerPort;

    @Mock
    private SaveCustomerPort saveCustomerPort;

    @InjectMocks
    private CustomerService customerService;

    // =====================================================
    // Test Fixtures
    // =====================================================

    private static final UUID CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID NON_EXISTENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");
    private static final UUID ADDRESS_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private Customer createCustomer() {
        return Customer.builder()
                .id(CUSTOMER_ID)
                .email("test@example.com")
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .build();
    }

    private Customer createCustomerWithAddress() {
        Customer customer = createCustomer();
        customer.addAddress(CustomerAddress.of(
                "집", "홍길동", "010-1234-5678",
                "12345", "서울시 강남구", "상세주소", true
        ));
        return customer;
    }

    private AddAddressCommand createAddAddressCommand() {
        return new AddAddressCommand(
                "회사",
                "홍길동",
                "010-9999-8888",
                "54321",
                "서울시 서초구",
                "회사 상세주소",
                false
        );
    }

    // =====================================================
    // 프로필 업데이트 테스트
    // =====================================================

    @Nested
    @DisplayName("프로필 업데이트")
    class UpdateProfile {

        @Test
        @DisplayName("프로필 업데이트 성공")
        void updateProfileSuccess() {
            // given
            Customer customer = createCustomer();
            UpdateProfileCommand command = new UpdateProfileCommand("김철수", "010-5555-6666", "https://example.com/profile.jpg");

            given(loadCustomerPort.findById(CUSTOMER_ID))
                    .willReturn(Optional.of(customer));
            given(saveCustomerPort.save(any(Customer.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            Customer result = customerService.updateProfile(CUSTOMER_ID, command);

            // then
            assertThat(result.getName()).isEqualTo("김철수");
            assertThat(result.getPhoneNumber()).isEqualTo("010-5555-6666");
            verify(saveCustomerPort).save(any(Customer.class));
        }

        @Test
        @DisplayName("존재하지 않는 고객 프로필 업데이트 시 예외")
        void updateProfileNotFoundThrowsException() {
            // given
            UpdateProfileCommand command = new UpdateProfileCommand("김철수", "010-5555-6666", null);

            given(loadCustomerPort.findById(NON_EXISTENT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> customerService.updateProfile(NON_EXISTENT_ID, command))
                    .isInstanceOf(CustomerNotFoundException.class);
        }
    }

    // =====================================================
    // 배송지 조회 테스트
    // =====================================================

    @Nested
    @DisplayName("배송지 조회")
    class GetAddresses {

        @Test
        @DisplayName("배송지 목록 조회 성공")
        void getAddressesSuccess() {
            // given
            Customer customer = createCustomerWithAddress();

            given(loadCustomerPort.findById(CUSTOMER_ID))
                    .willReturn(Optional.of(customer));

            // when
            List<CustomerAddress> result = customerService.getAddresses(CUSTOMER_ID);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("집");
            assertThat(result.get(0).isDefault()).isTrue();
        }

        @Test
        @DisplayName("빈 배송지 목록 조회")
        void getAddressesEmpty() {
            // given
            Customer customer = createCustomer();

            given(loadCustomerPort.findById(CUSTOMER_ID))
                    .willReturn(Optional.of(customer));

            // when
            List<CustomerAddress> result = customerService.getAddresses(CUSTOMER_ID);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 고객 배송지 조회 시 예외")
        void getAddressesNotFoundThrowsException() {
            // given
            given(loadCustomerPort.findById(NON_EXISTENT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> customerService.getAddresses(NON_EXISTENT_ID))
                    .isInstanceOf(CustomerNotFoundException.class);
        }
    }

    // =====================================================
    // 배송지 추가 테스트
    // =====================================================

    @Nested
    @DisplayName("배송지 추가")
    class AddAddress {

        @Test
        @DisplayName("배송지 추가 성공")
        void addAddressSuccess() {
            // given
            Customer customer = createCustomerWithAddress();
            AddAddressCommand command = createAddAddressCommand();

            given(loadCustomerPort.findById(CUSTOMER_ID))
                    .willReturn(Optional.of(customer));
            given(saveCustomerPort.save(any(Customer.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            CustomerAddress result = customerService.addAddress(CUSTOMER_ID, command);

            // then
            assertThat(result.name()).isEqualTo("회사");
            assertThat(result.recipientName()).isEqualTo("홍길동");
            assertThat(result.address1()).isEqualTo("서울시 서초구");
            verify(saveCustomerPort).save(any(Customer.class));
        }

        @Test
        @DisplayName("첫 번째 배송지는 기본 배송지로 설정")
        void firstAddressBecomesDefault() {
            // given
            Customer customer = createCustomer();
            AddAddressCommand command = new AddAddressCommand(
                    "집", "홍길동", "010-1234-5678",
                    "12345", "서울시 강남구", "상세주소", false
            );

            given(loadCustomerPort.findById(CUSTOMER_ID))
                    .willReturn(Optional.of(customer));
            given(saveCustomerPort.save(any(Customer.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            CustomerAddress result = customerService.addAddress(CUSTOMER_ID, command);

            // then
            assertThat(result.isDefault()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 고객에 배송지 추가 시 예외")
        void addAddressNotFoundThrowsException() {
            // given
            AddAddressCommand command = createAddAddressCommand();

            given(loadCustomerPort.findById(NON_EXISTENT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> customerService.addAddress(NON_EXISTENT_ID, command))
                    .isInstanceOf(CustomerNotFoundException.class);
        }
    }

    // =====================================================
    // 배송지 삭제 테스트
    // =====================================================

    @Nested
    @DisplayName("배송지 삭제")
    class RemoveAddress {

        @Test
        @DisplayName("배송지 삭제 성공")
        void removeAddressSuccess() {
            // given
            Customer customer = createCustomerWithAddress();
            UUID addressId = customer.getAddresses().get(0).id();

            given(loadCustomerPort.findById(CUSTOMER_ID))
                    .willReturn(Optional.of(customer));
            given(saveCustomerPort.save(any(Customer.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            customerService.removeAddress(CUSTOMER_ID, addressId);

            // then
            verify(saveCustomerPort).save(any(Customer.class));
        }

        @Test
        @DisplayName("존재하지 않는 배송지 삭제 시 예외")
        void removeNonExistentAddressThrowsException() {
            // given
            Customer customer = createCustomer();

            given(loadCustomerPort.findById(CUSTOMER_ID))
                    .willReturn(Optional.of(customer));

            // when & then
            assertThatThrownBy(() -> customerService.removeAddress(CUSTOMER_ID, NON_EXISTENT_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Address not found");
        }

        @Test
        @DisplayName("존재하지 않는 고객 배송지 삭제 시 예외")
        void removeAddressCustomerNotFoundThrowsException() {
            // given
            given(loadCustomerPort.findById(NON_EXISTENT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> customerService.removeAddress(NON_EXISTENT_ID, ADDRESS_ID))
                    .isInstanceOf(CustomerNotFoundException.class);
        }
    }

    // =====================================================
    // 기본 배송지 설정 테스트
    // =====================================================

    @Nested
    @DisplayName("기본 배송지 설정")
    class SetDefaultAddress {

        @Test
        @DisplayName("기본 배송지 설정 성공")
        void setDefaultAddressSuccess() {
            // given
            Customer customer = createCustomerWithAddress();
            // 두 번째 주소 추가
            customer.addAddress(CustomerAddress.of(
                    "회사", "홍길동", "010-9999-8888",
                    "54321", "서울시 서초구", "회사 상세", false
            ));
            UUID secondAddressId = customer.getAddresses().get(1).id();

            given(loadCustomerPort.findById(CUSTOMER_ID))
                    .willReturn(Optional.of(customer));
            given(saveCustomerPort.save(any(Customer.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            customerService.setDefaultAddress(CUSTOMER_ID, secondAddressId);

            // then
            verify(saveCustomerPort).save(any(Customer.class));
            // 두 번째 주소가 기본으로 설정되었는지 확인
            assertThat(customer.getAddresses().get(1).isDefault()).isTrue();
            assertThat(customer.getAddresses().get(0).isDefault()).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 배송지를 기본으로 설정 시 예외")
        void setDefaultNonExistentAddressThrowsException() {
            // given
            Customer customer = createCustomer();

            given(loadCustomerPort.findById(CUSTOMER_ID))
                    .willReturn(Optional.of(customer));

            // when & then
            assertThatThrownBy(() -> customerService.setDefaultAddress(CUSTOMER_ID, NON_EXISTENT_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Address not found");
        }

        @Test
        @DisplayName("존재하지 않는 고객 기본 배송지 설정 시 예외")
        void setDefaultAddressCustomerNotFoundThrowsException() {
            // given
            given(loadCustomerPort.findById(NON_EXISTENT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> customerService.setDefaultAddress(NON_EXISTENT_ID, ADDRESS_ID))
                    .isInstanceOf(CustomerNotFoundException.class);
        }
    }
}
