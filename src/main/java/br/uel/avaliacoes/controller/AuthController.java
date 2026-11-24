package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.TurmaDAO;
import br.uel.avaliacoes.dao.UsuarioDAO;
import br.uel.avaliacoes.model.Turma;
import br.uel.avaliacoes.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UsuarioDAO usuarioDAO;
    private final TurmaDAO turmaDAO;

    public AuthController(UsuarioDAO usuarioDAO, TurmaDAO turmaDAO) {
        this.usuarioDAO = usuarioDAO;
        this.turmaDAO = turmaDAO;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String senha = body.get("senha");
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Informe e-mail e senha."));
        }

        Optional<Usuario> opt = usuarioDAO.buscarPorEmailESenha(email.trim(), senha);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "E-mail ou senha invalidos."));
        }

        Usuario u = opt.get();
        u.setSenha(null);
        return ResponseEntity.ok(u);
    }

    @GetMapping("/turmas")
    public List<Turma> listarTurmas() {
        return turmaDAO.listarTodas();
    }

    @PostMapping("/turmas")
    public ResponseEntity<Turma> criarTurma(@RequestBody Turma body) {
        if (body.getCodigo() == null || body.getCodigo().isBlank()) {
            throw new IllegalArgumentException("O código da turma é obrigatório.");
        }
        if (body.getNome() == null || body.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome da turma é obrigatório.");
        }
        if (body.getAnoSemestre() == null || body.getAnoSemestre().isBlank()) {
            throw new IllegalArgumentException("O ano/semestre é obrigatório.");
        }
        String codigo = body.getCodigo().trim().toUpperCase();
        if (turmaDAO.existePorCodigo(codigo)) {
            throw new IllegalStateException("Já existe uma turma com o código " + codigo + ".");
        }
        Turma nova = new Turma();
        nova.setCodigo(codigo);
        nova.setNome(body.getNome().trim());
        nova.setAnoSemestre(body.getAnoSemestre().trim());
        nova.setId(turmaDAO.inserir(nova));
        return ResponseEntity.status(HttpStatus.CREATED).body(nova);
    }

    @GetMapping("/usuarios")
    public List<Usuario> listarUsuarios(@RequestParam(required = false) String perfil) {
        return usuarioDAO.listarPorPerfil(perfil);
    }
}
