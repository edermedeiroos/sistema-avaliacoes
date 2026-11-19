package br.uel.avaliacoes.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Questao {
    private Integer id;
    private String enunciado;
    private String tipo;
    private String disciplina;
    private String dificuldade;
    private Integer criadoPor;
    private String criadoPorNome;
    private LocalDateTime criadoEm;
    private int usosEmAvaliacoes;
    private List<Alternativa> alternativas = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDisciplina() { return disciplina; }
    public void setDisciplina(String disciplina) { this.disciplina = disciplina; }

    public String getDificuldade() { return dificuldade; }
    public void setDificuldade(String dificuldade) { this.dificuldade = dificuldade; }

    public Integer getCriadoPor() { return criadoPor; }
    public void setCriadoPor(Integer criadoPor) { this.criadoPor = criadoPor; }

    public String getCriadoPorNome() { return criadoPorNome; }
    public void setCriadoPorNome(String criadoPorNome) { this.criadoPorNome = criadoPorNome; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public int getUsosEmAvaliacoes() { return usosEmAvaliacoes; }
    public void setUsosEmAvaliacoes(int usosEmAvaliacoes) { this.usosEmAvaliacoes = usosEmAvaliacoes; }

    public List<Alternativa> getAlternativas() { return alternativas; }
    public void setAlternativas(List<Alternativa> alternativas) { this.alternativas = alternativas; }
}
