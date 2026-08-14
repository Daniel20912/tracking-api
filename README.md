# 📦 Tracking API — Rastreamento de Encomendas dos Correios

API REST desenvolvida em **Spring Boot** para rastreamento automático de encomendas dos Correios. O usuário cadastra um código de rastreio junto com seu email e a aplicação monitora a encomenda automaticamente, salvando o histórico de eventos no banco de dados e notificando por email sempre que houver uma atualização — até a entrega ser confirmada.

---

## 🚀 Demonstração

A API está hospedada e disponível para testes em:

```
https://tracking-api-pb24.onrender.com
```

> ⚠️ A aplicação está sempre ativa graças ao UptimeRobot. Porém, o banco e dados (Neon serverless) pode suspender após períodos de inatividade — a primeira requisição após essa suspensão pode levar alguns segundos a mais para responder enquanto o banco acorda.

A documentação interativa dos endpoints (Swagger) está disponível em:

```
https://tracking-api-pb24.onrender.com/swagger-ui.html
```

---

## 🧠 Como funciona

1. O usuário cadastra um código de rastreio e um email via `POST /tracking-api`
2. A API consulta a API externa **SeuRastreio**, valida o código e salva o primeiro evento no banco
3. Um **scheduler** roda automaticamente em segundo plano, verificando periodicamente se há atualizações para cada rastreio ainda não entregue
4. Sempre que uma atualização é detectada, o novo evento é salvo no banco e um **email é disparado** para o usuário via **Brevo**
5. Quando a encomenda é entregue, o monitoramento daquele rastreio é encerrado automaticamente
6. O histórico completo pode ser consultado em qualquer momento via `GET /tracking-api/findByCode/{codigo}`, sem necessidade de nova consulta à API externa

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
└── exception/       → tratamento centralizado de erros
```

### Fluxo de dados

- A API externa (**SeuRastreio**) retorna apenas o **evento mais recente** de cada consulta, sem histórico completo
- A própria aplicação constrói e acumula esse histórico no banco de dados, evento a evento, a cada execução do scheduler
- A leitura do histórico pelo usuário é feita **sempre pelo banco de dados**, nunca diretamente pela API externa

---

## 🛠️ Tecnologias utilizadas

- **Java 21**
- **Spring Boot 3** (Web, Data JPA, Validation, Scheduling)
- **PostgreSQL**
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

Valida o código junto à API externa, salva o rastreio e o primeiro evento, e retorna `201 Created`.

### Buscar o histórico de um rastreio

```http
GET /tracking-api/findByCode/{codigo}
```

Retorna o rastreio com o histórico completo de eventos salvos no banco, ordenados do mais antigo para o mais recente.

---

## ⚙️ Como executar localmente

### Pré-requisitos

- Docker e Docker Compose instalados
- Uma chave de API da [SeuRastreio](https://seurastreio.com.br)
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

---

## 🔑 Variáveis de ambiente

| Variável | Descrição |
|---|---|
| `DB_NAME` | Nome do banco PostgreSQL |
| `DB_USER` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco |
| `SPRING_DATASOURCE_URL` | URL de conexão JDBC |
| `SPRING_DATASOURCE_USERNAME` | Usuário da conexão JDBC |
| `SPRING_DATASOURCE_PASSWORD` | Senha da conexão JDBC |
| `SITERASTREIO_API_KEY` | Chave de acesso da API externa de rastreio |
| `BREVO_API_KEY` | Chave de acesso da API do Brevo para envio de emails |
| `BREVO_SENDER_EMAIL` | Email do remetente verificado no Brevo |

> Nenhum valor real está versionado no repositório.

---

## ⚠️ Limitações conhecidas

> As limitações abaixo são inerentes ao uso de infraestrutura gratuita e fazem parte do contexto de um projeto de portfólio e estudo. Em um ambiente de produção real, cada uma delas seria resolvida com recursos pagos e uma arquitetura mais robusta.

- **Limite da API externa:** o plano gratuito da SeuRastreio permite apenas **50 consultas por mês**. Por isso o scheduler foi configurado para rodar uma vez por dia, o que é suficiente considerando que uma encomenda leva em média 1 a 2 semanas para ser entregue. Em produção, um plano sem limitação de requisições permitiria intervalos muito menores e notificações mais próximas do tempo real.
- **Envio de email via API HTTP:** a maioria das plataformas de hospedagem gratuita bloqueia conexões SMTP de saída (portas 465/587). Por esse motivo o envio de email foi implementado usando a **API HTTP do Brevo** ao invés de SMTP tradicional — o que garante compatibilidade com qualquer plataforma de hospedagem.
- **Gerenciamento de conexões com o banco:** o banco de dados no Neon (serverless) suspende automaticamente após períodos de inatividade para economizar recursos. Para evitar erros de conexão obsoleta, o HikariCP foi configurado com um `max-lifetime` de 5 minutos — garantindo que conexões antigas sejam descartadas e recriadas antes de causarem falhas. Existe uma pequena janela de risco entre o banco suspender e a conexão ser descartada, mas o mecanismo de validação nativo do HikariCP cobre esse cenário na grande maioria dos casos.
- **Cold start:** a hospedagem gratuita no [Render](https://render.com) entra em modo inativo após períodos sem uso, podendo gerar uma resposta mais lenta na primeira requisição. Para contornar isso, o [UptimeRobot](https://uptimerobot.com) é utilizado para manter a aplicação sempre ativa.
- **Mensageria assíncrona:** o envio de email é feito de forma síncrona dentro do scheduler. Em um sistema de produção real, o ideal seria publicar um evento em uma fila (como Kafka ou RabbitMQ) para desacoplar a detecção da atualização do envio da notificação — ganhando resiliência e escalabilidade.
---

## 🧪 Testes

O projeto conta com testes de integração que validam o fluxo completo de cadastro, persistência e detecção de novos eventos, utilizando **H2** como banco em memória e **Mockito** para simular as respostas da API externa — sem depender de uma chamada real e sem consumir o limite de requisições.

```bash
mvn test
```

---

## 📌 Sobre o projeto

Este é um projeto de portfólio desenvolvido com foco em demonstrar:

- Integração com APIs externas via OpenFeign
- Persistência de dados relacionais com JPA
- Agendamento de tarefas com Spring Scheduler
- Notificações automáticas por email via API HTTP (Brevo)
- Separação de responsabilidades e boas práticas de arquitetura
- Tratamento centralizado de exceções
- Testes de integração com H2 e Mockito
- Containerização com Docker e multi-stage build
- Deploy em ambiente de produção com banco de dados cloud (Neon)
- Decisões técnicas conscientes frente a limitações reais de infraestrutura

---

## 👤 Autor

Desenvolvido por **Daniel Oliveira**.