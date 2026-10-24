# Bodies para testes manuais

Exemplos baseados nos controllers, DTOs e enums atuais. Cada arquivo JSON
contem somente o body, pronto para copiar no Swagger, Postman ou Insomnia
depois de substituir os placeholders.

Use apenas em desenvolvimento: nomes, emails, telefones, placa e senhas
sao dados ficticios de teste. Nao envie mensagens para os telefones.
Os enderecos sao de locais publicos reais de Sao Caetano do Sul/SP,
usados como escola ou pontos de encontro, nunca como residencias reais
dos responsaveis ficticios.

## Como usar

Use a URL base da sua instancia e `Content-Type: application/json`.
Nos endpoints autenticados, envie `Authorization: Bearer <accessToken>`
com o token retornado pelo login do motorista. No Authorize do Swagger,
informe somente o JWT, sem `Bearer`.

`{{variavel}}` e um placeholder, nao um UUID valido. Substitua todas as
ocorrencias antes de enviar, inclusive as chaves do JSON de presencas.
Postman/Insomnia podem resolver essas variaveis se configuradas no ambiente;
o Swagger exige substituicao manual. Nenhum ID e fixado pelos cadastros.

| Placeholder | De onde obter |
|-------------|---------------|
| `{{schoolId}}` | `id` retornado por `POST /schools` |
| `{{studentId1}}` | `id` retornado ao criar o aluno 01 |
| `{{studentId2}}` | `id` retornado ao criar o aluno 02 |
| `{{guardianId1}}` | `id` do responsavel do aluno 01 em `GET /students/{studentId1}/guardians` |
| `{{transportId}}` | `transportId` retornado pelo login |
| `{{serviceClientId}}` | Valor configurado em `auth.v2.service-token.client-id` |
| `{{serviceClientSecret}}` | Valor configurado em `auth.v2.service-token.client-secret` |

Nos caminhos abaixo, `{routeId}`, `{attendanceId}`, `{invitationId}` e
`{token}` tambem devem ser substituidos pelos valores reais retornados
ao criar a rota, iniciar a chamada ou criar o convite. O token do convite
nao e o JWT do login.

## Ordem sugerida

1. Registrar o motorista e fazer login. Guardar `accessToken` e `transportId`.
2. Criar a escola e guardar `schoolId`.
3. Criar os alunos 01 e 02 e guardar seus IDs. Cada cadastro cria seu proprio
   responsavel. Consultar os responsaveis do aluno 01 para obter `guardianId1`.
4. Opcionalmente, criar o responsavel avulso e vincula-lo por
   `PUT /students/{studentId}/guardians/{guardianId}`, sem body.
5. Criar a rota, adicionar os dois alunos e a escola. Depois testar as
   ordenacoes e atualizacoes.
6. Iniciar a chamada, registrar presencas e finalizar. Para testar cancelamento,
   iniciar outra chamada e cancela-la.
7. Testar os convites conforme os fluxos abaixo. Deixar exclusoes e
   inativacoes para o final, para nao invalidar as dependencias.

Cadastros/atualizacoes de escola e responsaveis tambem usam geocodificacao:
configure a API do Google. Alunos publicam eventos no RabbitMQ; a infraestrutura
de mensageria deve estar disponivel. Enderecos dos alunos e da escola precisam
ter coordenadas validas para serem associados a rotas. A otimizacao exige
tambem as credenciais e a Route Optimization API do Google configuradas.
Um body valido nao garante que as integracoes estejam disponiveis.

## Endpoints com body

Os caminhos dos arquivos nesta tabela sao relativos a esta pasta.
Nos endpoints de atualizacao, altere os campos desejados antes de enviar.

