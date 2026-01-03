package jjh.delivery.domain.seller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Seller Aggregate Root Unit Tests
 */
@DisplayName("Seller 도메인 테스트")
class SellerTest {

    // Deterministic UUIDs for testing
    private static final UUID CATEGORY_ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID CATEGORY_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000002");

    // =====================================================
    // Test Fixtures
    // =====================================================

    private Seller.Builder createValidSellerBuilder() {
        return Seller.builder()
                .businessName("테스트 상점")
                .businessNumber("123-45-67890")
                .representativeName("홍길동")
                .email("seller@example.com")
                .phoneNumber("010-1234-5678")
                .sellerType(SellerType.INDIVIDUAL);
    }

    // =====================================================
    // 판매자 생성 테스트
    // =====================================================

    @Nested
    @DisplayName("판매자 생성")
    class SellerCreation {

        @Test
        @DisplayName("필수 필드로 판매자 생성 성공")
        void createSellerWithRequiredFields() {
            // given & when
            Seller seller = createValidSellerBuilder().build();

            // then
            assertThat(seller.getId()).isNotNull();
            assertThat(seller.getBusinessName()).isEqualTo("테스트 상점");
            assertThat(seller.getBusinessNumber()).isEqualTo("123-45-67890");
            assertThat(seller.getRepresentativeName()).isEqualTo("홍길동");
            assertThat(seller.getEmail()).isEqualTo("seller@example.com");
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.PENDING);
            assertThat(seller.getSellerType()).isEqualTo(SellerType.INDIVIDUAL);
            assertThat(seller.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("businessName 없이 생성 시 예외 발생")
        void createWithoutBusinessNameThrowsException() {
            assertThatThrownBy(() ->
                    Seller.builder()
                            .businessNumber("123-45-67890")
                            .representativeName("홍길동")
                            .email("seller@example.com")
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("businessName");
        }

        @Test
        @DisplayName("businessNumber 없이 생성 시 예외 발생")
        void createWithoutBusinessNumberThrowsException() {
            assertThatThrownBy(() ->
                    Seller.builder()
                            .businessName("테스트 상점")
                            .representativeName("홍길동")
                            .email("seller@example.com")
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("businessNumber");
        }

        @Test
        @DisplayName("representativeName 없이 생성 시 예외 발생")
        void createWithoutRepresentativeNameThrowsException() {
            assertThatThrownBy(() ->
                    Seller.builder()
                            .businessName("테스트 상점")
                            .businessNumber("123-45-67890")
                            .email("seller@example.com")
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("representativeName");
        }

        @Test
        @DisplayName("email 없이 생성 시 예외 발생")
        void createWithoutEmailThrowsException() {
            assertThatThrownBy(() ->
                    Seller.builder()
                            .businessName("테스트 상점")
                            .businessNumber("123-45-67890")
                            .representativeName("홍길동")
                            .build()
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("email");
        }

        @Test
        @DisplayName("카테고리와 함께 생성")
        void createWithCategories() {
            // given & when
            Seller seller = createValidSellerBuilder()
                    .categoryIds(List.of(CATEGORY_ID_1, CATEGORY_ID_2))
                    .build();

            // then
            assertThat(seller.getCategoryIds()).containsExactly(CATEGORY_ID_1, CATEGORY_ID_2);
        }
    }

    // =====================================================
    // 판매자 승인 테스트
    // =====================================================

    @Nested
    @DisplayName("판매자 승인")
    class SellerApproval {

        @Test
        @DisplayName("PENDING 상태에서 승인 성공")
        void approveFromPending() {
            // given
            Seller seller = createValidSellerBuilder().build();
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.PENDING);

            // when
            seller.approve();

            // then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
            assertThat(seller.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("ACTIVE 상태에서 승인 시 예외 발생")
        void cannotApproveFromActive() {
            // given
            Seller seller = createValidSellerBuilder()
                    .status(SellerStatus.ACTIVE)
                    .build();

            // when & then
            assertThatThrownBy(seller::approve)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only pending sellers");
        }

        @Test
        @DisplayName("SUSPENDED 상태에서 승인 시 예외 발생")
        void cannotApproveFromSuspended() {
            // given
            Seller seller = createValidSellerBuilder()
                    .status(SellerStatus.SUSPENDED)
                    .build();

            // when & then
            assertThatThrownBy(seller::approve)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // =====================================================
    // 판매자 상태 관리 테스트
    // =====================================================

    @Nested
    @DisplayName("판매자 상태 관리")
    class SellerStatusManagement {

        @Test
        @DisplayName("ACTIVE 상태에서 정지 가능")
        void suspendFromActive() {
            // given
            Seller seller = createValidSellerBuilder()
                    .status(SellerStatus.ACTIVE)
                    .build();

            // when
            seller.suspend();

            // then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.SUSPENDED);
        }

        @Test
        @DisplayName("DORMANT 상태에서 정지 가능")
        void suspendFromDormant() {
            // given
            Seller seller = createValidSellerBuilder()
                    .status(SellerStatus.DORMANT)
                    .build();

            // when
            seller.suspend();

            // then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.SUSPENDED);
        }

        @Test
        @DisplayName("PENDING 상태에서 정지 불가")
        void cannotSuspendFromPending() {
            // given
            Seller seller = createValidSellerBuilder().build();

            // when & then
            assertThatThrownBy(seller::suspend)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("SUSPENDED 상태에서 활성화 가능")
        void activateFromSuspended() {
            // given
            Seller seller = createValidSellerBuilder()
                    .status(SellerStatus.SUSPENDED)
                    .build();

            // when
            seller.activate();

            // then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
        }

        @Test
        @DisplayName("DORMANT 상태에서 활성화 가능")
        void activateFromDormant() {
            // given
            Seller seller = createValidSellerBuilder()
                    .status(SellerStatus.DORMANT)
                    .build();

            // when
            seller.activate();

            // then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.ACTIVE);
        }

        @Test
        @DisplayName("PENDING 상태에서 활성화 불가")
        void cannotActivateFromPending() {
            // given
            Seller seller = createValidSellerBuilder().build();

            // when & then
            assertThatThrownBy(seller::activate)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("CLOSED 상태에서 활성화 불가")
        void cannotActivateFromClosed() {
            // given
            Seller seller = createValidSellerBuilder()
                    .status(SellerStatus.CLOSED)
                    .build();

            // when & then
            assertThatThrownBy(seller::activate)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("ACTIVE 상태에서 휴면 전환")
        void makeDormantFromActive() {
            // given
            Seller seller = createValidSellerBuilder()
                    .status(SellerStatus.ACTIVE)
                    .build();

            // when
            seller.makeDormant();

            // then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.DORMANT);
        }

        @Test
        @DisplayName("SUSPENDED 상태에서 휴면 전환 불가")
        void cannotMakeDormantFromSuspended() {
            // given
            Seller seller = createValidSellerBuilder()
                    .status(SellerStatus.SUSPENDED)
                    .build();

            // when & then
            assertThatThrownBy(seller::makeDormant)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only active sellers");
        }

        @Test
        @DisplayName("ACTIVE 상태에서 폐업 가능")
        void closeFromActive() {
            // given
            Seller seller = createValidSellerBuilder()
                    .status(SellerStatus.ACTIVE)
                    .build();

            // when
            seller.close();

            // then
            assertThat(seller.getStatus()).isEqualTo(SellerStatus.CLOSED);
        }

        @Test
        @DisplayName("PENDING 상태에서 폐업 불가")
        void cannotCloseFromPending() {
            // given
            Seller seller = createValidSellerBuilder().build();

            // when & then
            assertThatThrownBy(seller::close)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("이미 CLOSED 상태에서 폐업 불가")
        void cannotCloseFromClosed() {
            // given
            Seller seller = createValidSellerBuilder()
                    .status(SellerStatus.CLOSED)
                    .build();

            // when & then
            assertThatThrownBy(seller::close)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // =====================================================
    // 정보 업데이트 테스트
    // =====================================================

    @Nested
    @DisplayName("정보 업데이트")
    class InfoUpdate {

        @Test
        @DisplayName("판매자 정보 업데이트")
        void updateInfo() {
            // given
            Seller seller = createValidSellerBuilder().build();

            // when
            seller.updateInfo(
                    "새 상점명",
                    "김철수",
                    "newemail@example.com",
                    "010-9999-9999"
            );

            // then
            assertThat(seller.getBusinessName()).isEqualTo("새 상점명");
            assertThat(seller.getRepresentativeName()).isEqualTo("김철수");
            assertThat(seller.getEmail()).isEqualTo("newemail@example.com");
            assertThat(seller.getPhoneNumber()).isEqualTo("010-9999-9999");
        }

        @Test
        @DisplayName("창고 주소 업데이트")
        void updateWarehouseAddress() {
            // given
            Seller seller = createValidSellerBuilder().build();
            WarehouseAddress newAddress = WarehouseAddress.of(
                    "12345",
                    "경기도 화성시 동탄로 100",
                    "물류센터 1동",
                    "담당자",
                    "010-1234-5678"
            );

            // when
            seller.updateWarehouseAddress(newAddress);

            // then
            assertThat(seller.getWarehouseAddress()).isEqualTo(newAddress);
        }
    }

    // =====================================================
    // 카테고리 관리 테스트
    // =====================================================

    @Nested
    @DisplayName("카테고리 관리")
    class CategoryManagement {

        @Test
        @DisplayName("카테고리 추가")
        void addCategory() {
            // given
            Seller seller = createValidSellerBuilder().build();

            // when
            seller.addCategory(CATEGORY_ID_1);
            seller.addCategory(CATEGORY_ID_2);

            // then
            assertThat(seller.getCategoryIds()).containsExactly(CATEGORY_ID_1, CATEGORY_ID_2);
        }

        @Test
        @DisplayName("중복 카테고리 추가 시 무시")
        void addDuplicateCategoryIgnored() {
            // given
            Seller seller = createValidSellerBuilder().build();

            // when
            seller.addCategory(CATEGORY_ID_1);
            seller.addCategory(CATEGORY_ID_1);

            // then
            assertThat(seller.getCategoryIds()).hasSize(1);
        }

        @Test
        @DisplayName("카테고리 제거")
        void removeCategory() {
            // given
            Seller seller = createValidSellerBuilder()
                    .categoryIds(List.of(CATEGORY_ID_1, CATEGORY_ID_2))
                    .build();

            // when
            seller.removeCategory(CATEGORY_ID_1);

            // then
            assertThat(seller.getCategoryIds()).containsExactly(CATEGORY_ID_2);
        }

        @Test
        @DisplayName("카테고리 포함 여부 확인")
        void hasCategory() {
            // given
            Seller seller = createValidSellerBuilder()
                    .categoryIds(List.of(CATEGORY_ID_1))
                    .build();
            UUID nonExistentCategoryId = UUID.fromString("00000000-0000-0000-0000-000000000999");

            // then
            assertThat(seller.hasCategory(CATEGORY_ID_1)).isTrue();
            assertThat(seller.hasCategory(nonExistentCategoryId)).isFalse();
        }
    }

    // =====================================================
    // 조회 메서드 테스트
    // =====================================================

    @Nested
    @DisplayName("조회 메서드")
    class QueryMethods {

        @Test
        @DisplayName("ACTIVE 상태에서 판매 가능")
        void canSellWhenActive() {
            // given
            Seller seller = createValidSellerBuilder()
                    .status(SellerStatus.ACTIVE)
                    .build();

            // then
            assertThat(seller.canSell()).isTrue();
        }

        @Test
        @DisplayName("SUSPENDED 상태에서 판매 불가")
        void cannotSellWhenSuspended() {
            // given
            Seller seller = createValidSellerBuilder()
                    .status(SellerStatus.SUSPENDED)
                    .build();

            // then
            assertThat(seller.canSell()).isFalse();
        }

        @Test
        @DisplayName("PENDING 상태에서 판매 불가")
        void cannotSellWhenPending() {
            // given
            Seller seller = createValidSellerBuilder().build();

            // then
            assertThat(seller.canSell()).isFalse();
        }

        @Test
        @DisplayName("카테고리 목록은 불변")
        void categoryIdsIsImmutable() {
            // given
            Seller seller = createValidSellerBuilder()
                    .categoryIds(List.of(CATEGORY_ID_1))
                    .build();

            // when & then
            assertThatThrownBy(() -> seller.getCategoryIds().add(CATEGORY_ID_2))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
