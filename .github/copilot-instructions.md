# Copilot Instructions - Safeway Back-end

Objetivo
- API principal do Safeway para gestao de transporte escolar, cadastro de alunos, chamada, itinerarios e integracoes.

Arquitetura e stack
- Java 21, Spring Boot 3.
- Camadas: controllers (API) -> services -> repositories -> domain.
- Persistencia com Spring Data JPA.
- Seguranca com JWT/OAuth2.
- Mensageria com RabbitMQ (eventos de aluno).
- Integracoes via OpenFeign e Google Maps.

Padroes de codigo
- Entidades herdam BaseEntity (UUID, timestamps, soft delete por ativo).
- Servicos: interface + implementacao, com @Transactional em escrita.
- Mappers para converter entidade <-> DTO.
- Filtragem por usuario atual (CurrentUserService) para escopo multi-tenant.
- Validacao de entrada com @Valid e DTOs de request/response.
- Checkstyle baseado em Google Java Style.

Confiabilidades e riscos comuns
- Soft delete exige filtros por ativo nas queries.
- Relacoes EAGER podem causar N+1; preferir LAZY e fetch controlado.
- CORS configurado para localhost; revisar para ambiente.
- Mensageria com poucas tentativas; considerar DLQ em evolucoes.

Comandos usuais
- ./mvnw clean package
- ./mvnw test
- ./mvnw checkstyle:check
- ./mvnw spring-boot:run

Boas praticas de mudanca
- Preservar padrao de DTOs e mappers.
- Garantir que queries respeitam o usuario autenticado.
- Manter endpoints consistentes com controllers existentes.

Sugestoes de prompts
- "Mapeie o fluxo de criacao de aluno e a publicacao de eventos."
- "Proponha melhorias para evitar N+1 nas telas de itinerarios."
- "Como adicionar um novo endpoint mantendo validacao e mapeamento?"
