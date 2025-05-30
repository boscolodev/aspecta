package br.com.gbs.aspecta.exception.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompleteResponse {
    private String status;
    private String message;
    private Object details;
    private String path;
    private String timestamp;
}
