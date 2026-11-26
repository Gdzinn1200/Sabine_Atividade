package br.edu.cesumar.agendapro.web;

import br.edu.cesumar.agendapro.dominio.regras.Perfil;
import java.io.Serializable;

/**
 * Quem esta usando o sistema nesta sessao. Guardado na sessao HTTP apos o
 * login e consultado pelo filtro de acesso e pelas telas.
 */
public record UsuarioSessao(Long id, String nome, String email, Perfil perfil,
                            Long idCliente, Long idProfissional) implements Serializable {

    public static final String CHAVE = "usuarioLogado";

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public Long getIdProfissional() {
        return idProfissional;
    }

    public String getPrimeiroNome() {
        return nome == null || nome.isBlank() ? "" : nome.trim().split("\\s+")[0];
    }

    public boolean isAdministrador() {
        return perfil == Perfil.ADMINISTRADOR;
    }

    public boolean isProfissional() {
        return perfil == Perfil.PROFISSIONAL;
    }

    public boolean isCliente() {
        return perfil == Perfil.CLIENTE;
    }
}
