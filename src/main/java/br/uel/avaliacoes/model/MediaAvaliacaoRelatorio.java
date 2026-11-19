package br.uel.avaliacoes.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MediaAvaliacaoRelatorio {
    private Integer avaliacaoId;
    private String titulo;
    private String tipo;
    private LocalDate dataAplicacao;
    private int totalRespondentes;
    private BigDecimal notaMedia;
    private BigDecimal maiorNota;
    private BigDecimal menorNota;

    public Integer getAvaliacaoId() { return avaliacaoId; }
    public void setAvaliacaoId(Integer avaliacaoId) { this.avaliacaoId = avaliacaoId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public LocalDate getDataAplicacao() { return dataAplicacao; }
    public void setDataAplicacao(LocalDate dataAplicacao) { this.dataAplicacao = dataAplicacao; }

    public int getTotalRespondentes() { return totalRespondentes; }
    public void setTotalRespondentes(int totalRespondentes) { this.totalRespondentes = totalRespondentes; }

    public BigDecimal getNotaMedia() { return notaMedia; }
    public void setNotaMedia(BigDecimal notaMedia) { this.notaMedia = notaMedia; }

    public BigDecimal getMaiorNota() { return maiorNota; }
    public void setMaiorNota(BigDecimal maiorNota) { this.maiorNota = maiorNota; }

    public BigDecimal getMenorNota() { return menorNota; }
    public void setMenorNota(BigDecimal menorNota) { this.menorNota = menorNota; }
}
