package br.uel.avaliacoes.service;

import br.uel.avaliacoes.dao.AvaliacaoDAO;
import br.uel.avaliacoes.model.Alternativa;
import br.uel.avaliacoes.model.Avaliacao;
import br.uel.avaliacoes.model.AvaliacaoQuestao;
import br.uel.avaliacoes.model.AvaliacaoUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AvaliacaoService {

    private final AvaliacaoDAO avaliacaoDAO;

    public AvaliacaoService(AvaliacaoDAO avaliacaoDAO) {
        this.avaliacaoDAO = avaliacaoDAO;
    }

    public List<Avaliacao> listarTodas() {
        return avaliacaoDAO.listarTodas();
    }

    public Avaliacao buscarDetalhada(int id, boolean ocultarGabarito) {
        Avaliacao av = avaliacaoDAO.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Avaliacao nao encontrada."));
        List<AvaliacaoQuestao> itens = avaliacaoDAO.listarQuestoesDaAvaliacao(id);
        if (ocultarGabarito) {
            for (AvaliacaoQuestao item : itens) {
                if (item.getQuestao() != null && item.getQuestao().getAlternativas() != null) {
                    for (Alternativa alt : item.getQuestao().getAlternativas()) {
                        alt.setCorreta(false);
                    }
                }
            }
        }
        av.setItens(itens);
        return av;
    }

    @Transactional
    public Avaliacao criar(Avaliacao av) {
        if (av.getTitulo() == null || av.getTitulo().isBlank()) {
            throw new IllegalArgumentException("O titulo da avaliacao e obrigatorio.");
        }
        if (av.getDataAplicacao() == null || av.getHoraInicio() == null || av.getHoraFim() == null) {
            throw new IllegalArgumentException("Data e horario de inicio e fim sao obrigatorios.");
        }
        if (!av.getHoraFim().isAfter(av.getHoraInicio())) {
            throw new IllegalArgumentException("O horario de termino deve ser posterior ao inicio.");
        }
        if (av.getItens() == null || av.getItens().isEmpty()) {
            throw new IllegalArgumentException("Selecione ao menos uma questao para a avaliacao.");
        }
        if (av.getCriadoPor() == null) {
            av.setCriadoPor(1);
        }

        int avaliacaoId = avaliacaoDAO.inserir(av);
        int ordem = 1;
        for (AvaliacaoQuestao item : av.getItens()) {
            if (item.getQuestaoId() == null) {
                throw new IllegalArgumentException("Questao invalida na montagem da avaliacao.");
            }
            if (item.getValor() == null || item.getValor().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Cada questao deve possuir valor maior que zero.");
            }
            int itemOrdem = item.getOrdem() != null ? item.getOrdem() : ordem;
            avaliacaoDAO.adicionarQuestao(avaliacaoId, item.getQuestaoId(), itemOrdem, item.getValor());
            ordem++;
        }

        if (av.getTurmaId() != null) {
            avaliacaoDAO.atribuirTurma(avaliacaoId, av.getTurmaId());
        }
        if (av.getUsuarioIds() != null) {
            for (Integer usuarioId : av.getUsuarioIds()) {
                if (usuarioId != null) {
                    avaliacaoDAO.atribuirUsuario(avaliacaoId, usuarioId);
                }
            }
        }

        return buscarDetalhada(avaliacaoId, false);
    }

    @Transactional
    public void atribuir(int avaliacaoId, Integer turmaId, List<Integer> usuarioIds) {
        avaliacaoDAO.buscarPorId(avaliacaoId)
                .orElseThrow(() -> new NoSuchElementException("Avaliacao nao encontrada."));

        boolean atribuiu = false;
        if (turmaId != null) {
            avaliacaoDAO.atribuirTurma(avaliacaoId, turmaId);
            atribuiu = true;
        }
        if (usuarioIds != null) {
            for (Integer usuarioId : usuarioIds) {
                if (usuarioId != null) {
                    avaliacaoDAO.atribuirUsuario(avaliacaoId, usuarioId);
                    atribuiu = true;
                }
            }
        }
        if (!atribuiu) {
            throw new IllegalArgumentException("Informe uma turma ou lista de alunos para atribuicao.");
        }
    }

    public List<AvaliacaoUsuario> listarAtribuidos(int avaliacaoId) {
        return avaliacaoDAO.listarAtribuidos(avaliacaoId);
    }

    public List<AvaliacaoUsuario> listarAvaliacoesDoAluno(int usuarioId) {
        return avaliacaoDAO.listarAvaliacoesDoAluno(usuarioId);
    }
}
