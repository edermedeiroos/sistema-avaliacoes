let avaliacaoEmAndamento = null;

async function showAlunoLista() {
  document.querySelectorAll("main > section").forEach(s => s.classList.add("hidden"));
  document.getElementById("aluno-lista").classList.remove("hidden");
  fecharSubtelasAluno();

  const turmaTxt = usuarioLogado.turmaCodigo ? ` (${usuarioLogado.turmaCodigo})` : "";
  document.getElementById("alunoHeaderNome").textContent = `Minhas Avaliações - ${usuarioLogado.nome}${turmaTxt}`;

  try {
    const lista = await apiGet(`/api/alunos/${usuarioLogado.id}/avaliacoes`);
    renderAlunoStats(lista);
    renderAlunoTabela(lista);
  } catch (err) {
    showToast(err.message, true);
  }
}

function renderAlunoStats(lista) {
  const pendentes = lista.filter(a => a.status === "PENDENTE").length;
  const concluidas = lista.filter(a => a.status === "CORRIGIDA" && a.notaObtida !== null);
  const media = concluidas.length
    ? (concluidas.reduce((acc, a) => acc + Number(a.notaObtida), 0) / concluidas.length).toFixed(1)
    : "-";

  document.getElementById("alunoStats").innerHTML = `
    <div class="stat-box"><div class="stat-label">Avaliações Atribuídas</div><div class="stat-val">${lista.length}</div></div>
    <div class="stat-box"><div class="stat-label">Pendentes</div><div class="stat-val">${pendentes}</div></div>
    <div class="stat-box"><div class="stat-label">Média (Corrigidas)</div><div class="stat-val">${media}</div></div>
  `;
}

function renderAlunoTabela(lista) {
  const tbody = document.getElementById("tabelaAlunoBody");
  if (lista.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="text-muted">Nenhuma avaliação atribuída até o momento.</td></tr>`;
    return;
  }

  tbody.innerHTML = lista.map(a => {
    const badgeCls = a.status === "CORRIGIDA" ? "badge-ok" : a.status === "RESPONDIDA" ? "badge-info" : "badge-warn";
    const notaTxt = a.notaObtida !== null && a.notaObtida !== undefined
      ? `${Number(a.notaObtida).toFixed(1)} / ${Number(a.valorTotal).toFixed(1)}`
      : "-";
    const acao = a.status === "PENDENTE"
      ? `<button class="btn btn-primary btn-sm" onclick="abrirProva(${a.avaliacaoId})">Responder</button>`
      : `<button class="btn btn-secondary btn-sm" onclick="verResultado(${a.avaliacaoId}, '${esc(a.titulo).replace(/'/g, "\\'")}', '${esc(a.status)}')">Ver Respostas</button>`;

    return `
      <tr>
        <td><strong>${esc(a.titulo)}</strong></td>
        <td>${esc(a.tipo)}</td>
        <td>${esc(a.dataAplicacao)} | ${esc(a.horaInicio).slice(0, 5)}-${esc(a.horaFim).slice(0, 5)}</td>
        <td><span class="badge ${badgeCls}">${esc(a.status)}</span></td>
        <td>${notaTxt}</td>
        <td>${acao}</td>
      </tr>
    `;
  }).join("");
}

function fecharSubtelasAluno() {
  avaliacaoEmAndamento = null;
  document.getElementById("responderProvaCard").classList.add("hidden");
  document.getElementById("verResultadoCard").classList.add("hidden");
  document.getElementById("alunoAvaliacoesCard").classList.remove("hidden");
}

