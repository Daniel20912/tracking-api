# 📦 Tracking API — Rastreamento de Encomendas dos Correios

API REST desenvolvida em **Spring Boot** para rastreamento automático de encomendas dos Correios. O usuário cadastra um
código de rastreio junto com seu email e a aplicação monitora a encomenda automaticamente, salvando o histórico de
eventos no banco de dados e notificando por email sempre que houver uma atualização — até a entrega ser confirmada.

---

## 🚀 Demonstração

A API está hospedada e disponível para testes em:

```
https://tracking-api-pb24.onrender.com
```

> ⚠️ A aplicação roda em planos gratuitos de hospedagem, então a primeira requisição após um período sem uso pode levar
> alguns segundos para responder.

A documentação interativa dos endpoints (Swagger) está disponível em:

```
https://tracking-api-pb24.onrender.com/swagger-ui.html
```

### 🖥️ Interface Web

Um frontend foi desenvolvido para facilitar o uso da API, disponível em:

```
https://daniel20912.github.io/tracking-frontend
```

A interface permite cadastrar um novo rastreio informando o código e o email, consultar o histórico completo de eventos
de qualquer rastreio já cadastrado, e visualizar as atualizações em uma timeline organizada cronologicamente. Todas as
mensagens de erro retornadas pela API são exibidas diretamente na interface. O rodapé inclui o crédito
ao [SiteRastreio](https://www.siterastreio.com.br), exigido para o uso gratuito da API de rastreio.

---

## 🧠 Como funciona

1. O usuário cadastra um código de rastreio e um email via `POST /tracking-api`
2. A API consulta a API externa de rastreio (**SiteRastreio / Wonca**), valida o código e salva o **histórico completo**
   de eventos no banco
3. Um **scheduler** roda automaticamente em segundo plano, consultando periodicamente cada rastreio ainda não entregue
4. A cada consulta, apenas os eventos que ainda não estão no banco são salvos. Quando há novidade, um **email é
   disparado** para o usuário via **Brevo**
5. Quando um evento de entrega é registrado, o monitoramento daquele rastreio é encerrado automaticamente
6. O histórico completo pode ser consultado em qualquer momento via `GET /tracking-api/findByCode/{codigo}`, sem
   necessidade de nova consulta à API externa

---

## 🏗️ Arquitetura

O projeto é organizado por **domínio**, separando claramente as responsabilidades:

```
com.danieloliveira.tracking
├── tracking/        → cadastro e consulta de rastreios
├── event/           → persistência e regras dos eventos de rastreio
├── client/          → integração com a API externa (Feign Client)
├── email/           → montagem e envio de notificações por email via Brevo
├── scheduler/       → job agendado que verifica atualizações periodicamente
├── config/          → configurações da aplicação (CORS)
└── exception/       → tratamento centralizado de erros
```

### Fluxo de dados

- A API externa retorna o **histórico completo** de eventos a cada consulta
- A aplicação persiste esse histórico no banco, **descartando os eventos já conhecidos**, identificados pelo código do
  evento e pelo instante em que ocorreu. Assim, várias execuções do scheduler não geram duplicatas
- A leitura do histórico pelo usuário é feita **sempre pelo banco de dados**, nunca diretamente pela API externa, o que
  economiza consultas da cota do plano

---

## 🛠️ Tecnologias utilizadas

- **Java 21**
- **Spring Boot 3** (Web, Data JPA, Validation, Scheduling)
- **PostgreSQL** (Supabase em produção)
- **OpenFeign** — comunicação com a API externa de rastreio e com a API do Brevo
- **Brevo API** — envio de notificações por email via HTTP, sem uso de SMTP
- **Lombok**
- **SpringDoc OpenAPI (Swagger)**
- **JUnit + Mockito + H2** — testes de integração
- **Docker / Docker Compose**

---

## 📡 Endpoints principais

### Cadastrar um rastreio

```http
POST /tracking-api
Content-Type: application/json

{
  "code": "AB123456789BR",
  "email": "usuario@email.com"
}
```

Valida o código junto à API externa, salva o rastreio com o histórico de eventos existente e retorna `201 Created`.

### Buscar o histórico de um rastreio

```http
GET /tracking-api/findByCode/{codigo}
```

Retorna o rastreio com o histórico completo de eventos salvos no banco, ordenados do mais antigo para o mais recente.

---

## ⚙️ Como executar localmente

### Pré-requisitos

- Docker e Docker Compose instalados
- Uma chave de API do [SiteRastreio](https://www.siterastreio.com.br) (Wonca)
- Uma conta no [Brevo](https://brevo.com) com uma API Key gerada e um sender verificado

### Passo a passo

1. Clone o repositório

```bash
git clone https://github.com/Daniel20912/tracking-api.git
cd tracking-api
```

2. Crie um arquivo `.env` na raiz do projeto baseado no `.env.example`

3. Suba os containers

```bash
docker compose up --build
```

4. A API estará disponível em `http://localhost:8080`

Localmente o banco é um container PostgreSQL definido no `docker-compose`. Em produção a aplicação se conecta ao
Supabase.

---

## 🔑 Variáveis de ambiente

| Variável                     | Descrição                                            |
|------------------------------|------------------------------------------------------|
| `DB_NAME`                    | Nome do banco PostgreSQL (container local)           |
| `DB_USER`                    | Usuário do banco (container local)                   |
| `DB_PASSWORD`                | Senha do banco (container local)                     |
| `SPRING_DATASOURCE_URL`      | URL de conexão JDBC                                  |
| `SPRING_DATASOURCE_USERNAME` | Usuário da conexão JDBC                              |
| `SPRING_DATASOURCE_PASSWORD` | Senha da conexão JDBC                                |
| `API_URL`                    | URL base da API de rastreio                          |
| `API_KEY`                    | Chave de acesso da API de rastreio                   |
| `BREVO_API_KEY`              | Chave de acesso da API do Brevo para envio de emails |
| `BREVO_SENDER_EMAIL`         | Email do remetente verificado no Brevo               |
| `CORS_ALLOWED_ORIGINS`       | Origens autorizadas a chamar a API (frontend)        |

> Nenhum valor real está versionado no repositório. Consulte `.env.example` para a lista completa.

---

## ⚠️ Limitações conhecidas

> As limitações abaixo são inerentes ao uso de infraestrutura gratuita e fazem parte do contexto de um projeto de
> portfólio e estudo. Em um ambiente de produção real, cada uma delas seria resolvida com recursos pagos e uma arquitetura
> mais robusta.

- **Limite da API externa:** o plano gratuito da API de rastreio tem uma franquia mensal de consultas (1.000 por mês), e
  só as consultas em que o objeto é encontrado contam. Por isso o scheduler roda em intervalo configurável
  (`scheduler.interval`) e a leitura do histórico é sempre feita pelo banco, nunca pela API externa. Em produção, um
  plano pago permitiria intervalos menores e notificações mais próximas do tempo real.

- **Dependência de um provedor externo:** o projeto já precisou trocar de provedor de rastreio quando o anterior passou
  a exigir assinatura paga. Como a integração está isolada no pacote `client`, a troca ficou concentrada no client, nos
  DTOs e no mapper de eventos, sem alterar o restante da aplicação.

- **Envio de email via API HTTP:** a maioria das plataformas de hospedagem gratuita bloqueia conexões SMTP de saída
  (portas 465/587). Por esse motivo o envio de email foi implementado usando a **API HTTP do Brevo** ao invés de SMTP
  tradicional — o que garante compatibilidade com qualquer plataforma. Como o remetente usa um endereço de email
  gratuito, sem domínio próprio autenticado, as mensagens podem ocasionalmente cair na caixa de spam.

- **Banco de dados:** a aplicação usa o plano gratuito do [Supabase](https://supabase.com), conectando pelo *session
  pooler* (IPv4) com um pool de conexões pequeno. Esse plano tem **500 MB** de armazenamento e **pausa projetos com
  pouca atividade durante uma semana** — o scheduler periódico gera essa atividade, mas o limite exato não é publicado.
  O projeto começou no Neon (PostgreSQL serverless), mas o consumo de horas de computação do plano gratuito esgotava
  todo mês, o que motivou a migração. As tabelas têm *Row Level Security* habilitado para que a API REST pública do
  Supabase não exponha os dados; a aplicação acessa o banco pelo usuário dono das tabelas.

- **Cold start:** a hospedagem gratuita no [Render](https://render.com) entra em modo inativo após períodos sem uso, o
  que também interromperia o scheduler. Para contornar isso, o [UptimeRobot](https://uptimerobot.com) faz uma chamada
  periódica à rota de health check e mantém a aplicação ativa.

- **Mensageria assíncrona:** o envio de email é feito de forma síncrona dentro do scheduler. Em um sistema de produção
  real, o ideal seria publicar um evento em uma fila (como Kafka ou RabbitMQ) para desacoplar a detecção da atualização
  do envio da notificação — ganhando resiliência e escalabilidade.

---

## 🧪 Testes

O projeto conta com testes de integração que validam o fluxo completo de cadastro, persistência e detecção de novos
eventos, utilizando **H2** como banco em memória e **Mockito** para simular as respostas da API externa — sem depender
de uma chamada real e sem consumir a cota de consultas.

```bash
mvn test
```

---

## 📌 Sobre o projeto

Este é um projeto de portfólio desenvolvido com foco em demonstrar:

- Integração com APIs externas via OpenFeign
- Persistência de dados relacionais com JPA, incluindo deduplicação de eventos
- Agendamento de tarefas com Spring Scheduler
- Notificações automáticas por email via API HTTP (Brevo)
- Separação de responsabilidades e boas práticas de arquitetura
- Tratamento centralizado de exceções
- Testes de integração com H2 e Mockito
- Containerização com Docker e multi-stage build
- Deploy em ambiente de produção com banco de dados cloud (Supabase)
- Decisões técnicas conscientes frente a limitações reais de infraestrutura, como a troca de provedor de rastreio e de
  banco de dados

---

## 👤 Autor

Desenvolvido por **Daniel Oliveira**.