package api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import model.Projeto;
import service.ProjetoService;

public class ProjetoServer {

    public static void main(String[] args) throws IOException {

        ProjetoService service = new ProjetoService();

        try {
            service.carregar();
        } catch (Exception e) {
            System.err.println(
                    "Erro ao carregar projetos: " + e.getMessage());
            return;
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        // Mapeia a rota base /projetos

        server.createContext("/projetos", exchange -> {
            String metodo = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            try {
                if ("GET".equalsIgnoreCase(metodo)) {

                    String[] partes = path.split("/");

                    if (partes.length == 3 && !partes[2].isBlank()) {
                        try {
                            int id = Integer.parseInt(partes[2]);
                            Projeto projeto = service.buscaPorId(id);

                            if (projeto != null) {
                                responder(exchange, 200,
                                        converterObjetoParaJson(projeto));
                            } else {
                                responder(exchange, 404,
                                        "{\"erro\":\"Projeto não encontrado\"}");
                            }
                        } catch (NumberFormatException e) {
                            responder(exchange, 400,
                                    "{\"erro\":\"ID deve ser um número inteiro\"}");
                        }
                    } else if ("/projetos".equals(path)
                            || "/projetos/".equals(path)) {
                        responder(exchange, 200,
                                converterListaParaJson(service.listar()));
                    } else {
                        responder(exchange, 404,
                                "{\"erro\":\"Rota não encontrada\"}");
                    }

                } else if ("POST".equalsIgnoreCase(metodo)) {

                    if (!"/projetos".equals(path)
                            && !"/projetos/".equals(path)) {
                        responder(exchange, 404,
                                "{\"erro\":\"Rota não encontrada\"}");
                        return;
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode json = mapper.readTree(exchange.getRequestBody());

                    if (json == null || !json.isObject()) {
                        responder(exchange, 400,
                                "{\"erro\":\"Envie um objeto JSON válido\"}");
                        return;
                    }

                    JsonNode idNode = json.get("id");
                    JsonNode nomeNode = json.get("nome");
                    JsonNode descricaoNode = json.get("descricao");
                    JsonNode categoriaNode = json.get("categoria");
                    JsonNode statusNode = json.get("status");

                    if (idNode == null || !idNode.isIntegralNumber()
                            || !idNode.canConvertToInt()
                            || nomeNode == null || !nomeNode.isTextual()
                            || descricaoNode == null || !descricaoNode.isTextual()
                            || categoriaNode == null || !categoriaNode.isTextual()
                            || statusNode == null || !statusNode.isTextual()) {

                        responder(exchange, 400,
                                "{\"erro\":\"Informe id inteiro e os campos de texto: nome, descricao, categoria e status\"}");
                        return;
                    }

                    int id = idNode.intValue();
                    String nome = nomeNode.asText().trim();
                    String descricao = descricaoNode.asText().trim();
                    String categoria = categoriaNode.asText().trim();
                    String status = statusNode.asText().trim();

                    if (id <= 0 || nome.isBlank() || descricao.isBlank()
                            || categoria.isBlank() || status.isBlank()) {
                        responder(exchange, 400,
                                "{\"erro\":\"Todos os campos são obrigatórios e o ID deve ser positivo\"}");
                        return;
                    }

                    if (service.buscaPorId(id) != null) {
                        responder(exchange, 409,
                                "{\"erro\":\"Já existe um projeto com esse ID\"}");
                        return;
                    }

                    Projeto projeto = new Projeto(
                            id, nome, descricao, categoria, status);

                    try {
                        service.adicionarESalvar(projeto);

                        responder(exchange, 201,
                                converterObjetoParaJson(projeto));
                    } catch (Exception e) {
                        System.err.println(
                                "Erro ao salvar projeto: " + e.getMessage());

                        responder(exchange, 500,
                                "{\"erro\":\"Não foi possível salvar o projeto\"}");
                    }

                } else if ("PUT".equalsIgnoreCase(metodo)) {

                    String[] partes = path.split("/");

                    if (partes.length != 3 || partes[2].isBlank()) {
                        responder(exchange, 400,
                                "{\"erro\":\"Informe o ID na URL: /projetos/6\"}");
                        return;
                    }

                    int id;

                    try {
                        id = Integer.parseInt(partes[2]);

                        if (id <= 0) {
                            responder(exchange, 400,
                                    "{\"erro\":\"O ID deve ser positivo\"}");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        responder(exchange, 400,
                                "{\"erro\":\"ID deve ser um número inteiro\"}");
                        return;
                    }

                    if (service.buscaPorId(id) == null) {
                        responder(exchange, 404,
                                "{\"erro\":\"Projeto não encontrado\"}");
                        return;
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode json = mapper.readTree(exchange.getRequestBody());

                    if (json == null || !json.isObject()) {
                        responder(exchange, 400,
                                "{\"erro\":\"Envie um objeto JSON válido\"}");
                        return;
                    }

                    JsonNode nomeNode = json.get("nome");
                    JsonNode descricaoNode = json.get("descricao");
                    JsonNode categoriaNode = json.get("categoria");
                    JsonNode statusNode = json.get("status");

                    if (nomeNode == null || !nomeNode.isTextual()
                            || descricaoNode == null || !descricaoNode.isTextual()
                            || categoriaNode == null || !categoriaNode.isTextual()
                            || statusNode == null || !statusNode.isTextual()) {

                        responder(exchange, 400,
                                "{\"erro\":\"Informe nome, descricao, categoria e status como textos\"}");
                        return;
                    }

                    String nome = nomeNode.asText().trim();
                    String descricao = descricaoNode.asText().trim();
                    String categoria = categoriaNode.asText().trim();
                    String status = statusNode.asText().trim();

                    if (nome.isBlank() || descricao.isBlank()
                            || categoria.isBlank() || status.isBlank()) {
                        responder(exchange, 400,
                                "{\"erro\":\"Todos os campos são obrigatórios\"}");
                        return;
                    }

                    Projeto projetoAtualizado = new Projeto(
                            id, nome, descricao, categoria, status);

                    if (!service.alterar(projetoAtualizado)) {
                        responder(exchange, 404,
                                "{\"erro\":\"Projeto não encontrado\"}");
                        return;
                    }

                    try {
                        service.salvar();

                        Projeto projetoSalvo = service.buscaPorId(id);

                        responder(exchange, 200,
                                converterObjetoParaJson(projetoSalvo));

                    } catch (Exception e) {
                        System.err.println(
                                "Erro ao salvar projeto atualizado: "
                                        + e.getMessage());

                        responder(exchange, 500,
                                "{\"erro\":\"Não foi possível salvar a atualização\"}");
                    }

                } else if ("DELETE".equalsIgnoreCase(metodo)) {

                    String[] partes = path.split("/");

                    // Verifica se o ID foi informado na URL
                    if (partes.length != 3 || partes[2].isBlank()) {
                        responder(exchange, 400,
                                "{\"erro\":\"Informe o ID na URL: /projetos/6\"}");
                        return;
                    }

                    int id;

                    // Valida o formato e o valor do ID
                    try {
                        id = Integer.parseInt(partes[2]);

                        if (id <= 0) {
                            responder(exchange, 400,
                                    "{\"erro\":\"O ID deve ser positivo\"}");
                            return;
                        }

                    } catch (NumberFormatException e) {
                        responder(exchange, 400,
                                "{\"erro\":\"ID deve ser um número inteiro\"}");
                        return;
                    }

                    // Verifica se o projeto existe
                    Projeto projeto = service.buscaPorId(id);

                    if (projeto == null) {
                        responder(exchange, 404,
                                "{\"erro\":\"Projeto não encontrado\"}");
                        return;
                    }

                    // Remove o projeto da lista
                    if (!service.removePorId(id)) {
                        responder(exchange, 404,
                                "{\"erro\":\"Projeto não encontrado\"}");
                        return;
                    }

                    // Salva a lista atualizada no CSV
                    try {
                        service.salvar();

                        responder(exchange, 200,
                                "{\"mensagem\":\"Projeto removido com sucesso\"}");

                    } catch (Exception e) {
                        // Restaura o projeto na memória se a gravação falhar
                        service.adicionar(projeto);

                        System.err.println(
                                "Erro ao salvar exclusão: " + e.getMessage());

                        responder(exchange, 500,
                                "{\"erro\":\"Não foi possível salvar a exclusão\"}");
                    }

                } else {
                    exchange.getResponseHeaders().set(
                            "Allow", "GET, POST, PUT, DELETE");

                    responder(exchange, 405,
                            "{\"erro\":\"Método não permitido\"}");
                }

            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                responder(exchange, 400,
                        "{\"erro\":\"JSON inválido\"}");
            } catch (Exception e) {
                System.err.println(
                        "Erro ao processar requisição: " + e.getMessage());

                responder(exchange, 500,
                        "{\"erro\":\"Erro interno do servidor\"}");
            } finally {
                exchange.close();
            }
        });

        server.start();
        System.out.println("✅ Servidor rodando em: http://localhost:8081/projetos");
    }

    private static void responder(HttpExchange exchange, int statusCode, String resposta) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = resposta.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static String converterObjetoParaJson(Projeto p) {
        return "{"
                + "\"id\":" + p.getId() + ","
                + "\"nome\":\"" + escapar(p.getNome()) + "\","
                + "\"descricao\":\"" + escapar(p.getDescricao()) + "\","
                + "\"categoria\":\"" + escapar(p.getCategoria()) + "\","
                + "\"status\":\"" + escapar(p.getStatus()) + "\""
                + "}";
    }

    private static String converterListaParaJson(List<Projeto> projetos) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < projetos.size(); i++) {
            sb.append(converterObjetoParaJson(projetos.get(i)));
            if (i < projetos.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapar(String texto) {
        if (texto == null)
            return "";
        return texto.replace("\"", "\\\"");
    }
}