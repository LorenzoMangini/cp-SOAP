package br.com.hospital.soap.service;

import br.com.hospital.soap.model.AgendamentoResponse;
import br.com.hospital.soap.model.Consulta;
import br.com.hospital.soap.model.Consulta.StatusConsulta;
import br.com.hospital.soap.model.Paciente;
import br.com.hospital.soap.repository.ConsultaRepository;
import br.com.hospital.soap.repository.PacienteRepository;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.List;
import java.util.Optional;

/**
 * WebService SOAP para o Sistema de Agendamento Hospitalar.
 *
 * Contexto de implantação:
 *   Hospitais e clínicas com sistemas legados (ex.: Tasy, MV) frequentemente
 *   utilizam SOAP como protocolo de integração. Este serviço atua como camada
 *   de integração, permitindo que sistemas externos agendem e consultem
 *   atendimentos sem acesso direto ao banco de dados.
 *
 * Problemas resolvidos:
 *   1. Integração padronizada via contrato WSDL (sem ambiguidade de contrato)
 *   2. Tipagem forte no XML (JAXB valida estrutura antes de processar)
 *   3. Operações atômicas com resposta envelope (sucesso/erro sempre estruturado)
 *
 * Boas práticas aplicadas:
 *   - @WebService com targetNamespace explícito para controle de namespace no WSDL
 *   - @WebParam nomeia cada parâmetro no XML (evita arg0, arg1 no WSDL gerado)
 *   - Toda operação retorna AgendamentoResponse (nunca lança exceção para o cliente)
 *   - Validações de negócio antes de persistir
 *   - Separação clara entre camada de serviço e repositório
 */
@WebService(
    serviceName  = "AgendamentoHospitalarService",
    portName     = "AgendamentoPort",
    targetNamespace = "http://service.hospital.com.br/"
)
public class AgendamentoService {

    private final PacienteRepository pacienteRepo = PacienteRepository.getInstance();
    private final ConsultaRepository consultaRepo  = ConsultaRepository.getInstance();

    // =========================================================================
    // OPERAÇÕES DE PACIENTE
    // =========================================================================

    /**
     * Cadastra um novo paciente ou atualiza um existente (upsert por CPF).
     */
    @WebMethod(operationName = "cadastrarPaciente")
    public AgendamentoResponse cadastrarPaciente(
            @WebParam(name = "nome")           String nome,
            @WebParam(name = "cpf")            String cpf,
            @WebParam(name = "dataNascimento") String dataNascimento,
            @WebParam(name = "telefone")       String telefone,
            @WebParam(name = "email")          String email
    ) {
        if (nome == null || nome.isBlank())
            return AgendamentoResponse.erro("VAL001", "Nome do paciente e obrigatorio.");
        if (cpf == null || !cpf.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}"))
            return AgendamentoResponse.erro("VAL002", "CPF invalido. Use o formato 000.000.000-00.");

        // Upsert: atualiza se CPF já existe
        Optional<Paciente> existente = pacienteRepo.buscarPorCpf(cpf);
        Paciente paciente = existente.orElse(new Paciente());
        paciente.setNome(nome);
        paciente.setCpf(cpf);
        paciente.setDataNascimento(dataNascimento);
        paciente.setTelefone(telefone);
        paciente.setEmail(email);

        pacienteRepo.salvar(paciente);

        String msg = existente.isPresent()
                ? "Paciente atualizado com sucesso. ID: " + paciente.getId()
                : "Paciente cadastrado com sucesso. ID: " + paciente.getId();
        return AgendamentoResponse.sucesso(msg, paciente);
    }

    /**
     * Retorna um paciente pelo ID.
     */
    @WebMethod(operationName = "buscarPacientePorId")
    public AgendamentoResponse buscarPacientePorId(
            @WebParam(name = "pacienteId") Long pacienteId
    ) {
        return pacienteRepo.buscarPorId(pacienteId)
                .map(p -> AgendamentoResponse.sucesso("Paciente encontrado.", p))
                .orElse(AgendamentoResponse.erro("PAC404", "Paciente nao encontrado. ID: " + pacienteId));
    }

    /**
     * Lista todos os pacientes cadastrados.
     */
    @WebMethod(operationName = "listarPacientes")
    public AgendamentoResponse listarPacientes() {
        List<Paciente> pacientes = pacienteRepo.listarTodos();
        return AgendamentoResponse.sucesso(
                "Total de pacientes: " + pacientes.size(), pacientes);
    }

    // =========================================================================
    // OPERAÇÕES DE CONSULTA
    // =========================================================================

