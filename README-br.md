# Aspecta - Exception Handling & Logging Library

[![en](https://img.shields.io/badge/lang-en-red.svg)](https://github.com/boscolodev/aspecta/blob/main/README.md)

Aspecta é uma biblioteca Java baseada em Spring Boot que fornece um framework padronizado para:

✅ Tratamento global de exceções com respostas automáticas.  
✅ Fábrica de exceções para centralização das falhas.  
✅ Logger AOP para interceptação e registro de métodos automaticamente.

---

## ✨ Funcionalidades

### ✅ Exception Handling
- `@RestControllerAdvice` com `GlobalExceptionHandler`.  
- Conversão automática de exceções `ApiErrorException` em respostas JSON padronizadas (`BasicResponse` ou `CompleteResponse`).  
- Extração recursiva de `HttpStatus` e `ExceptionType`.  

### ✅ Exception Factory
- Classe `ApiExceptionFactory` para lançar exceções de forma fluente e centralizada com diferentes sobrecargas.  

### ✅ Logging Aspect
- Anotação `@LogOn` para ativar logging automático de métodos.  
- Configuração via `application.properties`.  
- Loga parâmetros de entrada, retorno e exceções.  

---

## 🛠️ Instalação

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

## ⚙️ Configuração

No `application.properties` ou `application.yml`:

```properties
logger.enabled=true
logger.project-name=MeuProjeto
```

---

## 📦 Como Usar

### ✅ 1. Tratamento de Exceção

Lance exceções usando a `ApiExceptionFactory`:

```java
import static br.com.gbs.aspecta.exception.handler.ApiExceptionFactory.troll;

if (usuario == null) {
    troll(ExceptionType.BASIC, "Usuário não encontrado", HttpStatus.NOT_FOUND);
}
```

A `GlobalExceptionHandler` automaticamente interceptará e retornará:

- **BasicResponse**: Simples, com `status` e `message`.  
- **CompleteResponse**: Detalhada, com `status`, `message`, `details`, `path` e `timestamp`.

**Exemplo de resposta (JSON):**

`ExceptionType.BASIC`:

```json
{
  "status": "404",
  "message": "Usuário não encontrado"
}
```

`ExceptionType.COMPLETE`:

```json
{
  "status": "500",
  "message": "Erro Interno",
  "details": "NullPointerException",
  "path": "/api/user",
  "timestamp": "2025-05-30T12:34:56"
}
```

---

### ✅ 2. Logging Automático

Anote métodos com `@LogOn` para ativar o log AOP:

```java
import br.com.gbs.aspecta.logger.anotations.LogOn;

@Service
public class UsuarioService {

    @LogOn
    public Usuario buscarUsuario(Long id) {
        return repository.findById(id)
                         .orElseThrow(() -> troll(ExceptionType.BASIC, "Usuário não encontrado", HttpStatus.NOT_FOUND));
    }
}
```

**Exemplo de log gerado:**

```text
[MeuProjeto][UsuarioService] Método: buscarUsuario() com | Args: [1]
[MeuProjeto][UsuarioService] Método: buscarUsuario() retornou | Retorno: Usuario{id=1, nome='João'}
```

Em caso de erro:

```text
[MeuProjeto][UsuarioService] Método: buscarUsuario() lançou exceção | Mensagem: Usuário não encontrado
```

---

## ✅ Componentes Internos

### 📁 Exception

- `ApiErrorException`: Exceção padrão.  
- `ApiExceptionFactory`: Fábrica para lançar exceções (`troll`).  
- `GlobalExceptionHandler`: Handler global para tratamento e formatação da resposta.

---

### 📁 Logger

- `@LogOn`: Anotação para marcar métodos que devem ser logados.  
- `LoggerAspect`: Aspecto AOP que intercepta os métodos.  
- `LoggerProperties`: Configuração via `application.properties`.

---

## 📝 Exemplo Completo

```java
@RestController
@RequestMapping("/api/user")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/{id}")
    @LogOn
    public Usuario buscarUsuario(@PathVariable Long id) {
        return usuarioService.buscarUsuario(id);
    }
}
```

---

## 🚨 Tratamento de Erros

| ExceptionType | Resposta            |
|---------------|---------------------|
| BASIC         | status + message    |
| COMPLETE      | status + message + details + path + timestamp |

---

## ✅ Vantagens

✅ Centralização no tratamento de erros.  
✅ Padronização na resposta de API.  
✅ Redução de código repetitivo.  
✅ Logging automático, configurável.

---

## ❗ Importante

- O `GlobalExceptionHandler` captura **somente** `ApiErrorException`.  
- Para capturar outras exceções, crie novos métodos com `@ExceptionHandler`.  
- Logging é **configurável** via `application.properties`.

---

## ✅ Roadmap (sugestão de evolução)

- [ ] Suporte para `ProblemDetail` do Spring 6.  
- [ ] Exportar para Maven Central.  
- [ ] Suporte para logs assíncronos.  
- [ ] Mensagens de erro internacionalizadas (i18n).
- [ ] Remoção de dados sensíveis dos logs
---

## 🤝 Contribuições

Pull Requests são bem-vindos!

---

## 🛡️ Licença

[MIT](LICENSE)

---

## 📞 Contato

- Autor: Guilherme Boscolo de Souza 
- Email: boscolo.dev@gmail.com
- Linkedin: https://www.linkedin.com/in/guilherme-boscolo/
