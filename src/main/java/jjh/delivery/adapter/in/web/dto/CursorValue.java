package jjh.delivery.adapter.in.web.dto;

import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Cursor-based pagination을 위한 커서 값 유틸리티
 *
 * <p>createdAt과 identifier를 조합하여 고유한 커서를 생성하고,
 * tie-breaking을 통해 동일 시간대 데이터도 정확히 구분합니다.</p>
 *
 * <p>identifier는 정렬에 사용되는 보조 키로, 다음과 같이 사용됩니다:</p>
 * <ul>
 *   <li>JPA/jOOQ: UUID.toString() (데이터베이스 id)</li>
 *   <li>Elasticsearch: orderNumber (정렬 가능한 Keyword 필드)</li>
 * </ul>
 */
public record CursorValue(
        Instant createdAt,
        String identifier
) {
    private static final String DELIMITER = "_";

    /**
     * createdAt과 identifier를 Base64로 인코딩하여 커서 문자열 생성
     */
    public static String encode(Instant createdAt, String identifier) {
        if (createdAt == null || identifier == null) {
            return null;
        }
        String raw = createdAt.toEpochMilli() + DELIMITER + identifier;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes());
    }

    /**
     * 커서 문자열을 디코딩하여 CursorValue 객체 반환
     */
    public static CursorValue decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor));
            String[] parts = raw.split(DELIMITER, 2);
            if (parts.length != 2) {
                return null;
            }
            return new CursorValue(
                    Instant.ofEpochMilli(Long.parseLong(parts[0])),
                    parts[1]
            );
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 현재 커서 값을 인코딩된 문자열로 반환
     */
    public String encode() {
        return encode(this.createdAt, this.identifier);
    }

    /**
     * identifier를 UUID로 파싱 (JPA/jOOQ adapter용)
     *
     * @return identifier를 UUID로 변환한 값
     * @throws IllegalArgumentException identifier가 유효한 UUID 형식이 아닌 경우
     */
    public UUID id() {
        return UUID.fromString(identifier);
    }

    /**
     * identifier를 String으로 반환 (Elasticsearch adapter용)
     *
     * @return identifier 문자열 (orderNumber 등)
     */
    public String orderNumber() {
        return identifier;
    }
}
