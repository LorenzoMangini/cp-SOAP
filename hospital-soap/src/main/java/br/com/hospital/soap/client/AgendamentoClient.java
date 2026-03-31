package br.com.hospital.soap.client;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Cliente SOAP HTTP puro para o AgendamentoHospitalarService.
 *
 * Por que HTTP puro e não wsimport / stub gerado?
 *   - Demonstra exatamente o que trafega na rede (envelope SOAP cru)
 *   - Não exige compilação do stub a partir do WSDL
 *   - Útil para entender o protocolo antes de usar frameworks
 *
 * Boas práticas aplicadas:
 *   - Constantes para namespace e endpoint (manutenção centralizada)
 *   - Método genérico sendRequest(action, body) evita repetição de código HTTP
 *   - Cada operação de negócio tem seu próprio método, ocultando XML do chamador
 *   - Tratamento de erros HTTP separado da lógica de negócio
 */
public class AgendamentoClient {

    private static final String ENDPOINT  = "http://localhost:8080/hospital/agendamento";
    private static final String NAMESPACE = "http://service.hospital.com.br/";

    // =========================================================================
    // Métodos de negócio públicos
    // =========================================================================

    public static void cadastrarPaciente(String nome, String cpf,
                                          String dataNascimento,
                                          String telefone, String email) throws Exception {
        String body = """
                <ser:cadastrarPaciente>
                    <nome>%s</nome>
                    <cpf>%s</cpf>
                    <dataNascimento>%s</dataNascimento>
                    <telefone>%s</telefone>
                    <email>%s</email>
                </ser:cadastrarPaciente>
                """.formatted(nome, cpf, dataNascimento, telefone, email);

        String resposta = sendRequest("cadastrarPaciente", body);
        printResultado("cadastrarPaciente", resposta);
    }

    public static void listarPacientes() throws Exception {
        String body = "<ser:listarPacientes/>";
        String resposta = sendRequest("listarPacientes", body);
        printResultado("listarPacientes", resposta);
    }

    public static void buscarPacientePorId(long pacienteId) throws Exception {
        String body = """
                <ser:buscarPacientePorId>
                    <pacienteId>%d</pacienteId>
                </ser:buscarPacientePorId>
                """.formatted(pacienteId);

        String resposta = sendRequest("buscarPacientePorId", body);
        printResultado("buscarPacientePorId", resposta);
    }

    public static void agendarConsulta(long pacienteId, String medico,
                                        String especialidade, String dataHora,
                                        String observacoes) throws Exception {
        String body = """
                <ser:agendarConsulta>
                    <pacienteId>%d</pacienteId>
                    <medico>%s</medico>
                    <especialidade>%s</especialidade>
                    <dataHora>%s</dataHora>
                    <observacoes>%s</observacoes>
                </ser:agendarConsulta>
                """.formatted(pacienteId, medico, especialidade, dataHora, observacoes);

        String resposta = sendRequest("agendarConsulta", body);
        printResultado("agendarConsulta", resposta);
    }

    public static void confirmarConsulta(long consultaId) throws Exception {
        String body = """
                <ser:confirmarConsulta>
                    <consultaId>%d</consultaId>
                </ser:confirmarConsulta>
                """.formatted(consultaId);

        String resposta = sendRequest("confirmarConsulta", body);
        printResultado("confirmarConsulta", resposta);
    }

    public static void cancelarConsulta(long consultaId) throws Exception {
        String body = """
                <ser:cancelarConsulta>
                    <consultaId>%d</consultaId>
                </ser:cancelarConsulta>
                """.formatted(consultaId);

        String resposta = sendRequest("cancelarConsulta", body);
        printResultado("cancelarConsulta", resposta);
    }

    public static void listarConsultasPorPaciente(long pacienteId) throws Exception {
        String body = """
                <ser:listarConsultasPorPaciente>
                    <pacienteId>%d</pacienteId>
                </ser:listarConsultasPorPaciente>
                """.formatted(pacienteId);

        String resposta = sendRequest("listarConsultasPorPaciente", body);
        printResultado("listarConsultasPorPaciente", resposta);
    }

    public static void listarConsultasPorStatus(String status) throws Exception {
        String body = """
                <ser:listarConsultasPorStatus>
                    <status>%s</status>
                </ser:listarConsultasPorStatus>
                """.formatted(status);

        String resposta = sendRequest("listarConsultasPorStatus", body);
        printResultado("listarConsultasPorStatus", resposta);
    }

