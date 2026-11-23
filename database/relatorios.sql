-- Relatorio 1: Desempenho medio por avaliacao
-- Recursos: INNER JOIN, subconsulta com SUM por aluno, COUNT/AVG/MAX/MIN, GROUP BY, HAVING, WHERE com BETWEEN e IN.
SELECT a.id AS avaliacao_id,
       a.titulo,
       a.tipo,
       a.data_aplicacao,
       COUNT(notas.usuario_id) AS total_respondentes,
       ROUND(AVG(notas.nota_total), 2) AS nota_media,
       MAX(notas.nota_total) AS maior_nota,
       MIN(notas.nota_total) AS menor_nota
FROM avaliacao a
INNER JOIN (
    SELECT r.avaliacao_id, r.usuario_id, SUM(r.nota_obtida) AS nota_total
    FROM resposta r
    INNER JOIN avaliacao_usuario au
        ON au.avaliacao_id = r.avaliacao_id AND au.usuario_id = r.usuario_id
    WHERE au.status = 'CORRIGIDA'
    GROUP BY r.avaliacao_id, r.usuario_id
) notas ON notas.avaliacao_id = a.id
WHERE a.data_aplicacao BETWEEN '2026-10-01' AND '2026-11-30'
  AND a.tipo IN ('PROVA', 'SIMULADO', 'QUIZ')
GROUP BY a.id, a.titulo, a.tipo, a.data_aplicacao
HAVING COUNT(notas.usuario_id) >= 1
ORDER BY a.data_aplicacao DESC, a.id DESC;

-- Relatorio 2: Desempenho individual do aluno vs. media da avaliacao
-- Recursos: INNER JOIN, LEFT JOIN, subconsulta correlacionada, SUM, AVG, GROUP BY, ORDER BY, WHERE com LIKE.
SELECT a.id AS avaliacao_id,
       a.titulo AS avaliacao_titulo,
       u.id AS usuario_id,
       u.nome AS aluno_nome,
       COALESCE(t.codigo, '-') AS turma_codigo,
       SUM(r.nota_obtida) AS nota_aluno,
       ROUND((
           SELECT AVG(sub.total_aluno)
           FROM (
               SELECT SUM(r2.nota_obtida) AS total_aluno
               FROM resposta r2
               INNER JOIN avaliacao_usuario au2
                   ON au2.avaliacao_id = r2.avaliacao_id AND au2.usuario_id = r2.usuario_id
               WHERE r2.avaliacao_id = a.id AND au2.status = 'CORRIGIDA'
               GROUP BY r2.usuario_id
           ) sub
       ), 2) AS media_turma
FROM avaliacao_usuario au
INNER JOIN avaliacao a ON a.id = au.avaliacao_id
INNER JOIN usuario u ON u.id = au.usuario_id
LEFT JOIN turma t ON t.id = u.turma_id
INNER JOIN resposta r ON r.avaliacao_id = au.avaliacao_id AND r.usuario_id = au.usuario_id
WHERE au.status = 'CORRIGIDA'
  AND (LOWER(u.nome) LIKE LOWER('%') OR LOWER(COALESCE(t.codigo, '')) LIKE LOWER('%'))
GROUP BY a.id, a.titulo, a.data_aplicacao, u.id, u.nome, t.codigo
ORDER BY a.data_aplicacao DESC, nota_aluno DESC, u.nome;

-- Relatorio 3: Ranking das questoes com maior percentual de erro
-- Recursos: INNER JOIN, COUNT, SUM, GROUP BY, HAVING, RANK(), ORDER BY e FETCH FIRST 10 ROWS ONLY.
SELECT RANK() OVER (
           ORDER BY (SUM(CASE WHEN r.nota_obtida < (aq.valor * 0.5) THEN 1 ELSE 0 END)::numeric * 100.0 / COUNT(*)) DESC,
                    SUM(CASE WHEN r.nota_obtida < (aq.valor * 0.5) THEN 1 ELSE 0 END) DESC
       ) AS posicao,
       q.id AS questao_id,
       q.enunciado,
       q.disciplina,
       COUNT(*) AS total_respostas,
       SUM(CASE WHEN r.nota_obtida < (aq.valor * 0.5) THEN 1 ELSE 0 END) AS total_erros,
       ROUND(SUM(CASE WHEN r.nota_obtida < (aq.valor * 0.5) THEN 1 ELSE 0 END)::numeric * 100.0 / COUNT(*), 1) AS percentual_erro
FROM resposta r
INNER JOIN avaliacao_questao aq
    ON aq.avaliacao_id = r.avaliacao_id AND aq.questao_id = r.questao_id
INNER JOIN questao q ON q.id = r.questao_id
WHERE r.nota_obtida IS NOT NULL
GROUP BY q.id, q.enunciado, q.disciplina
HAVING COUNT(*) >= 3
ORDER BY percentual_erro DESC, total_erros DESC, q.id
FETCH FIRST 10 ROWS ONLY;

-- Relatorio 4A: Resumo de entregas vs. pendencias por avaliacao (grafico empilhado)
-- Recursos: LEFT JOIN, COUNT, SUM, GROUP BY, ORDER BY.
SELECT a.id AS avaliacao_id,
       a.titulo,
       COUNT(au.usuario_id) AS total_atribuidos,
       COALESCE(SUM(CASE WHEN au.status <> 'PENDENTE' THEN 1 ELSE 0 END), 0) AS total_respondidas,
       COALESCE(SUM(CASE WHEN au.status = 'PENDENTE' THEN 1 ELSE 0 END), 0) AS total_pendentes
FROM avaliacao a
LEFT JOIN avaliacao_usuario au ON au.avaliacao_id = a.id
GROUP BY a.id, a.titulo, a.data_aplicacao
ORDER BY a.data_aplicacao DESC, a.id DESC;

-- Relatorio 4B: Alunos que ainda nao responderam cada avaliacao
-- Recursos: INNER JOIN e LEFT JOIN com filtro IS NULL.
SELECT au.avaliacao_id,
       a.titulo AS avaliacao_titulo,
       au.usuario_id,
       u.nome AS aluno_nome,
       COALESCE(t.codigo, '-') AS turma_codigo,
       au.status
FROM avaliacao_usuario au
INNER JOIN avaliacao a ON a.id = au.avaliacao_id
INNER JOIN usuario u ON u.id = au.usuario_id
LEFT JOIN turma t ON t.id = u.turma_id
LEFT JOIN resposta r
    ON r.avaliacao_id = au.avaliacao_id AND r.usuario_id = au.usuario_id
WHERE r.id IS NULL
ORDER BY a.data_aplicacao DESC, u.nome;

-- Relatorio 4C: Questoes cadastradas nunca utilizadas em avaliacoes
-- Recursos: INNER JOIN e LEFT JOIN com filtro IS NULL.
SELECT q.id,
       q.enunciado,
       q.tipo,
       q.disciplina,
       q.dificuldade,
       u.nome AS criado_por_nome
FROM questao q
INNER JOIN usuario u ON u.id = q.criado_por
LEFT JOIN avaliacao_questao aq ON aq.questao_id = q.id
WHERE aq.questao_id IS NULL
ORDER BY q.id;
