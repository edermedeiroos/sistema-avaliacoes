package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.AlunoPendenteRelatorio;
import br.uel.avaliacoes.model.DesempenhoAlunoRelatorio;
import br.uel.avaliacoes.model.MediaAvaliacaoRelatorio;
import br.uel.avaliacoes.model.Questao;
import br.uel.avaliacoes.model.RankingErroRelatorio;
import br.uel.avaliacoes.model.ResumoPendenciaRelatorio;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class RelatorioDAO {

    private final JdbcTemplate jdbc;

    public RelatorioDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<MediaAvaliacaoRelatorio> mediaPorAvaliacao(LocalDate dataInicio, LocalDate dataFim, List<String> tipos) {
        StringBuilder sql = new StringBuilder("""
                SELECT a.id AS avaliacao_id, a.titulo, a.tipo, a.data_aplicacao,
                       COUNT(notas.usuario_id) AS total_respondentes,
                       ROUND(AVG(notas.nota_total), 2) AS nota_media,
                       MAX(notas.nota_total) AS maior_nota,
                       MIN(notas.nota_total) AS menor_nota
                FROM avaliacao a
                INNER JOIN (
                    SELECT r.avaliacao_id, r.usuario_id, SUM(r.nota_obtida) AS nota_total
                    FROM resposta r
                    INNER JOIN avaliacao_usuario au
                        ON au.avaliacao_id = r.avaliacao_id AND au.usuario_id = r.usuario_id
                    WHERE au.status = 'CORRIGIDA'
                    GROUP BY r.avaliacao_id, r.usuario_id
                ) notas ON notas.avaliacao_id = a.id
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();
        if (dataInicio != null && dataFim != null) {
            sql.append(" AND a.data_aplicacao BETWEEN ? AND ?");
            params.add(Date.valueOf(dataInicio));
            params.add(Date.valueOf(dataFim));
        } else if (dataInicio != null) {
            sql.append(" AND a.data_aplicacao >= ?");
            params.add(Date.valueOf(dataInicio));
        } else if (dataFim != null) {
            sql.append(" AND a.data_aplicacao <= ?");
            params.add(Date.valueOf(dataFim));
        }

        if (tipos != null && !tipos.isEmpty()) {
            String placeholders = String.join(",", Collections.nCopies(tipos.size(), "?"));
            sql.append(" AND a.tipo IN (").append(placeholders).append(")");
            params.addAll(tipos);
        }

        sql.append("""
                 GROUP BY a.id, a.titulo, a.tipo, a.data_aplicacao
                 HAVING COUNT(notas.usuario_id) >= 1
                 ORDER BY a.data_aplicacao DESC, a.id DESC
                """);

        return jdbc.query(sql.toString(), (rs, rowNum) -> {
            MediaAvaliacaoRelatorio m = new MediaAvaliacaoRelatorio();
            m.setAvaliacaoId(rs.getInt("avaliacao_id"));
            m.setTitulo(rs.getString("titulo"));
            m.setTipo(rs.getString("tipo"));
            Date dt = rs.getDate("data_aplicacao");
            if (dt != null) {
                m.setDataAplicacao(dt.toLocalDate());
            }
            m.setTotalRespondentes(rs.getInt("total_respondentes"));
            m.setNotaMedia(rs.getBigDecimal("nota_media"));
            m.setMaiorNota(rs.getBigDecimal("maior_nota"));
            m.setMenorNota(rs.getBigDecimal("menor_nota"));
            return m;
        }, params.toArray());
    }

    public List<DesempenhoAlunoRelatorio> desempenhoVsMedia(String busca, Integer avaliacaoId) {
        StringBuilder sql = new StringBuilder("""
                SELECT a.id AS avaliacao_id, a.titulo AS avaliacao_titulo,
                       u.id AS usuario_id, u.nome AS aluno_nome,
                       COALESCE(t.codigo, '-') AS turma_codigo,
                       SUM(r.nota_obtida) AS nota_aluno,
                       ROUND((
                           SELECT AVG(sub.total_aluno)
                           FROM (
                               SELECT SUM(r2.nota_obtida) AS total_aluno
                               FROM resposta r2
                               INNER JOIN avaliacao_usuario au2
                                   ON au2.avaliacao_id = r2.avaliacao_id AND au2.usuario_id = r2.usuario_id
                               WHERE r2.avaliacao_id = a.id AND au2.status = 'CORRIGIDA'
                               GROUP BY r2.usuario_id
                           ) sub
                       ), 2) AS media_turma
                FROM avaliacao_usuario au
                INNER JOIN avaliacao a ON a.id = au.avaliacao_id
                INNER JOIN usuario u ON u.id = au.usuario_id
                LEFT JOIN turma t ON t.id = u.turma_id
                INNER JOIN resposta r ON r.avaliacao_id = au.avaliacao_id AND r.usuario_id = au.usuario_id
                WHERE au.status = 'CORRIGIDA'
                """);

        List<Object> params = new ArrayList<>();
        if (busca != null && !busca.isBlank()) {
            sql.append(" AND (LOWER(u.nome) LIKE LOWER(?) OR LOWER(COALESCE(t.codigo, '')) LIKE LOWER(?))");
            String padrao = "%" + busca.trim() + "%";
            params.add(padrao);
            params.add(padrao);
        }
        if (avaliacaoId != null) {
            sql.append(" AND a.id = ?");
            params.add(avaliacaoId);
        }

        sql.append("""
                 GROUP BY a.id, a.titulo, a.data_aplicacao, u.id, u.nome, t.codigo
                 ORDER BY a.data_aplicacao DESC, nota_aluno DESC, u.nome
                """);

        return jdbc.query(sql.toString(), (rs, rowNum) -> {
            DesempenhoAlunoRelatorio d = new DesempenhoAlunoRelatorio();
            d.setAvaliacaoId(rs.getInt("avaliacao_id"));
            d.setAvaliacaoTitulo(rs.getString("avaliacao_titulo"));
            d.setUsuarioId(rs.getInt("usuario_id"));
            d.setAlunoNome(rs.getString("aluno_nome"));
            d.setTurmaCodigo(rs.getString("turma_codigo"));
            BigDecimal nota = rs.getBigDecimal("nota_aluno");
            BigDecimal media = rs.getBigDecimal("media_turma");
            d.setNotaAluno(nota);
            d.setMediaTurma(media);
            if (nota != null && media != null) {
                d.setDiferenca(nota.subtract(media));
            }
            return d;
        }, params.toArray());
    }

    public List<RankingErroRelatorio> rankingQuestoesMaisErradas(String disciplina, int limite) {
        StringBuilder sql = new StringBuilder("""
                SELECT RANK() OVER (
                           ORDER BY (SUM(CASE WHEN r.nota_obtida < (aq.valor * 0.5) THEN 1 ELSE 0 END)::numeric * 100.0 / COUNT(*)) DESC,
                                    SUM(CASE WHEN r.nota_obtida < (aq.valor * 0.5) THEN 1 ELSE 0 END) DESC
                       ) AS posicao,
                       q.id AS questao_id, q.enunciado, q.disciplina,
                       COUNT(*) AS total_respostas,
                       SUM(CASE WHEN r.nota_obtida < (aq.valor * 0.5) THEN 1 ELSE 0 END) AS total_erros,
                       ROUND(SUM(CASE WHEN r.nota_obtida < (aq.valor * 0.5) THEN 1 ELSE 0 END)::numeric * 100.0 / COUNT(*), 1) AS percentual_erro
                FROM resposta r
                INNER JOIN avaliacao_questao aq
                    ON aq.avaliacao_id = r.avaliacao_id AND aq.questao_id = r.questao_id
                INNER JOIN questao q ON q.id = r.questao_id
                WHERE r.nota_obtida IS NOT NULL
                """);

        List<Object> params = new ArrayList<>();
        if (disciplina != null && !disciplina.isBlank()) {
            sql.append(" AND q.disciplina = ?");
            params.add(disciplina.trim());
        }

        sql.append("""
                 GROUP BY q.id, q.enunciado, q.disciplina
                 HAVING COUNT(*) >= 3
                 ORDER BY percentual_erro DESC, total_erros DESC, q.id
                 FETCH FIRST ? ROWS ONLY
                """);
        params.add(limite);

        return jdbc.query(sql.toString(), (rs, rowNum) -> {
            RankingErroRelatorio rk = new RankingErroRelatorio();
            rk.setPosicao(rs.getInt("posicao"));
            rk.setQuestaoId(rs.getInt("questao_id"));
            rk.setEnunciado(rs.getString("enunciado"));
            rk.setDisciplina(rs.getString("disciplina"));
            rk.setTotalRespostas(rs.getInt("total_respostas"));
            rk.setTotalErros(rs.getInt("total_erros"));
            rk.setPercentualErro(rs.getBigDecimal("percentual_erro"));
            return rk;
        }, params.toArray());
    }

    public List<ResumoPendenciaRelatorio> resumoPendenciasPorAvaliacao() {
        String sql = """
                SELECT a.id AS avaliacao_id, a.titulo,
                       COUNT(au.usuario_id) AS total_atribuidos,
                       COALESCE(SUM(CASE WHEN au.status <> 'PENDENTE' THEN 1 ELSE 0 END), 0) AS total_respondidas,
                       COALESCE(SUM(CASE WHEN au.status = 'PENDENTE' THEN 1 ELSE 0 END), 0) AS total_pendentes
                FROM avaliacao a
                LEFT JOIN avaliacao_usuario au ON au.avaliacao_id = a.id
                GROUP BY a.id, a.titulo, a.data_aplicacao
                ORDER BY a.data_aplicacao DESC, a.id DESC
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            ResumoPendenciaRelatorio r = new ResumoPendenciaRelatorio();
            r.setAvaliacaoId(rs.getInt("avaliacao_id"));
            r.setTitulo(rs.getString("titulo"));
            r.setTotalAtribuidos(rs.getInt("total_atribuidos"));
            r.setTotalRespondidas(rs.getInt("total_respondidas"));
            r.setTotalPendentes(rs.getInt("total_pendentes"));
            return r;
        });
    }

    public List<AlunoPendenteRelatorio> alunosPendentes() {
        String sql = """
                SELECT au.avaliacao_id, a.titulo AS avaliacao_titulo,
                       au.usuario_id, u.nome AS aluno_nome,
                       COALESCE(t.codigo, '-') AS turma_codigo, au.status
                FROM avaliacao_usuario au
                INNER JOIN avaliacao a ON a.id = au.avaliacao_id
                INNER JOIN usuario u ON u.id = au.usuario_id
                LEFT JOIN turma t ON t.id = u.turma_id
                LEFT JOIN resposta r
                    ON r.avaliacao_id = au.avaliacao_id AND r.usuario_id = au.usuario_id
                WHERE r.id IS NULL
                ORDER BY a.data_aplicacao DESC, u.nome
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            AlunoPendenteRelatorio ap = new AlunoPendenteRelatorio();
            ap.setAvaliacaoId(rs.getInt("avaliacao_id"));
            ap.setAvaliacaoTitulo(rs.getString("avaliacao_titulo"));
            ap.setUsuarioId(rs.getInt("usuario_id"));
            ap.setAlunoNome(rs.getString("aluno_nome"));
            ap.setTurmaCodigo(rs.getString("turma_codigo"));
            ap.setStatus(rs.getString("status"));
            return ap;
        });
    }

    public List<Questao> questoesNaoUtilizadas() {
        String sql = """
                SELECT q.id, q.enunciado, q.tipo, q.disciplina, q.dificuldade, u.nome AS criado_por_nome
                FROM questao q
                INNER JOIN usuario u ON u.id = q.criado_por
                LEFT JOIN avaliacao_questao aq ON aq.questao_id = q.id
                WHERE aq.questao_id IS NULL
                ORDER BY q.id
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            Questao q = new Questao();
            q.setId(rs.getInt("id"));
            q.setEnunciado(rs.getString("enunciado"));
            q.setTipo(rs.getString("tipo"));
            q.setDisciplina(rs.getString("disciplina"));
            q.setDificuldade(rs.getString("dificuldade"));
            q.setCriadoPorNome(rs.getString("criado_por_nome"));
            return q;
        });
    }

    public List<String> listarDisciplinas() {
        return jdbc.queryForList("SELECT DISTINCT disciplina FROM questao ORDER BY disciplina", String.class);
    }
}
