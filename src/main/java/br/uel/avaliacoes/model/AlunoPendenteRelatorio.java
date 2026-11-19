package br.uel.avaliacoes.model;

public class AlunoPendenteRelatorio {
    private Integer avaliacaoId;
    private String avaliacaoTitulo;
    private Integer usuarioId;
    private String alunoNome;
    private String turmaCodigo;
    private String status;

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
