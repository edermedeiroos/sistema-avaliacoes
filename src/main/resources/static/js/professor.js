let questoesCache = [];
let turmasCache = [];
let avaliacaoAtribuicaoId = null;
let editandoQuestaoId = null;

function toggleQuestaoForm(force) {
  const card = document.getElementById("novaQuestaoCard");
  const show = typeof force === "boolean" ? force : card.classList.contains("hidden");
  card.classList.toggle("hidden", !show);
  if (!show) {
    resetarFormQuestao();
  }
}

function resetarFormQuestao() {
  editandoQuestaoId = null;
  document.getElementById("formNovaQuestao").reset();
  document.getElementById("qDisciplina").value = "Banco de Dados I";
  document.getElementById("questaoFormTitle").textContent = "Cadastrar Questão";
  document.getElementById("btnSalvarQuestao").textContent = "Salvar Questão";
  toggleAlternativasBox();
}

function abrirNovaQuestao() {
  const card = document.getElementById("novaQuestaoCard");
  if (editandoQuestaoId !== null || card.classList.contains("hidden")) {
    resetarFormQuestao();
    toggleQuestaoForm(true);
  } else {
    toggleQuestaoForm(false);
  }
}

function toggleAlternativasBox() {
  const tipo = document.getElementById("qTipo").value;
  document.getElementById("alternativasBox").classList.toggle("hidden", tipo !== "MULTIPLA_ESCOLHA");
}

async function carregarQuestoes() {
  const tipo = document.getElementById("filtroQuestaoTipo").value;
  const busca = document.getElementById("filtroQuestaoTexto").value.trim();
  const params = new URLSearchParams();
  if (tipo) params.set("tipo", tipo);
  if (busca) params.set("busca", busca);
  const query = params.toString() ? `?${params.toString()}` : "";

  try {
    const lista = await apiGet(`/api/questoes${query}`);
    if (!tipo && !busca) {
      questoesCache = lista;
    }
    const tbody = document.getElementById("tabelaQuestoesBody");
    if (lista.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" class="text-muted">Nenhuma questão encontrada.</td></tr>`;
      return;
    }
    tbody.innerHTML = lista.map(q => `
      <tr>
        <td>#${q.id}</td>
        <td>${esc(q.enunciado)}</td>
        <td>
          <span class="badge ${q.tipo === "MULTIPLA_ESCOLHA" ? "badge-info" : "badge-neutral"}">
            ${q.tipo === "MULTIPLA_ESCOLHA" ? "Múltipla Escolha" : "Texto Livre"}
          </span>
        </td>
        <td>${esc(q.disciplina)}</td>
        <td>${esc(q.dificuldade)}</td>
        <td>${q.usosEmAvaliacoes > 0 ? `${q.usosEmAvaliacoes} aval.` : `<span class="badge badge-warn">Não usada</span>`}</td>
        <td>
          <div class="btn-group">
            <button class="btn btn-secondary btn-sm" onclick="editarQuestao(${q.id})">Editar</button>
            ${q.usosEmAvaliacoes === 0
              ? `<button class="btn btn-danger btn-sm" onclick="excluirQuestao(${q.id})">Excluir</button>`
              : ""}
          </div>
        </td>
      </tr>
    `).join("");
  } catch (err) {
    showToast(err.message, true);
  }
}

async function editarQuestao(id) {
  try {
    const q = await apiGet(`/api/questoes/${id}`);
    editandoQuestaoId = q.id;
    document.getElementById("questaoFormTitle").textContent = `Editar Questão #${q.id}`;
    document.getElementById("btnSalvarQuestao").textContent = "Atualizar Questão";
    document.getElementById("qEnunciado").value = q.enunciado || "";
    document.getElementById("qTipo").value = q.tipo || "MULTIPLA_ESCOLHA";
    document.getElementById("qDisciplina").value = q.disciplina || "";
    document.getElementById("qDificuldade").value = q.dificuldade || "MEDIA";
    toggleAlternativasBox();

    if (q.tipo === "MULTIPLA_ESCOLHA" && Array.isArray(q.alternativas)) {
      q.alternativas.forEach(alt => {
        const input = document.getElementById(`alt${alt.letra}`);
        if (input) input.value = alt.texto || "";
        if (alt.correta) {
          const radio = document.querySelector(`input[name="altCorreta"][value="${alt.letra}"]`);
          if (radio) radio.checked = true;
        }
      });
    } else {
      ["A", "B", "C", "D"].forEach(l => {
        document.getElementById(`alt${l}`).value = "";
      });
    }

    document.getElementById("novaQuestaoCard").classList.remove("hidden");
    document.getElementById("novaQuestaoCard").scrollIntoView({ behavior: "smooth", block: "start" });
  } catch (err) {
    showToast(err.message, true);
  }
}

