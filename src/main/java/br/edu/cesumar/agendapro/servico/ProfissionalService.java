package br.edu.cesumar.agendapro.servico;

import br.edu.cesumar.agendapro.dominio.Profissional;
import br.edu.cesumar.agendapro.dominio.Servico;
import br.edu.cesumar.agendapro.dominio.regras.RegrasProfissional;
import br.edu.cesumar.agendapro.repositorio.ProfissionalRepository;
import br.edu.cesumar.agendapro.repositorio.ServicoRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Regras de aplicacao do cadastro de profissionais (RF03). */
@Service
public class ProfissionalService {

    private final ProfissionalRepository repositorio;
    private final ServicoRepository servicos;

    public ProfissionalService(ProfissionalRepository repositorio, ServicoRepository servicos) {
        this.repositorio = repositorio;
        this.servicos = servicos;
    }

    public List<Profissional> listar() {
        return repositorio.findAllByOrderByNome();
    }

    public List<Profissional> listarAtivos() {
        return repositorio.findByAtivoTrueOrderByNome();
    }

    /** RN06: profissionais ativos habilitados a executar o servico informado. */
    public List<Profissional> habilitadosPara(Long idServico) {
        return listarAtivos().stream().filter(p -> p.executa(idServico)).toList();
    }

    public Profissional porId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profissional nao encontrado: " + id));
    }

    @Transactional
    public Profissional salvar(Long id, String nome, String telefone, String email, List<Long> idServicos) {
        RegrasProfissional.validarOuFalhar(nome, telefone, idServicos);

        Profissional profissional = id == null ? new Profissional() : porId(id);
        profissional.setNome(nome.trim());
        profissional.setTelefone(telefone.trim());
        profissional.setEmail(email == null || email.isBlank() ? null : email.trim().toLowerCase());

        Set<Servico> selecionados = new LinkedHashSet<>(servicos.findAllById(idServicos));
        profissional.setServicos(selecionados);
        return repositorio.save(profissional);
    }

    /** RN12: o profissional nao e excluido, apenas inativado. */
    @Transactional
    public void inativar(Long id) {
        Profissional profissional = porId(id);
        profissional.setAtivo(false);
        repositorio.save(profissional);
    }

    @Transactional
    public void reativar(Long id) {
        Profissional profissional = porId(id);
        profissional.setAtivo(true);
        repositorio.save(profissional);
    }
}
