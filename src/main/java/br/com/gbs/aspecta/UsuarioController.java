package br.com.gbs.aspecta;

import br.com.gbs.aspecta.logger.anotations.LogOn;
import br.com.gbs.aspecta.logger.i18n.I18nService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UsuarioController {

    private final I18nService i18nService;

    @LogOn
    @GetMapping()
    public String filtrarUsuarios(@Valid @ModelAttribute UsuarioFiltro filtro) {
        // Apenas para simular validação
        if (filtro.getEmail() == null || filtro.getEmail().isBlank()) {
            throw new IllegalArgumentException(i18nService.getMessage("usuario.email.obrigatorio"));
        }

        return "Usuário filtrado com sucesso!";
    }
}
