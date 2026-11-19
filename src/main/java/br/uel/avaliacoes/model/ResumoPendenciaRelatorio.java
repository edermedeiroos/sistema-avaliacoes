package br.uel.avaliacoes.model;

public class ResumoPendenciaRelatorio {
    private Integer avaliacaoId;
    private String titulo;
    private int totalAtribuidos;
    private int totalRespondidas;
    private int totalPendentes;

    public Integer getAvaliacaoId() { return avaliacaoId; }
    public void setAvaliacaoId(Integer avaliacaoId) { this.avaliacaoId = avaliacaoId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public int getTotalAtribuidos() { return totalAtribuidos; }
    public void setTotalAtribuidos(int totalAtribuidos) { this.totalAtribuidos = totalAtribuidos; }

    public int getTotalRespondidas() { return totalRespondidas; }
    public void setTotalRespondidas(int totalRespondidas) { this.totalRespondidas = totalRespondidas; }

    public int getTotalPendentes() { return totalPendentes; }
    public void setTotalPendentes(int totalPendentes) { this.totalPendentes = totalPendentes; }
}
