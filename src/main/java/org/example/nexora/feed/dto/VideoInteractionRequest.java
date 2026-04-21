package org.example.nexora.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoInteractionRequest {

    @NotBlank(message = "Interaction type cannot be blank")
    private String interactionType; // "view", "like", "comment", "share", "complete", "skip"
}
