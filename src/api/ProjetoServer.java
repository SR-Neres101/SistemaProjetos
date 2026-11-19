package api;

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
        "Erro ao carregar projetos: " + e.getMessage()
    );
    return;
}

        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        // Mapeia a rota base /projetos
        server.createContext("/projetos", exchange -> {

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {

                // Pega o caminho completo acessado (ex: "/projetos" ou "/projetos/2")
                String path = exchange.getRequestURI().getPath();
                String[] partes = path.split("/");

                // Se houver um parâmetro após /projetos/ (ex: /projetos/2)
                if (partes.length == 3 && !partes[2].isBlank()) {
    try {
        int id = Integer.parseInt(partes[2]);
        Projeto projeto = service.buscaPorId(id);

        if (projeto != null) {
            String json = converterObjetoParaJson(projeto);
            responder(exchange, 200, json);
        } else {
            responder(exchange, 404,
                "{\"erro\":\"Projeto não encontrado\"}");
        }

    } catch (NumberFormatException e) {
        responder(exchange, 400,
            "{\"erro\":\"ID deve ser um número inteiro\"}");
    }
} else {
    List<Projeto> projetos = service.listar();
    String json = converterListaParaJson(projetos);
    responder(exchange, 200, json);
}
            } else {
                responder(exchange, 405, "{\"erro\": \"Método não permitido\"}");
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
        if (texto == null) return "";
        return texto.replace("\"", "\\\"");
    }
}