package br.uel.avaliacoes.dao;

import br.uel.avaliacoes.model.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioDAO {

    private final JdbcTemplate jdbc;

    public UsuarioDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Usuario> mapper = (rs, rowNum) -> {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNome(rs.getString("nome"));
        u.setEmail(rs.getString("email"));
        u.setSenha(rs.getString("senha"));
        u.setPerfil(rs.getString("perfil"));
        int turmaId = rs.getInt("turma_id");
        u.setTurmaId(rs.wasNull() ? null : turmaId);
        u.setTurmaCodigo(rs.getString("turma_codigo"));
        u.setAtivo(rs.getBoolean("ativo"));
        return u;
    };

    public Optional<Usuario> buscarPorEmailESenha(String email, String senha) {
        String sql = """
                SELECT u.id, u.nome, u.email, u.senha, u.perfil, u.turma_id, t.codigo AS turma_codigo, u.ativo
                FROM usuario u
                LEFT JOIN turma t ON t.id = u.turma_id
                WHERE LOWER(u.email) = LOWER(?) AND u.senha = ? AND u.ativo = TRUE
                """;
        List<Usuario> lista = jdbc.query(sql, mapper, email, senha);
        return lista.stream().findFirst();
    }

    public Optional<Usuario> buscarPorId(int id) {
        String sql = """
                SELECT u.id, u.nome, u.email, u.senha, u.perfil, u.turma_id, t.codigo AS turma_codigo, u.ativo
                FROM usuario u
                LEFT JOIN turma t ON t.id = u.turma_id
                WHERE u.id = ?
                """;
        List<Usuario> lista = jdbc.query(sql, mapper, id);
        return lista.stream().findFirst();
    }

    public List<Usuario> listarPorPerfil(String perfil) {
        StringBuilder sql = new StringBuilder("""
                SELECT u.id, u.nome, u.email, '' AS senha, u.perfil, u.turma_id, t.codigo AS turma_codigo, u.ativo
                FROM usuario u
                LEFT JOIN turma t ON t.id = u.turma_id
                WHERE u.ativo = TRUE
                """);
        if (perfil != null && !perfil.isBlank()) {
            sql.append(" AND u.perfil = ? ORDER BY u.nome");
            return jdbc.query(sql.toString(), mapper, perfil);
        }
        sql.append(" ORDER BY u.nome");
        return jdbc.query(sql.toString(), mapper);
    }
}
