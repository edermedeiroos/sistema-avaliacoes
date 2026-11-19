package br.uel.avaliacoes.model;

import java.math.BigDecimal;

public class AvaliacaoQuestao {
    private Integer avaliacaoId;
    private Integer questaoId;
    private Integer ordem;
    private BigDecimal valor;
    private Questao questao;

    public Integer getAvaliacaoId() { return avaliacaoId; }
    public void setAvaliacaoId(Integer avaliacaoId) { this.avaliacaoId = avaliacaoId; }

    public Integer getQuestaoId() { return questaoId; }
    public void setQuestaoId(Integer questaoId) { this.questaoId = questaoId; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public Questao getQuestao() { return questao; }
    public void setQuestao(Questao questao) { this.questao = questao; }
}