| Metodo | Endpoint | Arquivo |
|--------|----------|---------|
| POST | `/auth/register` | `auth\registrar.json` |
| POST | `/auth/login` | `auth\login.json` |
| POST | `/auth/token/service` | `auth\token-servico.json` |
| PUT | `/users/me` | `users\atualizar.json` |
| PUT | `/transports` | `transports\atualizar.json` |
| POST | `/schools` | `schools\salvar.json` |
| PUT | `/schools/{schoolId}` | `schools\salvar.json` |
| POST | `/guardians` | `guardians\salvar.json` |
| PUT | `/guardians/{guardianId}` | `guardians\salvar.json` |
| POST | `/students` | `students\criar-01.json` ou `students\criar-02.json` |
| PUT | `/students/{studentId1}` | `students\atualizar-01.json` |
| POST | `/students/lote` | `students\lote.json` |
| POST | `/routes` | `routes\criar.json` |
| PUT | `/routes/{routeId}` | `routes\atualizar-alunos.json` ou `routes\atualizar-paradas.json` |
| POST | `/routes/{routeId}/students` | `routes\adicionar-aluno-01.json` ou `routes\adicionar-aluno-02.json` |
| POST | `/routes/{routeId}/schools` | `routes\adicionar-escola.json` |
| PATCH | `/routes/{routeId}/students/order` | `routes\ordenar-alunos.json` |
| PATCH | `/routes/{routeId}/schools/order` | `routes\ordenar-escolas.json` |
| POST | `/routes/optimize` | `routes\otimizar.json` |
| PUT | `/attendances/{attendanceId}/presences` | `attendances\presencas.json` |
| POST | `/guardian-invitations` | `guardian-invitations\criar-generico.json` ou `guardian-invitations\criar-especifico.json` |
| POST | `/guardian-invitations/public/{token}/accept` | `guardian-invitations\aceitar-generico.json` ou `guardian-invitations\aceitar-especifico.json` |
| POST | `/guardian-invitations/{invitationId}/approve` | `guardian-invitations\aprovar.json` |

As tres rotas de autenticacao tambem aceitam o prefixo `/auth/v2`
(por exemplo, `/auth/v2/login`), com os mesmos bodies. Registro, login,
emissao do token de servico e `/guardian-invitations/public/**` nao exigem
JWT. A emissao do token de servico exige as credenciais configuradas no
body; nao use o JWT de servico como substituto do login do motorista
nos fluxos que precisam de usuario/transporte.

### Cuidados com os formatos

- `guardian` no aluno e uma **lista**, no singular. No cadastro cria novos
  responsaveis; para reutilizar um existente use o vinculo sem body.
  No PUT do aluno esse campo continua obrigatorio na validacao, mas o servico
  atual nao atualiza os responsaveis por ele. Use `PUT /guardians/{guardianId}`.
- `birthdate` usa `yyyy-MM-dd`; horarios usam `HH:mm:ss`. `monthlyFee` deve
  ser positivo e `dueDate` deve estar entre 1 e 31.
- `addressId: null` na associacao de aluno deixa a API escolher o endereco
  de um responsavel vinculado. Pode ser substituido pelo UUID do endereco
  retornado em `GET /students/{studentId}/guardians` (campo `address.id`).
  Na associacao de escola, `null` usa o endereco da escola.
- `atualizar-alunos.json` substitui a lista de alunos da rota.
  `atualizar-paradas.json` substitui alunos e escolas: envie a lista completa.
  Se `stops` estiver presente, ele tem prioridade sobre `students`.
  Para atualizar apenas os dados da rota, omita ambos os campos; nao envie
  listas vazias a menos que queira remover os vinculos.
- `generalOrder` ordena todas as paradas; `specificOrder` ordena alunos ou
  escolas separadamente. Os PATCHs recebem arrays de UUIDs dos alunos/escolas,
  nao IDs dos vinculos. O exemplo de escolas tem um item; para verificar troca
  de ordem, cadastre e associe outra escola e inclua seu ID.
- Presencas sao um objeto **UUID do aluno -> status**, nao uma lista.
  Os dois alunos precisam estar na rota, e a chamada deve estar `IN_PROGRESS`.
- Em `otimizar.json`, **`intialLocation`** e o nome exato esperado pelo DTO
  atual (nao corrigir para `initialLocation`). As coordenadas sao aproximadas
  do teatro e da escola, nao pontos de embarque certificados. Para um teste
  preciso, substitua-as por `address.latitude`/`address.longitude` retornados
  pela API. Esse exemplo usa somente o aluno 01 e a escola; o endpoint de
  otimizacao recebe pontos diretamente, nao carrega toda a rota cadastrada.
  Para testar a outra opcao, altere `mustOptimizeOrder` para `false`.

### Convites

**Generico:** criar com `criar-generico.json`, consultar o token publico,
aceitar com `aceitar-generico.json`, listar `/guardian-invitations/pending`
como motorista e aprovar com `aprovar.json`. O aceite cria uma conta de
responsavel e fica `AWAITING_APPROVAL`; a aprovacao vincula os alunos.
`preRegistrationGuardianId: null` preserva esse novo cadastro. Para testar
a transferencia da conta a um pre-cadastro, use o ID de um candidato
compativel retornado pela listagem de pendencias.

