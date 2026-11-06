package br.edu.cesumar.agendapro.servico;

import br.edu.cesumar.agendapro.dominio.Servico;
import br.edu.cesumar.agendapro.dominio.regras.RegrasServico;
import br.edu.cesumar.agendapro.repositorio.ServicoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Regras de aplicacao do cadastro de servicos (RF04). */
@Service
public class ServicoService {

    private final ServicoRepository repositorio;

    public ServicoService(ServicoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Servico> listar() {
        return repositorio.findAllByOrderByNome();
    }

    public Servico porId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servico nao encontrado: " + id));
    }

    @Transactional
    public Servico salvar(Servico servico) {
        List<RegrasServico.ServicoExistente> cadastrados = repositorio.findAll().stream()
                .map(s -> new RegrasServico.ServicoExistente(s.getId(), s.getNome(), s.isAtivo()))
                .toList();

        RegrasServico.validarOuFalhar(servico.getId(), servico.getNome(),
                servico.getDuracaoMinutos(), servico.getPreco(), cadastrados);

        servico.setNome(servico.getNome().trim());
        return repositorio.save(servico);
    }

    /** RN12: o servico nao e excluido, apenas inativado. */
    @Transactional
    public void inativar(Long id) {
        Servico servico = porId(id);
        servico.setAtivo(false);
        repositorio.save(servico);
    }

    @Transactional
    public void reativar(Long id) {
        Servico servico = porId(id);
        servico.setAtivo(true);
        repositorio.save(servico);
    }
}
