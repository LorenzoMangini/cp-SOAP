package br.com.hospital.soap.repository;

import br.com.hospital.soap.model.Paciente;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Repositório em memória para Pacientes.
 *
 * Boas práticas aplicadas:
 * - ConcurrentHashMap garante thread-safety sem sincronização manual
 * - AtomicLong para IDs evita race conditions em ambiente multi-thread
 * - Retornos com Optional forçam o chamador a tratar ausência de dados
 * - Dados de exemplo pré-carregados para facilitar testes
 *
 * Próxima feature: substituir por JPA/Hibernate com banco relacional
 */
public class PacienteRepository {

    private final Map<Long, Paciente> store = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    // Instância singleton (sem Spring, gerenciamos manualmente)
    private static final PacienteRepository INSTANCE = new PacienteRepository();

    private PacienteRepository() {
        carregarDadosExemplo();
    }

    public static PacienteRepository getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    public Paciente salvar(Paciente paciente) {
        if (paciente.getId() == null) {
            paciente.setId(idSequence.getAndIncrement());
        }
        store.put(paciente.getId(), paciente);
        return paciente;
    }

    public Optional<Paciente> buscarPorId(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<Paciente> buscarPorCpf(String cpf) {
        return store.values().stream()
                .filter(p -> p.getCpf().equals(cpf))
                .findFirst();
    }

    public List<Paciente> listarTodos() {
        return new ArrayList<>(store.values());
    }

    public boolean remover(Long id) {
        return store.remove(id) != null;
    }

    // -------------------------------------------------------------------------
    // Dados de exemplo
    // -------------------------------------------------------------------------

    private void carregarDadosExemplo() {
        salvar(new Paciente(null, "Ana Lima",       "111.111.111-11",
                "1990-05-14", "(11) 99999-0001", "ana.lima@email.com"));
        salvar(new Paciente(null, "Bruno Souza",    "222.222.222-22",
                "1985-11-30", "(11) 99999-0002", "bruno.souza@email.com"));
        salvar(new Paciente(null, "Carla Mendes",   "333.333.333-33",
                "2000-02-28", "(11) 99999-0003", "carla.mendes@email.com"));
        salvar(new Paciente(null, "Diego Ferreira", "444.444.444-44",
                "1978-07-07", "(11) 99999-0004", "diego.f@email.com"));
    }
}
