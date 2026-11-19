package br.uel.avaliacoes.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Avaliacao {
    private Integer id;
    private String titulo;
    private String descricao;
    private String tipo;
    private LocalDate dataAplicacao;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private Integer criadoPor;
    private String criadoPorNome;
    private BigDecimal valorTotal;
    private int totalQuestoes;
    private int totalAtribuidos;
    private int totalRespondidas;
    private Integer turmaId;
    private List<Integer> usuarioIds = new ArrayList<>();
    private List<AvaliacaoQuestao> itens = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

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

    public Integer getCriadoPor() { return criadoPor; }
    public void setCriadoPor(Integer criadoPor) { this.criadoPor = criadoPor; }

    public String getCriadoPorNome() { return criadoPorNome; }
    public void setCriadoPorNome(String criadoPorNome) { this.criadoPorNome = criadoPorNome; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public int getTotalQuestoes() { return totalQuestoes; }
    public void setTotalQuestoes(int totalQuestoes) { this.totalQuestoes = totalQuestoes; }

    public int getTotalAtribuidos() { return totalAtribuidos; }
    public void setTotalAtribuidos(int totalAtribuidos) { this.totalAtribuidos = totalAtribuidos; }

    public int getTotalRespondidas() { return totalRespondidas; }
    public void setTotalRespondidas(int totalRespondidas) { this.totalRespondidas = totalRespondidas; }

    public Integer getTurmaId() { return turmaId; }
    public void setTurmaId(Integer turmaId) { this.turmaId = turmaId; }

    public List<Integer> getUsuarioIds() { return usuarioIds; }
    public void setUsuarioIds(List<Integer> usuarioIds) { this.usuarioIds = usuarioIds; }

    public List<AvaliacaoQuestao> getItens() { return itens; }
    public void setItens(List<AvaliacaoQuestao> itens) { this.itens = itens; }
}