**Especifico:** criar com `criar-especifico.json`, usando o responsavel
ja vinculado ao aluno 01, e aceitar com `aceitar-especifico.json`.
Email e telefone devem corresponder ao pre-cadastro. O aceite conclui
o convite (`COMPLETED`), sem a etapa de aprovacao do generico.

Use o token de cada convite no seu respectivo aceite. Se o responsavel
ja tiver conta, informe sua senha existente. Para cancelar, crie outro
convite e cancele enquanto estiver `PENDING`; nao reutilize um concluido.

## Endpoints sem body

Nao envie `{}` nem `[]`: deixe o body vazio. IDs sao parametros do caminho.

| Metodo | Endpoint |
|--------|----------|
| GET | `/users/me` |
| GET | `/users/feign/{userId}` |
| GET | `/transports` |
| DELETE | `/transports` |
| GET | `/schools` |
| GET | `/schools/{schoolId}` |
| DELETE | `/schools/{schoolId}` |
| GET | `/guardians` |
| GET | `/guardians/{guardianId}` |
| DELETE | `/guardians/{guardianId}` |
| GET | `/students` |
| GET | `/students/{studentId}` |
| DELETE | `/students/{studentId}` |
| GET | `/students/{studentId}/guardians` |
| PUT | `/students/{studentId}/guardians/{guardianId}` |
| DELETE | `/students/{studentId}/guardians/{guardianId}` |
| GET | `/students/feign/{studentId}` |
| GET | `/students/ativos` |
| GET | `/routes` |
| GET | `/routes/{routeId}` |
| GET | `/routes/{routeId}/students` |
| DELETE | `/routes/{routeId}` |
| DELETE | `/routes/{routeId}/students/{studentId}` |
| DELETE | `/routes/{routeId}/schools/{schoolId}` |
| POST | `/routes/{routeId}/attendances` |
| GET | `/routes/{routeId}/attendances` |
| POST | `/attendances/{attendanceId}/finish` |
| POST | `/attendances/{attendanceId}/cancel` |
| GET | `/guardian-invitations/public/{token}` |
| GET | `/guardian-invitations/pending` |
| DELETE | `/guardian-invitations/{invitationId}` |

O historico aceita query params, por exemplo:
`/routes/{routeId}/attendances?status=FINISHED&status=CANCELLED&page=0&size=10&sort=id,desc`.
Para criar uma rota de volta, reutilize `routes\criar.json` alterando
nome/horarios e `routeType` para `FROM_SCHOOL`.

## Valores aceitos pelos enums

| Campo | Valores |
|-------|---------|
| `educationLevel` | `HIGH_SCHOOL`, `MIDDLE_SCHOOL`, `DAYCARE_CENTER`, `KINDERGARTEN` |
| `routeType` | `TO_SCHOOL`, `FROM_SCHOOL` |
| Presenca | `PRESENT`, `ABSENT` |
| Status da chamada (filtro) | `IN_PROGRESS`, `FINISHED`, `CANCELLED` |
| Tipo de convite | `GENERIC`, `STUDENT_SPECIFIC` |

`address.type` e texto livre no DTO; nao e um enum.

## Enderecos e fontes

| Local real | Endereco usado |
|------------|---------------|
| EME Professora Alcina Dantas Feijao | Rua Capivari, 500, Nova Gerty, Sao Caetano do Sul/SP, 09580-370 |
| Teatro Paulo Machado de Carvalho | Alameda Conde de Porto Alegre, 840, Santa Maria, Sao Caetano do Sul/SP, 09561-000 |
| USCS - Campus Barcelona | Avenida Goias, 3400, Barcelona, Sao Caetano do Sul/SP, 09550-051 |

Fontes consultadas em 24/10/2026:

- Escola: https://alcina.com.br/ e https://sites.google.com/scseduca.com.br/ept/localiza%C3%A7%C3%A3o
- CEP/bairro da Rua Capivari, trecho do numero 500: https://viacep.com.br/ws/09580370/json/
- Teatro: https://www.saocaetanodigital.com.br/sao-caetano-do-sul-2/teatro-paulo-machado-de-carvalho/
- CEP/bairro da alameda, trecho do numero 840: https://viacep.com.br/ws/09561000/json/
- USCS: https://foprop.org.br/filiados-foprop/universidade-municipal-de-sao-caetano-do-sul-2/
- Coordenadas do teatro: https://mapcarta.com/pt/W277402185

O site da escola usa a denominacao Maua; o JSON usa Nova Gerty, conforme
o cadastro postal do CEP para esse trecho. Para o teatro, foi usado o CEP
do trecho ate 1439/1440, nao o CEP do trecho a partir de 1441/1442.
