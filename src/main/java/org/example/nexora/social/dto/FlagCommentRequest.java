package org.example.nexora.social.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlagCommentRequest {

    @NotBlank(message = "Reason cannot be blank")
    @Size(min = 1, max = 500, message = "Reason must be between 1 and 500 characters")
    private String reason;
}
