package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.model.Questao;
import br.uel.avaliacoes.service.QuestaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questoes")
public class QuestaoController {

    private final QuestaoService questaoService;

    public QuestaoController(QuestaoService questaoService) {
        this.questaoService = questaoService;
    }

    @GetMapping
    public List<Questao> listar(@RequestParam(required = false) String tipo,
                                @RequestParam(required = false) String busca) {
        return questaoService.listar(tipo, busca);
    }

    @GetMapping("/{id}")
    public Questao buscarPorId(@PathVariable int id) {
        return questaoService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Questao> criar(@RequestBody Questao questao) {
        Questao criada = questaoService.criar(questao);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @PutMapping("/{id}")
    public Questao atualizar(@PathVariable int id, @RequestBody Questao questao) {
        return questaoService.atualizar(id, questao);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable int id) {
        questaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