async function abrirProva(avaliacaoId) {
  try {
    const av = await apiGet(`/api/avaliacoes/${avaliacaoId}?ocultarGabarito=true`);
    avaliacaoEmAndamento = av;

    document.getElementById("alunoAvaliacoesCard").classList.add("hidden");
    document.getElementById("verResultadoCard").classList.add("hidden");
    document.getElementById("responderProvaCard").classList.remove("hidden");

    document.getElementById("respAvTitulo").textContent = av.titulo;
    document.getElementById("respAvMeta").textContent =
      `${av.tipo} | ${av.dataAplicacao} (${String(av.horaInicio).slice(0, 5)} às ${String(av.horaFim).slice(0, 5)})`;

    document.getElementById("respQuestoesContainer").innerHTML = av.itens.map(item => {
      const q = item.questao;
      if (q.tipo === "MULTIPLA_ESCOLHA") {
        return `
          <div class="exam-question">
            <div class="exam-question-head">
              <span>${item.ordem}. ${esc(q.enunciado)}</span>
              <span class="badge badge-neutral">${Number(item.valor).toFixed(1)} pts</span>
            </div>
            ${q.alternativas.map(alt => `
              <label class="option-item">
                <input type="radio" name="resp_q_${q.id}" value="${alt.id}" required />
                <span><strong>${esc(alt.letra)})</strong> ${esc(alt.texto)}</span>
              </label>
            `).join("")}
          </div>
        `;
      }
      return `
        <div class="exam-question">
          <div class="exam-question-head">
            <span>${item.ordem}. ${esc(q.enunciado)}</span>
            <span class="badge badge-neutral">${Number(item.valor).toFixed(1)} pts (Discursiva)</span>
          </div>
          <textarea name="resp_txt_${q.id}" rows="3" placeholder="Digite sua resposta..." required></textarea>
        </div>
      `;
    }).join("");
  } catch (err) {
    showToast(err.message, true);
  }
}

async function enviarProva(e) {
  e.preventDefault();
  if (!avaliacaoEmAndamento) return;

  const itens = avaliacaoEmAndamento.itens.map(item => {
    const q = item.questao;
    if (q.tipo === "MULTIPLA_ESCOLHA") {
      const marcado = document.querySelector(`input[name="resp_q_${q.id}"]:checked`);
      return {
        questaoId: q.id,
        alternativaId: marcado ? parseInt(marcado.value, 10) : null,
        textoResposta: null
      };
    }
    const txt = document.querySelector(`textarea[name="resp_txt_${q.id}"]`).value.trim();
    return {
      questaoId: q.id,
      alternativaId: null,
      textoResposta: txt
    };
  });

  try {
    const salvas = await apiPost(`/api/avaliacoes/${avaliacaoEmAndamento.id}/respostas`, {
      usuarioId: usuarioLogado.id,
      itens
    });
    const temPendente = salvas.some(r => r.notaObtida === null);
    const totalParcial = salvas.reduce((acc, r) => acc + (r.notaObtida ? Number(r.notaObtida) : 0), 0);

    await showAlunoLista();
    showToast(temPendente
      ? `Respostas enviadas! Nota parcial das objetivas: ${totalParcial.toFixed(1)} pts (discursiva aguardando professor).`
      : `Avaliação corrigida automaticamente! Nota final: ${totalParcial.toFixed(1)} pts.`);
  } catch (err) {
    showToast(err.message, true);
  }
}

async function verResultado(avaliacaoId, titulo, status) {
  try {
    const respostas = await apiGet(`/api/avaliacoes/${avaliacaoId}/respostas/${usuarioLogado.id}`);
    document.getElementById("alunoAvaliacoesCard").classList.add("hidden");
    document.getElementById("responderProvaCard").classList.add("hidden");
    document.getElementById("verResultadoCard").classList.remove("hidden");

    document.getElementById("resAvTitulo").textContent = titulo;
    document.getElementById("resAvMeta").textContent = `Status: ${status}`;

    document.getElementById("resQuestoesContainer").innerHTML = respostas.map((r, idx) => {
      const notaTxt = r.notaObtida !== null && r.notaObtida !== undefined
        ? `${Number(r.notaObtida).toFixed(1)} / ${Number(r.valorQuestao).toFixed(1)} pts`
        : "Aguardando correção";
      const respTxt = r.tipoQuestao === "MULTIPLA_ESCOLHA"
        ? `<strong>${esc(r.alternativaLetra)})</strong> ${esc(r.alternativaTexto)}`
        : esc(r.textoResposta);

      return `
        <div class="exam-question">
          <div class="exam-question-head">
            <span>${idx + 1}. ${esc(r.enunciado)}</span>
            <span class="badge ${r.notaObtida !== null ? "badge-ok" : "badge-warn"}">${notaTxt}</span>
          </div>
          <div class="answer-box" style="margin-bottom:0;">
            <strong>Sua resposta:</strong> ${respTxt}
          </div>
        </div>
      `;
    }).join("");
  } catch (err) {
    showToast(err.message, true);
  }
}
