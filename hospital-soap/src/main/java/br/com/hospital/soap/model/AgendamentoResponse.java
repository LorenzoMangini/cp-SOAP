package br.com.hospital.soap.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Envelope de resposta padronizado para todas as operações do WebService.
 *
 * Boas práticas aplicadas:
 * - Resposta sempre encapsula sucesso/erro (nunca lança exceção genérica ao cliente)
 * - Campo "mensagem" humaniza o retorno para o sistema consumidor
 * - Campo "dados" carrega o objeto de negócio quando aplicável
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AgendamentoResponse {

    private boolean sucesso;
    private String mensagem;
    private String codigoErro;   // null quando sucesso = true
    private Object dados;        // Consulta, Paciente, List<?>, etc.

    // Construtor padrão obrigatório para o JAXB
    public AgendamentoResponse() {}

    // Factory methods para padronizar criação de respostas
    public static AgendamentoResponse sucesso(String mensagem, Object dados) {
        AgendamentoResponse r = new AgendamentoResponse();
        r.sucesso = true;
        r.mensagem = mensagem;
        r.dados = dados;
        return r;
    }

    public static AgendamentoResponse erro(String codigoErro, String mensagem) {
        AgendamentoResponse r = new AgendamentoResponse();
        r.sucesso = false;
        r.codigoErro = codigoErro;
        r.mensagem = mensagem;
        return r;
    }

    public boolean isSucesso() { return sucesso; }
    public void setSucesso(boolean sucesso) { this.sucesso = sucesso; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public String getCodigoErro() { return codigoErro; }
    public void setCodigoErro(String codigoErro) { this.codigoErro = codigoErro; }

    public Object getDados() { return dados; }
    public void setDados(Object dados) { this.dados = dados; }
}
