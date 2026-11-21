package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.model.PendenciaCorrecao;
import br.uel.avaliacoes.model.Resposta;
import br.uel.avaliacoes.service.RespostaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RespostaController {

    private final RespostaService service;

    public RespostaController(RespostaService service) {
        this.service = service;
    }

    @PostMapping("/avaliacoes/{avaliacaoId}/respostas")
    public ResponseEntity<List<Resposta>> enviar(@PathVariable int avaliacaoId,
                                                 @RequestBody EnvioBody body) {
        if (body.usuarioId() == null) {
            throw new IllegalArgumentException("Usuario nao informado.");
        }
        List<Resposta> salvas = service.enviarRespostas(avaliacaoId, body.usuarioId(), body.itens());
        return ResponseEntity.status(HttpStatus.CREATED).body(salvas);
    }

    @GetMapping("/avaliacoes/{avaliacaoId}/respostas/{usuarioId}")
    public List<Resposta> buscarRespostas(@PathVariable int avaliacaoId,
                                          @PathVariable int usuarioId) {
        return service.listarRespostas(avaliacaoId, usuarioId);
    }

    @GetMapping("/correcoes/pendentes")
    public List<PendenciaCorrecao> pendentes() {
        return service.listarDiscursivasPendentes();
    }

    @PutMapping("/correcoes/{respostaId}")
    public Map<String, String> corrigir(@PathVariable int respostaId,
                                        @RequestBody NotaBody body) {
        int profId = body.professorId() != null ? body.professorId() : 1;
        service.corrigirDiscursiva(respostaId, body.nota(), profId);
        return Map.of("mensagem", "Nota registrada com sucesso.");
    }

    public record EnvioBody(Integer usuarioId, List<Resposta> itens) {}

    public record NotaBody(BigDecimal nota, Integer professorId) {}
}
