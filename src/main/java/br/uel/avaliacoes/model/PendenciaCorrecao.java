package br.uel.avaliacoes.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PendenciaCorrecao {
    private Integer respostaId;
    private Integer avaliacaoId;
    private String avaliacaoTitulo;
    private Integer usuarioId;
    private String alunoNome;
    private String turmaCodigo;
    private Integer questaoId;
    private String enunciado;
    private String textoResposta;
    private BigDecimal valorMaximo;
    private BigDecimal notaObtida;
    private LocalDateTime respondidaEm;
    private LocalDateTime corrigidaEm;

    public Integer getRespostaId() { return respostaId; }
    public void setRespostaId(Integer respostaId) { this.respostaId = respostaId; }

    public Integer getAvaliacaoId() { return avaliacaoId; }
    public void setAvaliacaoId(Integer avaliacaoId) { this.avaliacaoId = avaliacaoId; }

    public String getAvaliacaoTitulo() { return avaliacaoTitulo; }
    public void setAvaliacaoTitulo(String avaliacaoTitulo) { this.avaliacaoTitulo = avaliacaoTitulo; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getAlunoNome() { return alunoNome; }
    public void setAlunoNome(String alunoNome) { this.alunoNome = alunoNome; }

    public String getTurmaCodigo() { return turmaCodigo; }
    public void setTurmaCodigo(String turmaCodigo) { this.turmaCodigo = turmaCodigo; }

    public Integer getQuestaoId() { return questaoId; }
    public void setQuestaoId(Integer questaoId) { this.questaoId = questaoId; }

    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }

    public String getTextoResposta() { return textoResposta; }
    public void setTextoResposta(String textoResposta) { this.textoResposta = textoResposta; }

    public BigDecimal getValorMaximo() { return valorMaximo; }
    public void setValorMaximo(BigDecimal valorMaximo) { this.valorMaximo = valorMaximo; }

    public BigDecimal getNotaObtida() { return notaObtida; }
    public void setNotaObtida(BigDecimal notaObtida) { this.notaObtida = notaObtida; }

    public LocalDateTime getRespondidaEm() { return respondidaEm; }
    public void setRespondidaEm(LocalDateTime respondidaEm) { this.respondidaEm = respondidaEm; }

    public LocalDateTime getCorrigidaEm() { return corrigidaEm; }
    public void setCorrigidaEm(LocalDateTime corrigidaEm) { this.corrigidaEm = corrigidaEm; }
}
