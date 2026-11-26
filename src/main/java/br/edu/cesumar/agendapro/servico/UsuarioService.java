package br.edu.cesumar.agendapro.servico;

import br.edu.cesumar.agendapro.dominio.RecuperacaoSenha;
import br.edu.cesumar.agendapro.dominio.Usuario;
import br.edu.cesumar.agendapro.dominio.regras.ErroValidacao;
import br.edu.cesumar.agendapro.dominio.regras.Perfil;
import br.edu.cesumar.agendapro.dominio.regras.Senha;
import br.edu.cesumar.agendapro.dominio.regras.ValidacaoException;
import br.edu.cesumar.agendapro.repositorio.RecuperacaoSenhaRepository;
import br.edu.cesumar.agendapro.repositorio.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Contas de acesso, autenticacao e recuperacao de senha (RF02, RF17, RF18). */
@Service
public class UsuarioService {

    public static final int HORAS_VALIDADE_TOKEN = 2;

    private final UsuarioRepository repositorio;
    private final RecuperacaoSenhaRepository recuperacoes;

    public UsuarioService(UsuarioRepository repositorio, RecuperacaoSenhaRepository recuperacoes) {
        this.repositorio = repositorio;
        this.recuperacoes = recuperacoes;
    }

    public List<Usuario> listar() {
        return repositorio.findAllByOrderByNome();
    }

    public Usuario porId(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado: " + id));
    }

    /**
     * RF02: confere e-mail e senha. Devolve vazio quando as credenciais nao
     * conferem ou a conta esta inativa, sem dizer qual dos dois, para nao
     * revelar quais e-mails existem.
     */
    public Optional<Usuario> autenticar(String email, String senha) {
        if (email == null || senha == null) {
            return Optional.empty();
        }
        Optional<Usuario> achado = repositorio.findByEmailIgnoreCase(email.trim());
        if (achado.isEmpty()) {
            return Optional.empty();
        }
        Usuario usuario = achado.get();
        if (!usuario.isAtivo() || !Senha.confere(senha, usuario.getSenhaHash())) {
            return Optional.empty();
        }
        usuario.setUltimoAcesso(java.time.LocalDateTime.now());
        repositorio.save(usuario);
        return Optional.of(usuario);
    }

    /** RF17: cria a conta de acesso. RN07: o e-mail nao pode repetir. */
    @Transactional
    public Usuario criar(String nome, String email, String senha, String confirmacao, Perfil perfil) {
        List<ErroValidacao> erros = new ArrayList<>();

        String nomeLimpo = nome == null ? "" : nome.trim();
        String emailLimpo = email == null ? "" : email.trim().toLowerCase();

        if (nomeLimpo.length() < 3) {
            erros.add(ErroValidacao.de("nome", "Informe o nome do usuario."));
        }
        if (!emailLimpo.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            erros.add(ErroValidacao.de("email", "Informe um e-mail valido."));
        } else if (repositorio.findByEmailIgnoreCase(emailLimpo).isPresent()) {
            erros.add(ErroValidacao.de("email", "Ja existe uma conta com este e-mail.", "RN07"));
        }
        if (perfil == null) {
            erros.add(ErroValidacao.de("perfil", "Selecione o perfil de acesso."));
        }
        erros.addAll(Senha.validar(senha, confirmacao));

        if (!erros.isEmpty()) {
            throw new ValidacaoException(erros);
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nomeLimpo);
        usuario.setEmail(emailLimpo);
        usuario.setSenhaHash(Senha.cifrar(senha));
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        usuario.setDataCadastro(LocalDateTime.now());
        return repositorio.save(usuario);
    }

    /** RF17: inativar bloqueia o acesso sem apagar o registro (RN12). */
    @Transactional
    public void inativar(Long id) {
        Usuario usuario = porId(id);
        usuario.setAtivo(false);
        repositorio.save(usuario);
    }

    @Transactional
    public void reativar(Long id) {
        Usuario usuario = porId(id);
        usuario.setAtivo(true);
        repositorio.save(usuario);
    }

    /**
     * RF18: cria o pedido de redefinicao. Devolve vazio quando o e-mail nao
     * existe, e a tela responde sempre a mesma coisa, para nao revelar quais
     * e-mails estao cadastrados.
     */
    @Transactional
    public Optional<RecuperacaoSenha> solicitarRedefinicao(String email) {
        Optional<Usuario> achado = email == null
                ? Optional.empty() : repositorio.findByEmailIgnoreCase(email.trim());
        if (achado.isEmpty() || !achado.get().isAtivo()) {
            return Optional.empty();
        }
        RecuperacaoSenha pedido = new RecuperacaoSenha();
        pedido.setUsuario(achado.get());
        pedido.setToken(Senha.tokenAleatorio());
        pedido.setCriadoEm(LocalDateTime.now());
        pedido.setExpiraEm(LocalDateTime.now().plusHours(HORAS_VALIDADE_TOKEN));
        pedido.setUsado(false);
        return Optional.of(recuperacoes.save(pedido));
    }

    /** O token so vale uma vez e dentro do prazo. */
    public Optional<RecuperacaoSenha> pedidoValido(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return recuperacoes.findByToken(token)
                .filter(p -> !p.isUsado())
                .filter(p -> p.getExpiraEm().isAfter(LocalDateTime.now()));
    }

    /** RF18: grava a nova senha e consome o token. */
    @Transactional
    public void redefinirSenha(String token, String senha, String confirmacao) {
        Optional<RecuperacaoSenha> achado = pedidoValido(token);
        if (achado.isEmpty()) {
            throw new ValidacaoException(List.of(ErroValidacao.de("token",
                    "Este link de redefinicao expirou ou ja foi utilizado. Solicite outro.")));
        }
        Senha.validarOuFalhar(senha, confirmacao);

        RecuperacaoSenha pedido = achado.get();
        Usuario usuario = pedido.getUsuario();
        usuario.setSenhaHash(Senha.cifrar(senha));
        repositorio.save(usuario);

        pedido.setUsado(true);
        recuperacoes.save(pedido);
    }

    /** Troca de senha pelo proprio usuario, conferindo a senha atual. */
    @Transactional
    public void trocarSenha(Long idUsuario, String senhaAtual, String nova, String confirmacao) {
        Usuario usuario = porId(idUsuario);
        if (!Senha.confere(senhaAtual, usuario.getSenhaHash())) {
            throw new ValidacaoException(List.of(
                    ErroValidacao.de("senhaAtual", "A senha atual nao confere.")));
        }
        Senha.validarOuFalhar(nova, confirmacao);
        usuario.setSenhaHash(Senha.cifrar(nova));
        repositorio.save(usuario);
    }
}
