package com.overwatch.balancer.api.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Error response")
public class ErrorResponseDTO {

    @Schema(description = "Error message")
    private String message;

    @Schema(description = "Error code")
    private String code;

    @Schema(description = "Timestamp")
    private Instant timestamp;

    public static ErrorResponseDTO of(String message) {
        return ErrorResponseDTO.builder()
                .message(message)
                .code("ERROR")
                .timestamp(Instant.now())
                .build();
    }

    public static ErrorResponseDTO of(String message, String code) {
        return ErrorResponseDTO.builder()
                .message(message)
                .code(code)
                .timestamp(Instant.now())
                .build();
    }

}
