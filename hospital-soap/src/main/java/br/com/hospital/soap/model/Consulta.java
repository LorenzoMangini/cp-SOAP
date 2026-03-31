package br.com.hospital.soap.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Representa uma consulta médica agendada.
 * StatusConsulta define o ciclo de vida da consulta no sistema.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Consulta {

    public enum StatusConsulta {
        AGENDADA,
        CONFIRMADA,
        CANCELADA,
        REALIZADA
    }

    private Long id;
    private Long pacienteId;
    private String nomePaciente;        // desnormalizado para facilitar exibição
    private String medico;
    private String especialidade;
    private String dataHora;            // formato ISO: yyyy-MM-dd'T'HH:mm
    private String observacoes;
    private StatusConsulta status;

    // Construtor padrão obrigatório para o JAXB
    public Consulta() {}

    public Consulta(Long id, Long pacienteId, String nomePaciente,
                    String medico, String especialidade,
                    String dataHora, String observacoes) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.nomePaciente = nomePaciente;
        this.medico = medico;
        this.especialidade = especialidade;
        this.dataHora = dataHora;
        this.observacoes = observacoes;
        this.status = StatusConsulta.AGENDADA;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public String getNomePaciente() { return nomePaciente; }
    public void setNomePaciente(String nomePaciente) { this.nomePaciente = nomePaciente; }

    public String getMedico() { return medico; }
    public void setMedico(String medico) { this.medico = medico; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    public String getDataHora() { return dataHora; }
    public void setDataHora(String dataHora) { this.dataHora = dataHora; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public StatusConsulta getStatus() { return status; }
    public void setStatus(StatusConsulta status) { this.status = status; }

    @Override
    public String toString() {
        return "Consulta{id=" + id + ", paciente='" + nomePaciente
                + "', medico='" + medico + "', dataHora='" + dataHora
                + "', status=" + status + "}";
    }
}
