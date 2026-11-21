async function requestJson(url, options = {}) {
  const config = {
    headers: { "Content-Type": "application/json" },
    ...options
  };
  const resp = await fetch(url, config);
  const text = await resp.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch (_) {
      data = text;
    }
  }
  if (!resp.ok) {
    const msg = (data && data.erro) ? data.erro : `Erro HTTP ${resp.status}`;
    throw new Error(msg);
  }
  return data;
}

function apiGet(url) {
  return requestJson(url, { method: "GET" });
}

function apiPost(url, body) {
  return requestJson(url, { method: "POST", body: JSON.stringify(body) });
}

function apiPut(url, body) {
  return requestJson(url, { method: "PUT", body: JSON.stringify(body) });
}

function apiDelete(url) {
  return requestJson(url, { method: "DELETE" });
}
