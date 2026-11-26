
const API_URL = "http://localhost:8081/projetos";

// Elementos do formulário
const formulario = document.getElementById("formProjeto");
const campoId = document.getElementById("projetoId");
const campoNome = document.getElementById("nome");
const campoDescricao = document.getElementById("descricao");
const campoCategoria = document.getElementById("categoria");
const campoStatus = document.getElementById("status");

// Elementos da lista e dos controles
const listaProjetos = document.getElementById("listaProjetos");
const mensagem = document.getElementById("mensagem");
const botaoLimpar = document.getElementById("botaoLimpar");
const botaoAtualizarLista = document.getElementById("botaoAtualizarLista");
const campoBusca = document.getElementById("buscarProjeto");
const contadorProjetos = document.getElementById("contadorProjetos");
const botaoTema = document.getElementById("alternarTema");

// Indicadores
const totalProjetosPagina = document.getElementById("totalProjetosPagina");
const totalAndamento = document.getElementById("totalAndamento");
const totalPlanejados = document.getElementById("totalPlanejados");
const totalConcluidosPagina = document.getElementById("totalConcluidosPagina");

// Estado da página
let projetosCarregados = [];
let filtroStatusAtual = "";

// Normaliza textos para comparar status sem depender de acentos.
function normalizarTexto(texto) {
    return String(texto ?? "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .trim()
        .toLowerCase();
}

function statusEh(status, tipo) {
    const valor = normalizarTexto(status);

    if (tipo === "concluido") {
        return valor.includes("conclu");
    }

    if (tipo === "planejado") {
        return valor.includes("planej");
    }

    if (tipo === "andamento") {
        return valor.includes("desenvolvimento") ||
               valor.includes("andamento");
    }

    return false;
}

function obterClasseStatus(status) {
    if (statusEh(status, "concluido")) return "status-concluido";
    if (statusEh(status, "planejado")) return "status-planejado";
    if (statusEh(status, "andamento")) return "status-andamento";
    return "status-padrao";
}

function criarElemento(tag, classe, texto) {
    const elemento = document.createElement(tag);

    if (classe) elemento.className = classe;
    if (texto !== undefined) elemento.textContent = texto;

    return elemento;
}

// Atualiza os quatro indicadores com dados reais da API.
function atualizarIndicadores(projetos) {
    totalProjetosPagina.textContent = projetos.length;

    totalAndamento.textContent = projetos.filter(
        projeto => statusEh(projeto.status, "andamento")
    ).length;

    totalPlanejados.textContent = projetos.filter(
        projeto => statusEh(projeto.status, "planejado")
    ).length;

    totalConcluidosPagina.textContent = projetos.filter(
        projeto => statusEh(projeto.status, "concluido")
    ).length;
}

// Atualiza a tabela aplicando a busca e o filtro selecionado.
function renderizarProjetos() {
    const busca = normalizarTexto(campoBusca.value);

    const filtrados = projetosCarregados.filter(projeto => {
        const correspondeBusca =
            normalizarTexto(projeto.nome).includes(busca) ||
            normalizarTexto(projeto.categoria).includes(busca);

        const correspondeStatus =
            !filtroStatusAtual ||
            normalizarTexto(projeto.status) ===
                normalizarTexto(filtroStatusAtual);

        return correspondeBusca && correspondeStatus;
    });

    listaProjetos.replaceChildren();

    if (filtrados.length === 0) {
        const linha = document.createElement("tr");
        const celula = document.createElement("td");

        celula.colSpan = 5;
        celula.textContent = projetosCarregados.length === 0
            ? "Nenhum projeto cadastrado."
            : "Nenhum projeto corresponde aos filtros.";

        linha.appendChild(celula);
        listaProjetos.appendChild(linha);
    }

    filtrados.forEach(projeto => {
        const linha = document.createElement("tr");

        const celulaId = criarElemento("td", "", projeto.id);
        const celulaProjeto = document.createElement("td");

        celulaProjeto.appendChild(
            criarElemento("strong", "", projeto.nome || "Sem nome")
        );

        if (projeto.descricao) {
            const descricao = criarElemento(
                "small",
                "descricao-tabela",
                projeto.descricao
            );
            celulaProjeto.appendChild(descricao);
        }

        const celulaCategoria = criarElemento(
            "td",
            "",
            projeto.categoria || "Sem categoria"
        );

        const celulaStatus = document.createElement("td");
        celulaStatus.appendChild(
            criarElemento(
                "span",
                `badge-status ${obterClasseStatus(projeto.status)}`,
                projeto.status || "Sem status"
            )
        );

        const celulaAcoes = criarElemento("td", "acoes-tabela");

        const botaoEditar = criarElemento("button", "botao-acao", "Editar");
        botaoEditar.type = "button";
        botaoEditar.addEventListener("click", () => editarProjeto(projeto));

        const botaoExcluir = criarElemento("button", "botao-acao botao-excluir", "Excluir");
        botaoExcluir.type = "button";
        botaoExcluir.addEventListener("click", () => excluirProjeto(projeto.id));

        celulaAcoes.append(botaoEditar, botaoExcluir);
        linha.append(
            celulaId,
            celulaProjeto,
            celulaCategoria,
            celulaStatus,
            celulaAcoes
        );

        listaProjetos.appendChild(linha);
    });

    contadorProjetos.textContent =
        `Exibindo ${filtrados.length} de ${projetosCarregados.length} projeto(s).`;

    if (window.lucide) window.lucide.createIcons();
}

// Consulta a API e atualiza os dados da página.
async function carregarProjetos() {
    try {
        mensagem.textContent = "Carregando projetos...";

        const resposta = await fetch(API_URL);

        if (!resposta.ok) {
            throw new Error("Não foi possível buscar os projetos.");
        }

        const projetos = await resposta.json();

        if (!Array.isArray(projetos)) {
            throw new Error("A API retornou dados em formato inesperado.");
        }

        projetosCarregados = projetos;

        atualizarIndicadores(projetosCarregados);
        renderizarProjetos();

        mensagem.textContent = "Lista de projetos atualizada.";
    } catch (erro) {
        console.error("Erro ao carregar projetos:", erro);
        mensagem.textContent =
            "Não foi possível carregar os projetos. Verifique se a API está ligada.";

        listaProjetos.replaceChildren();

        const linha = document.createElement("tr");
        const celula = document.createElement("td");
        celula.colSpan = 5;
        celula.textContent = "Falha ao carregar os dados.";
        linha.appendChild(celula);
        listaProjetos.appendChild(linha);

        contadorProjetos.textContent = "";

        totalProjetosPagina.textContent = "—";
        totalAndamento.textContent = "—";
        totalPlanejados.textContent = "—";
        totalConcluidosPagina.textContent = "—";
    }
}

function editarProjeto(projeto) {
    campoId.value = projeto.id;
    campoNome.value = projeto.nome ?? "";
    campoDescricao.value = projeto.descricao ?? "";
    campoCategoria.value = projeto.categoria ?? "";
    campoStatus.value = projeto.status ?? "";

    document.getElementById("tituloFormulario").textContent =
        "Editar projeto";
    document.getElementById("botaoSalvar").textContent =
        "Salvar alterações";

    mensagem.textContent = `Editando o projeto ${projeto.id}.`;

    formulario.scrollIntoView({ behavior: "smooth", block: "start" });
    campoNome.focus();
}

async function salvarProjeto(evento) {
    evento.preventDefault();

    const projeto = {
        nome: campoNome.value.trim(),
        descricao: campoDescricao.value.trim(),
        categoria: campoCategoria.value.trim(),
        status: campoStatus.value
    };

    if (
        !projeto.nome ||
        !projeto.descricao ||
        !projeto.categoria ||
        !projeto.status
    ) {
        mensagem.textContent = "Preencha todos os campos.";
        return;
    }

    if (projeto.nome.length > 150) {
        mensagem.textContent =
            "O nome do projeto deve ter no máximo 150 caracteres.";
        campoNome.focus();
        return;
    }

    const nomeNormalizado = normalizarTexto(projeto.nome);

const nomeDuplicado = projetosCarregados.some(item =>
    normalizarTexto(item.nome) === nomeNormalizado &&
    String(item.id) !== String(campoId.value)
);

if (nomeDuplicado) {
    mensagem.textContent =
        "Já existe um projeto cadastrado com esse nome.";
    campoNome.focus();
    return;
}

    const idAtual = campoId.value;
    const editando = idAtual !== "";

    let url = API_URL;
    let metodo = "POST";

    if (editando) {
        url += `/${encodeURIComponent(idAtual)}`;
        metodo = "PUT";
        projeto.id = Number(idAtual);
    } else {
        try {
            const respostaLista = await fetch(API_URL);

            if (!respostaLista.ok) {
                throw new Error("Não foi possível consultar os projetos.");
            }

            const projetos = await respostaLista.json();

            projeto.id = projetos.reduce(
                (maiorId, item) => Math.max(maiorId, Number(item.id) || 0),
                0
            ) + 1;
        } catch (erro) {
            console.error(erro);
            mensagem.textContent = "Não foi possível gerar o ID do projeto.";
            return;
        }
    }

    try {
        mensagem.textContent = editando
            ? "Salvando alterações..."
            : "Cadastrando projeto...";

        const resposta = await fetch(url, {
            method: metodo,
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(projeto)
        });

        if (!resposta.ok) {
            const erroServidor = await resposta.json().catch(() => ({}));
            throw new Error(
                erroServidor.erro || "Não foi possível salvar o projeto."
            );
        }

        formulario.reset();
        campoId.value = "";
        restaurarFormulario();

        // Atualiza a tabela e os indicadores antes de mostrar o sucesso.
        await carregarProjetos();
        mensagem.textContent = editando
            ? "Projeto atualizado com sucesso!"
            : "Projeto cadastrado com sucesso!";
    } catch (erro) {
        console.error(erro);
        mensagem.textContent = erro.message;
    }
}

async function excluirProjeto(id) {
    const confirmar = confirm(
        `Tem certeza de que deseja excluir o projeto ${id}?`
    );

    if (!confirmar) return;

    try {
        const resposta = await fetch(
            `${API_URL}/${encodeURIComponent(id)}`,
            { method: "DELETE" }
        );

        if (!resposta.ok) {
            const erroServidor = await resposta.json().catch(() => ({}));
            throw new Error(
                erroServidor.erro || "Não foi possível excluir o projeto."
            );
        }

        if (campoId.value === String(id)) {
            formulario.reset();
            campoId.value = "";
            restaurarFormulario();
        }

        await carregarProjetos();
        mensagem.textContent = "Projeto excluído com sucesso!";
    } catch (erro) {
        console.error(erro);
        mensagem.textContent = erro.message;
    }
}

function restaurarFormulario() {
    document.getElementById("tituloFormulario").textContent =
        "Cadastrar projeto";
    document.getElementById("botaoSalvar").textContent =
        "Cadastrar projeto";
}

function limparFormulario() {
    formulario.reset();
    campoId.value = "";
    restaurarFormulario();
    mensagem.textContent = "Formulário limpo.";
}

// Filtro por status: clicar novamente no mesmo cartão limpa o filtro.
document.querySelectorAll(".filtro-status").forEach(botao => {
    botao.addEventListener("click", () => {
        const status = botao.dataset.statusFiltro;

        filtroStatusAtual =
            normalizarTexto(filtroStatusAtual) === normalizarTexto(status)
                ? ""
                : status;

        document.querySelectorAll(".filtro-status").forEach(cartao => {
            const ativo = cartao.dataset.statusFiltro === filtroStatusAtual;
            cartao.classList.toggle("filtro-ativo", ativo);
            cartao.setAttribute("aria-pressed", String(ativo));
        });

        renderizarProjetos();
    });

    botao.setAttribute("aria-pressed", "false");
});

// Busca em tempo real.
campoBusca.addEventListener("input", renderizarProjetos);

// Formulário e botões.
formulario.addEventListener("submit", salvarProjeto);
botaoLimpar.addEventListener("click", limparFormulario);
botaoAtualizarLista.addEventListener("click", carregarProjetos);

// Tema claro/escuro compartilhado com o Dashboard.
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
        escuro ? "Ativar modo claro" : "Ativar modo escuro"
    );

    if (window.lucide) window.lucide.createIcons();
}

function configurarTema() {
    const temaSalvo = localStorage.getItem("temaSistema");
    aplicarTema(temaSalvo === "escuro" ? "escuro" : "claro");

    botaoTema.addEventListener("click", () => {
        const escuro = document.body.classList.contains("tema-escuro");
        aplicarTema(escuro ? "claro" : "escuro");
    });
}

configurarTema();
carregarProjetos();

if (window.lucide) window.lucide.createIcons();
