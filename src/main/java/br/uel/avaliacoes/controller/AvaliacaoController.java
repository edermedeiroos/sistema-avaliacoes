package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.model.Avaliacao;
import br.uel.avaliacoes.model.AvaliacaoUsuario;
import br.uel.avaliacoes.service.AvaliacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AvaliacaoController {

    private final AvaliacaoService service;

    public AvaliacaoController(AvaliacaoService service) {
        this.service = service;
    }

    @GetMapping("/avaliacoes")
    public List<Avaliacao> listar() {
        return service.listarTodas();
    }

    @GetMapping("/avaliacoes/{id}")
    public Avaliacao buscar(@PathVariable int id,
                            @RequestParam(defaultValue = "false") boolean ocultarGabarito) {
        return service.buscarDetalhada(id, ocultarGabarito);
    }

    @PostMapping("/avaliacoes")
    public ResponseEntity<Avaliacao> criar(@RequestBody Avaliacao payload) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(payload));
    }

    @PostMapping("/avaliacoes/{id}/atribuir")
    public Map<String, String> atribuir(@PathVariable int id, @RequestBody AtribuicaoBody body) {
        service.atribuir(id, body.turmaId(), body.usuarioIds());
        return Map.of("mensagem", "Atribuicao registrada.");
    }

    @GetMapping("/avaliacoes/{id}/atribuidos")
    public List<AvaliacaoUsuario> listarAtribuidos(@PathVariable int id) {
        return service.listarAtribuidos(id);
    }

    @GetMapping("/alunos/{usuarioId}/avaliacoes")
    public List<AvaliacaoUsuario> listarPorAluno(@PathVariable int usuarioId) {
        return service.listarAvaliacoesDoAluno(usuarioId);
    }

    public record AtribuicaoBody(Integer turmaId, List<Integer> usuarioIds) {}
}
