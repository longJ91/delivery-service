-- =====================================================
-- Delivery Service Dummy Data Generation Script
-- =====================================================
-- 이 스크립트는 테스트/개발 환경에서 사용할 더미 데이터를 생성합니다.
--
-- 사용법:
--   PGPASSWORD=secret psql -h localhost -U delivery -d delivery -f dummy_data.sql
--
-- 생성 데이터:
--   - Sellers: 20개
--   - Categories: 30개 (3-depth 계층 구조)
--   - Products: 10,000개
--   - Product Variants: ~25,000개 (상품당 평균 2.5개)
--   - Product Images: ~30,000개 (상품당 평균 3개)
--   - Product Categories: ~15,000개 (상품당 평균 1.5개)
-- =====================================================

-- 기존 더미 데이터 삭제 (선택적)
-- TRUNCATE product_images, product_categories, product_variants, products, categories, sellers CASCADE;

-- =====================================================
-- 1. Sellers (판매자) - 20개
-- =====================================================
-- seller_type: INDIVIDUAL, CORPORATION, OVERSEAS
-- status: PENDING, ACTIVE, DORMANT, SUSPENDED, CLOSED
INSERT INTO sellers (id, business_name, business_number, representative_name, email, phone_number, seller_type, status, warehouse_postal_code, warehouse_address1, warehouse_address2, created_at, updated_at, approved_at, version)
VALUES
    ('a0000000-0000-0000-0000-000000000001', '패션플러스', '123-45-67890', '김패션', 'fashion@example.com', '02-1234-5678', 'CORPORATION', 'ACTIVE', '06234', '서울시 강남구 테헤란로 123', '패션빌딩 5층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000002', '테크월드', '234-56-78901', '이테크', 'tech@example.com', '02-2345-6789', 'CORPORATION', 'ACTIVE', '04530', '서울시 중구 세종대로 110', '테크타워 10층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000003', '홈앤리빙', '345-67-89012', '박가구', 'home@example.com', '02-3456-7890', 'CORPORATION', 'ACTIVE', '07281', '서울시 영등포구 여의대로 24', '리빙센터 3층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000004', '뷰티샵', '456-78-90123', '최뷰티', 'beauty@example.com', '02-4567-8901', 'CORPORATION', 'ACTIVE', '06164', '서울시 강남구 논현로 508', '뷰티프라자 2층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000005', '스포츠킹', '567-89-01234', '정스포', 'sports@example.com', '02-5678-9012', 'CORPORATION', 'ACTIVE', '05510', '서울시 송파구 올림픽로 300', '스포츠몰 B1', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000006', '푸드마켓', '678-90-12345', '강푸드', 'food@example.com', '02-6789-0123', 'CORPORATION', 'ACTIVE', '04104', '서울시 마포구 월드컵북로 396', '푸드타운 1층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000007', '키즈랜드', '789-01-23456', '윤키즈', 'kids@example.com', '02-7890-1234', 'CORPORATION', 'ACTIVE', '03925', '서울시 마포구 마포대로 45', '키즈센터 4층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000008', '펫프렌즈', '890-12-34567', '임펫', 'pet@example.com', '02-8901-2345', 'CORPORATION', 'ACTIVE', '06611', '서울시 서초구 서초대로 397', '펫타워 6층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000009', '북스토어', '901-23-45678', '한책', 'books@example.com', '02-9012-3456', 'CORPORATION', 'ACTIVE', '04513', '서울시 중구 청계천로 100', '북센터 2층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000010', '가전마트', '012-34-56789', '조가전', 'appliance@example.com', '02-0123-4567', 'CORPORATION', 'ACTIVE', '07335', '서울시 영등포구 경인로 846', '가전월드 1층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000011', '오가닉팜', '111-22-33333', '김유기', 'organic@example.com', '031-1111-2222', 'INDIVIDUAL', 'ACTIVE', '13529', '경기도 성남시 분당구 판교역로 235', '팜빌딩 3층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000012', '디자인하우스', '222-33-44444', '이디자', 'design@example.com', '02-2222-3333', 'CORPORATION', 'ACTIVE', '06035', '서울시 강남구 학동로 171', '디자인센터 7층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000013', '헬스케어', '333-44-55555', '박건강', 'health@example.com', '02-3333-4444', 'CORPORATION', 'ACTIVE', '06132', '서울시 강남구 봉은사로 524', '헬스타워 5층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000014', '아웃도어존', '444-55-66666', '최캠핑', 'outdoor@example.com', '02-4444-5555', 'CORPORATION', 'ACTIVE', '04778', '서울시 성동구 성수일로 77', '아웃도어몰 2층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000015', '주얼리박스', '555-66-77777', '정보석', 'jewelry@example.com', '02-5555-6666', 'INDIVIDUAL', 'ACTIVE', '04538', '서울시 중구 명동길 74', '주얼리프라자 8층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000016', '악기나라', '666-77-88888', '강음악', 'music@example.com', '02-6666-7777', 'CORPORATION', 'ACTIVE', '04004', '서울시 마포구 양화로 45', '뮤직센터 3층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000017', '자동차용품', '777-88-99999', '윤카', 'auto@example.com', '02-7777-8888', 'CORPORATION', 'ACTIVE', '08503', '서울시 금천구 가산디지털1로 145', '오토몰 1층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000018', '문구천국', '888-99-00000', '임문구', 'stationery@example.com', '02-8888-9999', 'INDIVIDUAL', 'ACTIVE', '04527', '서울시 중구 삼일대로 363', '문구센터 4층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000019', '게임월드', '999-00-11111', '한게임', 'game@example.com', '02-9999-0000', 'CORPORATION', 'ACTIVE', '08380', '서울시 구로구 디지털로 288', '게임타워 12층', NOW(), NOW(), NOW(), 0),
    ('a0000000-0000-0000-0000-000000000020', '공구마트', '000-11-22222', '조공구', 'tools@example.com', '02-0000-1111', 'CORPORATION', 'ACTIVE', '07547', '서울시 강서구 공항대로 247', '공구센터 B1', NOW(), NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- 2. Categories (카테고리) - 30개 (3-depth 계층 구조)
-- =====================================================
-- Level 1: 대분류 (6개)
INSERT INTO categories (id, parent_id, name, description, image_url, display_order, depth, is_active, created_at, updated_at)
VALUES
    ('c0000000-0000-0000-0001-000000000000', NULL, '패션의류', '남녀 의류 및 패션 아이템', 'https://example.com/categories/fashion.jpg', 1, 0, true, NOW(), NOW()),
    ('c0000000-0000-0000-0002-000000000000', NULL, '전자기기', '스마트폰, 컴퓨터, 가전제품', 'https://example.com/categories/electronics.jpg', 2, 0, true, NOW(), NOW()),
    ('c0000000-0000-0000-0003-000000000000', NULL, '가구/인테리어', '가구, 홈데코, 수납용품', 'https://example.com/categories/furniture.jpg', 3, 0, true, NOW(), NOW()),
    ('c0000000-0000-0000-0004-000000000000', NULL, '뷰티', '화장품, 향수, 스킨케어', 'https://example.com/categories/beauty.jpg', 4, 0, true, NOW(), NOW()),
    ('c0000000-0000-0000-0005-000000000000', NULL, '스포츠/레저', '운동용품, 캠핑, 여행', 'https://example.com/categories/sports.jpg', 5, 0, true, NOW(), NOW()),
    ('c0000000-0000-0000-0006-000000000000', NULL, '식품', '신선식품, 가공식품, 건강식품', 'https://example.com/categories/food.jpg', 6, 0, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Level 2: 중분류 (12개)
INSERT INTO categories (id, parent_id, name, description, image_url, display_order, depth, is_active, created_at, updated_at)
VALUES
    -- 패션의류 하위
    ('c0000000-0000-0000-0001-000000000001', 'c0000000-0000-0000-0001-000000000000', '남성의류', '남성 상의, 하의, 아우터', NULL, 1, 1, true, NOW(), NOW()),
    ('c0000000-0000-0000-0001-000000000002', 'c0000000-0000-0000-0001-000000000000', '여성의류', '여성 상의, 하의, 원피스', NULL, 2, 1, true, NOW(), NOW()),
    -- 전자기기 하위
    ('c0000000-0000-0000-0002-000000000001', 'c0000000-0000-0000-0002-000000000000', '스마트폰', '스마트폰 및 액세서리', NULL, 1, 1, true, NOW(), NOW()),
    ('c0000000-0000-0000-0002-000000000002', 'c0000000-0000-0000-0002-000000000000', '컴퓨터', '노트북, 데스크탑, 주변기기', NULL, 2, 1, true, NOW(), NOW()),
    -- 가구/인테리어 하위
    ('c0000000-0000-0000-0003-000000000001', 'c0000000-0000-0000-0003-000000000000', '거실가구', '소파, TV장, 거실장', NULL, 1, 1, true, NOW(), NOW()),
    ('c0000000-0000-0000-0003-000000000002', 'c0000000-0000-0000-0003-000000000000', '침실가구', '침대, 옷장, 화장대', NULL, 2, 1, true, NOW(), NOW()),
    -- 뷰티 하위
    ('c0000000-0000-0000-0004-000000000001', 'c0000000-0000-0000-0004-000000000000', '스킨케어', '스킨, 로션, 크림, 에센스', NULL, 1, 1, true, NOW(), NOW()),
    ('c0000000-0000-0000-0004-000000000002', 'c0000000-0000-0000-0004-000000000000', '메이크업', '파운데이션, 립스틱, 아이섀도', NULL, 2, 1, true, NOW(), NOW()),
    -- 스포츠/레저 하위
    ('c0000000-0000-0000-0005-000000000001', 'c0000000-0000-0000-0005-000000000000', '헬스/요가', '운동기구, 요가매트, 홈트레이닝', NULL, 1, 1, true, NOW(), NOW()),
    ('c0000000-0000-0000-0005-000000000002', 'c0000000-0000-0000-0005-000000000000', '캠핑/아웃도어', '텐트, 침낭, 캠핑용품', NULL, 2, 1, true, NOW(), NOW()),
    -- 식품 하위
    ('c0000000-0000-0000-0006-000000000001', 'c0000000-0000-0000-0006-000000000000', '신선식품', '과일, 채소, 육류, 해산물', NULL, 1, 1, true, NOW(), NOW()),
    ('c0000000-0000-0000-0006-000000000002', 'c0000000-0000-0000-0006-000000000000', '가공식품', '라면, 통조림, 냉동식품', NULL, 2, 1, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Level 3: 소분류 (12개)
INSERT INTO categories (id, parent_id, name, description, image_url, display_order, depth, is_active, created_at, updated_at)
VALUES
    -- 남성의류 하위
    ('c0000000-0000-0000-0001-000000000011', 'c0000000-0000-0000-0001-000000000001', '남성 상의', '티셔츠, 셔츠, 니트', NULL, 1, 2, true, NOW(), NOW()),
    ('c0000000-0000-0000-0001-000000000012', 'c0000000-0000-0000-0001-000000000001', '남성 하의', '청바지, 슬랙스, 반바지', NULL, 2, 2, true, NOW(), NOW()),
    -- 여성의류 하위
    ('c0000000-0000-0000-0001-000000000021', 'c0000000-0000-0000-0001-000000000002', '여성 상의', '블라우스, 니트, 카디건', NULL, 1, 2, true, NOW(), NOW()),
    ('c0000000-0000-0000-0001-000000000022', 'c0000000-0000-0000-0001-000000000002', '여성 하의', '스커트, 청바지, 레깅스', NULL, 2, 2, true, NOW(), NOW()),
    -- 스마트폰 하위
    ('c0000000-0000-0000-0002-000000000011', 'c0000000-0000-0000-0002-000000000001', '스마트폰 본체', '아이폰, 갤럭시, 기타 스마트폰', NULL, 1, 2, true, NOW(), NOW()),
    ('c0000000-0000-0000-0002-000000000012', 'c0000000-0000-0000-0002-000000000001', '스마트폰 케이스', '케이스, 필름, 거치대', NULL, 2, 2, true, NOW(), NOW()),
    -- 컴퓨터 하위
    ('c0000000-0000-0000-0002-000000000021', 'c0000000-0000-0000-0002-000000000002', '노트북', '게이밍, 사무용, 울트라북', NULL, 1, 2, true, NOW(), NOW()),
    ('c0000000-0000-0000-0002-000000000022', 'c0000000-0000-0000-0002-000000000002', '모니터', 'LED, 게이밍, 곡면 모니터', NULL, 2, 2, true, NOW(), NOW()),
    -- 거실가구 하위
    ('c0000000-0000-0000-0003-000000000011', 'c0000000-0000-0000-0003-000000000001', '소파', '3인용, 4인용, 리클라이너', NULL, 1, 2, true, NOW(), NOW()),
    ('c0000000-0000-0000-0003-000000000012', 'c0000000-0000-0000-0003-000000000001', '테이블', '거실 테이블, 사이드 테이블', NULL, 2, 2, true, NOW(), NOW()),
    -- 스킨케어 하위
    ('c0000000-0000-0000-0004-000000000011', 'c0000000-0000-0000-0004-000000000001', '에센스/세럼', '앰플, 에센스, 세럼', NULL, 1, 2, true, NOW(), NOW()),
    ('c0000000-0000-0000-0004-000000000012', 'c0000000-0000-0000-0004-000000000001', '크림/로션', '수분크림, 영양크림, 로션', NULL, 2, 2, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- 3. Products (상품) - 10,000개 생성
-- =====================================================
-- PostgreSQL 함수를 사용한 대량 데이터 생성

DO $$
DECLARE
    v_product_id UUID;
    v_seller_id UUID;
    v_category_id UUID;
    v_variant_id UUID;
    v_product_name TEXT;
    v_base_price DECIMAL(12,2);
    v_stock INT;
    v_status TEXT;
    i INT;
    j INT;
    k INT;

    -- 상품명 배열
    product_prefixes TEXT[] := ARRAY['프리미엄', '베이직', '클래식', '모던', '빈티지', '럭셔리', '에코', '스마트', '울트라', '프로'];
    product_names TEXT[] := ARRAY[
        '티셔츠', '청바지', '원피스', '자켓', '코트', '니트', '셔츠', '블라우스', '스커트', '레깅스',
        '스마트폰', '노트북', '태블릿', '이어폰', '스피커', '키보드', '마우스', '모니터', '충전기', '케이블',
        '소파', '침대', '책상', '의자', '테이블', '수납장', '행거', '조명', '커튼', '러그',
        '스킨토너', '로션', '크림', '에센스', '마스크팩', '클렌저', '선크림', '립스틱', '파운데이션', '아이섀도',
        '러닝화', '요가매트', '덤벨', '자전거', '텐트', '침낭', '배낭', '등산화', '폴대', '랜턴',
        '쌀', '라면', '과자', '음료', '커피', '차', '소스', '조미료', '통조림', '냉동식품'
    ];
    product_suffixes TEXT[] := ARRAY['세트', '단품', '기획전', '한정판', '스페셜', '에디션', '컬렉션', '시리즈', '베스트', '인기'];

    -- 판매자 ID 배열
    seller_ids UUID[] := ARRAY[
        'a0000000-0000-0000-0000-000000000001'::UUID, 'a0000000-0000-0000-0000-000000000002'::UUID,
        'a0000000-0000-0000-0000-000000000003'::UUID, 'a0000000-0000-0000-0000-000000000004'::UUID,
        'a0000000-0000-0000-0000-000000000005'::UUID, 'a0000000-0000-0000-0000-000000000006'::UUID,
        'a0000000-0000-0000-0000-000000000007'::UUID, 'a0000000-0000-0000-0000-000000000008'::UUID,
        'a0000000-0000-0000-0000-000000000009'::UUID, 'a0000000-0000-0000-0000-000000000010'::UUID,
        'a0000000-0000-0000-0000-000000000011'::UUID, 'a0000000-0000-0000-0000-000000000012'::UUID,
        'a0000000-0000-0000-0000-000000000013'::UUID, 'a0000000-0000-0000-0000-000000000014'::UUID,
        'a0000000-0000-0000-0000-000000000015'::UUID, 'a0000000-0000-0000-0000-000000000016'::UUID,
        'a0000000-0000-0000-0000-000000000017'::UUID, 'a0000000-0000-0000-0000-000000000018'::UUID,
        'a0000000-0000-0000-0000-000000000019'::UUID, 'a0000000-0000-0000-0000-000000000020'::UUID
    ];

    -- 카테고리 ID 배열 (소분류 위주)
    category_ids UUID[] := ARRAY[
        'c0000000-0000-0000-0001-000000000011'::UUID, 'c0000000-0000-0000-0001-000000000012'::UUID,
        'c0000000-0000-0000-0001-000000000021'::UUID, 'c0000000-0000-0000-0001-000000000022'::UUID,
        'c0000000-0000-0000-0002-000000000011'::UUID, 'c0000000-0000-0000-0002-000000000012'::UUID,
        'c0000000-0000-0000-0002-000000000021'::UUID, 'c0000000-0000-0000-0002-000000000022'::UUID,
        'c0000000-0000-0000-0003-000000000011'::UUID, 'c0000000-0000-0000-0003-000000000012'::UUID,
        'c0000000-0000-0000-0004-000000000011'::UUID, 'c0000000-0000-0000-0004-000000000012'::UUID,
        'c0000000-0000-0000-0005-000000000001'::UUID, 'c0000000-0000-0000-0005-000000000002'::UUID,
        'c0000000-0000-0000-0006-000000000001'::UUID, 'c0000000-0000-0000-0006-000000000002'::UUID
    ];

    -- 상태 배열
    statuses TEXT[] := ARRAY['ACTIVE', 'ACTIVE', 'ACTIVE', 'ACTIVE', 'ACTIVE', 'ACTIVE', 'ACTIVE', 'ACTIVE', 'ACTIVE', 'INACTIVE'];

    -- 색상 옵션
    colors TEXT[] := ARRAY['블랙', '화이트', '네이비', '그레이', '베이지', '레드', '블루', '그린', '브라운', '핑크'];

    -- 사이즈 옵션
    sizes TEXT[] := ARRAY['XS', 'S', 'M', 'L', 'XL', 'XXL', 'FREE'];

BEGIN
    RAISE NOTICE 'Starting product data generation...';

    FOR i IN 1..10000 LOOP
        -- 고유 상품 ID 생성
        v_product_id := gen_random_uuid();

        -- 랜덤 판매자 선택
        v_seller_id := seller_ids[1 + floor(random() * 20)::int];

        -- 랜덤 상품명 생성
        v_product_name := product_prefixes[1 + floor(random() * 10)::int] || ' ' ||
                          product_names[1 + floor(random() * 60)::int] || ' ' ||
                          product_suffixes[1 + floor(random() * 10)::int] || ' ' ||
                          i::text;

        -- 랜덤 가격 (10,000 ~ 500,000원)
        v_base_price := (10000 + floor(random() * 490000))::decimal(12,2);

        -- 랜덤 재고 (10 ~ 1000)
        v_stock := 10 + floor(random() * 990)::int;

        -- 랜덤 상태 (90% ACTIVE, 10% INACTIVE)
        v_status := statuses[1 + floor(random() * 10)::int];

        -- 상품 삽입
        INSERT INTO products (id, seller_id, name, description, base_price, status, total_stock_quantity, created_at, updated_at, version)
        VALUES (
            v_product_id,
            v_seller_id,
            v_product_name,
            v_product_name || '의 상세 설명입니다. 고품질 소재와 섬세한 마감으로 제작되었습니다. ' ||
            '편안한 착용감과 세련된 디자인이 특징이며, 다양한 스타일에 매치하기 좋습니다. ' ||
            '사이즈 가이드를 참고하여 선택해 주세요.',
            v_base_price,
            v_status,
            v_stock,
            NOW() - (random() * interval '365 days'),
            NOW(),
            0
        );

        -- 상품 변형 (Variants) 생성 (1~5개)
        FOR j IN 1..(1 + floor(random() * 4)::int) LOOP
            v_variant_id := gen_random_uuid();

            INSERT INTO product_variants (id, product_id, name, sku, option_values, additional_price, stock_quantity, is_active)
            VALUES (
                v_variant_id,
                v_product_id,
                colors[1 + floor(random() * 10)::int] || '/' || sizes[1 + floor(random() * 7)::int],
                'SKU-' || substr(md5(random()::text), 1, 8),
                jsonb_build_object(
                    '색상', colors[1 + floor(random() * 10)::int],
                    '사이즈', sizes[1 + floor(random() * 7)::int]
                ),
                floor(random() * 10000)::decimal(10,2),
                10 + floor(random() * 200)::int,
                random() > 0.1
            );
        END LOOP;

        -- 상품 이미지 생성 (1~5개)
        FOR k IN 1..(1 + floor(random() * 4)::int) LOOP
            INSERT INTO product_images (product_id, image_url, display_order)
            VALUES (
                v_product_id,
                'https://picsum.photos/seed/' || v_product_id || '-' || k || '/800/800',
                k - 1
            )
            ON CONFLICT (product_id, display_order) DO NOTHING;
        END LOOP;

        -- 상품 카테고리 매핑 (1~2개)
        FOR k IN 1..(1 + floor(random())::int) LOOP
            v_category_id := category_ids[1 + floor(random() * 16)::int];

            INSERT INTO product_categories (product_id, category_id)
            SELECT v_product_id, v_category_id
            WHERE NOT EXISTS (
                SELECT 1 FROM product_categories
                WHERE product_id = v_product_id AND category_id = v_category_id
            );
        END LOOP;

        -- 진행률 표시 (1000개마다)
        IF i % 1000 = 0 THEN
            RAISE NOTICE 'Generated % products...', i;
        END IF;
    END LOOP;

    RAISE NOTICE 'Product data generation completed!';
END $$;

-- =====================================================
-- 4. 통계 확인
-- =====================================================
SELECT 'sellers' as table_name, count(*) as count FROM sellers
UNION ALL
SELECT 'categories', count(*) FROM categories
UNION ALL
SELECT 'products', count(*) FROM products
UNION ALL
SELECT 'product_variants', count(*) FROM product_variants
UNION ALL
SELECT 'product_images', count(*) FROM product_images
UNION ALL
SELECT 'product_categories', count(*) FROM product_categories
ORDER BY table_name;
