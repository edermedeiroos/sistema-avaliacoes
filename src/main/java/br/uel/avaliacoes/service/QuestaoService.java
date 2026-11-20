package br.uel.avaliacoes.service;

import br.uel.avaliacoes.dao.QuestaoDAO;
import br.uel.avaliacoes.model.Alternativa;
import br.uel.avaliacoes.model.Questao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class QuestaoService {

    private final QuestaoDAO questaoDAO;

    public QuestaoService(QuestaoDAO questaoDAO) {
        this.questaoDAO = questaoDAO;
    }

    public List<Questao> listar(String tipo, String busca) {
        return questaoDAO.listar(tipo, busca);
    }

    public Questao buscarPorId(int id) {
        return questaoDAO.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Questao nao encontrada."));
    }

    @Transactional
    public Questao criar(Questao q) {
        if (q.getEnunciado() == null || q.getEnunciado().isBlank()) {
            throw new IllegalArgumentException("O enunciado da questao e obrigatorio.");
        }
        if (q.getDisciplina() == null || q.getDisciplina().isBlank()) {
            throw new IllegalArgumentException("A disciplina e obrigatoria.");
        }
        if (!"MULTIPLA_ESCOLHA".equals(q.getTipo()) && !"TEXTO_LIVRE".equals(q.getTipo())) {
            throw new IllegalArgumentException("Tipo de questao invalido.");
        }
        if (q.getDificuldade() == null || q.getDificuldade().isBlank()) {
            q.setDificuldade("MEDIA");
        }
        if (q.getCriadoPor() == null) {
            q.setCriadoPor(1);
        }

        if ("MULTIPLA_ESCOLHA".equals(q.getTipo())) {
            List<Alternativa> alts = q.getAlternativas();
            if (alts == null || alts.size() < 2) {
                throw new IllegalArgumentException("Questoes de multipla escolha exigem pelo menos 2 alternativas.");
            }
            long corretas = alts.stream().filter(Alternativa::isCorreta).count();
            if (corretas != 1) {
                throw new IllegalArgumentException("Marque exatamente 1 alternativa correta.");
            }
            for (Alternativa alt : alts) {
                if (alt.getTexto() == null || alt.getTexto().isBlank()) {
                    throw new IllegalArgumentException("Todas as alternativas devem possuir texto.");
                }
            }
        } else if (q.getAlternativas() != null && !q.getAlternativas().isEmpty()) {
            throw new IllegalArgumentException("Questoes de texto livre nao possuem alternativas.");
        }

        int questaoId = questaoDAO.inserir(q);
        q.setId(questaoId);

        if ("MULTIPLA_ESCOLHA".equals(q.getTipo())) {
            for (Alternativa alt : q.getAlternativas()) {
                alt.setQuestaoId(questaoId);
                int altId = questaoDAO.inserirAlternativa(alt);
                alt.setId(altId);
            }
        }
        return buscarPorId(questaoId);
    }

    public void excluir(int id) {
        if (questaoDAO.excluir(id) == 0) {
            throw new NoSuchElementException("Questao nao encontrada.");
        }
    }
}
