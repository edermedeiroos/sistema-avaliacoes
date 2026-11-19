package br.uel.avaliacoes.model;

public class Usuario {
    private Integer id;
    private String nome;
    private String email;
    private String senha;
    private String perfil;
    private Integer turmaId;
    private String turmaCodigo;
    private Boolean ativo;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public Integer getTurmaId() { return turmaId; }
    public void setTurmaId(Integer turmaId) { this.turmaId = turmaId; }

    public String getTurmaCodigo() { return turmaCodigo; }
    public void setTurmaCodigo(String turmaCodigo) { this.turmaCodigo = turmaCodigo; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
