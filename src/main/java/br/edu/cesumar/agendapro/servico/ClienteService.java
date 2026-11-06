package br.edu.cesumar.agendapro.servico;

import br.edu.cesumar.agendapro.dominio.Cliente;
import br.edu.cesumar.agendapro.dominio.regras.RegrasCliente;
import br.edu.cesumar.agendapro.repositorio.ClienteRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Regras de aplicacao do cadastro de clientes (RF01). */
@Service
public class ClienteService {

    private final ClienteRepository repositorio;

    public ClienteService(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Cliente> listar(String termo, boolean somenteAtivos) {
        if (termo == null || termo.isBlank()) {
            return somenteAtivos ? repositorio.findByAtivoTrueOrderByNome() : repositorio.findAll();
        }
        return repositorio.buscar(termo.trim(), somenteAtivos);
    }

    public Cliente porId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente nao encontrado: " + id));
    }

    @Transactional
    public Cliente salvar(Cliente cliente) {
        List<RegrasCliente.ClienteExistente> cadastrados = repositorio.findAll().stream()
                .map(c -> new RegrasCliente.ClienteExistente(c.getId(), c.getEmail(), c.getTelefone(), c.isAtivo()))
                .toList();

        RegrasCliente.validarOuFalhar(cliente.getId(), cliente.getNome(), cliente.getTelefone(),
                cliente.getEmail(), cliente.getDataNascimento(), cadastrados);

        cliente.setNome(cliente.getNome().trim());
        cliente.setTelefone(cliente.getTelefone().trim());
        cliente.setEmail(cliente.getEmail() == null || cliente.getEmail().isBlank()
                ? null : cliente.getEmail().trim().toLowerCase());
        return repositorio.save(cliente);
    }

    /** RN12: o cadastro nao e excluido, apenas inativado. */
    @Transactional
    public void inativar(Long id) {
        Cliente cliente = porId(id);
        cliente.setAtivo(false);
        repositorio.save(cliente);
    }

    @Transactional
    public void reativar(Long id) {
        Cliente cliente = porId(id);
        cliente.setAtivo(true);
        repositorio.save(cliente);
    }
}
