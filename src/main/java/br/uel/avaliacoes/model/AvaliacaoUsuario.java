package br.uel.avaliacoes.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AvaliacaoUsuario {
    private Integer avaliacaoId;
    private Integer usuarioId;
    private String titulo;
    private String descricao;
    private String tipo;
    private LocalDate dataAplicacao;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String usuarioNome;
    private String turmaCodigo;
    private String status;
    private LocalDateTime enviadaEm;
    private BigDecimal notaObtida;
    private BigDecimal valorTotal;

    public Integer getAvaliacaoId() { return avaliacaoId; }
    public void setAvaliacaoId(Integer avaliacaoId) { this.avaliacaoId = avaliacaoId; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public LocalDate getDataAplicacao() { return dataAplicacao; }
    public void setDataAplicacao(LocalDate dataAplicacao) { this.dataAplicacao = dataAplicacao; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }

    public String getUsuarioNome() { return usuarioNome; }
    public void setUsuarioNome(String usuarioNome) { this.usuarioNome = usuarioNome; }

    public String getTurmaCodigo() { return turmaCodigo; }
    public void setTurmaCodigo(String turmaCodigo) { this.turmaCodigo = turmaCodigo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getEnviadaEm() { return enviadaEm; }
    public void setEnviadaEm(LocalDateTime enviadaEm) { this.enviadaEm = enviadaEm; }

    public BigDecimal getNotaObtida() { return notaObtida; }
    public void setNotaObtida(BigDecimal notaObtida) { this.notaObtida = notaObtida; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
}
