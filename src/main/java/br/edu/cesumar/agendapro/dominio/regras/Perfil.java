package br.edu.cesumar.agendapro.dominio.regras;

/** Perfis de acesso previstos no documento de visao e requisitos. */
public enum Perfil {

    ADMINISTRADOR("Administrador"),
    PROFISSIONAL("Profissional"),
    CLIENTE("Cliente");

    private final String rotulo;

    Perfil(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public boolean isAdministrador() {
        return this == ADMINISTRADOR;
    }
}
