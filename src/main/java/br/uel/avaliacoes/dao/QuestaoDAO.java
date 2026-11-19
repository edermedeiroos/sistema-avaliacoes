package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.Alternativa;
import br.uel.avaliacoes.model.Questao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class QuestaoDAO {

    private final JdbcTemplate jdbc;

    public QuestaoDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Questao> questaoMapper = (rs, rowNum) -> {
        Questao q = new Questao();
        q.setId(rs.getInt("id"));
        q.setEnunciado(rs.getString("enunciado"));
        q.setTipo(rs.getString("tipo"));
        q.setDisciplina(rs.getString("disciplina"));
        q.setDificuldade(rs.getString("dificuldade"));
        q.setCriadoPor(rs.getInt("criado_por"));
        q.setCriadoPorNome(rs.getString("criado_por_nome"));
        Timestamp ts = rs.getTimestamp("criado_em");
        if (ts != null) {
            q.setCriadoEm(ts.toLocalDateTime());
        }
        q.setUsosEmAvaliacoes(rs.getInt("usos"));
        return q;
    };

    private final RowMapper<Alternativa> alternativaMapper = (rs, rowNum) -> {
        Alternativa a = new Alternativa();
        a.setId(rs.getInt("id"));
        a.setQuestaoId(rs.getInt("questao_id"));
        a.setLetra(rs.getString("letra"));
        a.setTexto(rs.getString("texto"));
        a.setCorreta(rs.getBoolean("correta"));
        return a;
    };

    public List<Questao> listar(String tipo, String busca) {
        StringBuilder sql = new StringBuilder("""
                SELECT q.id, q.enunciado, q.tipo, q.disciplina, q.dificuldade,
                       q.criado_por, u.nome AS criado_por_nome, q.criado_em,
                       COUNT(aq.avaliacao_id) AS usos
                FROM questao q
                JOIN usuario u ON u.id = q.criado_por
                LEFT JOIN avaliacao_questao aq ON aq.questao_id = q.id
                WHERE 1 = 1
                """);
        List<Object> params = new ArrayList<>();
        if (tipo != null && !tipo.isBlank()) {
            sql.append(" AND q.tipo = ?");
            params.add(tipo.trim());
        }
        if (busca != null && !busca.isBlank()) {
            sql.append(" AND (LOWER(q.enunciado) LIKE LOWER(?) OR LOWER(q.disciplina) LIKE LOWER(?))");
            String padrao = "%" + busca.trim() + "%";
            params.add(padrao);
            params.add(padrao);
        }
        sql.append(" GROUP BY q.id, u.nome ORDER BY q.id DESC");

        List<Questao> lista = jdbc.query(sql.toString(), questaoMapper, params.toArray());
        for (Questao q : lista) {
            if ("MULTIPLA_ESCOLHA".equals(q.getTipo())) {
                q.setAlternativas(listarAlternativasPorQuestao(q.getId()));
            }
        }
        return lista;
    }

    public Optional<Questao> buscarPorId(int id) {
        String sql = """
                SELECT q.id, q.enunciado, q.tipo, q.disciplina, q.dificuldade,
                       q.criado_por, u.nome AS criado_por_nome, q.criado_em,
                       COUNT(aq.avaliacao_id) AS usos
                FROM questao q
                JOIN usuario u ON u.id = q.criado_por
                LEFT JOIN avaliacao_questao aq ON aq.questao_id = q.id
                WHERE q.id = ?
                GROUP BY q.id, u.nome
                """;
        List<Questao> lista = jdbc.query(sql, questaoMapper, id);
        if (lista.isEmpty()) {
            return Optional.empty();
        }
        Questao q = lista.get(0);
        if ("MULTIPLA_ESCOLHA".equals(q.getTipo())) {
            q.setAlternativas(listarAlternativasPorQuestao(q.getId()));
        }
        return Optional.of(q);
    }

    public List<Alternativa> listarAlternativasPorQuestao(int questaoId) {
        String sql = """
                SELECT id, questao_id, letra, texto, correta
                FROM alternativa
                WHERE questao_id = ?
                ORDER BY letra
                """;
        return jdbc.query(sql, alternativaMapper, questaoId);
    }

    public Optional<Alternativa> buscarAlternativaPorId(int alternativaId) {
        String sql = "SELECT id, questao_id, letra, texto, correta FROM alternativa WHERE id = ?";
        List<Alternativa> lista = jdbc.query(sql, alternativaMapper, alternativaId);
        return lista.stream().findFirst();
    }

    public int inserir(Questao q) {
        String sql = """
                INSERT INTO questao (enunciado, tipo, disciplina, dificuldade, criado_por)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;
        Integer id = jdbc.queryForObject(
                sql,
                Integer.class,
                q.getEnunciado(),
                q.getTipo(),
                q.getDisciplina(),
                q.getDificuldade(),
                q.getCriadoPor()
        );
        return id != null ? id : 0;
    }

    public int inserirAlternativa(Alternativa a) {
        String sql = """
                INSERT INTO alternativa (questao_id, letra, texto, correta)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """;
        Integer id = jdbc.queryForObject(
                sql,
                Integer.class,
                a.getQuestaoId(),
                a.getLetra(),
                a.getTexto(),
                a.isCorreta()
        );
        return id != null ? id : 0;
    }

    public int excluir(int id) {
        return jdbc.update("DELETE FROM questao WHERE id = ?", id);
    }
}
