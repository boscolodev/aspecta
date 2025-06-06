package br.com.gbs.aspecta;

import br.com.gbs.aspecta.logger.anotations.LogOn;
import org.springframework.stereotype.Component;

@Component
public class UsuarioService {

    @LogOn
    public void a() {}

    @LogOn
    public void b() {}

    @LogOn
    public void c() {}
}
