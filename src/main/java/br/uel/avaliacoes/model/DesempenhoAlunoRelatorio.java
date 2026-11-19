package br.uel.avaliacoes.model;

import java.math.BigDecimal;

public class DesempenhoAlunoRelatorio {
    private Integer avaliacaoId;
    private String avaliacaoTitulo;
    private Integer usuarioId;
    private String alunoNome;
    private String turmaCodigo;
    private BigDecimal notaAluno;
    private BigDecimal mediaTurma;
    private BigDecimal diferenca;

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

    public BigDecimal getNotaAluno() { return notaAluno; }
    public void setNotaAluno(BigDecimal notaAluno) { this.notaAluno = notaAluno; }

    public BigDecimal getMediaTurma() { return mediaTurma; }
    public void setMediaTurma(BigDecimal mediaTurma) { this.mediaTurma = mediaTurma; }

    public BigDecimal getDiferenca() { return diferenca; }
    public void setDiferenca(BigDecimal diferenca) { this.diferenca = diferenca; }
}
