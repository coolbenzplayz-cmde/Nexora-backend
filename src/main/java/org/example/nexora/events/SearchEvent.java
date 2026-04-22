package org.example.nexora.events;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Search domain event
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchEvent extends DomainEvent {
    
    private Long userId;
    private String query;
    private String action; // SEARCH_PERFORMED, SEARCH_COMPLETED, RESULT_CLICKED
    private String searchType; // USER, CONTENT, PRODUCT, SERVICE
    private int resultCount;
    private String filters;
    
    public SearchEvent() {
        super("SEARCH_EVENT", "SEARCH_SERVICE");
    }
    
    public SearchEvent(Long userId, String query, String action) {
        this();
        this.userId = userId;
        this.query = query;
        this.action = action;
    }
}
