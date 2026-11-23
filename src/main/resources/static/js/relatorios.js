const reportCharts = {};
let filtrosCarregados = false;

async function inicializarRelatorios() {
  if (!filtrosCarregados) {
    try {
      const [avaliacoes, disciplinas] = await Promise.all([
        apiGet("/api/avaliacoes"),
        apiGet("/api/relatorios/disciplinas")
      ]);
      const selAv = document.getElementById("rep2Avaliacao");
      selAv.innerHTML = `<option value="">Todas</option>` +
        avaliacoes.map(a => `<option value="${a.id}">${esc(a.titulo)}</option>`).join("");

      const selDisc = document.getElementById("rep3Disc");
      selDisc.innerHTML = `<option value="">Todas</option>` +
        disciplinas.map(d => `<option value="${esc(d)}">${esc(d)}</option>`).join("");
      filtrosCarregados = true;
    } catch (err) {
      showToast(err.message, true);
    }
  }
  showReport(1);
}

function showReport(num) {
  document.querySelectorAll(".report-tab").forEach(t => {
    t.classList.toggle("active", parseInt(t.dataset.rep, 10) === num);
  });
  document.querySelectorAll(".report-panel").forEach(p => p.classList.add("hidden"));
  document.getElementById(`rep-${num}`).classList.remove("hidden");

  if (num === 1) carregarRelatorio1();
  if (num === 2) carregarRelatorio2();
  if (num === 3) carregarRelatorio3();
  if (num === 4) carregarRelatorio4();
}

function destroyReportChart(key) {
  if (reportCharts[key]) {
    reportCharts[key].destroy();
  }
}