async function salvarQuestao(e) {
  e.preventDefault();
  const tipo = document.getElementById("qTipo").value;
  const payload = {
    enunciado: document.getElementById("qEnunciado").value.trim(),
    tipo,
    disciplina: document.getElementById("qDisciplina").value.trim(),
    dificuldade: document.getElementById("qDificuldade").value,
    criadoPor: usuarioLogado.id,
    alternativas: []
  };

  if (tipo === "MULTIPLA_ESCOLHA") {
    const correta = document.querySelector('input[name="altCorreta"]:checked').value;
    payload.alternativas = ["A", "B", "C", "D"].map(letra => ({
      letra,
      texto: document.getElementById(`alt${letra}`).value.trim(),
      correta: letra === correta
    }));
  }

  try {
    const eraEdicao = editandoQuestaoId !== null;
    if (eraEdicao) {
      await apiPut(`/api/questoes/${editandoQuestaoId}`, payload);
    } else {
      await apiPost("/api/questoes", payload);
    }
    toggleQuestaoForm(false);
    await carregarQuestoes();
    showToast(eraEdicao ? "Questão atualizada com sucesso." : "Questão cadastrada com sucesso.");
  } catch (err) {
    showToast(err.message, true);
  }
}

async function excluirQuestao(id) {
  if (!confirm(`Excluir a questão #${id}?`)) return;
  try {
    await apiDelete(`/api/questoes/${id}`);
    await carregarQuestoes();
    showToast("Questão removida.");
  } catch (err) {
    showToast(err.message, true);
  }
}

async function carregarTelaAvaliacoes() {
  try {
    const [avaliacoes, turmas, questoes, alunos] = await Promise.all([
      apiGet("/api/avaliacoes"),
      apiGet("/api/turmas"),
      apiGet("/api/questoes"),
      apiGet("/api/usuarios?perfil=ALUNO")
    ]);
    turmasCache = turmas;
    questoesCache = questoes;

    const selectNova = document.getElementById("avTurma");
    selectNova.innerHTML = `<option value="">-- Selecionar depois --</option>` +
      turmas.map(t => `<option value="${t.id}">${esc(t.codigo)} - ${esc(t.nome)}</option>`).join("");

    const selectAtr = document.getElementById("atribuirTurmaSelect");
    selectAtr.innerHTML = turmas.map(t => `<option value="${t.id}">${esc(t.codigo)} - ${esc(t.nome)}</option>`).join("");

    const selectAluno = document.getElementById("atribuirAlunoSelect");
    selectAluno.innerHTML = alunos.map(u => `<option value="${u.id}">${esc(u.nome)} (${esc(u.turmaCodigo || "-")})</option>`).join("");

    renderAvaliacoesTabela(avaliacoes);
    renderQuestionPicker();
  } catch (err) {
    showToast(err.message, true);
  }
}

function renderAvaliacoesTabela(avaliacoes) {
  const tbody = document.getElementById("tabelaAvaliacoesBody");
  if (avaliacoes.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-muted">Nenhuma avaliação cadastrada.</td></tr>`;
    return;
  }
  tbody.innerHTML = avaliacoes.map(a => `
    <tr>
      <td><strong>${esc(a.titulo)}</strong></td>
      <td><span class="badge badge-neutral">${esc(a.tipo)}</span></td>
      <td>${esc(a.dataAplicacao)} | ${esc(a.horaInicio).slice(0, 5)} às ${esc(a.horaFim).slice(0, 5)}</td>
      <td>${a.totalQuestoes}</td>
      <td>${Number(a.valorTotal).toFixed(1)} pts</td>
      <td>${a.totalRespondidas} / ${a.totalAtribuidos}</td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="abrirAtribuicao(${a.id}, '${esc(a.titulo).replace(/'/g, "\\'")}')">
          Atribuir / Ver Alunos
        </button>
      </td>
    </tr>
  `).join("");
}

function toggleAvaliacaoForm(force) {
  const card = document.getElementById("novaAvaliacaoCard");
  const show = typeof force === "boolean" ? force : card.classList.contains("hidden");
  card.classList.toggle("hidden", !show);
  if (show) {
    const hoje = new Date().toISOString().slice(0, 10);
    if (!document.getElementById("avData").value) {
      document.getElementById("avData").value = hoje;
    }
    renderQuestionPicker();
  }
}

