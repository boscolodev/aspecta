# Aspecta - Exception Handling & Logging Library

[![en](https://img.shields.io/badge/lang-en-red.svg)](https://github.com/boscolodev/aspecta/blob/main/README.md)

Aspecta é uma biblioteca Java baseada em Spring Boot que fornece um framework padronizado para:

- Tratamento global de exceções com respostas automáticas.  
- Fábrica de exceções para centralização das falhas.  
- Logger AOP para interceptação e registro de métodos automaticamente.

---

## Funcionalidades

###  Exception Handling
- `@RestControllerAdvice` com `GlobalExceptionHandler`.
- Conversão automática de exceções `ApiErrorException` em respostas JSON padronizadas (`BasicResponse` ou `CompleteResponse`).
- Extração recursiva de `HttpStatus` e `ExceptionType`.

### Exception Factory
- Classe `ApiExceptionFactory` para lançar exceções de forma fluente e centralizada com diferentes sobrecargas.

### Logging Aspect
- Anotação `@LogOn` para ativar logging automático de métodos.
- Configuração via `application.properties`.
- Loga parâmetros de entrada, retorno e exceções.

---

## Instalação

**1. Adicione a dependência:**  
Caso publique no Maven Central ou Nexus:

```xml
<dependency>
    <groupId>br.com.gbs</groupId>
    <artifactId>aspecta</artifactId>
    <version>1.0.0</version>
</dependency>
```

**2. Habilite o `@ComponentScan`** (se não estiver no mesmo pacote).

---

## Configuração

No `application.properties` ou `application.yml`, você pode configurar o comportamento do logger e do tratamento de exceções:

```properties
# Configurações do Logger
logger.enabled=true
logger.project-name=MeuProjeto
logger.enable-i18n=true
logger.sensitive-keys=password,senha,cpf,cnpj,token

# Configurações de Internacionalização (i18n) para mensagens de log
# As mensagens são carregadas de arquivos como messages.properties, messages_en.properties, messages_pt_BR.properties
# Exemplo de messages_pt_BR.properties:
# log.entry.message=Entrando no método {0} com argumentos: {1}
# log.exit.message=Saindo do método {0} com resultado: {1}
# log.error.message=Erro no método {0}: {1} - {2}
```

*   `logger.enabled`: Habilita ou desabilita o logging do aspecto (padrão: `true`).
*   `logger.project-name`: Define o nome do projeto a ser exibido nos logs.
*   `logger.enable-i18n`: Habilita ou desabilita a internacionalização das mensagens de log (padrão: `true`).
*   `logger.sensitive-keys`: Lista de chaves (separadas por vírgula) que, se encontradas nos argumentos dos métodos, terão seus valores mascarados nos logs (padrão: `password,senha,cpf,cnpj,token`).

---

## 📦 Como Usar

### 1. Tratamento de Exceção

O projeto Aspecta oferece um mecanismo robusto para tratamento de exceções, centralizado na `ApiExceptionFactory` e no `GlobalExceptionHandler`.

#### Lançando Exceções com `ApiExceptionFactory.troll()`

Use o método estático `ApiExceptionFactory.troll()` para lançar exceções customizadas. Ele possui sobrecargas para diferentes cenários:

**1. `ApiExceptionFactory.troll(ExceptionType type, String message, Throwable cause)`**

**Uso:** Lança uma `ApiErrorException` com uma mensagem, um tipo de exceção e uma causa (outra exceção). O `HttpStatus` padrão será `INTERNAL_SERVER_ERROR`.

**Exemplo de Código:**

```java
import br.com.gbs.aspecta.exception.ExceptionType;
import br.com.gbs.aspecta.exception.handler.ApiExceptionFactory;

public class ExemploService {
    public void processarDados() {
        try {
            // Simula uma operação que pode falhar
            throw new RuntimeException("Erro inesperado ao processar.");
        } catch (RuntimeException e) {
            // Lança uma ApiErrorException encapsulando o erro original
            throw ApiExceptionFactory.troll(ExceptionType.BASIC, "Falha ao executar a operação.", e);
        }
    }
}
```

**Saída Esperada (Resposta HTTP - Exemplo JSON):**

```json
{
  "timestamp": "2025-06-21T10:00:00.000+00:00",
  "status": 500, 
  "error": "Internal Server Error",
  "message": "Falha ao executar a operação.",
  "path": "/api/exemplo-endpoint"
}
```

**2. `ApiExceptionFactory.troll(ExceptionType type, String message, HttpStatus status)`**

**Uso:** Lança uma `ApiErrorException` com uma mensagem, um tipo de exceção e um `HttpStatus` customizado.

**Exemplo de Código:**

```java
import br.com.gbs.aspecta.exception.ExceptionType;
import br.com.gbs.aspecta.exception.handler.ApiExceptionFactory;
import org.springframework.http.HttpStatus;

public class ExemploController {
    public void validarEntrada(String input) {
        if (input == null || input.isEmpty()) {
            // Lança uma exceção com status BAD_REQUEST
            throw ApiExceptionFactory.troll(ExceptionType.BASIC, "O campo 'input' não pode ser vazio.", HttpStatus.BAD_REQUEST);
        }
        // ... lógica de negócio
    }
}
```

**Saída Esperada (Resposta HTTP - Exemplo JSON):**

```json
{
  "timestamp": "2025-06-21T10:00:00.000+00:00",
  "status": 400, 
  "error": "Bad Request",
  "message": "O campo 'input' não pode ser vazio.",
  "path": "/api/exemplo-endpoint"
}
```

**3. `ApiExceptionFactory.troll(ExceptionType type, String message, HttpStatus status, Throwable cause)`**

**Uso:** Lança uma `ApiErrorException` com uma mensagem, um tipo de exceção, um `HttpStatus` customizado e uma causa.

**Exemplo de Código:**

```java
import br.com.gbs.aspecta.exception.ExceptionType;
import br.com.gbs.aspecta.exception.handler.ApiExceptionFactory;
import org.springframework.http.HttpStatus;

public class ExemploService {
    public void acessarRecursoExterno() {
        try {
            // Simula uma falha de comunicação com um serviço externo
            throw new java.io.IOException("Conexão recusada pelo servidor externo.");
        } catch (java.io.IOException e) {
            // Lança uma exceção com status SERVICE_UNAVAILABLE e a causa original
            throw ApiExceptionFactory.troll(ExceptionType.COMPLETE, "Não foi possível conectar ao serviço de dados.", HttpStatus.SERVICE_UNAVAILABLE, e);
        }
    }
}
```

**Saída Esperada (Resposta HTTP - Exemplo JSON):**

```json
{
  "timestamp": "2025-06-21T10:00:00.000+00:00",
  "status": 503, 
  "error": "Service Unavailable",
  "message": "Não foi possível conectar ao serviço de dados.",
  "path": "/api/exemplo-endpoint"
}
```

#### Respostas Padronizadas do `GlobalExceptionHandler`

O `@RestControllerAdvice` com `GlobalExceptionHandler` intercepta automaticamente as `ApiErrorException` e as converte em respostas JSON padronizadas:

- **BasicResponse**: Retornada para `ExceptionType.BASIC`, contendo `status` e `message`.

```json
{
  "status": "404",
  "message": "Usuário não encontrado"
}
```

- **CompleteResponse**: Retornada para `ExceptionType.COMPLETE`, contendo `status`, `message`, `details`, `path` e `timestamp`.

```json
{
  "status": "500",
  "message": "Erro Interno",
  "details": "NullPointerException",
  "path": "/api/user",
  "timestamp": "2025-05-30T12:34:56"
}
```

### 2. Logging Automático com `@LogOn`

O `LoggerAspect` utiliza a anotação `@LogOn` para ativar o logging automático de métodos, registrando informações de entrada, saída e exceções. O mascaramento de dados sensíveis e a internacionalização são configuráveis.

#### Exemplo de Uso de `@LogOn`

```java
import br.com.gbs.aspecta.logger.anotations.LogOn;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @LogOn(sensitiveData = true) // Argumentos serão logados e dados sensíveis mascarados
    public String criarUsuario(String nome, String email, String senha) {
        // Lógica para criar o usuário
        return "Usuário " + nome + " criado com sucesso!";
    }

    @LogOn(sensitiveData = false) // Argumentos serão logados sem mascaramento
    public String buscarUsuario(Long id) {
        // Lógica para buscar o usuário
        return "Usuário encontrado: " + id;
    }

    @LogOn
    public void metodoQuePodeFalhar() {
        throw new IllegalStateException("Erro simulado no método.");
    }
}
```

#### Exemplos de Saída de Log

As saídas de log são formatadas de acordo com as configurações em `application.properties` e os arquivos de internacionalização.

**Log de Entrada de Método (`criarUsuario` com `sensitiveData = true`):**

```text
[MeuProjeto][UsuarioService] Entrando no método criarUsuario com argumentos: [nome=João, email=j***@example.com, senha=********]
```

**Log de Saída de Método (`criarUsuario` bem-sucedido):**

```text
[MeuProjeto][UsuarioService] Saindo do método criarUsuario com resultado: Usuário João criado com sucesso!
```

**Log de Erro de Método (`metodoQuePodeFalhar`):**

```text
[MeuProjeto][UsuarioService] Erro no método metodoQuePodeFalhar: IllegalStateException - Erro simulado no método.
```

---

## Componentes Internos

### Exception

- `ApiErrorException`: Exceção padrão da biblioteca.
- `ApiExceptionFactory`: Fábrica para lançar exceções (`troll`) de forma centralizada.
- `GlobalExceptionHandler`: Handler global (`@RestControllerAdvice`) para tratamento e formatação das respostas de erro.

### Logger

- `@LogOn`: Anotação para marcar métodos que devem ser logados.
- `LoggerAspect`: Aspecto AOP que intercepta os métodos anotados com `@LogOn`.
- `LoggerProperties`: Classe de configuração que mapeia as propriedades do `application.properties` para o logger.

---

## Exemplo Completo (Controller)

```java
@RestController
@RequestMapping("/api/user")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/{id}")
    @LogOn
    public Usuario buscarUsuario(@PathVariable Long id) {
        return usuarioService.buscarUsuario(id);
    }

    @PostMapping
    @LogOn(sensitiveData = true)
    public String criarUsuario(@RequestBody Usuario usuario) {
        return usuarioService.criarUsuario(usuario.getNome(), usuario.getEmail(), usuario.getSenha());
    }
}
```

---

## Tratamento de Erros (Tipos de Resposta)

| ExceptionType | Resposta            | Campos Incluídos                                  |
|---------------|---------------------|---------------------------------------------------|
| `BASIC`       | `BasicResponse`     | `status`, `message`                               |
| `COMPLETE`    | `CompleteResponse`  | `status`, `message`, `details`, `path`, `timestamp` |

---

## Vantagens

- Centralização no tratamento de erros.  
- Padronização na resposta de API.  
- Redução de código repetitivo.  
- Logging automático, configurável e internacionalizado.
- Mascaramento de dados sensíveis nos logs.

---

## Importante

- O `GlobalExceptionHandler` captura `ApiErrorException` e `MethodArgumentNotValidException`.

- O Logging é **altamente configurável** via `application.properties` ou `application.yml`.

---

## Roadmap

- [x] Suporte para `ProblemDetail` do Spring 6.
- [x] Suporte para logs assíncronos.
- [x] Mensagens de erro internacionalizadas (i18n).
- [x] Remoção de dados sensíveis dos logs.
- [x] Remoção de dados sensíveis parametrizados.
- [ ] Exportar para Maven Central.

---

## Contribuições

Pull Requests são bem-vindos! Para contribuir, por favor, siga as diretrizes de código e envie seus PRs para revisão.

---

## Licença

Este projeto está licenciado sob a [Licença MIT](https://opensource.org/licenses/MIT).

---

## Contato

- Autor: Guilherme Boscolo de Souza
- Email: boscolo.dev@gmail.com
- Linkedin: https://www.linkedin.com/in/guilherme-boscolo/


