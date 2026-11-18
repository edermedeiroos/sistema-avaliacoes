DROP TABLE IF EXISTS resposta, avaliacao_usuario, avaliacao_questao, avaliacao, alternativa, questao, usuario, turma CASCADE;

CREATE TABLE turma (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nome VARCHAR(100) NOT NULL,
    ano_semestre VARCHAR(10) NOT NULL
);

CREATE TABLE usuario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha VARCHAR(100) NOT NULL,
    perfil VARCHAR(20) NOT NULL CHECK (perfil IN ('PROFESSOR', 'ALUNO')),
    turma_id INTEGER REFERENCES turma(id) ON DELETE SET NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE questao (
    id SERIAL PRIMARY KEY,
    enunciado TEXT NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('MULTIPLA_ESCOLHA', 'TEXTO_LIVRE')),
    disciplina VARCHAR(80) NOT NULL,
    dificuldade VARCHAR(15) NOT NULL CHECK (dificuldade IN ('FACIL', 'MEDIA', 'DIFICIL')),
    criado_por INTEGER NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE alternativa (
    id SERIAL PRIMARY KEY,
    questao_id INTEGER NOT NULL REFERENCES questao(id) ON DELETE CASCADE,
    letra CHAR(1) NOT NULL CHECK (letra IN ('A', 'B', 'C', 'D', 'E')),
    texto TEXT NOT NULL,
    correta BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (questao_id, letra)
);

CREATE UNIQUE INDEX uq_alternativa_correta_questao
    ON alternativa (questao_id)
    WHERE correta = TRUE;

CREATE TABLE avaliacao (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('PROVA', 'SIMULADO', 'QUIZ')),
    data_aplicacao DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fim TIME NOT NULL,
    criado_por INTEGER NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    CHECK (hora_fim > hora_inicio)
);

CREATE TABLE avaliacao_questao (
    avaliacao_id INTEGER NOT NULL REFERENCES avaliacao(id) ON DELETE CASCADE,
    questao_id INTEGER NOT NULL REFERENCES questao(id) ON DELETE RESTRICT,
    ordem INTEGER NOT NULL CHECK (ordem > 0),
    valor NUMERIC(5, 2) NOT NULL CHECK (valor > 0),
    PRIMARY KEY (avaliacao_id, questao_id),
    UNIQUE (avaliacao_id, ordem)
);

CREATE TABLE avaliacao_usuario (
    avaliacao_id INTEGER NOT NULL REFERENCES avaliacao(id) ON DELETE CASCADE,
    usuario_id INTEGER NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE' CHECK (status IN ('PENDENTE', 'RESPONDIDA', 'CORRIGIDA')),
    enviada_em TIMESTAMP,
    PRIMARY KEY (avaliacao_id, usuario_id)
);

CREATE TABLE resposta (
    id SERIAL PRIMARY KEY,
    avaliacao_id INTEGER NOT NULL,
    usuario_id INTEGER NOT NULL,
    questao_id INTEGER NOT NULL,
    alternativa_id INTEGER REFERENCES alternativa(id) ON DELETE RESTRICT,
    texto_resposta TEXT,
    nota_obtida NUMERIC(5, 2) CHECK (nota_obtida IS NULL OR nota_obtida >= 0),
    corrigida_por INTEGER REFERENCES usuario(id) ON DELETE SET NULL,
    corrigida_em TIMESTAMP,
    respondida_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (avaliacao_id, usuario_id, questao_id),
    FOREIGN KEY (avaliacao_id, usuario_id) REFERENCES avaliacao_usuario(avaliacao_id, usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (avaliacao_id, questao_id) REFERENCES avaliacao_questao(avaliacao_id, questao_id) ON DELETE RESTRICT,
    CHECK (
        (alternativa_id IS NOT NULL AND texto_resposta IS NULL)
        OR (alternativa_id IS NULL AND texto_resposta IS NOT NULL)
    )
);
