package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.PendenciaCorrecao;
import br.uel.avaliacoes.model.Resposta;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class RespostaDAO {

    private final JdbcTemplate jdbc;

    public RespostaDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int inserir(Resposta r) {
        String sql = """
                INSERT INTO resposta (avaliacao_id, usuario_id, questao_id, alternativa_id, texto_resposta, nota_obtida, corrigida_em)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
        Timestamp corrigidaEm = r.getNotaObtida() != null ? new Timestamp(System.currentTimeMillis()) : null;
        Integer id = jdbc.queryForObject(
                sql,
                new Object[]{
                        r.getAvaliacaoId(),
                        r.getUsuarioId(),
                        r.getQuestaoId(),
                        r.getAlternativaId(),
                        r.getTextoResposta(),
                        r.getNotaObtida(),
                        corrigidaEm
                },
                new int[]{
                        Types.INTEGER,
                        Types.INTEGER,
                        Types.INTEGER,
                        Types.INTEGER,
                        Types.VARCHAR,
                        Types.NUMERIC,
                        Types.TIMESTAMP
                },
                Integer.class
        );
        return id != null ? id : 0;
    }

    public boolean jaRespondeu(int avaliacaoId, int usuarioId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM resposta WHERE avaliacao_id = ? AND usuario_id = ?",
                Integer.class,
                avaliacaoId,
                usuarioId
        );
        return count != null && count > 0;
    }

    public List<Resposta> listarPorAvaliacaoEUsuario(int avaliacaoId, int usuarioId) {
        String sql = """
                SELECT r.id, r.avaliacao_id, r.usuario_id, r.questao_id,
                       q.enunciado, q.tipo AS tipo_questao, aq.valor AS valor_questao,
                       r.alternativa_id, alt.letra AS alternativa_letra, alt.texto AS alternativa_texto,
                       r.texto_resposta, r.nota_obtida, r.corrigida_por, r.corrigida_em, r.respondida_em
                FROM resposta r
                JOIN avaliacao_questao aq ON aq.avaliacao_id = r.avaliacao_id AND aq.questao_id = r.questao_id
                JOIN questao q ON q.id = r.questao_id
                LEFT JOIN alternativa alt ON alt.id = r.alternativa_id
                WHERE r.avaliacao_id = ? AND r.usuario_id = ?
                ORDER BY aq.ordem
                """;
        return jdbc.query(sql, (rs, rowNum) -> mapResposta(rs), avaliacaoId, usuarioId);
    }

    public Optional<Resposta> buscarPorId(int id) {
        String sql = """
                SELECT r.id, r.avaliacao_id, r.usuario_id, r.questao_id,
                       q.enunciado, q.tipo AS tipo_questao, aq.valor AS valor_questao,
                       r.alternativa_id, alt.letra AS alternativa_letra, alt.texto AS alternativa_texto,
                       r.texto_resposta, r.nota_obtida, r.corrigida_por, r.corrigida_em, r.respondida_em
                FROM resposta r
                JOIN avaliacao_questao aq ON aq.avaliacao_id = r.avaliacao_id AND aq.questao_id = r.questao_id
                JOIN questao q ON q.id = r.questao_id
                LEFT JOIN alternativa alt ON alt.id = r.alternativa_id
                WHERE r.id = ?
                """;
        List<Resposta> lista = jdbc.query(sql, (rs, rowNum) -> mapResposta(rs), id);
        return lista.stream().findFirst();
    }

    private Resposta mapResposta(java.sql.ResultSet rs) throws java.sql.SQLException {
        Resposta r = new Resposta();
        r.setId(rs.getInt("id"));
        r.setAvaliacaoId(rs.getInt("avaliacao_id"));
        r.setUsuarioId(rs.getInt("usuario_id"));
        r.setQuestaoId(rs.getInt("questao_id"));
        r.setEnunciado(rs.getString("enunciado"));
        r.setTipoQuestao(rs.getString("tipo_questao"));
        r.setValorQuestao(rs.getBigDecimal("valor_questao"));
        int altId = rs.getInt("alternativa_id");
        r.setAlternativaId(rs.wasNull() ? null : altId);
        r.setAlternativaLetra(rs.getString("alternativa_letra"));
        r.setAlternativaTexto(rs.getString("alternativa_texto"));
        r.setTextoResposta(rs.getString("texto_resposta"));
        r.setNotaObtida(rs.getBigDecimal("nota_obtida"));
        int corrPor = rs.getInt("corrigida_por");
        r.setCorrigidaPor(rs.wasNull() ? null : corrPor);
        Timestamp corrEm = rs.getTimestamp("corrigida_em");
        if (corrEm != null) {
            r.setCorrigidaEm(corrEm.toLocalDateTime());
        }
        Timestamp respEm = rs.getTimestamp("respondida_em");
        if (respEm != null) {
            r.setRespondidaEm(respEm.toLocalDateTime());
        }
        return r;
    }

    public List<PendenciaCorrecao> listarDiscursivasPendentes() {
        String sql = """
                SELECT r.id AS resposta_id, r.avaliacao_id, a.titulo AS avaliacao_titulo,
                       r.usuario_id, u.nome AS aluno_nome, COALESCE(t.codigo, '-') AS turma_codigo,
                       r.questao_id, q.enunciado, r.texto_resposta, aq.valor AS valor_maximo,
                       r.respondida_em
                FROM resposta r
                JOIN avaliacao a ON a.id = r.avaliacao_id
                JOIN usuario u ON u.id = r.usuario_id
                LEFT JOIN turma t ON t.id = u.turma_id
                JOIN questao q ON q.id = r.questao_id
                JOIN avaliacao_questao aq ON aq.avaliacao_id = r.avaliacao_id AND aq.questao_id = r.questao_id
                WHERE q.tipo = 'TEXTO_LIVRE' AND r.nota_obtida IS NULL
                ORDER BY r.respondida_em ASC, r.id ASC
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            PendenciaCorrecao p = new PendenciaCorrecao();
            p.setRespostaId(rs.getInt("resposta_id"));
            p.setAvaliacaoId(rs.getInt("avaliacao_id"));
            p.setAvaliacaoTitulo(rs.getString("avaliacao_titulo"));
            p.setUsuarioId(rs.getInt("usuario_id"));
            p.setAlunoNome(rs.getString("aluno_nome"));
            p.setTurmaCodigo(rs.getString("turma_codigo"));
            p.setQuestaoId(rs.getInt("questao_id"));
            p.setEnunciado(rs.getString("enunciado"));
            p.setTextoResposta(rs.getString("texto_resposta"));
            p.setValorMaximo(rs.getBigDecimal("valor_maximo"));
            Timestamp ts = rs.getTimestamp("respondida_em");
            if (ts != null) {
                p.setRespondidaEm(ts.toLocalDateTime());
            }
            return p;
        });
    }

    public void atualizarNotaDiscursiva(int respostaId, BigDecimal nota, int corrigidaPor) {
        String sql = """
                UPDATE resposta
                SET nota_obtida = ?, corrigida_por = ?, corrigida_em = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        jdbc.update(sql, nota, corrigidaPor, respostaId);
    }
}
