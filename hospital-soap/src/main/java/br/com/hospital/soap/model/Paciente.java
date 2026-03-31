package br.com.hospital.soap.model;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Representa um paciente do sistema hospitalar.
 *
 * @XmlRootElement - permite serialização direta para XML pelo JAXB
 * @XmlAccessorType(XmlAccessType.FIELD) - JAXB acessa os campos diretamente,
 *   evitando problemas com getters/setters não convencionais
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Paciente {

    private Long id;
    private String nome;
    private String cpf;
    private String dataNascimento; // formato ISO: yyyy-MM-dd
    private String telefone;
    private String email;

    // Construtor padrão obrigatório para o JAXB
    public Paciente() {}

    public Paciente(Long id, String nome, String cpf,
                    String dataNascimento, String telefone, String email) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "Paciente{id=" + id + ", nome='" + nome + "', cpf='" + cpf + "'}";
    }
}
