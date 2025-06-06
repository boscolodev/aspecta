package br.com.gbs.aspecta;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioFiltro {


    @NotBlank(message = "O nome é obrigatório!")
    private String nome;

    @NotBlank(message = "O email é obrigatório!")
    private String email;
}
