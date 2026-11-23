package dao;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import model.Projeto;

public class ProjetoCSV {

    private final Path caminho;

    public ProjetoCSV() {
        caminho = Path.of("dados", "projetos.csv");
    }

    public void salvar(List<Projeto> projetos) throws IOException {
    Files.createDirectories(caminho.getParent());

    List<String> linhas = new ArrayList<>();
    linhas.add("id;nome;descricao;categoria;status");

    for (Projeto projeto : projetos) {
        String linha =
            projeto.getId() + ";" +
            escaparCampo(projeto.getNome()) + ";" +
            escaparCampo(projeto.getDescricao()) + ";" +
            escaparCampo(projeto.getCategoria()) + ";" +
            escaparCampo(projeto.getStatus());

        linhas.add(linha);
    }

    // Cria um arquivo temporário na mesma pasta do CSV.
    Path temporario = Files.createTempFile(
        caminho.getParent(), "projetos-", ".tmp"
    );

    boolean movido = false;

    try {
        // Escreve os dados no arquivo temporário primeiro.
        Files.write(temporario, linhas, StandardCharsets.UTF_8);

        // Só substitui o CSV original depois de concluir a escrita.
        try {
            Files.move(
                temporario,
                caminho,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            );
        } catch (AtomicMoveNotSupportedException e) {
            // Alternativa para sistemas sem suporte à movimentação atômica.
            Files.move(
                temporario,
                caminho,
                StandardCopyOption.REPLACE_EXISTING
            );
        }

        movido = true;

    } finally {
        // Se a operação falhar, tenta remover o temporário.
        if (!movido) {
            try {
                Files.deleteIfExists(temporario);
            } catch (IOException ignored) {
                // Um temporário pode permanecer se não puder ser removido.
            }
        }
    }
}

    public List<Projeto> listar() throws IOException {
        List<Projeto> projetos = new ArrayList<>();

        if (!Files.exists(caminho)) {
            return projetos;
        }

        List<String> linhas =
            Files.readAllLines(caminho, StandardCharsets.UTF_8);

        for (int i = 1; i < linhas.size(); i++) {
            String linha = linhas.get(i);

            if (linha.isBlank()) {
                continue;
            }

            String[] dados = separarCampos(linha);

            if (dados.length != 5) {
                throw new IOException(
                    "Linha " + (i + 1) +
                    " inválida no CSV. Esperados 5 campos, encontrados " +
                    dados.length + "."
                );
            }

            int id;

            try {
                id = Integer.parseInt(dados[0]);
            } catch (NumberFormatException e) {
                throw new IOException(
                    "ID inválido na linha " + (i + 1) + " do CSV.", e
                );
            }

            Projeto projeto = new Projeto(
                id,
                dados[1],
                dados[2],
                dados[3],
                dados[4]
            );

            projetos.add(projeto);
        }

        return projetos;
    }

    private String escaparCampo(String valor) {
        if (valor == null) {
            return "";
        }

        if (valor.contains(";") || valor.contains("\"")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }

        return valor;
    }

    private String[] separarCampos(String linha) throws IOException {
        List<String> campos = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean dentroAspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char atual = linha.charAt(i);

            if (atual == '"') {
                if (dentroAspas
                        && i + 1 < linha.length()
                        && linha.charAt(i + 1) == '"') {

                    campo.append('"');
                    i++;

                } else if (dentroAspas) {
                    dentroAspas = false;

                } else if (campo.length() == 0) {
                    dentroAspas = true;

                } else {
                    campo.append(atual);
                }

            } else if (atual == ';' && !dentroAspas) {
                campos.add(campo.toString());
                campo.setLength(0);

            } else {
                campo.append(atual);
            }
        }

        if (dentroAspas) {
            throw new IOException(
                "Campo com aspas não fechadas no CSV: " + linha
            );
        }

        // Também preserva o último campo quando ele está vazio.
        campos.add(campo.toString());

        return campos.toArray(new String[0]);
    }
}