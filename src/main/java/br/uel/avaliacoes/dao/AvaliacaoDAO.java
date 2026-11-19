package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.Alternativa;
import br.uel.avaliacoes.model.Avaliacao;
import br.uel.avaliacoes.model.AvaliacaoQuestao;
import br.uel.avaliacoes.model.AvaliacaoUsuario;
import br.uel.avaliacoes.model.Questao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class AvaliacaoDAO {

    private final JdbcTemplate jdbc;
    private final QuestaoDAO questaoDAO;

    public AvaliacaoDAO(JdbcTemplate jdbc, QuestaoDAO questaoDAO) {
        this.jdbc = jdbc;
        this.questaoDAO = questaoDAO;
    }

    public List<Avaliacao> listarTodas() {
        String sql = """
                SELECT a.id, a.titulo, a.descricao, a.tipo, a.data_aplicacao, a.hora_inicio, a.hora_fim,
                       a.criado_por, u.nome AS criado_por_nome,
                       COALESCE((SELECT SUM(aq.valor) FROM avaliacao_questao aq WHERE aq.avaliacao_id = a.id), 0) AS valor_total,
                       (SELECT COUNT(*) FROM avaliacao_questao aq WHERE aq.avaliacao_id = a.id) AS total_questoes,
                       (SELECT COUNT(*) FROM avaliacao_usuario au WHERE au.avaliacao_id = a.id) AS total_atribuidos,
                       (SELECT COUNT(*) FROM avaliacao_usuario au WHERE au.avaliacao_id = a.id AND au.status <> 'PENDENTE') AS total_respondidas
                FROM avaliacao a
                JOIN usuario u ON u.id = a.criado_por
                ORDER BY a.data_aplicacao DESC, a.id DESC
                """;
        return jdbc.query(sql, (rs, rowNum) -> mapAvaliacao(rs));
    }

    public Optional<Avaliacao> buscarPorId(int id) {
        String sql = """
                SELECT a.id, a.titulo, a.descricao, a.tipo, a.data_aplicacao, a.hora_inicio, a.hora_fim,
                       a.criado_por, u.nome AS criado_por_nome,
                       COALESCE((SELECT SUM(aq.valor) FROM avaliacao_questao aq WHERE aq.avaliacao_id = a.id), 0) AS valor_total,
                       (SELECT COUNT(*) FROM avaliacao_questao aq WHERE aq.avaliacao_id = a.id) AS total_questoes,
                       (SELECT COUNT(*) FROM avaliacao_usuario au WHERE au.avaliacao_id = a.id) AS total_atribuidos,
                       (SELECT COUNT(*) FROM avaliacao_usuario au WHERE au.avaliacao_id = a.id AND au.status <> 'PENDENTE') AS total_respondidas
                FROM avaliacao a
                JOIN usuario u ON u.id = a.criado_por
                WHERE a.id = ?
                """;
        List<Avaliacao> lista = jdbc.query(sql, (rs, rowNum) -> mapAvaliacao(rs), id);
        return lista.stream().findFirst();
    }

    private Avaliacao mapAvaliacao(java.sql.ResultSet rs) throws java.sql.SQLException {
        Avaliacao a = new Avaliacao();
        a.setId(rs.getInt("id"));
        a.setTitulo(rs.getString("titulo"));
        a.setDescricao(rs.getString("descricao"));
        a.setTipo(rs.getString("tipo"));
        Date dt = rs.getDate("data_aplicacao");
        if (dt != null) {
            a.setDataAplicacao(dt.toLocalDate());
        }
        Time hi = rs.getTime("hora_inicio");
        if (hi != null) {
            a.setHoraInicio(hi.toLocalTime());
        }
        Time hf = rs.getTime("hora_fim");
        if (hf != null) {
            a.setHoraFim(hf.toLocalTime());
        }
        a.setCriadoPor(rs.getInt("criado_por"));
        a.setCriadoPorNome(rs.getString("criado_por_nome"));
        a.setValorTotal(rs.getBigDecimal("valor_total"));
        a.setTotalQuestoes(rs.getInt("total_questoes"));
        a.setTotalAtribuidos(rs.getInt("total_atribuidos"));
        a.setTotalRespondidas(rs.getInt("total_respondidas"));
        return a;
    }

    public int inserir(Avaliacao a) {
        String sql = """
                INSERT INTO avaliacao (titulo, descricao, tipo, data_aplicacao, hora_inicio, hora_fim, criado_por)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
        Integer id = jdbc.queryForObject(
                sql,
                Integer.class,
                a.getTitulo(),
                a.getDescricao(),
                a.getTipo(),
                Date.valueOf(a.getDataAplicacao()),
                Time.valueOf(a.getHoraInicio()),
                Time.valueOf(a.getHoraFim()),
                a.getCriadoPor()
        );
        return id != null ? id : 0;
    }

    public void adicionarQuestao(int avaliacaoId, int questaoId, int ordem, BigDecimal valor) {
        String sql = """
                INSERT INTO avaliacao_questao (avaliacao_id, questao_id, ordem, valor)
                VALUES (?, ?, ?, ?)
                """;
        jdbc.update(sql, avaliacaoId, questaoId, ordem, valor);
    }

    public List<AvaliacaoQuestao> listarQuestoesDaAvaliacao(int avaliacaoId) {
        String sql = """
                SELECT aq.avaliacao_id, aq.questao_id, aq.ordem, aq.valor,
                       q.enunciado, q.tipo, q.disciplina, q.dificuldade
                FROM avaliacao_questao aq
                JOIN questao q ON q.id = aq.questao_id
                WHERE aq.avaliacao_id = ?
                ORDER BY aq.ordem
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            AvaliacaoQuestao aq = new AvaliacaoQuestao();
            aq.setAvaliacaoId(rs.getInt("avaliacao_id"));
            aq.setQuestaoId(rs.getInt("questao_id"));
            aq.setOrdem(rs.getInt("ordem"));
            aq.setValor(rs.getBigDecimal("valor"));

            Questao q = new Questao();
            q.setId(rs.getInt("questao_id"));
            q.setEnunciado(rs.getString("enunciado"));
            q.setTipo(rs.getString("tipo"));
            q.setDisciplina(rs.getString("disciplina"));
            q.setDificuldade(rs.getString("dificuldade"));
            if ("MULTIPLA_ESCOLHA".equals(q.getTipo())) {
                List<Alternativa> alts = questaoDAO.listarAlternativasPorQuestao(q.getId());
                q.setAlternativas(alts);
            }
            aq.setQuestao(q);
            return aq;
        }, avaliacaoId);
    }

    public void atribuirUsuario(int avaliacaoId, int usuarioId) {
        String sql = """
                INSERT INTO avaliacao_usuario (avaliacao_id, usuario_id, status)
                VALUES (?, ?, 'PENDENTE')
                ON CONFLICT (avaliacao_id, usuario_id) DO NOTHING
                """;
        jdbc.update(sql, avaliacaoId, usuarioId);
    }

    public int atribuirTurma(int avaliacaoId, int turmaId) {
        String sql = """
                INSERT INTO avaliacao_usuario (avaliacao_id, usuario_id, status)
                SELECT ?, u.id, 'PENDENTE'
                FROM usuario u
                WHERE u.turma_id = ? AND u.perfil = 'ALUNO' AND u.ativo = TRUE
                ON CONFLICT (avaliacao_id, usuario_id) DO NOTHING
                """;
        return jdbc.update(sql, avaliacaoId, turmaId);
    }

    public List<AvaliacaoUsuario> listarAtribuidos(int avaliacaoId) {
        String sql = """
                SELECT au.avaliacao_id, au.usuario_id, u.nome AS usuario_nome, t.codigo AS turma_codigo,
                       au.status, au.enviada_em,
                       SUM(r.nota_obtida) AS nota_obtida,
                       (SELECT COALESCE(SUM(aq.valor), 0) FROM avaliacao_questao aq WHERE aq.avaliacao_id = au.avaliacao_id) AS valor_total
                FROM avaliacao_usuario au
                JOIN usuario u ON u.id = au.usuario_id
                LEFT JOIN turma t ON t.id = u.turma_id
                LEFT JOIN resposta r ON r.avaliacao_id = au.avaliacao_id AND r.usuario_id = au.usuario_id
                WHERE au.avaliacao_id = ?
                GROUP BY au.avaliacao_id, au.usuario_id, u.nome, t.codigo, au.status, au.enviada_em
                ORDER BY u.nome
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            AvaliacaoUsuario au = new AvaliacaoUsuario();
            au.setAvaliacaoId(rs.getInt("avaliacao_id"));
            au.setUsuarioId(rs.getInt("usuario_id"));
            au.setUsuarioNome(rs.getString("usuario_nome"));
            au.setTurmaCodigo(rs.getString("turma_codigo"));
            au.setStatus(rs.getString("status"));
            Timestamp ts = rs.getTimestamp("enviada_em");
            if (ts != null) {
                au.setEnviadaEm(ts.toLocalDateTime());
            }
            au.setNotaObtida(rs.getBigDecimal("nota_obtida"));
            au.setValorTotal(rs.getBigDecimal("valor_total"));
            return au;
        }, avaliacaoId);
    }

    public List<AvaliacaoUsuario> listarAvaliacoesDoAluno(int usuarioId) {
        String sql = """
                SELECT a.id AS avaliacao_id, au.usuario_id, a.titulo, a.descricao, a.tipo,
                       a.data_aplicacao, a.hora_inicio, a.hora_fim,
                       au.status, au.enviada_em,
                       SUM(r.nota_obtida) AS nota_obtida,
                       (SELECT COALESCE(SUM(aq.valor), 0) FROM avaliacao_questao aq WHERE aq.avaliacao_id = a.id) AS valor_total
                FROM avaliacao_usuario au
                JOIN avaliacao a ON a.id = au.avaliacao_id
                LEFT JOIN resposta r ON r.avaliacao_id = au.avaliacao_id AND r.usuario_id = au.usuario_id
                WHERE au.usuario_id = ?
                GROUP BY a.id, au.usuario_id, a.titulo, a.descricao, a.tipo,
                         a.data_aplicacao, a.hora_inicio, a.hora_fim, au.status, au.enviada_em
                ORDER BY a.data_aplicacao DESC, a.id DESC
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            AvaliacaoUsuario au = new AvaliacaoUsuario();
            au.setAvaliacaoId(rs.getInt("avaliacao_id"));
            au.setUsuarioId(rs.getInt("usuario_id"));
            au.setTitulo(rs.getString("titulo"));
            au.setDescricao(rs.getString("descricao"));
            au.setTipo(rs.getString("tipo"));
            Date dt = rs.getDate("data_aplicacao");
            if (dt != null) {
                au.setDataAplicacao(dt.toLocalDate());
            }
            Time hi = rs.getTime("hora_inicio");
            if (hi != null) {
                au.setHoraInicio(hi.toLocalTime());
            }
            Time hf = rs.getTime("hora_fim");
            if (hf != null) {
                au.setHoraFim(hf.toLocalTime());
            }
            au.setStatus(rs.getString("status"));
            Timestamp ts = rs.getTimestamp("enviada_em");
            if (ts != null) {
                au.setEnviadaEm(ts.toLocalDateTime());
            }
            au.setNotaObtida(rs.getBigDecimal("nota_obtida"));
            au.setValorTotal(rs.getBigDecimal("valor_total"));
            return au;
        }, usuarioId);
    }

    public boolean usuarioEstaAtribuido(int avaliacaoId, int usuarioId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM avaliacao_usuario WHERE avaliacao_id = ? AND usuario_id = ?",
                Integer.class,
                avaliacaoId,
                usuarioId
        );
        return count != null && count > 0;
    }

    public void atualizarStatusUsuario(int avaliacaoId, int usuarioId, String status, boolean registrarEnvio) {
        if (registrarEnvio) {
            jdbc.update(
                    "UPDATE avaliacao_usuario SET status = ?, enviada_em = CURRENT_TIMESTAMP WHERE avaliacao_id = ? AND usuario_id = ?",
                    status, avaliacaoId, usuarioId
            );
        } else {
            jdbc.update(
                    "UPDATE avaliacao_usuario SET status = ? WHERE avaliacao_id = ? AND usuario_id = ?",
                    status, avaliacaoId, usuarioId
            );
        }
    }

    public Optional<BigDecimal> buscarValorQuestao(int avaliacaoId, int questaoId) {
        List<BigDecimal> lista = jdbc.query(
                "SELECT valor FROM avaliacao_questao WHERE avaliacao_id = ? AND questao_id = ?",
                (rs, rowNum) -> rs.getBigDecimal("valor"),
                avaliacaoId, questaoId
        );
        return lista.stream().findFirst();
    }

    public int contarRespostasSemNota(int avaliacaoId, int usuarioId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM resposta WHERE avaliacao_id = ? AND usuario_id = ? AND nota_obtida IS NULL",
                Integer.class,
                avaliacaoId,
                usuarioId
        );
        return count != null ? count : 0;
    }
}
