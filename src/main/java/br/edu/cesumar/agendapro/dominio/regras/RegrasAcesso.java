package br.edu.cesumar.agendapro.dominio.regras;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Controle de acesso por perfil (RNF02) e montagem do menu (RF17).
 * Classe de dominio puro: a mesma tabela decide o que aparece no menu e o que
 * o filtro de requisicoes libera, entao nao existe tela acessivel por URL que
 * o menu esconde.
 */
public final class RegrasAcesso {

    private RegrasAcesso() {
    }

    /** Uma area do sistema: o caminho, o rotulo no menu e quem pode entrar. */
    public record Area(String caminho, String rotulo, String grupo, Set<Perfil> perfis, boolean noMenu) {

        public String getCaminho() {
            return caminho;
        }

        public String getRotulo() {
            return rotulo;
        }

        public String getGrupo() {
            return grupo;
        }
    }

    private static final Set<Perfil> TODOS = Set.of(Perfil.ADMINISTRADOR, Perfil.PROFISSIONAL, Perfil.CLIENTE);
    private static final Set<Perfil> EQUIPE = Set.of(Perfil.ADMINISTRADOR, Perfil.PROFISSIONAL);
    private static final Set<Perfil> SO_ADMIN = Set.of(Perfil.ADMINISTRADOR);

    /**
     * Areas na ordem de verificacao: o caminho mais especifico vem primeiro,
     * porque a correspondencia e por prefixo e a primeira que casar decide.
     */
    private static final List<Area> AREAS = List.of(
            new Area("/agendamentos/novo", "Novo agendamento", "Atendimento", TODOS, true),
            new Area("/agendamentos", "Agendamentos", "Atendimento", TODOS, false),
            new Area("/meu-historico", "Meus atendimentos", "Atendimento", Set.of(Perfil.CLIENTE), true),
            new Area("/agenda", "Agenda", "Atendimento", EQUIPE, true),
            new Area("/clientes", "Clientes", "Cadastros", EQUIPE, true),
            new Area("/profissionais", "Profissionais", "Cadastros", SO_ADMIN, true),
            new Area("/servicos", "Servicos", "Cadastros", SO_ADMIN, true),
            new Area("/horarios", "Horarios", "Cadastros", EQUIPE, true),
            new Area("/relatorios", "Relatorios", "Administracao", SO_ADMIN, true),
            new Area("/notificacoes", "Notificacoes", "Administracao", SO_ADMIN, true),
            new Area("/usuarios", "Usuarios", "Administracao", SO_ADMIN, true),
            new Area("/", "Painel", "Atendimento", TODOS, true));

    /** Caminhos liberados sem autenticacao. */
    private static final List<String> PUBLICOS = List.of(
            "/login", "/logout", "/recuperar-senha", "/redefinir-senha", "/sem-acesso",
            "/css/", "/js/", "/imagens/", "/favicon.ico", "/error");

    public static boolean publico(String caminho) {
        String c = normalizar(caminho);
        for (String p : PUBLICOS) {
            if (p.endsWith("/") ? c.startsWith(p) : c.equals(p)) {
                return true;
            }
        }
        return false;
    }

    /** A area que responde por este caminho, se houver. */
    public static Area areaDe(String caminho) {
        String c = normalizar(caminho);
        for (Area a : AREAS) {
            if (a.caminho().equals("/")) {
                if (c.equals("/")) {
                    return a;
                }
            } else if (c.equals(a.caminho()) || c.startsWith(a.caminho() + "/")) {
                return a;
            }
        }
        return null;
    }

    /** RNF02: o perfil pode acessar este caminho? */
    public static boolean permitido(Perfil perfil, String caminho) {
        if (perfil == null) {
            return publico(caminho);
        }
        if (publico(caminho)) {
            return true;
        }
        Area area = areaDe(caminho);
        if (area == null) {
            return false;
        }
        return area.perfis().contains(perfil);
    }

    /** Itens de menu do perfil, na ordem de exibicao. */
    /**
     * Quadro de permissoes para conferencia e para a tela de usuarios.
     * Sai da mesma lista de areas usada pelo filtro, por isso nao pode divergir dele.
     */
    public record LinhaMatriz(String rotulo, String grupo,
                              boolean administrador, boolean profissional, boolean cliente) {
        public String getRotulo() {
            return rotulo;
        }

        public String getGrupo() {
            return grupo;
        }

        public boolean isAdministrador() {
            return administrador;
        }

        public boolean isProfissional() {
            return profissional;
        }

        public boolean isCliente() {
            return cliente;
        }
    }

    public static List<LinhaMatriz> matriz() {
        List<LinhaMatriz> linhas = new ArrayList<>();
        for (Area area : AREAS) {
            linhas.add(new LinhaMatriz(area.rotulo(), area.grupo(),
                    area.perfis().contains(Perfil.ADMINISTRADOR),
                    area.perfis().contains(Perfil.PROFISSIONAL),
                    area.perfis().contains(Perfil.CLIENTE)));
        }
        return linhas;
    }

    public static List<Area> menuDe(Perfil perfil) {
        List<Area> itens = new ArrayList<>();
        if (perfil == null) {
            return itens;
        }
        for (String grupo : List.of("Atendimento", "Cadastros", "Administracao")) {
            for (Area a : AREAS) {
                if (a.noMenu() && a.grupo().equals(grupo) && a.perfis().contains(perfil)) {
                    itens.add(a);
                }
            }
        }
        itens.sort((x, y) -> {
            if (x.caminho().equals("/")) {
                return -1;
            }
            if (y.caminho().equals("/")) {
                return 1;
            }
            return 0;
        });
        return itens;
    }

    private static String normalizar(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            return "/";
        }
        String c = caminho;
        int interrogacao = c.indexOf('?');
        if (interrogacao >= 0) {
            c = c.substring(0, interrogacao);
        }
        if (c.length() > 1 && c.endsWith("/")) {
            c = c.substring(0, c.length() - 1);
        }
        return c;
    }
}
