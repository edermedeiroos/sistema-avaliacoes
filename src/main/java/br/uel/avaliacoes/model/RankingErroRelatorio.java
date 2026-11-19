package br.uel.avaliacoes.model;

import java.math.BigDecimal;

public class RankingErroRelatorio {
    private int posicao;
    private Integer questaoId;
    private String enunciado;
    private String disciplina;
    private int totalRespostas;
    private int totalErros;
    private BigDecimal percentualErro;

    public int getPosicao() { return posicao; }
    public void setPosicao(int posicao) { this.posicao = posicao; }

    public Integer getQuestaoId() { return questaoId; }
    public void setQuestaoId(Integer questaoId) { this.questaoId = questaoId; }

    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }

    public String getDisciplina() { return disciplina; }
    public void setDisciplina(String disciplina) { this.disciplina = disciplina; }

    public int getTotalRespostas() { return totalRespostas; }
    public void setTotalRespostas(int totalRespostas) { this.totalRespostas = totalRespostas; }

    public int getTotalErros() { return totalErros; }
    public void setTotalErros(int totalErros) { this.totalErros = totalErros; }

    public BigDecimal getPercentualErro() { return percentualErro; }
    public void setPercentualErro(BigDecimal percentualErro) { this.percentualErro = percentualErro; }
}
