package br.com.gbs.aspecta.exception.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FieldMessage extends BasicResponse{
    private String fieldName;
    private String message;
    private Object details;

    public FieldMessage(String status, String message) {
        super(status, message);
    }

}
