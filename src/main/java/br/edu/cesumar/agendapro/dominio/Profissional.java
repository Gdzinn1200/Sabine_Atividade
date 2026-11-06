package br.edu.cesumar.agendapro.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

/** Profissional que executa os servicos (tabela profissional do modelo logico). */
@Entity
@Table(name = "profissional")
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profissional")
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    @Column(name = "email", length = 120)
    private String email;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    /** RN06: servicos que este profissional pode executar (tabela profissional_servico). */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "profissional_servico",
            joinColumns = @JoinColumn(name = "id_profissional"),
            inverseJoinColumns = @JoinColumn(name = "id_servico"))
    private Set<Servico> servicos = new LinkedHashSet<>();

    public boolean executa(Long idServico) {
        if (idServico == null) {
            return false;
        }
        for (Servico s : servicos) {
            if (idServico.equals(s.getId())) {
                return true;
            }
        }
        return false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Set<Servico> getServicos() {
        return servicos;
    }

    public void setServicos(Set<Servico> servicos) {
        this.servicos = servicos;
    }
}
