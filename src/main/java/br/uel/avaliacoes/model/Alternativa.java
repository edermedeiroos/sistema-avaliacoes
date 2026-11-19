package br.uel.avaliacoes.model;

public class Alternativa {
    private Integer id;
    private Integer questaoId;
    private String letra;
    private String texto;
    private boolean correta;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getQuestaoId() { return questaoId; }
    public void setQuestaoId(Integer questaoId) { this.questaoId = questaoId; }

    public String getLetra() { return letra; }
    public void setLetra(String letra) { this.letra = letra; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public boolean isCorreta() { return correta; }
    public void setCorreta(boolean correta) { this.correta = correta; }
}