    // =========================================================================
    // Infraestrutura HTTP / SOAP
    // =========================================================================

    /**
     * Envia um request SOAP e retorna o body da resposta como String.
     *
     * @param action  nome da operação (usado no SOAPAction header)
     * @param body    elemento XML filho de soapenv:Body
     */
    private static String sendRequest(String action, String body) throws Exception {
        String envelope = buildEnvelope(body);

        URL url = new URL(ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
        conn.setRequestProperty("SOAPAction",   "\"" + NAMESPACE + action + "\"");
        conn.setConnectTimeout(5_000);
        conn.setReadTimeout(10_000);

        // Envia o envelope
        try (OutputStream os = conn.getOutputStream()) {
            os.write(envelope.getBytes(StandardCharsets.UTF_8));
        }

        int httpStatus = conn.getResponseCode();
        var stream = (httpStatus >= 400) ? conn.getErrorStream() : conn.getInputStream();

        String resposta = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

        if (httpStatus >= 400) {
            throw new RuntimeException("HTTP " + httpStatus + ": " + resposta);
        }
        return resposta;
    }

    /**
     * Monta o envelope SOAP 1.1 com o namespace do serviço.
     */
    private static String buildEnvelope(String body) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope
                    xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                    xmlns:ser="%s">
                    <soapenv:Header/>
                    <soapenv:Body>
                        %s
                    </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(NAMESPACE, body);
    }

    /**
     * Imprime o resultado da operação de forma legível no console.
     */
    private static void printResultado(String operacao, String xml) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("  Operação: " + operacao);
        System.out.println("-".repeat(60));
        // Exibe o XML indentado (simples: apenas a linha do <mensagem> e <sucesso>)
        for (String line : xml.split("><")) {
            String trimmed = line.trim();
            if (trimmed.contains("sucesso")
                    || trimmed.contains("mensagem")
                    || trimmed.contains("codigoErro")) {
                System.out.println("  " + (line.startsWith("<") ? line : "<" + line));
            }
        }
        // Imprime o XML completo para análise
        System.out.println("\n  [XML completo]");
        System.out.println(xml.replaceAll("><", ">\n<"));
    }

    // =========================================================================
    // Demo: executa todas as operações em sequência
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("  HOSPITAL SOAP Client - Demonstração Completa");
        System.out.println("=".repeat(60));

        try {
            // 1. Listar pacientes pré-carregados
            System.out.println("\n[1] Listando pacientes existentes...");
            listarPacientes();

            // 2. Cadastrar novo paciente
            System.out.println("\n[2] Cadastrando novo paciente...");
            cadastrarPaciente("Mariana Oliveira", "555.555.555-55",
                    "1995-03-20", "(11) 98888-1111", "mariana@email.com");

            // 3. Buscar paciente por ID
            System.out.println("\n[3] Buscando paciente ID=1...");
            buscarPacientePorId(1L);

            // 4. Buscar paciente inexistente (teste de erro)
            System.out.println("\n[4] Buscando paciente inexistente (ID=999)...");
            buscarPacientePorId(999L);

            // 5. Agendar nova consulta
            System.out.println("\n[5] Agendando consulta...");
            agendarConsulta(1L, "Dr. Carlos Melo", "Neurologia",
                    "2026-05-05T11:00", "Dor de cabeca recorrente");

            // 6. Confirmar consulta
            System.out.println("\n[6] Confirmando consulta ID=1...");
            confirmarConsulta(1L);

            // 7. Listar consultas do paciente 1
            System.out.println("\n[7] Consultas do paciente ID=1...");
            listarConsultasPorPaciente(1L);

            // 8. Listar consultas confirmadas
            System.out.println("\n[8] Consultas com status CONFIRMADA...");
            listarConsultasPorStatus("CONFIRMADA");

            // 9. Cancelar consulta
            System.out.println("\n[9] Cancelando consulta ID=2...");
            cancelarConsulta(2L);

            // 10. Tentativa de cancelar consulta ja cancelada (regra de negocio)
            System.out.println("\n[10] Tentando cancelar consulta ID=2 novamente (deve falhar)...");
            cancelarConsulta(2L);

        } catch (Exception e) {
            System.err.println("\n[ERRO] Verifique se o servidor esta rodando em " + ENDPOINT);
            System.err.println("       Detalhe: " + e.getMessage());
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("  Demonstração concluída.");
        System.out.println("=".repeat(60));
    }
}
