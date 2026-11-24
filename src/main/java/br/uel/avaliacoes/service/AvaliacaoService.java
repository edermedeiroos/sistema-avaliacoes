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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

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
        validarAvaliacao(av);
        if (av.getCriadoPor() == null) {
            av.setCriadoPor(1);
        }

        int avaliacaoId = avaliacaoDAO.inserir(av);
        int ordem = 1;
        for (AvaliacaoQuestao item : av.getItens()) {
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
    public Avaliacao atualizar(int id, Avaliacao av) {
        avaliacaoDAO.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("Avaliação não encontrada."));
        validarAvaliacao(av);
        av.setId(id);
        avaliacaoDAO.atualizar(av);

        int totalRespostas = avaliacaoDAO.contarRespostasDaAvaliacao(id);
        if (totalRespostas == 0) {
            avaliacaoDAO.removerQuestoesDaAvaliacao(id);
            int ordem = 1;
            for (AvaliacaoQuestao item : av.getItens()) {
                avaliacaoDAO.adicionarQuestao(id, item.getQuestaoId(), ordem++, item.getValor());
            }
        } else {
            List<AvaliacaoQuestao> atuais = avaliacaoDAO.listarQuestoesDaAvaliacao(id);
            Map<Integer, AvaliacaoQuestao> mapaAtuais = atuais.stream()
                    .collect(Collectors.toMap(AvaliacaoQuestao::getQuestaoId, x -> x));
            Set<Integer> novosIds = av.getItens().stream()
                    .map(AvaliacaoQuestao::getQuestaoId)
                    .collect(Collectors.toSet());

            for (AvaliacaoQuestao atualItem : atuais) {
                int qId = atualItem.getQuestaoId();
                if (!novosIds.contains(qId)) {
                    if (avaliacaoDAO.contarRespostasDaQuestaoNaAvaliacao(id, qId) > 0) {
                        throw new IllegalStateException("Não é possível remover questões que já possuem respostas registradas.");
                    }
                    avaliacaoDAO.removerQuestaoDaAvaliacao(id, qId);
                }
            }

            avaliacaoDAO.deslocarOrdemQuestoes(id, 10000);
            int ordem = 1;
            for (AvaliacaoQuestao item : av.getItens()) {
                int qId = item.getQuestaoId();
                if (mapaAtuais.containsKey(qId)) {
                    AvaliacaoQuestao anterior = mapaAtuais.get(qId);
                    if (avaliacaoDAO.contarRespostasDaQuestaoNaAvaliacao(id, qId) > 0
                            && anterior.getValor().compareTo(item.getValor()) != 0) {
                        throw new IllegalStateException("Não é possível alterar o valor de questões já respondidas.");
                    }
                    avaliacaoDAO.atualizarQuestaoDaAvaliacao(id, qId, ordem++, item.getValor());
                } else {
                    avaliacaoDAO.adicionarQuestao(id, qId, ordem++, item.getValor());
                }
            }
        }

        if (av.getTurmaId() != null) {
            avaliacaoDAO.atribuirTurma(id, av.getTurmaId());
        }
        return buscarDetalhada(id, false);
    }

    @Transactional
    public void removerAtribuicao(int avaliacaoId, int usuarioId) {
        String status = avaliacaoDAO.buscarStatusUsuario(avaliacaoId, usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Atribuição não encontrada."));
        if (!"PENDENTE".equals(status)) {
            throw new IllegalStateException("Não é possível remover um aluno que já enviou respostas.");
        }
        avaliacaoDAO.removerUsuarioAtribuido(avaliacaoId, usuarioId);
    }

    private void validarAvaliacao(Avaliacao av) {
        if (av.getTitulo() == null || av.getTitulo().isBlank()) {
            throw new IllegalArgumentException("O título da avaliação é obrigatório.");
        }
        if (av.getDataAplicacao() == null || av.getHoraInicio() == null || av.getHoraFim() == null) {
            throw new IllegalArgumentException("Data e horário de início e fim são obrigatórios.");
        }
        if (!av.getHoraFim().isAfter(av.getHoraInicio())) {
            throw new IllegalArgumentException("O horário de término deve ser posterior ao início.");
        }
        if (av.getItens() == null || av.getItens().isEmpty()) {
            throw new IllegalArgumentException("Selecione ao menos uma questão para a avaliação.");
        }
        for (AvaliacaoQuestao item : av.getItens()) {
            if (item.getQuestaoId() == null) {
                throw new IllegalArgumentException("Questão inválida na montagem da avaliação.");
            }
            if (item.getValor() == null || item.getValor().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Cada questão deve possuir valor maior que zero.");
            }
        }
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