function renderQuestionPicker() {
  const box = document.getElementById("pickerQuestoes");
  box.innerHTML = questoesCache.map(q => `
    <div class="question-picker-row">
      <label>
        <input type="checkbox" class="pick-q-chk" value="${q.id}" onchange="atualizarTotalAvaliacao()" />
        <span><strong>#${q.id}</strong> [${q.tipo === "MULTIPLA_ESCOLHA" ? "Objetiva" : "Discursiva"}] ${esc(q.enunciado)}</span>
      </label>
      <input type="number" class="pick-q-val" data-qid="${q.id}" min="0.5" step="0.5" value="2.0" oninput="atualizarTotalAvaliacao()" />
    </div>
  `).join("");
  atualizarTotalAvaliacao();
}

function atualizarTotalAvaliacao() {
  let total = 0;
  let count = 0;
  document.querySelectorAll(".pick-q-chk:checked").forEach(chk => {
    count++;
    const valInput = document.querySelector(`.pick-q-val[data-qid="${chk.value}"]`);
    total += parseFloat(valInput.value || 0);
  });
  document.getElementById("avTotalPreview").textContent = `${count} questão(ões) | Total: ${total.toFixed(1)} pts`;
}

async function salvarAvaliacao(e) {
  e.preventDefault();
  const itens = [];
  let ordem = 1;
  document.querySelectorAll(".pick-q-chk:checked").forEach(chk => {
    const qId = parseInt(chk.value, 10);
    const valor = parseFloat(document.querySelector(`.pick-q-val[data-qid="${qId}"]`).value || 0);
    itens.push({ questaoId: qId, ordem: ordem++, valor });
  });

  if (itens.length === 0) {
    showToast("Selecione ao menos uma questão.", true);
    return;
  }

  const turmaVal = document.getElementById("avTurma").value;
  const payload = {
    titulo: document.getElementById("avTitulo").value.trim(),
    descricao: document.getElementById("avDesc").value.trim(),
    tipo: document.getElementById("avTipo").value,
    dataAplicacao: document.getElementById("avData").value,
    horaInicio: document.getElementById("avInicio").value,
    horaFim: document.getElementById("avFim").value,
    criadoPor: usuarioLogado.id,
    turmaId: turmaVal ? parseInt(turmaVal, 10) : null,
    itens
  };

  try {
    await apiPost("/api/avaliacoes", payload);
    e.target.reset();
    toggleAvaliacaoForm(false);
    await carregarTelaAvaliacoes();
    showToast("Avaliação criada com sucesso.");
  } catch (err) {
    showToast(err.message, true);
  }
}

async function abrirAtribuicao(avaliacaoId, titulo) {
  avaliacaoAtribuicaoId = avaliacaoId;
  document.getElementById("atribuirTitulo").textContent = `Atribuições - ${titulo}`;
  document.getElementById("atribuirPanel").classList.remove("hidden");
  await carregarAtribuidos(avaliacaoId);
}

function fecharAtribuicao() {
  avaliacaoAtribuicaoId = null;
  document.getElementById("atribuirPanel").classList.add("hidden");
}

async function carregarAtribuidos(avaliacaoId) {
  try {
    const lista = await apiGet(`/api/avaliacoes/${avaliacaoId}/atribuidos`);
    const tbody = document.getElementById("atribuidosBody");
    if (lista.length === 0) {
      tbody.innerHTML = `<tr><td colspan="4" class="text-muted">Nenhum aluno atribuído ainda.</td></tr>`;
      return;
    }
    tbody.innerHTML = lista.map(a => {
      const notaTxt = a.notaObtida !== null && a.notaObtida !== undefined
        ? `${Number(a.notaObtida).toFixed(1)} / ${Number(a.valorTotal).toFixed(1)}`
        : "-";
      return `
        <tr>
          <td>${esc(a.usuarioNome)}</td>
          <td>${esc(a.turmaCodigo || "-")}</td>
          <td><span class="badge ${a.status === "CORRIGIDA" ? "badge-ok" : a.status === "RESPONDIDA" ? "badge-info" : "badge-warn"}">${esc(a.status)}</span></td>
          <td>${notaTxt}</td>
        </tr>
      `;
    }).join("");
  } catch (err) {
    showToast(err.message, true);
  }
}

async function confirmarAtribuicaoTurma() {
  if (!avaliacaoAtribuicaoId) return;
  const turmaId = parseInt(document.getElementById("atribuirTurmaSelect").value, 10);
  try {
    await apiPost(`/api/avaliacoes/${avaliacaoAtribuicaoId}/atribuir`, { turmaId });
    await carregarAtribuidos(avaliacaoAtribuicaoId);
    await carregarTelaAvaliacoes();
    showToast("Turma atribuída à avaliação.");
  } catch (err) {
    showToast(err.message, true);
  }
}

