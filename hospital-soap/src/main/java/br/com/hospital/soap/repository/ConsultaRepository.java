package br.com.hospital.soap.repository;

import br.com.hospital.soap.model.Consulta;
import br.com.hospital.soap.model.Consulta.StatusConsulta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Repositório em memória para Consultas.
 *
 * Boas práticas aplicadas:
 * - Mesma estratégia de ConcurrentHashMap + AtomicLong do PacienteRepository
 * - Consultas de negócio (por paciente, por status) centralizam a lógica de filtro
 * - Dados de exemplo referenciando IDs do PacienteRepository
 */
public class ConsultaRepository {

    private final Map<Long, Consulta> store = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    private static final ConsultaRepository INSTANCE = new ConsultaRepository();

    private ConsultaRepository() {
        carregarDadosExemplo();
    }

    public static ConsultaRepository getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    public Consulta salvar(Consulta consulta) {
        if (consulta.getId() == null) {
            consulta.setId(idSequence.getAndIncrement());
        }
        store.put(consulta.getId(), consulta);
        return consulta;
    }

    public Optional<Consulta> buscarPorId(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Consulta> listarTodas() {
        return new ArrayList<>(store.values());
    }

    public List<Consulta> listarPorPaciente(Long pacienteId) {
        return store.values().stream()
                .filter(c -> c.getPacienteId().equals(pacienteId))
                .collect(Collectors.toList());
    }

    public List<Consulta> listarPorStatus(StatusConsulta status) {
        return store.values().stream()
                .filter(c -> c.getStatus() == status)
                .collect(Collectors.toList());
    }

    public boolean remover(Long id) {
        return store.remove(id) != null;
    }

    // -------------------------------------------------------------------------
    // Dados de exemplo
    // -------------------------------------------------------------------------

    private void carregarDadosExemplo() {
        Consulta c1 = new Consulta(null, 1L, "Ana Lima",
                "Dr. Ricardo Nunes", "Cardiologia",
                "2026-04-10T09:00", "Retorno pos-exame");
        salvar(c1);

        Consulta c2 = new Consulta(null, 2L, "Bruno Souza",
                "Dra. Fernanda Costa", "Ortopedia",
                "2026-04-11T14:30", "Dor no joelho esquerdo");
        c2.setStatus(StatusConsulta.CONFIRMADA);
        salvar(c2);

        Consulta c3 = new Consulta(null, 1L, "Ana Lima",
                "Dra. Patricia Lima", "Dermatologia",
                "2026-04-15T10:00", "Avaliacao anual");
        salvar(c3);
    }
}
