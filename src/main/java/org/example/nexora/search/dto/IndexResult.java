package org.example.nexora.search.dto;

import lombok.Data;

/**
 * Index result for search indexing
 */
@Data
public class IndexResult {
    
    private boolean success;
    private String contentId;
    private String message;
    private String indexName;
    private long indexTime;
    
    public IndexResult() {
        this.success = false;
        this.indexTime = 0;
    }
    
    public static IndexResult success(String contentId, String indexName) {
        IndexResult result = new IndexResult();
        result.setSuccess(true);
        result.setContentId(contentId);
        result.setIndexName(indexName);
        result.setMessage("Content indexed successfully");
        return result;
    }
    
    public static IndexResult failure(String contentId, String message) {
        IndexResult result = new IndexResult();
        result.setSuccess(false);
        result.setContentId(contentId);
        result.setMessage(message);
        return result;
    }
}