async function confirmarAtribuicaoAluno() {
  if (!avaliacaoAtribuicaoId) return;
  const alunoId = parseInt(document.getElementById("atribuirAlunoSelect").value, 10);
  try {
    await apiPost(`/api/avaliacoes/${avaliacaoAtribuicaoId}/atribuir`, { usuarioIds: [alunoId] });
    await carregarAtribuidos(avaliacaoAtribuicaoId);
    await carregarTelaAvaliacoes();
    showToast("Aluno atribuído à avaliação.");
  } catch (err) {
    showToast(err.message, true);
  }
}

async function carregarCorrecoesPendentes() {
  const container = document.getElementById("correcaoContainer");
  try {
    const lista = await apiGet("/api/correcoes/pendentes");
    if (lista.length === 0) {
      container.innerHTML = `<div class="card"><p class="text-muted" style="margin:0;">Nenhuma resposta discursiva aguardando correção.</p></div>`;
      return;
    }
    container.innerHTML = lista.map(c => `
      <div class="card">
        <div class="panel-header" style="margin-bottom:8px;">
          <div><strong>${esc(c.alunoNome)}</strong> <span class="text-muted">(${esc(c.turmaCodigo)} - ${esc(c.avaliacaoTitulo)})</span></div>
          <span class="badge badge-warn">Máx. ${Number(c.valorMaximo).toFixed(1)} pts</span>
        </div>
        <p style="margin:0 0 8px;"><strong>Questão:</strong> ${esc(c.enunciado)}</p>
        <div class="answer-box">
          <strong>Resposta do aluno:</strong><br/>${esc(c.textoResposta)}
        </div>
        <div class="form-actions">
          <label style="margin:0;" for="nota_${c.respostaId}">Nota:</label>
          <input type="number" id="nota_${c.respostaId}" min="0" max="${c.valorMaximo}" step="0.5" value="${Number(c.valorMaximo).toFixed(1)}" style="width:100px;" />
          <button class="btn btn-primary btn-sm" onclick="salvarCorrecao(${c.respostaId})">Salvar Nota</button>
        </div>
      </div>
    `).join("");
  } catch (err) {
    showToast(err.message, true);
  }
}

async function salvarCorrecao(respostaId) {
  const nota = parseFloat(document.getElementById(`nota_${respostaId}`).value);
  if (isNaN(nota)) {
    showToast("Informe uma nota válida.", true);
    return;
  }
  try {
    await apiPut(`/api/correcoes/${respostaId}`, {
      nota,
      professorId: usuarioLogado.id
    });
    await carregarCorrecoesPendentes();
    showToast("Nota registrada com sucesso.");
  } catch (err) {
    showToast(err.message, true);
  }
}

function toggleTurmaForm(force) {
  const card = document.getElementById("novaTurmaCard");
  const show = typeof force === "boolean" ? force : card.classList.contains("hidden");
  card.classList.toggle("hidden", !show);
  if (!show) {
    document.getElementById("formNovaTurma").reset();
    document.getElementById("turmaSemestre").value = "2026/2";
  }
}

async function carregarTurmas() {
  try {
    const lista = await apiGet("/api/turmas");
    turmasCache = lista;
    const tbody = document.getElementById("tabelaTurmasBody");
    if (lista.length === 0) {
      tbody.innerHTML = `<tr><td colspan="5" class="text-muted">Nenhuma turma cadastrada.</td></tr>`;
      return;
    }
    tbody.innerHTML = lista.map(t => `
      <tr>
        <td>#${t.id}</td>
        <td><strong>${esc(t.codigo)}</strong></td>
        <td>${esc(t.nome)}</td>
        <td>${esc(t.anoSemestre)}</td>
        <td>${t.totalAlunos}</td>
      </tr>
    `).join("");
  } catch (err) {
    showToast(err.message, true);
  }
}

async function salvarTurma(e) {
  e.preventDefault();
  const payload = {
    codigo: document.getElementById("turmaCodigo").value.trim(),
    nome: document.getElementById("turmaNome").value.trim(),
    anoSemestre: document.getElementById("turmaSemestre").value.trim()
  };
  try {
    await apiPost("/api/turmas", payload);
    toggleTurmaForm(false);
    await carregarTurmas();
    showToast("Turma cadastrada com sucesso.");
  } catch (err) {
    showToast(err.message, true);
  }
}
