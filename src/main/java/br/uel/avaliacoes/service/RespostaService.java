package br.uel.avaliacoes.service;

import br.uel.avaliacoes.dao.AvaliacaoDAO;
import br.uel.avaliacoes.dao.QuestaoDAO;
import br.uel.avaliacoes.dao.RespostaDAO;
import br.uel.avaliacoes.model.Alternativa;
import br.uel.avaliacoes.model.AvaliacaoQuestao;
import br.uel.avaliacoes.model.PendenciaCorrecao;
import br.uel.avaliacoes.model.Resposta;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RespostaService {

    private final RespostaDAO respostaDAO;
    private final AvaliacaoDAO avaliacaoDAO;
    private final QuestaoDAO questaoDAO;

    public RespostaService(RespostaDAO respostaDAO, AvaliacaoDAO avaliacaoDAO, QuestaoDAO questaoDAO) {
        this.respostaDAO = respostaDAO;
        this.avaliacaoDAO = avaliacaoDAO;
        this.questaoDAO = questaoDAO;
    }

    @Transactional
    public List<Resposta> enviarRespostas(int avaliacaoId, int usuarioId, List<Resposta> respostas) {
        if (!avaliacaoDAO.usuarioEstaAtribuido(avaliacaoId, usuarioId)) {
            throw new IllegalStateException("Aluno nao esta atribuido a esta avaliacao.");
        }
        if (respostaDAO.jaRespondeu(avaliacaoId, usuarioId)) {
            throw new IllegalStateException("Esta avaliacao ja foi respondida por este aluno.");
        }
        if (respostas == null || respostas.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma resposta foi enviada.");
        }

        List<AvaliacaoQuestao> itens = avaliacaoDAO.listarQuestoesDaAvaliacao(avaliacaoId);
        Map<Integer, AvaliacaoQuestao> itensPorQuestao = itens.stream()
                .collect(Collectors.toMap(AvaliacaoQuestao::getQuestaoId, Function.identity()));

        if (respostas.size() != itens.size()) {
            throw new IllegalArgumentException("Responda todas as questoes da avaliacao antes de enviar.");
        }

        boolean possuiDiscursiva = false;

        for (Resposta r : respostas) {
            if (r.getQuestaoId() == null || !itensPorQuestao.containsKey(r.getQuestaoId())) {
                throw new IllegalArgumentException("Questao nao pertence a esta avaliacao.");
            }
            AvaliacaoQuestao item = itensPorQuestao.get(r.getQuestaoId());
            String tipo = item.getQuestao().getTipo();

            r.setAvaliacaoId(avaliacaoId);
            r.setUsuarioId(usuarioId);

            if ("MULTIPLA_ESCOLHA".equals(tipo)) {
                if (r.getAlternativaId() == null) {
                    throw new IllegalArgumentException("Selecione uma alternativa na questao " + item.getOrdem() + ".");
                }
                Alternativa alt = questaoDAO.buscarAlternativaPorId(r.getAlternativaId())
                        .orElseThrow(() -> new IllegalArgumentException("Alternativa invalida."));
                if (!alt.getQuestaoId().equals(r.getQuestaoId())) {
                    throw new IllegalArgumentException("Alternativa nao pertence a questao informada.");
                }
                r.setTextoResposta(null);
                r.setNotaObtida(alt.isCorreta() ? item.getValor() : BigDecimal.ZERO);
            } else {
                if (r.getTextoResposta() == null || r.getTextoResposta().isBlank()) {
                    throw new IllegalArgumentException("Preencha a resposta da questao discursiva " + item.getOrdem() + ".");
                }
                r.setAlternativaId(null);
                r.setNotaObtida(null);
                possuiDiscursiva = true;
            }

            respostaDAO.inserir(r);
        }

        String novoStatus = possuiDiscursiva ? "RESPONDIDA" : "CORRIGIDA";
        avaliacaoDAO.atualizarStatusUsuario(avaliacaoId, usuarioId, novoStatus, true);

        return respostaDAO.listarPorAvaliacaoEUsuario(avaliacaoId, usuarioId);
    }

    public List<Resposta> listarRespostas(int avaliacaoId, int usuarioId) {
        return respostaDAO.listarPorAvaliacaoEUsuario(avaliacaoId, usuarioId);
    }

    public List<PendenciaCorrecao> listarDiscursivasPendentes() {
        return respostaDAO.listarDiscursivasPendentes();
    }

    @Transactional
    public void corrigirDiscursiva(int respostaId, BigDecimal nota, int professorId) {
        if (nota == null || nota.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("A nota nao pode ser negativa.");
        }
        Resposta r = respostaDAO.buscarPorId(respostaId)
                .orElseThrow(() -> new NoSuchElementException("Resposta nao encontrada."));

        BigDecimal valorMaximo = avaliacaoDAO.buscarValorQuestao(r.getAvaliacaoId(), r.getQuestaoId())
                .orElseThrow(() -> new NoSuchElementException("Questao nao vinculada a avaliacao."));
        if (nota.compareTo(valorMaximo) > 0) {
            throw new IllegalArgumentException("A nota nao pode ultrapassar o valor maximo da questao (" + valorMaximo + ").");
        }

        respostaDAO.atualizarNotaDiscursiva(respostaId, nota, professorId);

        int pendentes = avaliacaoDAO.contarRespostasSemNota(r.getAvaliacaoId(), r.getUsuarioId());
        if (pendentes == 0) {
            avaliacaoDAO.atualizarStatusUsuario(r.getAvaliacaoId(), r.getUsuarioId(), "CORRIGIDA", false);
        }
    }
}
