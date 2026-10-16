# Arquitetura — safeway-core-api

Este documento registra as decisões arquiteturais do `safeway-core-api` e o porquê de cada uma. O objetivo é que qualquer pessoa do time saiba, sem precisar perguntar, como estruturar uma feature nova e por que o projeto tem os padrões que tem — inclusive os que parecem inconsistentes à primeira vista.
 
---

## 1. Contexto histórico

O `safeway-core-api` nasceu como monolito único (login, alunos, rotas, financeiro). Com a exigência de microsserviço, o módulo financeiro foi extraído como serviço separado, comunicando-se com o core via **Feign** (síncrono) e **RabbitMQ** (assíncrono, para propagar criação/atualização de aluno → mensalidades).

---

## 4. Padrões de acesso a dados

### 4.1. `ChamadaSpecs` (Specification pattern) — mantido

**Por quê**: a tela de chamada precisa suportar múltiplos filtros combináveis e simultâneos. Specification é o padrão correto para esse caso — evita explosão de métodos de repository por combinação de filtro ou lógica condicional acumulada no service.

**Regra para o time**: qualquer outro domínio que precise de filtros dinâmicos e combináveis (ex: busca de rota em andamento) deve reaproveitar Specification, não criar uma segunda abordagem para o mesmo tipo de problema.

## 5. Consolidação de exceptions

**Decisão**: substituir as exceptions `*NotFoundException` específicas por domínio (`AlunoNotFoundException`, `EscolaNotFoundException`, `EventoNotFoundException`, etc.) por uma única `EntityNotFoundException` genérica.

**Por quê**: 9+ classes que fazem exatamente a mesma coisa (guardam um id, retornam 404) é peso de manutenção sem benefício — nem o tratamento de erro melhora, já que o `GlobalExceptionHandler` passaria a tratar um tipo em vez de nove.

```java
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(Class<?> entityType, Object id) {
        super("%s não encontrado com id: %s".formatted(entityType.getSimpleName(), id));
    }
}
```
 
---

## 6. DTOs

### 6.2. Redução de proliferação de `ResumeResponse`

**Ação pendente**: auditar, por domínio, se `*ResumeResponse` é de fato necessário como classe própria ou se pode ser substituído por projeção JPA (`interface XResumoProjection`) ou por um único DTO com campos opcionais. Objetivo: reduzir o número de classes sem perder funcionalidade.
 
---

## 7. Isolamento de dados por tenant (multi-motorista)

Com o pivot do projeto para múltiplos motoristas, qualquer endpoint que retorne dados de alunos, rotas ou chamadas deve ser escopado ao motorista autenticado.

**Regra obrigatória**: o escopo (`transporteId`) nunca é recebido como parâmetro da requisição — é sempre derivado do usuário autenticado via `CurrentUserService`. Receber esse valor do cliente permite manipulação e quebra o isolamento entre motoristas, o que é crítico por envolver dados de menores (LGPD).

```java
@GetMapping("/alunos")
public List<AlunoResponse> listar() {
    Long transporteId = currentUserService.getTransporteId(); // do token, não da request
    return alunoService.listarPorTransporte(transporteId);
}
```

Essa regra vale para todo endpoint com escopo por motorista, não apenas listagem de alunos.
 
---

## 8. Cadastro e vínculo de responsáveis

O CPF não deve ser usado como identificador no fluxo de responsáveis. É um dado pessoal sensível e não é necessário para os fluxos definidos.

Há três formas de entrada:

1. **Convite específico de aluno**: o motorista faz o pré-cadastro básico do responsável e envia um link associado ao aluno. O responsável conclui o cadastro e cria a própria conta; não precisa de aprovação adicional do motorista.
2. **Link genérico**: o responsável se cadastra por um link do motorista. O cadastro fica pendente de aprovação, e o motorista faz manualmente a associação ao(s) aluno(s).
3. **Pré-cadastro + link genérico**: o motorista já registrou dados básicos do responsável, mas ele entra pelo link genérico. O sistema pode localizar um pré-cadastro candidato por telefone e e-mail; ainda assim, o motorista precisa aprovar o cadastro e associá-lo manualmente ao(s) aluno(s).

Telefone e e-mail servem para localizar um possível pré-cadastro, não como prova de identidade nem como autorização para criar vínculos automaticamente. A migration inicial não estabelece unicidade para esses campos; portanto, duplicidades precisam ser tratadas como correspondências ambíguas, não resolvidas escolhendo um registro arbitrariamente. A validação do contato e as regras de aprovação devem ser definidas no fluxo de cadastro.

O modelo relacional permite que um responsável tenha vários alunos e que um aluno tenha vários responsáveis (`guardian_students`). A tabela `guardian_invitations` representa o processo de convite/cadastro, não o responsável. Seus tipos `GENERIC` e `STUDENT_SPECIFIC` e o status `PENDING` sustentam esses fluxos; a migration inicial é a referência para a intenção original do schema.

**Estado conhecido da implementação**: esses fluxos ainda não estão implementados de ponta a ponta. O cadastro atual de aluno cria registros de responsável imediatamente e não implementa aprovação, conclusão por convite ou vínculo manual. `GuardianRequest` também não contempla todos os dados de pré-cadastro descritos na migration.
---

## 9. Confiabilidade de integrações e rotas

Eventos de aluno são publicados no RabbitMQ durante a transação que grava o aluno. Falhas síncronas na publicação devem propagar para que a transação do banco não seja confirmada sem o envio tentado. Isso não torna a gravação no banco e a entrega no broker atomicamente garantidas; uma garantia mais forte exige outbox ou mecanismo equivalente e, portanto, mudança de schema que deve ser discutida antes.

A otimização de rotas não deve inventar coordenadas ou métricas quando a resposta do provedor estiver incompleta ou inválida: falhar explicitamente evita devolver uma rota aparentemente válida com dados artificiais.

O schema atual associa uma parada escolar ao endereço da própria escola. Portanto, `addressId` fornecido ao adicionar escola deve corresponder a esse endereço; suportar endereços alternativos requer uma regra de propriedade/validação que o modelo atual não expressa.

## 10. Escopo de tracking — distinção importante

Duas features com nome parecido, escopo diferente:

- **Tracking em tempo real para pais** (WebSocket, posição contínua processada pelo backend): **cortado** desta fase do projeto (ver decisão de KISS review). Só reconsiderar com polling simples, se sobrar tempo.
- **Navegação do motorista via Google Maps** (`/rotas/{id}`): **mantido**. É majoritariamente client-side — o backend fornece pontos de parada/ordem de rota (via `RotasCompostasService`/`GoogleOptimizationClient`); o app do motorista desenha a navegação usando a localização do próprio dispositivo. Não envolve backend armazenando ou processando posição em tempo real.
  Essa distinção existe para evitar que a feature cortada reapareça disfarçada por semelhança de nome.

## 11. Migration inicial como referência

`src/main/resources/db.migration/V1.0.0__intial_sql.sql` é a referência de base para entender o modelo persistido e as regras de negócio implícitas no schema e em seus comentários. Ao revisar ou implementar comportamento, compare as entidades, repositories e services com essa migration; divergências devem ser identificadas e discutidas, não presumidas como decisões novas.

**Não alterar a migration inicial sem consultar o usuário antes.** A exceção já autorizada foi adicionar `active BOOLEAN NOT NULL` em `guardian_invitations`, para alinhar a tabela ao campo `active` herdado de `BaseEntity`.

**Divergência técnica corrigida**: `GuardianInvitationRepository` agora declara `JpaRepository<GuardianInvitation, UUID>`, alinhado à entidade correspondente, sem mudança de migration.