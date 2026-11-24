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
        validarQuestao(q);
        if (q.getCriadoPor() == null) {
            q.setCriadoPor(1);
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

    @Transactional
    public Questao atualizar(int id, Questao q) {
        Questao atual = buscarPorId(id);
        validarQuestao(q);
        q.setId(id);

        boolean temRespostas = questaoDAO.contarRespostasDaQuestao(id) > 0;
        if (temRespostas) {
            if (!atual.getTipo().equals(q.getTipo())) {
                throw new IllegalStateException("Não é possível alterar o tipo de uma questão que já possui respostas.");
            }
            if ("MULTIPLA_ESCOLHA".equals(atual.getTipo())) {
                String corretaAtual = atual.getAlternativas().stream()
                        .filter(Alternativa::isCorreta)
                        .map(Alternativa::getLetra)
                        .findFirst()
                        .orElse("");
                String novaCorreta = q.getAlternativas().stream()
                        .filter(Alternativa::isCorreta)
                        .map(Alternativa::getLetra)
                        .findFirst()
                        .orElse("");
                if (!corretaAtual.equals(novaCorreta)) {
                    throw new IllegalStateException("Não é possível alterar a alternativa correta de uma questão que já possui respostas.");
                }
                questaoDAO.atualizar(q);
                for (Alternativa alt : q.getAlternativas()) {
                    questaoDAO.atualizarTextoAlternativa(id, alt.getLetra(), alt.getTexto().trim());
                }
            } else {
                questaoDAO.atualizar(q);
            }
            return buscarPorId(id);
        }

        questaoDAO.atualizar(q);
        questaoDAO.excluirAlternativasPorQuestao(id);
        if ("MULTIPLA_ESCOLHA".equals(q.getTipo())) {
            for (Alternativa alt : q.getAlternativas()) {
                alt.setQuestaoId(id);
                questaoDAO.inserirAlternativa(alt);
            }
        }
        return buscarPorId(id);
    }

    private void validarQuestao(Questao q) {
        if (q.getEnunciado() == null || q.getEnunciado().isBlank()) {
            throw new IllegalArgumentException("O enunciado da questão é obrigatório.");
        }
        if (q.getDisciplina() == null || q.getDisciplina().isBlank()) {
            throw new IllegalArgumentException("A disciplina é obrigatória.");
        }
        if (!"MULTIPLA_ESCOLHA".equals(q.getTipo()) && !"TEXTO_LIVRE".equals(q.getTipo())) {
            throw new IllegalArgumentException("Tipo de questão inválido.");
        }
        if (q.getDificuldade() == null || q.getDificuldade().isBlank()) {
            q.setDificuldade("MEDIA");
        }

        if ("MULTIPLA_ESCOLHA".equals(q.getTipo())) {
            List<Alternativa> alts = q.getAlternativas();
            if (alts == null || alts.size() < 2) {
                throw new IllegalArgumentException("Questões de múltipla escolha exigem pelo menos 2 alternativas.");
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
            throw new IllegalArgumentException("Questões de texto livre não possuem alternativas.");
        }
    }

    public void excluir(int id) {
        if (questaoDAO.excluir(id) == 0) {
            throw new NoSuchElementException("Questao nao encontrada.");
        }
    }
}
