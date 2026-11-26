const API_URL = "http://localhost:8081/projetos";

const totalProjetos = document.getElementById("totalProjetos");
const projetosConcluidos = document.getElementById("projetosConcluidos");
const totalGrafico = document.getElementById("totalGrafico");
const graficoStatus = document.getElementById("graficoStatus");
const legendaStatus = document.getElementById("legendaStatus");
const listaRecentes = document.getElementById("listaRecentes");
const listaAtividades = document.getElementById("listaAtividades");
const botaoTema = document.getElementById("alternarTema");


const CORES_STATUS = {
    "Concluído": "#78947a",
    "Concluido": "#78947a",
    "Planejado": "#ead58a",
    "Em Desenvolvimento": "#b86f52"
};

const CORES_CATEGORIA = {
    "Software": "#78947a",
    "Web": "#ead58a",
    "Mobile": "#b86f52"
};

const CORES_GERAL = {
    "Concluídos": "#78947a",
    "Não concluídos": "#ead58a"
};


function normalizarTexto(texto) {
  return String(texto ?? "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .trim()
    .toLowerCase();
}

function obterCorStatus(status) {
  const normalizado = normalizarTexto(status);

  if (normalizado.includes("conclu")) {
    return CORES_STATUS["Concluído"];
  }

  if (normalizado.includes("planej")) {
    return CORES_STATUS["Planejado"];
  }

  if (
    normalizado.includes("desenvolvimento") ||
    normalizado.includes("andamento")
  ) {
    return CORES_STATUS["Em Desenvolvimento"];
  }

  return "#9aa4a0";
}

function obterClasseStatus(status) {
  const normalizado = normalizarTexto(status);

  if (normalizado.includes("conclu")) {
    return "status-concluido";
  }

  if (normalizado.includes("planej")) {
    return "status-planejado";
  }

  if (
    normalizado.includes("desenvolvimento") ||
    normalizado.includes("andamento")
  ) {
    return "status-andamento";
  }

  return "status-padrao";
}

function criarElemento(tag, classe, texto) {
  const elemento = document.createElement(tag);

  if (classe) {
    elemento.className = classe;
  }

  if (texto !== undefined) {
    elemento.textContent = texto;
  }

  return elemento;
}

function atualizarResumo(projetos) {
  totalProjetos.textContent = projetos.length;

  const quantidadeConcluida = projetos.filter((projeto) =>
    normalizarTexto(projeto.status).includes("conclu"),
  ).length;

  projetosConcluidos.textContent = quantidadeConcluida;
  totalGrafico.textContent = projetos.length;

  atualizarGrafico(projetos);
}

function atualizarGrafico(projetos) {
  const contagem = new Map();

  projetos.forEach((projeto) => {
    const status = String(projeto.status || "Sem status").trim();
    contagem.set(status, (contagem.get(status) || 0) + 1);
  });

  legendaStatus.replaceChildren();

  if (projetos.length === 0) {
    graficoStatus.style.setProperty(
      "--progresso-grafico",
      "conic-gradient(var(--borda) 0% 100%)",
    );

    legendaStatus.appendChild(
      criarElemento("p", "estado-vazio", "Nenhum projeto cadastrado."),
    );

    return;
  }

  const fatias = [];
  let acumulado = 0;

  contagem.forEach((quantidade, status) => {
    const inicio = acumulado;
    acumulado += (quantidade / projetos.length) * 100;

    fatias.push(`${obterCorStatus(status)} ${inicio}% ${acumulado}%`);

    const item = criarElemento("div", "item-legenda");

    const marcador = criarElemento("span", "cor-legenda");
    marcador.style.backgroundColor = obterCorStatus(status);

    const nome = criarElemento("span", "", status);
    const valor = criarElemento("span", "valor-legenda", quantidade);

    item.append(marcador, nome, valor);
    legendaStatus.appendChild(item);
  });

  graficoStatus.style.setProperty(
    "--progresso-grafico",
    `conic-gradient(${fatias.join(", ")})`,
  );
}

function atualizarGraficoCategoria(projetos) {
  const grafico = document.getElementById("graficoCategoria");
  const total = document.getElementById("totalGraficoCategoria");
  const legenda = document.getElementById("legendaCategoria");

  if (!grafico || !total || !legenda) {
  throw new Error(
    "Elementos do gráfico de categorias não encontrados no HTML."
  );
}

  const categorias = new Map();

  projetos.forEach((projeto) => {
    const categoria = projeto.categoria || "Sem categoria";
    categorias.set(categoria, (categorias.get(categoria) || 0) + 1);
  });

  const totalProjetos = projetos.length;
  total.textContent = totalProjetos;

  let inicio = 0;
  const fatias = [];

  categorias.forEach((quantidade, categoria) => {
    const percentual = totalProjetos ? (quantidade / totalProjetos) * 100 : 0;

    const cor = CORES_CATEGORIA[categoria] || "#9aa4a0";
    fatias.push(`${cor} ${inicio}% ${inicio + percentual}%`);
    inicio += percentual;
  });

  grafico.style.setProperty(
  "--progresso-grafico",
  totalProjetos
    ? `conic-gradient(${fatias.join(", ")})`
    : "conic-gradient(var(--borda) 0% 100%)"
);

  legenda.replaceChildren();

  categorias.forEach((quantidade, categoria) => {
    const linha = criarElemento("div", "item-legenda");
    const cor = CORES_CATEGORIA[categoria] || "#9aa4a0";
    const indicador = criarElemento("span", "cor-legenda");
    indicador.style.backgroundColor = cor;

    linha.append(
      indicador,
      criarElemento("span", "texto-legenda", categoria),
      criarElemento("strong", "", quantidade),
    );

    legenda.appendChild(linha);
  });

  if (!totalProjetos) {
    legenda.appendChild(criarElemento("p", "", "Nenhum projeto cadastrado."));
  }
}

function atualizarGraficoGeral(projetos) {
  const grafico = document.getElementById("graficoGeral");
  const total = document.getElementById("totalGraficoGeral");
  const legenda = document.getElementById("legendaGeral");

  const concluidos = projetos.filter(
    (projeto) => normalizarTexto(projeto.status) === "concluido",
  ).length;

  const naoConcluidos = projetos.length - concluidos;

  total.textContent = projetos.length;

  const percentualConcluidos = projetos.length
    ? (concluidos / projetos.length) * 100
    : 0;

  grafico.style.setProperty(
  "--progresso-grafico",
  projetos.length
    ? `conic-gradient(
        ${CORES_GERAL["Concluídos"]} 0% ${percentualConcluidos}%,
        ${CORES_GERAL["Não concluídos"]} ${percentualConcluidos}% 100%
      )`
    : "conic-gradient(var(--borda) 0% 100%)"
);

  legenda.replaceChildren();

  [
    ["Concluídos", concluidos],
    ["Não concluídos", naoConcluidos],
  ].forEach(([situacao, quantidade]) => {
    const linha = criarElemento("div", "item-legenda");
    const indicador = criarElemento("span", "cor-legenda");
    indicador.style.backgroundColor = CORES_GERAL[situacao];

    linha.append(
      indicador,
      criarElemento("span", "texto-legenda", situacao),
      criarElemento("strong", "", quantidade),
    );

    legenda.appendChild(linha);
  });
}

function atualizarProjetosRecentes(projetos) {
  listaRecentes.replaceChildren();

  if (projetos.length === 0) {
    listaRecentes.appendChild(
      criarElemento("p", "estado-vazio", "Nenhum projeto cadastrado."),
    );

    return;
  }

  const recentes = [...projetos]
    .sort((a, b) => Number(b.id) - Number(a.id))
    .slice(0, 5);

  recentes.forEach((projeto) => {
    const item = criarElemento("article", "projeto-recente");

    const informacoes = criarElemento("div");
    informacoes.appendChild(
      criarElemento("h3", "", projeto.nome || "Projeto sem nome"),
    );

    informacoes.appendChild(
      criarElemento(
        "p",
        "",
        `ID ${projeto.id} · ${projeto.categoria || "Sem categoria"}`,
      ),
    );

    const status = criarElemento(
      "span",
      `badge-status ${obterClasseStatus(projeto.status)}`,
      projeto.status || "Sem status",
    );

    item.append(informacoes, status);
    listaRecentes.appendChild(item);
  });
}

function atualizarAtividades() {
  listaAtividades.replaceChildren();

  const item = criarElemento("div", "item-atividade");
  const icone = criarElemento("div", "icone-atividade");

  const simbolo = document.createElement("i");
  simbolo.setAttribute("data-lucide", "info");
  icone.appendChild(simbolo);

  const informacoes = criarElemento("div");

  informacoes.appendChild(
    criarElemento("strong", "", "Histórico de atividades"),
  );

  informacoes.appendChild(
    criarElemento(
      "p",
      "",
      "O sistema ainda não disponibiliza um histórico de alterações.",
    ),
  );

  item.append(icone, informacoes);
  listaAtividades.appendChild(item);
}

async function carregarDashboard() {
  try {
    totalProjetos.textContent = "…";
    projetosConcluidos.textContent = "…";
    totalGrafico.textContent = "…";

    const resposta = await fetch(API_URL);

    if (!resposta.ok) {
      throw new Error("Não foi possível consultar a API.");
    }

    const projetos = await resposta.json();

    if (!Array.isArray(projetos)) {
      throw new Error("A API retornou um formato inesperado.");
    }

    atualizarResumo(projetos);
    atualizarProjetosRecentes(projetos);
    atualizarGraficoCategoria(projetos);
    atualizarGraficoGeral(projetos);
    atualizarAtividades();
  } catch (erro) {
    console.error("Erro ao carregar o Dashboard:", erro);

    totalProjetos.textContent = "—";
    projetosConcluidos.textContent = "—";
    totalGrafico.textContent = "—";

    legendaStatus.replaceChildren(
      criarElemento("p", "estado-vazio", "Não foi possível carregar os dados."),
    );

    listaRecentes.replaceChildren(
      criarElemento("p", "estado-vazio", "Verifique se a API está ligada."),
    );
  }
}

function configurarTema() {
  const temaSalvo = localStorage.getItem("temaSistema");
  const temaInicial = temaSalvo === "escuro" ? "escuro" : "claro";

  aplicarTema(temaInicial);

  botaoTema.addEventListener("click", () => {
    const temaAtual = document.body.classList.contains("tema-escuro")
      ? "escuro"
      : "claro";

    aplicarTema(temaAtual === "escuro" ? "claro" : "escuro");
  });
}

function aplicarTema(tema) {
  const escuro = tema === "escuro";

  document.body.classList.toggle("tema-escuro", escuro);
  localStorage.setItem("temaSistema", tema);

  botaoTema.replaceChildren();

  const icone = document.createElement("i");
  icone.setAttribute("data-lucide", escuro ? "sun" : "moon");
  botaoTema.appendChild(icone);

  botaoTema.title = escuro ? "Ativar modo claro" : "Ativar modo escuro";
  botaoTema.setAttribute(
    "aria-label",
    escuro ? "Ativar modo claro" : "Ativar modo escuro",
  );

  if (window.lucide) {
    window.lucide.createIcons();
  }
}

configurarTema();
carregarDashboard();

if (window.lucide) {
  window.lucide.createIcons();
}
