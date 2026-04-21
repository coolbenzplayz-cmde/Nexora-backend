package org.example.nexora.common;

import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

/**
 * Generic pagination response wrapper for list endpoints.
 * Provides pagination metadata along with the data.
 */
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

    // Default constructor
    public PaginationResponse() {
    }

    /**
     * Build from a Spring Data {@link Page} (e.g. repository {@code findAll(Pageable)}).
     */
    public PaginationResponse(Page<T> page) {
        this.data = page.getContent();
        this.currentPage = page.getNumber() + 1;
        this.pageSize = page.getSize();
        this.totalItems = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.hasNext = page.hasNext();
        this.hasPrevious = page.hasPrevious();
    }

    /**
     * Build from a full in-memory list slice using zero-based page index (matches typical {@code @RequestParam} page=0).
     */
    public PaginationResponse(List<T> data, int zeroBasedPage, int pageSize, long totalItems) {
        this.data = data;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = pageSize > 0 ? (int) Math.ceil((double) totalItems / pageSize) : 0;
        this.currentPage = zeroBasedPage + 1;
        this.first = zeroBasedPage == 0;
        this.last = this.totalPages == 0 || this.currentPage >= this.totalPages;
        this.hasNext = this.totalPages > 0 && this.currentPage < this.totalPages;
        this.hasPrevious = zeroBasedPage > 0;
    }

    // Getters and Setters
    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }

    public static <T> PaginationResponse<T> of(List<T> data, int currentPage, int pageSize, long totalItems) {
        PaginationResponse<T> response = new PaginationResponse<>();
        response.setData(data);
        response.setCurrentPage(currentPage);
        response.setPageSize(pageSize);
        response.setTotalItems(totalItems);
        response.setTotalPages(
                pageSize > 0 ? (int) Math.ceil((double) totalItems / pageSize) : 0);
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