    /**
     * Agenda uma nova consulta para o paciente informado.
     */
    @WebMethod(operationName = "agendarConsulta")
    public AgendamentoResponse agendarConsulta(
            @WebParam(name = "pacienteId")    Long   pacienteId,
            @WebParam(name = "medico")        String medico,
            @WebParam(name = "especialidade") String especialidade,
            @WebParam(name = "dataHora")      String dataHora,
            @WebParam(name = "observacoes")   String observacoes
    ) {
        if (pacienteId == null)
            return AgendamentoResponse.erro("VAL003", "pacienteId e obrigatorio.");
        if (medico == null || medico.isBlank())
            return AgendamentoResponse.erro("VAL004", "Nome do medico e obrigatorio.");
        if (dataHora == null || !dataHora.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}"))
            return AgendamentoResponse.erro("VAL005",
                    "dataHora invalida. Use o formato yyyy-MM-ddTHH:mm (ex.: 2026-04-10T09:00).");

        Optional<Paciente> paciente = pacienteRepo.buscarPorId(pacienteId);
        if (paciente.isEmpty())
            return AgendamentoResponse.erro("PAC404",
                    "Paciente nao encontrado. ID: " + pacienteId);

        Consulta consulta = new Consulta(
                null,
                pacienteId,
                paciente.get().getNome(),
                medico,
                especialidade,
                dataHora,
                observacoes
        );
        consultaRepo.salvar(consulta);

        return AgendamentoResponse.sucesso(
                "Consulta agendada com sucesso. ID: " + consulta.getId(), consulta);
    }

    /**
     * Confirma uma consulta previamente agendada.
     */
    @WebMethod(operationName = "confirmarConsulta")
    public AgendamentoResponse confirmarConsulta(
            @WebParam(name = "consultaId") Long consultaId
    ) {
        return consultaRepo.buscarPorId(consultaId)
                .map(c -> {
                    if (c.getStatus() == StatusConsulta.CANCELADA)
                        return AgendamentoResponse.erro("CON400",
                                "Nao e possivel confirmar uma consulta cancelada.");
                    c.setStatus(StatusConsulta.CONFIRMADA);
                    consultaRepo.salvar(c);
                    return AgendamentoResponse.sucesso("Consulta confirmada.", c);
                })
                .orElse(AgendamentoResponse.erro("CON404",
                        "Consulta nao encontrada. ID: " + consultaId));
    }

    /**
     * Cancela uma consulta agendada ou confirmada.
     */
    @WebMethod(operationName = "cancelarConsulta")
    public AgendamentoResponse cancelarConsulta(
            @WebParam(name = "consultaId") Long consultaId
    ) {
        return consultaRepo.buscarPorId(consultaId)
                .map(c -> {
                    if (c.getStatus() == StatusConsulta.REALIZADA)
                        return AgendamentoResponse.erro("CON400",
                                "Nao e possivel cancelar uma consulta ja realizada.");
                    c.setStatus(StatusConsulta.CANCELADA);
                    consultaRepo.salvar(c);
                    return AgendamentoResponse.sucesso("Consulta cancelada.", c);
                })
                .orElse(AgendamentoResponse.erro("CON404",
                        "Consulta nao encontrada. ID: " + consultaId));
    }

    /**
     * Lista todas as consultas de um paciente.
     */
    @WebMethod(operationName = "listarConsultasPorPaciente")
    public AgendamentoResponse listarConsultasPorPaciente(
            @WebParam(name = "pacienteId") Long pacienteId
    ) {
        if (pacienteRepo.buscarPorId(pacienteId).isEmpty())
            return AgendamentoResponse.erro("PAC404",
                    "Paciente nao encontrado. ID: " + pacienteId);

        List<Consulta> consultas = consultaRepo.listarPorPaciente(pacienteId);
        return AgendamentoResponse.sucesso(
                "Consultas encontradas: " + consultas.size(), consultas);
    }

    /**
     * Lista todas as consultas com determinado status.
     */
    @WebMethod(operationName = "listarConsultasPorStatus")
    public AgendamentoResponse listarConsultasPorStatus(
            @WebParam(name = "status") String status
    ) {
        try {
            StatusConsulta statusEnum = StatusConsulta.valueOf(status.toUpperCase());
            List<Consulta> consultas = consultaRepo.listarPorStatus(statusEnum);
            return AgendamentoResponse.sucesso(
                    "Consultas com status " + status + ": " + consultas.size(), consultas);
        } catch (IllegalArgumentException e) {
            return AgendamentoResponse.erro("VAL006",
                    "Status invalido. Use: AGENDADA, CONFIRMADA, CANCELADA ou REALIZADA.");
        }
    }
}
