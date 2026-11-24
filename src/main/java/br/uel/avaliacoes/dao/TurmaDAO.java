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
        String sql = """
                SELECT t.id, t.codigo, t.nome, t.ano_semestre,
                       COUNT(u.id) AS total_alunos
                FROM turma t
                LEFT JOIN usuario u ON u.turma_id = t.id AND u.perfil = 'ALUNO' AND u.ativo = TRUE
                GROUP BY t.id, t.codigo, t.nome, t.ano_semestre
                ORDER BY t.codigo
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            Turma t = new Turma();
            t.setId(rs.getInt("id"));
            t.setCodigo(rs.getString("codigo"));
            t.setNome(rs.getString("nome"));
            t.setAnoSemestre(rs.getString("ano_semestre"));
            t.setTotalAlunos(rs.getInt("total_alunos"));
            return t;
        });
    }

    public boolean existePorCodigo(String codigo) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM turma WHERE LOWER(codigo) = LOWER(?)",
                Integer.class,
                codigo
        );
        return count != null && count > 0;
    }

    public int inserir(Turma t) {
        String sql = """
                INSERT INTO turma (codigo, nome, ano_semestre)
                VALUES (?, ?, ?)
                RETURNING id
                """;
        Integer id = jdbc.queryForObject(sql, Integer.class, t.getCodigo(), t.getNome(), t.getAnoSemestre());
        return id != null ? id : 0;
    }
}
