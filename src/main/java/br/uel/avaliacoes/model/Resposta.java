package br.uel.avaliacoes.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Resposta {
    private Integer id;
    private Integer avaliacaoId;
    private Integer usuarioId;
    private Integer questaoId;
    private String enunciado;
    private String tipoQuestao;
    private BigDecimal valorQuestao;
    private Integer alternativaId;
    private String alternativaLetra;
    private String alternativaTexto;
    private String textoResposta;
    private BigDecimal notaObtida;
    private Integer corrigidaPor;
    private LocalDateTime corrigidaEm;
    private LocalDateTime respondidaEm;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getAvaliacaoId() { return avaliacaoId; }
    public void setAvaliacaoId(Integer avaliacaoId) { this.avaliacaoId = avaliacaoId; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public Integer getQuestaoId() { return questaoId; }
    public void setQuestaoId(Integer questaoId) { this.questaoId = questaoId; }

    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }

    public String getTipoQuestao() { return tipoQuestao; }
    public void setTipoQuestao(String tipoQuestao) { this.tipoQuestao = tipoQuestao; }

    public BigDecimal getValorQuestao() { return valorQuestao; }
    public void setValorQuestao(BigDecimal valorQuestao) { this.valorQuestao = valorQuestao; }

    public Integer getAlternativaId() { return alternativaId; }
    public void setAlternativaId(Integer alternativaId) { this.alternativaId = alternativaId; }

    public String getAlternativaLetra() { return alternativaLetra; }
    public void setAlternativaLetra(String alternativaLetra) { this.alternativaLetra = alternativaLetra; }

    public String getAlternativaTexto() { return alternativaTexto; }
    public void setAlternativaTexto(String alternativaTexto) { this.alternativaTexto = alternativaTexto; }

    public String getTextoResposta() { return textoResposta; }
    public void setTextoResposta(String textoResposta) { this.textoResposta = textoResposta; }

    public BigDecimal getNotaObtida() { return notaObtida; }
    public void setNotaObtida(BigDecimal notaObtida) { this.notaObtida = notaObtida; }

    public Integer getCorrigidaPor() { return corrigidaPor; }
    public void setCorrigidaPor(Integer corrigidaPor) { this.corrigidaPor = corrigidaPor; }

    public LocalDateTime getCorrigidaEm() { return corrigidaEm; }
    public void setCorrigidaEm(LocalDateTime corrigidaEm) { this.corrigidaEm = corrigidaEm; }

    public LocalDateTime getRespondidaEm() { return respondidaEm; }
    public void setRespondidaEm(LocalDateTime respondidaEm) { this.respondidaEm = respondidaEm; }
}
