package br.edu.cesumar.agendapro.web;

import br.edu.cesumar.agendapro.dominio.Cliente;
import br.edu.cesumar.agendapro.dominio.Profissional;
import br.edu.cesumar.agendapro.dominio.RecuperacaoSenha;
import br.edu.cesumar.agendapro.dominio.Usuario;
import br.edu.cesumar.agendapro.dominio.regras.ValidacaoException;
import br.edu.cesumar.agendapro.repositorio.ClienteRepository;
import br.edu.cesumar.agendapro.repositorio.ProfissionalRepository;
import br.edu.cesumar.agendapro.servico.NotificacaoService;
import br.edu.cesumar.agendapro.servico.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Entrada e saida do sistema e recuperacao de senha (RF02 e RF18). */
@Controller
public class AutenticacaoController {

    private final UsuarioService usuarios;
    private final ClienteRepository clientes;
    private final ProfissionalRepository profissionais;
    private final NotificacaoService notificacoes;

    public AutenticacaoController(UsuarioService usuarios,
                                  ClienteRepository clientes,
                                  ProfissionalRepository profissionais,
                                  NotificacaoService notificacoes) {
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.profissionais = profissionais;
        this.notificacoes = notificacoes;
    }

    @GetMapping("/login")
    public String formulario(@RequestParam(required = false) String saiu,
                             @RequestParam(required = false) String expirou,
                             Model model,
                             HttpServletRequest request) {
        if (SessaoAtual.de(request) != null) {
            return "redirect:/";
        }
        if (saiu != null) {
            model.addAttribute("aviso", "Voce saiu do sistema.");
        }
        if (expirou != null) {
            model.addAttribute("aviso", "Sua sessao expirou. Entre novamente.");
        }
        return "autenticacao/login";
    }

    @PostMapping("/login")
    public String entrar(@RequestParam(defaultValue = "") String email,
                         @RequestParam(defaultValue = "") String senha,
                         Model model,
                         HttpServletRequest request) {
        Optional<Usuario> achado = usuarios.autenticar(email, senha);
        if (achado.isEmpty()) {
            model.addAttribute("email", email);
            model.addAttribute("erro", "E-mail ou senha incorretos. Verifique os dados e tente novamente.");
            return "autenticacao/login";
        }

        Usuario usuario = achado.get();
        Long idCliente = clientes.findByIdUsuario(usuario.getId()).map(Cliente::getId).orElse(null);
        Long idProfissional = profissionais.findByIdUsuario(usuario.getId())
                .map(Profissional::getId).orElse(null);

        SessaoAtual.gravar(request, new UsuarioSessao(usuario.getId(), usuario.getNome(),
                usuario.getEmail(), usuario.getPerfil(), idCliente, idProfissional));
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String sair(HttpServletRequest request) {
        SessaoAtual.encerrar(request);
        return "redirect:/login?saiu";
    }

    @GetMapping("/sem-acesso")
    public String semAcesso() {
        return "autenticacao/sem-acesso";
    }

    // ------------------------------------------------------------ RF18
    @GetMapping("/recuperar-senha")
    public String formularioRecuperacao() {
        return "autenticacao/recuperar-senha";
    }

    @PostMapping("/recuperar-senha")
    public String solicitarRecuperacao(@RequestParam(defaultValue = "") String email, Model model) {
        Optional<RecuperacaoSenha> pedido = usuarios.solicitarRedefinicao(email);
        pedido.ifPresent(notificacoes::registrarRecuperacaoSenha);

        // A resposta e sempre a mesma, exista ou nao a conta, para nao revelar
        // quais e-mails estao cadastrados.
        model.addAttribute("enviado", true);
        model.addAttribute("validade", UsuarioService.HORAS_VALIDADE_TOKEN);
        pedido.ifPresent(p -> model.addAttribute("link", "/redefinir-senha?token=" + p.getToken()));
        return "autenticacao/recuperar-senha";
    }

    @GetMapping("/redefinir-senha")
    public String formularioRedefinicao(@RequestParam(defaultValue = "") String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("valido", usuarios.pedidoValido(token).isPresent());
        return "autenticacao/redefinir-senha";
    }

    @PostMapping("/redefinir-senha")
    public String redefinir(@RequestParam(defaultValue = "") String token,
                            @RequestParam(defaultValue = "") String senha,
                            @RequestParam(defaultValue = "") String confirmacao,
                            Model model) {
        try {
            usuarios.redefinirSenha(token, senha, confirmacao);
            model.addAttribute("aviso", "Senha redefinida com sucesso. Entre com a nova senha.");
            return "autenticacao/login";
        } catch (ValidacaoException e) {
            model.addAttribute("token", token);
            model.addAttribute("valido", usuarios.pedidoValido(token).isPresent());
            model.addAttribute("erro", Mensagens.resumo(e));
            return "autenticacao/redefinir-senha";
        }
    }
}
