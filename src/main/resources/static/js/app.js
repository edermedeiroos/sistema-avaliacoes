let usuarioLogado = null;

function esc(str) {
  if (str === null || str === undefined) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function showToast(msg, isError = false) {
  const el = document.getElementById("toast");
  el.textContent = msg;
  el.className = `alert ${isError ? "alert-err" : "alert-ok"}`;
  clearTimeout(window._toastTimer);
  window._toastTimer = setTimeout(() => el.classList.add("hidden"), 4000);
}

function preencherLogin(email, senha) {
  document.getElementById("loginEmail").value = email;
  document.getElementById("loginPass").value = senha;
}

async function handleLogin(e) {
  e.preventDefault();
  const email = document.getElementById("loginEmail").value.trim();
  const senha = document.getElementById("loginPass").value;
  try {
    const user = await apiPost("/api/auth/login", { email, senha });
    sessionStorage.setItem("usuario", JSON.stringify(user));
    iniciarSessao(user);
  } catch (err) {
    showToast(err.message, true);
  }
}

function logout() {
  sessionStorage.removeItem("usuario");
  usuarioLogado = null;
  document.getElementById("userBadge").textContent = "Não autenticado";
  document.getElementById("btnLogout").classList.add("hidden");
  document.getElementById("profNav").classList.add("hidden");
  document.getElementById("alunoNav").classList.add("hidden");
  document.querySelectorAll("main > section").forEach(s => s.classList.add("hidden"));
  document.getElementById("view-login").classList.remove("hidden");
}

function iniciarSessao(user) {
  usuarioLogado = user;
  document.getElementById("view-login").classList.add("hidden");
  document.getElementById("btnLogout").classList.remove("hidden");

  const turmaTxt = user.turmaCodigo ? ` (${user.turmaCodigo})` : "";
  document.getElementById("userBadge").textContent = `${user.nome}${turmaTxt} (${user.perfil})`;

  if (user.perfil === "PROFESSOR") {
    document.getElementById("alunoNav").classList.add("hidden");
    document.getElementById("profNav").classList.remove("hidden");
    showProfTab("prof-questoes");
  } else {
    document.getElementById("profNav").classList.add("hidden");
    document.getElementById("alunoNav").classList.remove("hidden");
    showAlunoLista();
  }
}

function showProfTab(tabId) {
  document.querySelectorAll("#profNav .nav-tab").forEach(t => {
    t.classList.toggle("active", t.dataset.tab === tabId);
  });
  ["prof-questoes", "prof-avaliacoes", "prof-turmas", "prof-correcao", "prof-relatorios"].forEach(id => {
    document.getElementById(id).classList.toggle("hidden", id !== tabId);
  });

  if (tabId === "prof-questoes") carregarQuestoes();
  if (tabId === "prof-avaliacoes") carregarTelaAvaliacoes();
  if (tabId === "prof-turmas") carregarTurmas();
  if (tabId === "prof-correcao") carregarCorrecoesPendentes();
  if (tabId === "prof-relatorios") inicializarRelatorios();
}

window.addEventListener("DOMContentLoaded", () => {
  const salvo = sessionStorage.getItem("usuario");
  if (salvo) {
    try {
      iniciarSessao(JSON.parse(salvo));
      return;
    } catch (_) {
      sessionStorage.removeItem("usuario");
    }
  }
  logout();
});
