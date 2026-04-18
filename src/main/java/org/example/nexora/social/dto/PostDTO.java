package org.example.nexora.social.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Long id;
    private String content;
    private String imageUrl;
    private Long userId;
    private Integer likesCount;
    private Integer commentsCount;
}
