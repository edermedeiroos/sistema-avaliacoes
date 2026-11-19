package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.Turma;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TurmaDAO {

    private final JdbcTemplate jdbc;

    public TurmaDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Turma> listarTodas() {
        String sql = "SELECT id, codigo, nome, ano_semestre FROM turma ORDER BY codigo";
        return jdbc.query(sql, (rs, rowNum) -> {
            Turma t = new Turma();
            t.setId(rs.getInt("id"));
            t.setCodigo(rs.getString("codigo"));
            t.setNome(rs.getString("nome"));
            t.setAnoSemestre(rs.getString("ano_semestre"));
            return t;
        });
    }
}
