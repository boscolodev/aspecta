package br.com.gbs.aspecta;

import br.com.gbs.aspecta.exception.ExceptionType;
import br.com.gbs.aspecta.exception.handler.ApiExceptionFactory;
import br.com.gbs.aspecta.logger.anotations.LogOn;
import br.com.gbs.aspecta.logger.interfaces.I18nLogger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UsuarioController {

    private final I18nLogger i18NLoggerService;
    private final UsuarioService service;

    @LogOn
    @GetMapping()
    public String filtrarUsuarios(@Valid @ModelAttribute UsuarioFiltro filtro) throws InterruptedException {
        service.a();
        Thread.sleep(100);
        service.b();
        Thread.sleep(200);
        service.c();
        Thread.sleep(100);
        service.a();
        return "Usuário filtrado com sucesso!";
    }

    @LogOn
    public void a() {
    }

    @LogOn
    public void b() {

    }

    @LogOn
    public void c() {

    }

}
