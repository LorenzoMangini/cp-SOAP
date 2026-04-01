- INTEGRANTES:
- NOME: Lorenzo Hayashi Mangini | RM: 554901
- NOME: Milton Cezar Bacanieski | RM: 555206
- NOME: Vitor Bebiano Mulford | RM: 555026
- NOME: Victório Maia Bastelli | RM: 554723

Sistema de Agendamento Hospitalar implementado como Web Service SOAP em Java puro, publicando e consumindo serviços via protocolo SOAP 1.1 com envelope XML.

---

## 📋 Índice

- [Contexto de Implantação](#-contexto-de-implantação)
- [Problemas Resolvidos](#-problemas-resolvidos)
- [Tecnologias](#-tecnologias)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Operações Disponíveis](#-operações-disponíveis)
- [Regras de Negócio](#-regras-de-negócio)
- [Boas Práticas Aplicadas](#-boas-práticas-aplicadas)
- [Como Executar](#-como-executar)
- [Testando com Postman](#-testando-com-postman)
- [Próximas Features](#-próximas-features)

---

## 🏗 Contexto de Implantação

Hospitais e clínicas médicas frequentemente operam com sistemas legados de gestão hospitalar — como **Tasy**, **MV** e **Soul MV** — que utilizam SOAP como protocolo padrão de integração entre módulos e parceiros externos.

O **Hospital SOAP Service** foi desenvolvido para atuar como **camada de integração central**, permitindo que sistemas externos (laboratórios, convênios, portais de marcação de consultas) realizem operações de agendamento sem acesso direto ao banco de dados interno.

O contrato da API é definido pelo **WSDL gerado automaticamente** pelo runtime JAX-WS, garantindo tipagem forte e sem ambiguidade para todos os consumidores do serviço.

| Atributo | Detalhe |
|---|---|
| Protocolo | SOAP 1.1 com envelope XML |
| Linguagem | Java 17 (JAX-WS — `javax.jws`) |
| Servidor HTTP | Embutido na JDK (`Endpoint.publish`) |
| Porta padrão | `8080` |
| Endpoint | `http://localhost:8080/hospital/agendamento` |
| WSDL | `http://localhost:8080/hospital/agendamento?wsdl` |
| Persistência | Repositórios em memória (`HashMap`) |
| Gerenciador de deps. | Maven (`pom.xml`) |

---

## 🔧 Problemas Resolvidos

### 1. Contrato explícito e sem ambiguidade
O WSDL gerado automaticamente pelo JAX-WS funciona como **contrato vivo**: qualquer cliente pode inspecioná-lo e gerar stubs sem comunicação adicional entre equipes, eliminando a ambiguidade comum em integrações REST sem documentação formal.

### 2. Validação estrutural automática
O envelope SOAP é validado pelo runtime JAX-WS **antes mesmo de chegar à lógica de negócio**. Campos obrigatórios ausentes ou com tipo errado geram `Fault SOAP` padronizado — não um erro HTTP 500 genérico — facilitando o diagnóstico pelo consumidor.

### 3. Resposta sempre estruturada
Toda operação retorna um `AgendamentoResponse` contendo `sucesso` (boolean), `mensagem`, `codigoErro` e `dados`. O cliente **nunca** recebe uma exceção Java crua; sempre recebe uma resposta previsível e tratável programaticamente.

### 4. Isolamento do banco de dados
Sistemas externos nunca acessam o banco diretamente. Todas as operações passam pelo contrato SOAP, garantindo que regras de negócio (ex.: impossibilidade de cancelar consulta já realizada) sejam sempre aplicadas, independentemente do cliente.

---

## 🛠 Tecnologias

- **Java 17**
- **JAX-WS** (`javax.xml.ws.Endpoint`) — publicação do WebService
- **Maven** — gerenciamento de dependências e build
- **HTTP puro** (`HttpURLConnection`) — cliente SOAP sem frameworks externos

---

## 📁 Estrutura do Projeto

```
cp-SOAP-main/
└── hospital-soap/
    ├── pom.xml
    └── src/main/java/br/com/hospital/soap/
        ├── publisher/
        │   └── ServicePublisher.java        ← sobe o servidor SOAP
        ├── service/
        │   └── AgendamentoService.java      ← WebService com 8 operações
        ├── client/
        │   └── AgendamentoClient.java       ← cliente HTTP puro + demo completa
        ├── model/
        │   ├── Paciente.java
        │   ├── Consulta.java                ← enum StatusConsulta
        │   └── AgendamentoResponse.java     ← envelope de resposta padronizado
        └── repository/
            ├── PacienteRepository.java
            └── ConsultaRepository.java
```

---

## 📡 Operações Disponíveis

### Pacientes

| Operação | SOAPAction | Descrição |
|---|---|---|
| `cadastrarPaciente` | `cadastrarPaciente` | Cadastra ou atualiza paciente (upsert por CPF) |
| `buscarPacientePorId` | `buscarPacientePorId` | Retorna dados de um paciente pelo ID |
| `listarPacientes` | `listarPacientes` | Lista todos os pacientes cadastrados |

### Consultas

| Operação | SOAPAction | Descrição |
|---|---|---|
| `agendarConsulta` | `agendarConsulta` | Agenda nova consulta para um paciente |
| `confirmarConsulta` | `confirmarConsulta` | Confirma consulta no status AGENDADA |
| `cancelarConsulta` | `cancelarConsulta` | Cancela consulta (exceto já realizadas) |
| `listarConsultasPorPaciente` | `listarConsultasPorPaciente` | Lista consultas de um paciente |
| `listarConsultasPorStatus` | `listarConsultasPorStatus` | Filtra consultas por status |

---

## 📐 Regras de Negócio

### Pacientes
- **Nome** é obrigatório → erro `VAL001`
- **CPF** deve estar no formato `000.000.000-00` → erro `VAL002`
- Se o CPF já existe, o cadastro realiza **upsert** (atualiza os dados do paciente existente)

### Consultas
- **`pacienteId`** é obrigatório → erro `VAL003`
- **`medico`** é obrigatório → erro `VAL004`
- **`dataHora`** deve seguir o formato `yyyy-MM-ddTHH:mm` (ex.: `2026-05-10T09:00`) → erro `VAL005`
- Status inválido → erro `VAL006` (valores aceitos: `AGENDADA`, `CONFIRMADA`, `CANCELADA`, `REALIZADA`)

### Ciclo de vida de uma consulta

```
AGENDADA ──► CONFIRMADA ──► REALIZADA
    │              │
    └──────────────┴──► CANCELADA
```

| Transição | Permitida? |
|---|---|
| AGENDADA → CONFIRMADA | ✅ |
| AGENDADA → CANCELADA | ✅ |
| CONFIRMADA → CANCELADA | ✅ |
| CANCELADA → CONFIRMADA | ❌ erro `CON400` |
| REALIZADA → CANCELADA | ❌ erro `CON400` |

---

## ✅ Boas Práticas Aplicadas

### Servidor (`AgendamentoService`)
- `@WebService` com `targetNamespace` explícito — controla o namespace gerado no WSDL, evitando nomes default não controlados
- `@WebParam` nomeia cada parâmetro — o WSDL gerado mostra `nome`, `cpf`, `dataHora` em vez de `arg0`, `arg1`
- Toda operação retorna `AgendamentoResponse` — o cliente nunca recebe uma exceção Java crua
- Validações de negócio antes de persistir — CPF com regex, datas com pattern, campos obrigatórios checados no início do método
- **Upsert** por CPF — evita duplicatas e simplifica a experiência do integrador
- Separação clara de responsabilidades — `Service` orquestra regras de negócio; `Repository` isola acesso a dados
- **Shutdown hook** no `ServicePublisher` — o endpoint é encerrado graciosamente ao receber `SIGTERM` ou `Ctrl+C`

### Cliente (`AgendamentoClient`)
- **HTTP puro** (`HttpURLConnection`) — demonstra exatamente o que trafega na rede sem abstração de framework
- Método genérico `sendRequest(action, body)` — toda a lógica HTTP/SOAP em um lugar; os métodos de negócio apenas montam o XML do body
- Constantes para `ENDPOINT` e `NAMESPACE` — manutenção centralizada; trocar de ambiente muda apenas duas linhas
- Tratamento separado de erros HTTP (`>= 400`) — usa `getErrorStream()` em vez de `getInputStream()`, evitando `NullPointerException` silencioso
- **Timeouts** configurados (`connect 5s`, `read 10s`) — evita que o cliente fique bloqueado indefinidamente
- `main()` de demonstração completa — executa todas as operações em sequência, incluindo casos de erro controlados

---

## ▶️ Como Executar

### Pré-requisitos
- Java 17+
- Maven 3.8+

### 1. Compilar

```bash
cd hospital-soap
mvn clean package -q
```

### 2. Iniciar o servidor

```bash
mvn exec:java -Dexec.mainClass="br.com.hospital.soap.publisher.ServicePublisher"
```

Output esperado:
```
============================================================
  HOSPITAL SOAP WebService - Agendamento
============================================================
  Endpoint : http://localhost:8080/hospital/agendamento
  WSDL     : http://localhost:8080/hospital/agendamento?wsdl
============================================================
  Pressione Ctrl+C para encerrar.
```

### 3. Executar o cliente (demo completa)

Em outro terminal, com o servidor rodando:

```bash
mvn exec:java -Dexec.mainClass="br.com.hospital.soap.client.AgendamentoClient"
```

O cliente executa automaticamente 10 operações em sequência, incluindo casos de sucesso e de erro controlado.

---

## 🧪 Testando com Postman

**Configuração:**

| Campo | Valor |
|---|---|
| Method | `POST` |
| URL | `http://localhost:8080/hospital/agendamento` |
| Content-Type | `text/xml;charset=UTF-8` |
| SOAPAction | `"cadastrarPaciente"` |
| Body | `raw → XML` |

**Exemplo — Cadastrar Paciente:**

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ser="http://service.hospital.com.br/">
   <soapenv:Header/>
   <soapenv:Body>
      <ser:cadastrarPaciente>
         <nome>João Silva</nome>
         <cpf>123.456.789-00</cpf>
         <dataNascimento>1990-05-10</dataNascimento>
         <telefone>11999999999</telefone>
         <email>joao@email.com</email>
      </ser:cadastrarPaciente>
   </soapenv:Body>
</soapenv:Envelope>
```

**Exemplo — Agendar Consulta:**

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ser="http://service.hospital.com.br/">
   <soapenv:Header/>
   <soapenv:Body>
      <ser:agendarConsulta>
         <pacienteId>1</pacienteId>
         <medico>Dr. Carlos Melo</medico>
         <especialidade>Neurologia</especialidade>
         <dataHora>2026-05-10T09:00</dataHora>
         <observacoes>Dor de cabeça recorrente</observacoes>
      </ser:agendarConsulta>
   </soapenv:Body>
</soapenv:Envelope>
```

> 💡 **Dica:** Muitos serviços SOAP com Spring Boot / JAX-WS embutido não exigem o header `SOAPAction`. Se receber erro, tente a requisição sem ele.

---

## 🚀 Próximas Features

### Curto prazo
- [ ] **Banco de dados real** — substituir repositórios em memória por JPA/Hibernate com H2 (dev) e PostgreSQL (produção)
- [ ] **Configuração externalizada** — mover host, porta e path para `application.properties`
- [ ] **Logging estruturado** — adicionar SLF4J/Logback para registrar cada chamada SOAP com timestamp e tempo de resposta

### Médio prazo
- [ ] **Autenticação WS-Security** — adicionar `UsernameToken` no header SOAP para autenticar consumidores externos
- [ ] **Spring Boot + Apache CXF** — migrar o servidor para um framework gerenciado, ganhando auto-configuração, health check e métricas
- [ ] **Geração de stubs com `wsimport`** — disponibilizar client stub Java gerado a partir do WSDL para parceiros integradores
- [ ] **Bean Validation (JSR-380)** — usar `@NotNull`, `@Pattern` e `@Size` diretamente nos parâmetros do `@WebMethod`

### Longo prazo
- [ ] **Gateway de integração** — publicar o serviço atrás de um ESB (Apache Camel / MuleSoft) para roteamento e transformação de mensagens
- [ ] **API REST paralela** — expor as mesmas operações via REST/JSON, mantendo o SOAP para sistemas legados
- [ ] **Dashboard de monitoramento** — painel com volume de chamadas, taxa de erro e latência média por operação
