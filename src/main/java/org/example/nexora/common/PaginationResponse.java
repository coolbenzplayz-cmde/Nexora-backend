package org.example.nexora.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.nexora.ride.RideRequest;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

/**
 * Generic pagination response wrapper for list endpoints.
 * Provides pagination metadata along with the data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> data;
    private int currentPage;
    private int pageSize;
    private long totalItems;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;

    public PaginationResponse(Page<RideRequest> rides) {
    }

    public static <T> PaginationResponse<T> of(List<T> data, int currentPage, int pageSize, long totalItems) {
        PaginationResponse<T> response = new PaginationResponse<>();
        response.setData(data);
        response.setCurrentPage(currentPage);
        response.setPageSize(pageSize);
        response.setTotalItems(totalItems);
        response.setTotalPages((int) Math.ceil((double) totalItems / pageSize));
        response.setFirst(currentPage == 1);
        response.setLast(currentPage >= response.getTotalPages());
        response.setHasNext(currentPage < response.getTotalPages());
        response.setHasPrevious(currentPage > 1);
        return response;
    }

    public static <T> PaginationResponse<T> empty(int page, int size) {
        return of(List.of(), page, size, 0);
    }

    public int getStartIndex() {
        return (currentPage - 1) * pageSize;
    }

    public int getEndIndex() {
        return Math.min(currentPage * pageSize, (int) totalItems);
    }

    public long getNextPage() {
        return hasNext ? currentPage + 1 : currentPage;
    }

    public long getPreviousPage() {
        return hasPrevious ? currentPage - 1 : currentPage;
    }
}