package br.uel.avaliacoes.controller;

import br.uel.avaliacoes.dao.RelatorioDAO;
import br.uel.avaliacoes.model.DesempenhoAlunoRelatorio;
import br.uel.avaliacoes.model.MediaAvaliacaoRelatorio;
import br.uel.avaliacoes.model.RankingErroRelatorio;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioDAO dao;

    public RelatorioController(RelatorioDAO dao) {
        this.dao = dao;
    }

    @GetMapping("/medias-avaliacoes")
    public List<MediaAvaliacaoRelatorio> medias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String tipos) {
        List<String> listaTipos = null;
        if (tipos != null && !tipos.isBlank()) {
            listaTipos = Arrays.stream(tipos.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
        return dao.mediaPorAvaliacao(dataInicio, dataFim, listaTipos);
    }

    @GetMapping("/desempenho-alunos")
    public List<DesempenhoAlunoRelatorio> desempenho(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Integer avaliacaoId) {
        return dao.desempenhoVsMedia(busca, avaliacaoId);
    }

    @GetMapping("/ranking-erros")
    public List<RankingErroRelatorio> ranking(
            @RequestParam(required = false) String disciplina,
            @RequestParam(defaultValue = "10") int limite) {
        int lim = Math.max(1, Math.min(limite, 50));
        return dao.rankingQuestoesMaisErradas(disciplina, lim);
    }

    @GetMapping("/pendencias")
    public Map<String, Object> pendencias() {
        return Map.of(
                "resumo", dao.resumoPendenciasPorAvaliacao(),
                "alunosPendentes", dao.alunosPendentes(),
                "questoesNaoUsadas", dao.questoesNaoUtilizadas()
        );
    }

    @GetMapping("/disciplinas")
    public List<String> disciplinas() {
        return dao.listarDisciplinas();
    }
}
