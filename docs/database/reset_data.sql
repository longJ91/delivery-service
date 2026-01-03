-- =====================================================
-- Delivery Service Data Reset Script
-- =====================================================
-- 이 스크립트는 모든 테이블의 데이터를 삭제하고
-- 더미 데이터를 다시 생성할 때 사용합니다.
--
-- 사용법:
--   PGPASSWORD=secret psql -h localhost -U delivery -d delivery -f reset_data.sql
--
-- 주의: 모든 데이터가 삭제됩니다!
-- =====================================================

-- 외래 키 제약조건 비활성화
SET session_replication_role = replica;

-- =====================================================
-- 1. 상품 관련 데이터 삭제 (역순)
-- =====================================================
TRUNCATE TABLE product_images CASCADE;
TRUNCATE TABLE product_categories CASCADE;
TRUNCATE TABLE product_variants CASCADE;
TRUNCATE TABLE products CASCADE;

-- =====================================================
-- 2. 주문/리뷰/장바구니 관련 데이터 삭제
-- =====================================================
TRUNCATE TABLE review_replies CASCADE;
TRUNCATE TABLE review_images CASCADE;
TRUNCATE TABLE reviews CASCADE;

TRUNCATE TABLE order_items CASCADE;
TRUNCATE TABLE orders CASCADE;

TRUNCATE TABLE cart_items CASCADE;
TRUNCATE TABLE carts CASCADE;

TRUNCATE TABLE return_items CASCADE;
TRUNCATE TABLE returns CASCADE;

TRUNCATE TABLE shipment_trackings CASCADE;
TRUNCATE TABLE shipments CASCADE;

TRUNCATE TABLE payments CASCADE;

-- =====================================================
-- 3. 쿠폰/웹훅 데이터 삭제
-- =====================================================
TRUNCATE TABLE coupons CASCADE;

TRUNCATE TABLE webhook_deliveries CASCADE;
TRUNCATE TABLE webhook_subscribed_events CASCADE;
TRUNCATE TABLE webhook_subscriptions CASCADE;

-- =====================================================
-- 4. 기준 데이터 삭제 (선택적)
-- =====================================================
TRUNCATE TABLE categories CASCADE;
TRUNCATE TABLE seller_categories CASCADE;
TRUNCATE TABLE sellers CASCADE;

TRUNCATE TABLE customer_addresses CASCADE;
TRUNCATE TABLE customers CASCADE;

-- 외래 키 제약조건 재활성화
SET session_replication_role = DEFAULT;

-- 확인
SELECT 'All data has been deleted!' as message;

-- =====================================================
-- 테이블 상태 확인
-- =====================================================
SELECT
    schemaname as schema,
    relname as table_name,
    n_live_tup as row_count
FROM pg_stat_user_tables
WHERE schemaname = 'public'
ORDER BY relname;
