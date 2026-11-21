# Cardápio Digital — Popeye Lanches, Porções e Pizzas

Prática Extensionista VII · Engenharia da Computação · Universidade de Ribeirão Preto

Aplicação web distribuída que funciona como um **Cardápio Digital**: o cliente vê o cardápio, monta e envia o pedido e acompanha o andamento em tempo real; o administrador gerencia o cardápio e recebe os pedidos.

## Funcionalidades

| Requisito | O que faz | Onde |
|---|---|---|
| RF001 | Cardápio por categorias (navegação sem recarregar a página), dados vindos da API | Site do cliente |
| RF002 | Detalhes do item: ingredientes, tamanhos, opcionais, observação, foto ampliada | Site do cliente |
| RF003 | Carrinho com contador, quantidades e estado mantido ao navegar | Site do cliente |
| RF004 | Revisão e envio do pedido (`POST`), com número de confirmação | Site do cliente |
| RF005 | Login do administrador com **JWT** (sessão persistente, tratamento de 401/403) | Painel `#/admin` |
| RF006 | CRUD de itens do cardápio, validações e **upload de foto** | Painel `#/admin/itens` |
| RF007 | Lista de pedidos com filtro por status e **aviso sonoro/visual em tempo real** | Painel `#/admin` |
| RF008 | Mudança de status com **histórico** (quem e quando) e atualização **em tempo real** para o cliente; o cliente confirma o recebimento e o painel é avisado | Painel e site do cliente |
| RF009 | Documentação **Swagger** da API, com exemplos, códigos de erro e teste interativo | `/swagger-ui/index.html` |

## Arquitetura

```
 Navegador (cliente / admin)
        │   HTTP + JSON (REST)      SSE (tempo real)
        ▼
 Frontend  React + Vite  (porta 5173)
        │   fetch / EventSource
        ▼
 Backend   Spring Boot — API REST  (porta 8080)
        │   JPA / Hibernate                 │ upload de imagens
        ▼                                   ▼
 PostgreSQL                        Cloudinary (opcional) ou pasta local "uploads"
```

**Tecnologias:** Java · Spring Boot (Web, Data JPA, Security, Validation) · JWT (Spring Security OAuth2 Resource Server) · springdoc-openapi (Swagger) · PostgreSQL · React · Vite · SSE (Server-Sent Events).

## Pré-requisitos

- **JDK 17 ou superior** (a versão usada fica em `backend/pom.xml`, na propriedade `<java.version>`)
- **Node.js 20.19+ ou 22.12+** (inclui o npm)
- **PostgreSQL 14 ou superior** (ou Docker, para subir só o banco — veja a opção B)
- Git

## Como executar

### 1. Baixar o projeto

```bash
git clone https://github.com/annyannx/cardapio-digital.git
cd cardapio-digital
```

### 2. Criar o banco de dados

**Opção A — PostgreSQL instalado no computador**

Na pasta principal do projeto (se o `psql` não for reconhecido no Windows, use o caminho completo, por exemplo `& "C:\Program Files\PostgreSQL\17\bin\psql.exe"`):

```bash
psql -U postgres -c "CREATE DATABASE cardapio_digital WITH ENCODING 'UTF8' TEMPLATE template0;"
psql -U postgres -d cardapio_digital -f backend/database/schema.sql
psql -U postgres -d cardapio_digital -f backend/database/seed.sql
```

No **Windows (PowerShell)**, antes dos comandos acima, rode `$env:PGCLIENTENCODING = "UTF8"` para os acentos entrarem corretamente.

O `seed.sql` carrega o cardápio de exemplo (pizzas, esfihas, refrigerantes, sucos e opcionais). **Os preços do seed são fictícios**: os valores reais são ajustados pelo painel do administrador.

**Opção B — somente Docker**

```bash
docker compose up -d
```

