package br.com.gbs.aspecta.logger.providers;

import br.com.gbs.aspecta.logger.interfaces.MessageProvider;
import org.springframework.stereotype.Component;

@Component
public class DefaultMessageProvider implements MessageProvider {
    @Override
    public String entryMessage(String method, String args) {
        return String.format("Entrando no método: %s() com | Args: %s", method, args);
    }

    @Override
    public String exitMessage(String method, Object result) {
        return String.format("Saindo do método: %s() retornou | Retorno: %s", method, result);
    }

    @Override
    public String errorMessage(String method, String exceptionName, String message) {
        return String.format("Erro no método %s: %s - %s", method, exceptionName, message);
    }
}