async function carregarRelatorio1() {
  const di = document.getElementById("rep1DataInicio").value;
  const df = document.getElementById("rep1DataFim").value;
  const tipo = document.getElementById("rep1Tipo").value;
  const params = new URLSearchParams();
  if (di) params.set("dataInicio", di);
  if (df) params.set("dataFim", df);
  if (tipo) params.set("tipos", tipo);
  const query = params.toString() ? `?${params.toString()}` : "";

  try {
    const dados = await apiGet(`/api/relatorios/medias-avaliacoes${query}`);
    const tbody = document.getElementById("rep1Body");
    tbody.innerHTML = dados.length === 0
      ? `<tr><td colspan="6" class="text-muted">Nenhum resultado para os filtros informados.</td></tr>`
      : dados.map(d => `
      <tr>
        <td>${esc(d.titulo)}</td>
        <td>${esc(d.tipo)}</td>
        <td>${d.totalRespondentes}</td>
        <td><strong>${Number(d.notaMedia).toFixed(2)}</strong></td>
        <td>${Number(d.maiorNota).toFixed(2)}</td>
        <td>${Number(d.menorNota).toFixed(2)}</td>
      </tr>
    `).join("");

    destroyReportChart("rep1");
    reportCharts.rep1 = new Chart(document.getElementById("chartRep1"), {
      type: "bar",
      data: {
        labels: dados.map(d => d.titulo),
        datasets: [{
          label: "Nota Média",
          data: dados.map(d => Number(d.notaMedia)),
          backgroundColor: "#1f4d3a"
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: { y: { beginAtZero: true, max: 10 } }
      }
    });
  } catch (err) {
    showToast(err.message, true);
  }
}

async function carregarRelatorio2() {
  const busca = document.getElementById("rep2Busca").value.trim();
  const avaliacaoId = document.getElementById("rep2Avaliacao").value;
  const params = new URLSearchParams();
  if (busca) params.set("busca", busca);
  if (avaliacaoId) params.set("avaliacaoId", avaliacaoId);
  const query = params.toString() ? `?${params.toString()}` : "";

  try {
    const dados = await apiGet(`/api/relatorios/desempenho-alunos${query}`);
    const top = dados.slice(0, 12);
    document.getElementById("rep2Body").innerHTML = top.map(r => {
      const dif = Number(r.diferenca);
      const cls = dif >= 0 ? "badge-ok" : "badge-danger";
      return `
        <tr>
          <td>${esc(r.alunoNome)}</td>
          <td>${esc(r.turmaCodigo)}</td>
          <td>${esc(r.avaliacaoTitulo)}</td>
          <td><strong>${Number(r.notaAluno).toFixed(2)}</strong></td>
          <td>${Number(r.mediaTurma).toFixed(2)}</td>
          <td><span class="badge ${cls}">${dif >= 0 ? "+" : ""}${dif.toFixed(2)}</span></td>
        </tr>
      `;
    }).join("");

    destroyReportChart("rep2");
    reportCharts.rep2 = new Chart(document.getElementById("chartRep2"), {
      type: "bar",
      data: {
        labels: top.map(r => `${r.alunoNome.split(" ")[0]} (${r.avaliacaoTitulo.slice(0, 14)})`),
        datasets: [
          { label: "Nota do Aluno", data: top.map(r => Number(r.notaAluno)), backgroundColor: "#1f4d3a" },
          { label: "Média da Avaliação", data: top.map(r => Number(r.mediaTurma)), backgroundColor: "#94a3b8" }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: { y: { beginAtZero: true, max: 10 } }
      }
    });
  } catch (err) {
    showToast(err.message, true);
  }
}

async function carregarRelatorio3() {
  const disc = document.getElementById("rep3Disc").value;
  const limite = document.getElementById("rep3Limite").value;
  const params = new URLSearchParams();
  if (disc) params.set("disciplina", disc);
  params.set("limite", limite);

  try {
    const dados = await apiGet(`/api/relatorios/ranking-erros?${params.toString()}`);
    document.getElementById("rep3Body").innerHTML = dados.map(r => `
      <tr>
        <td>#${r.posicao}</td>
        <td>${esc(r.enunciado)}</td>
        <td>${esc(r.disciplina)}</td>
        <td>${r.totalErros} / ${r.totalRespostas}</td>
        <td><strong>${Number(r.percentualErro).toFixed(1)}%</strong></td>
      </tr>
    `).join("");

    destroyReportChart("rep3");
    reportCharts.rep3 = new Chart(document.getElementById("chartRep3"), {
      type: "bar",
      data: {
        labels: dados.map(r => `#${r.questaoId} ${r.enunciado.slice(0, 28)}...`),
        datasets: [{
          label: "% de Erro",
          data: dados.map(r => Number(r.percentualErro)),
          backgroundColor: "#b42318"
        }]
      },
      options: {
        indexAxis: "y",
        responsive: true,
        maintainAspectRatio: false,
        scales: { x: { beginAtZero: true, max: 100 } }
      }
    });
  } catch (err) {
    showToast(err.message, true);
  }
}

async function carregarRelatorio4() {
  try {
    const data = await apiGet("/api/relatorios/pendencias");
    document.getElementById("rep4PendBody").innerHTML = data.alunosPendentes.map(p => `
      <tr>
        <td>${esc(p.avaliacaoTitulo)}</td>
        <td>${esc(p.alunoNome)}</td>
        <td>${esc(p.turmaCodigo)}</td>
        <td><span class="badge badge-warn">${esc(p.status)}</span></td>
      </tr>
    `).join("");

    document.getElementById("rep4NaoUsadasBody").innerHTML = data.questoesNaoUsadas.map(q => `
      <tr>
        <td>#${q.id}</td>
        <td>${esc(q.enunciado)}</td>
        <td>${esc(q.tipo)}</td>
        <td>${esc(q.disciplina)}</td>
      </tr>
    `).join("");

    const resumo = data.resumo;
    destroyReportChart("rep4");
    reportCharts.rep4 = new Chart(document.getElementById("chartRep4"), {
      type: "bar",
      data: {
        labels: resumo.map(r => r.titulo),
        datasets: [
          { label: "Respondidas", data: resumo.map(r => r.totalRespondidas), backgroundColor: "#1f4d3a" },
          { label: "Pendentes", data: resumo.map(r => r.totalPendentes), backgroundColor: "#d97706" }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: { x: { stacked: true }, y: { stacked: true, beginAtZero: true } }
      }
    });
  } catch (err) {
    showToast(err.message, true);
  }
}