Isso cria um PostgreSQL já com o schema e o seed carregados (usuário `postgres`, senha `postgres`, banco `cardapio_digital`). Use `postgres` como senha no próximo passo. Se a porta 5432 já estiver ocupada por outro PostgreSQL, pare-o antes.

Caminho para acesso (inclusão) das imagens do cardápio - psql -U postgres -d cardapio_digital -f backend/database/seed-imagens.sql

### 3. Ligar o backend (API)

```bash
cd backend
```

Defina a senha do banco e ligue (a senha **não** fica em nenhum arquivo do projeto):

- **Windows (PowerShell):**
  ```powershell
  $env:DB_PASSWORD = 'SUA_SENHA_DO_POSTGRES'
  .\mvnw.cmd spring-boot:run
  ```
- **Linux / macOS:**
  ```bash
  export DB_PASSWORD='SUA_SENHA_DO_POSTGRES'
  ./mvnw spring-boot:run
  ```

Na primeira vez, o Maven baixa as dependências (pode demorar alguns minutos). O backend está pronto quando aparecer `Started CardapioApiApplication`. Ao ligar, ele cria automaticamente o usuário administrador (veja a tabela de acessos).

### 4. Ligar o frontend (site)

Em **outro terminal**:

```bash
cd frontend
npm install
npm run dev
```

### 5. Acessar

| O quê | Endereço |
|---|---|
| Cardápio do cliente | http://localhost:5173/ |
| Painel do administrador | http://localhost:5173/#/admin |
| Documentação da API (Swagger) | http://localhost:8080/swagger-ui/index.html |

**Login do administrador (apenas para desenvolvimento/demonstração):**

- E-mail: `admin@cardapio.com`
- Senha: `admin123`

Para usar outros valores, defina `ADMIN_EMAIL` e `ADMIN_PASSWORD` antes de ligar o backend pela primeira vez.

## Como testar o fluxo completo

1. No site do cliente, abra um item, escolha tamanho e opcionais, adicione ao pedido e envie.
2. Em outra aba, entre no painel do administrador, clique em **Ativar som de novos pedidos** e faça um novo pedido: o aviso aparece na hora.
3. No painel, mude o status do pedido (Iniciar preparo → Pronto → Entregue). A tela de acompanhamento do cliente muda sozinha.
4. Quando o pedido estiver **Entregue**, o cliente confirma o recebimento e o painel mostra a confirmação.
5. Na aba **Cardápio** do painel, cadastre, edite, esconda ou remova um item, e envie uma foto.

No Swagger, use **POST /api/auth/login**, copie o `token` e cole em **Authorize** para testar as rotas protegidas.

## Variáveis de ambiente (backend)

Todas são opcionais, exceto `DB_PASSWORD`.

| Variável | Padrão | Para quê |
|---|---|---|
| `DB_PASSWORD` | — (**obrigatória**) | Senha do PostgreSQL |
| `DB_URL` | `jdbc:postgresql://localhost:5432/cardapio_digital` | Endereço do banco |
| `DB_USER` | `postgres` | Usuário do banco |
| `JWT_SECRET` | chave de desenvolvimento | Chave de assinatura dos tokens (mín. 32 caracteres). **Troque em produção** |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | `admin@cardapio.com` / `admin123` | Administrador criado na primeira execução |
| `CORS_ORIGENS` | `http://localhost:5173,http://localhost:3000` | Sites autorizados a chamar a API |
| `PUBLIC_URL` | `http://localhost:8080` | Endereço público da API (para montar a URL das imagens locais) |
| `IMAGENS_PASTA` | `uploads` | Pasta das imagens quando o Cloudinary não está configurado |
| `CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_UPLOAD_PRESET` | vazias | Se preenchidas, as fotos vão para o Cloudinary (serviço de imagens na nuvem) |

**Frontend:** `VITE_API_URL` (padrão `http://localhost:8080`), em um arquivo `frontend/.env`, se a API estiver em outro endereço.

## Principais endpoints

