package br.com.gbs.aspecta.exception.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FieldMessage extends BasicResponse {
    private Object details;

    public FieldMessage(int status, String message) {
        super(status, null, message);
    }
}
