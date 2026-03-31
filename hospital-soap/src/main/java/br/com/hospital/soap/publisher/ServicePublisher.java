package br.com.hospital.soap.publisher;

import br.com.hospital.soap.service.AgendamentoService;

import javax.xml.ws.Endpoint;

/**
 * Ponto de entrada do servidor SOAP.
 *
 * Endpoint.publish() cria automaticamente:
 *   - Um servidor HTTP embutido (light-http da JDK)
 *   - O endpoint SOAP vinculado à URL fornecida
 *   - A geração do WSDL acessível em ?wsdl
 *
 * Boas práticas aplicadas:
 *   - URL e porta externalizadas como constantes (próximo passo: arquivo .properties)
 *   - Shutdown hook garante que o endpoint é encerrado graciosamente ao parar a JVM
 *   - Log de inicialização informa ao operador o endereço do WSDL
 */
public class ServicePublisher {

    private static final String HOST = "http://localhost";
    private static final int    PORT = 8080;
    private static final String PATH = "/hospital/agendamento";

    public static void main(String[] args) {
        String url = HOST + ":" + PORT + PATH;

        Endpoint endpoint = Endpoint.publish(url, new AgendamentoService());

        // Shutdown hook: encerra o endpoint ao receber SIGTERM ou Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[ServicePublisher] Encerrando endpoint SOAP...");
            endpoint.stop();
            System.out.println("[ServicePublisher] Servico encerrado.");
        }));

        System.out.println("=".repeat(60));
        System.out.println("  HOSPITAL SOAP WebService - Agendamento");
        System.out.println("=".repeat(60));
        System.out.println("  Endpoint : " + url);
        System.out.println("  WSDL     : " + url + "?wsdl");
        System.out.println("=".repeat(60));
        System.out.println("  Pressione Ctrl+C para encerrar.");
    }
}
