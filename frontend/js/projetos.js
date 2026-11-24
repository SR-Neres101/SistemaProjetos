
const API_URL = "http://localhost:8081/projetos";

const formulario = document.getElementById("formProjeto");
const campoId = document.getElementById("projetoId");
const campoNome = document.getElementById("nome");
const campoDescricao = document.getElementById("descricao");
const campoCategoria = document.getElementById("categoria");
const campoStatus = document.getElementById("status");

const listaProjetos = document.getElementById("listaProjetos");
const mensagem = document.getElementById("mensagem");

const botaoLimpar = document.getElementById("botaoLimpar");
const botaoAtualizarLista = document.getElementById("botaoAtualizarLista");

async function carregarProjetos() {
    try {
        mensagem.textContent = "Carregando projetos...";

        const resposta = await fetch(API_URL);

        if (!resposta.ok) {
            throw new Error("Não foi possível buscar os projetos.");
        }

        const projetos = await resposta.json();

        listaProjetos.replaceChildren();

        if (projetos.length === 0) {
            const linha = document.createElement("tr");
            const celula = document.createElement("td");

            celula.colSpan = 6;
            celula.textContent = "Nenhum projeto cadastrado.";

            linha.appendChild(celula);
            listaProjetos.appendChild(linha);
        }

        projetos.forEach(projeto => {
            const linha = document.createElement("tr");

            [
                projeto.id,
                projeto.nome,
                projeto.descricao,
                projeto.categoria,
                projeto.status
            ].forEach(valor => {
                const celula = document.createElement("td");
                celula.textContent = valor;
                linha.appendChild(celula);
            });

            const celulaAcoes = document.createElement("td");

            const botaoEditar = document.createElement("button");
            botaoEditar.textContent = "Editar";
            botaoEditar.type = "button";
            botaoEditar.addEventListener("click", () => editarProjeto(projeto));

            const botaoExcluir = document.createElement("button");
            botaoExcluir.textContent = "Excluir";
            botaoExcluir.type = "button";
            botaoExcluir.style.marginLeft = "6px";
            botaoExcluir.addEventListener("click", () => excluirProjeto(projeto.id));

            celulaAcoes.append(botaoEditar, botaoExcluir);
            linha.appendChild(celulaAcoes);

            listaProjetos.appendChild(linha);
        });

        mensagem.textContent = "Lista de projetos atualizada.";

    } catch (erro) {
        console.error(erro);
        mensagem.textContent =
            "Não foi possível carregar os projetos. Verifique se a API está ligada.";
    }
}

function editarProjeto(projeto) {
    campoId.value = projeto.id;
    campoNome.value = projeto.nome;
    campoDescricao.value = projeto.descricao;
    campoCategoria.value = projeto.categoria;
    campoStatus.value = projeto.status;

    mensagem.textContent = `Editando o projeto ${projeto.id}.`;
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

    const idAtual = campoId.value;
    const editando = idAtual !== "";

    let url = API_URL;
    let metodo = "POST";

    if (editando) {
        url += `/${idAtual}`;
        metodo = "PUT";
    } else {
        try {
            const respostaLista = await fetch(API_URL);

            if (!respostaLista.ok) {
                throw new Error("Não foi possível consultar os projetos.");
            }

            const projetos = await respostaLista.json();

            projeto.id = projetos.reduce(
                (maiorId, item) => Math.max(maiorId, item.id),
                0
            ) + 1;

        } catch (erro) {
            console.error(erro);
            mensagem.textContent = "Não foi possível gerar o ID do projeto.";
            return;
        }
    }

    try {
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

        mensagem.textContent = editando
            ? "Projeto atualizado com sucesso!"
            : "Projeto cadastrado com sucesso!";

        formulario.reset();
        campoId.value = "";

        await carregarProjetos();

    } catch (erro) {
        console.error(erro);
        mensagem.textContent = erro.message;
    }
}

async function excluirProjeto(id) {
    const confirmar = confirm(
        `Tem certeza de que deseja excluir o projeto ${id}?`
    );

    if (!confirmar) {
        return;
    }

    try {
        const resposta = await fetch(`${API_URL}/${id}`, {
            method: "DELETE"
        });

        if (!resposta.ok) {
            const erroServidor = await resposta.json().catch(() => ({}));
            throw new Error(
                erroServidor.erro || "Não foi possível excluir o projeto."
            );
        }

        mensagem.textContent = "Projeto excluído com sucesso!";
        formulario.reset();
        campoId.value = "";

        await carregarProjetos();

    } catch (erro) {
        console.error(erro);
        mensagem.textContent = erro.message;
    }
}

formulario.addEventListener("submit", salvarProjeto);

botaoLimpar.addEventListener("click", () => {
    formulario.reset();
    campoId.value = "";
    mensagem.textContent = "Formulário limpo.";
});

botaoAtualizarLista.addEventListener("click", carregarProjetos);

carregarProjetos();