**Públicos**

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/api/cardapio` | Cardápio completo por categorias |
| GET | `/api/cardapio/itens/{id}` | Detalhes de um item |
| POST | `/api/pedidos` | Envia um pedido |
| GET | `/api/pedidos/{numero}` | Situação do pedido |
| GET | `/api/pedidos/{numero}/stream` | Tempo real do pedido (SSE) |
| POST | `/api/pedidos/{numero}/confirmar-recebimento` | Cliente confirma o recebimento |
| POST | `/api/auth/login` | Login do administrador (devolve o JWT) |

**Administrador (exigem o token JWT)**

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/api/admin/me` | Dados do usuário logado |
| GET | `/api/admin/categorias` | Lista as categorias |
| GET, POST | `/api/admin/itens` | Lista / cadastra itens |
| GET, PUT, DELETE | `/api/admin/itens/{id}` | Busca / edita / remove um item |
| POST | `/api/admin/imagens` | Envia uma foto (JPG, PNG ou WEBP, até 5 MB) |
| GET | `/api/admin/pedidos` | Lista os pedidos (filtro `?status=`) |
| PATCH | `/api/admin/pedidos/{numero}/status` | Muda o status do pedido |
| GET | `/api/admin/pedidos/stream` | Tempo real do painel (SSE) |

## Estrutura do repositório

```
cardapio-digital/
├── backend/                 API Spring Boot
│   ├── database/            schema.sql, seed.sql e migrações
│   └── src/main/java/...    controller, service, repository, model, dto, config
├── frontend/                Site React (cliente e painel do administrador)
│   └── src/                 pages, components, admin
├── docker-compose.yml       PostgreSQL pronto (opcional)
├── alunos.txt               Integrantes e links
└── README.md
```

## Fotos dos itens

Por padrão, as fotos enviadas pelo painel ficam na pasta `backend/uploads` (ignorada pelo Git) e são servidas pela própria API. Para guardá-las na nuvem, crie uma conta gratuita no Cloudinary, um *upload preset* do tipo **Unsigned**, e defina `CLOUDINARY_CLOUD_NAME` e `CLOUDINARY_UPLOAD_PRESET` antes de ligar o backend.

## Solução de problemas

- **`psql` não é reconhecido (Windows):** use o caminho completo do `psql.exe` (em `C:\Program Files\PostgreSQL\<versão>\bin\`) ou o pgAdmin.
- **`npm` bloqueado no PowerShell:** rode `Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned`.
- **Backend não liga e fala do banco:** confira se o PostgreSQL está em execução, se o banco `cardapio_digital` existe e se `DB_PASSWORD` está definida **no mesmo terminal** em que roda o backend.
- **Erro `Schema-validation` ao ligar:** o schema do banco está desatualizado. Em um banco novo, rode de novo o `schema.sql` e o `seed.sql` (atenção: o `schema.sql` recria as tabelas e apaga os dados).
- **Porta 8080 ou 5173 ocupada:** feche o outro programa que a usa.
- **O site mostra "Não foi possível conectar ao servidor":** o backend não está ligado, ou a URL do site não está em `CORS_ORIGENS`.

## Desenvolvedora

Veja o arquivo [`alunos.txt`](alunos.txt).


## Fotos do cardápio no banco

As fotos dos itens ficam guardadas no Cloudinary. Depois do `seed.sql`, rode o `seed-imagens.sql` para ligar cada item à sua foto:

    psql -U postgres -d cardapio_digital -f backend/database/seed-imagens.sql

Com Docker (`docker compose up -d`), esse arquivo já é carregado automaticamente. Para ver as fotos não é preciso configurar o Cloudinary: os endereços são públicos.


## Preços reais do cardápio

Depois do `seed.sql` e do `seed-imagens.sql`, rode o `seed-precos.sql`, que troca os preços fictícios pelos preços reais (inclusive dos opcionais):

    psql -U postgres -d cardapio_digital -f backend/database/seed-precos.sql

Com Docker (`docker compose up -d`), esse arquivo já é carregado automaticamente.