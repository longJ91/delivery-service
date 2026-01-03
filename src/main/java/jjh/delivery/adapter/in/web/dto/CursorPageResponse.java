package jjh.delivery.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Cursor-based pagination 응답 DTO
 *
 * <p>모든 도메인에서 재사용 가능한 제네릭 커서 페이지 응답입니다.</p>
 *
 * @param <T> 컨텐츠 아이템 타입
 */
public record CursorPageResponse<T>(
        List<T> content,
        int size,
        boolean hasNext,
        String nextCursor
) {
    /**
     * 결과 목록과 커서 정보로 CursorPageResponse 생성
     *
     * @param items         조회된 아이템 목록 (size + 1개 조회)
     * @param size          요청된 페이지 크기
     * @param createdAtExtractor 아이템에서 createdAt 추출 함수
     * @param idExtractor        아이템에서 id 추출 함수
     */
    public static <T> CursorPageResponse<T> of(
            List<T> items,
            int size,
            Function<T, Instant> createdAtExtractor,
            Function<T, UUID> idExtractor
    ) {
        boolean hasNext = items.size() > size;
        List<T> content = hasNext ? items.subList(0, size) : items;

        String nextCursor = null;
        if (hasNext && !content.isEmpty()) {
            T lastItem = content.get(content.size() - 1);
            nextCursor = CursorValue.encode(
                    createdAtExtractor.apply(lastItem),
                    idExtractor.apply(lastItem)
            );
        }

        return new CursorPageResponse<>(content, size, hasNext, nextCursor);
    }

    /**
     * 컨텐츠를 변환하여 새로운 CursorPageResponse 생성
     */
    public <R> CursorPageResponse<R> map(Function<T, R> mapper) {
        return new CursorPageResponse<>(
                content.stream().map(mapper).toList(),
                size,
                hasNext,
                nextCursor
        );
    }
}
