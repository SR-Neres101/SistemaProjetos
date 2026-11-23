
package client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Projeto;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {

        private final ObjectMapper mapper = new ObjectMapper();
        private final HttpClient client;

        private final String baseUrl = "http://localhost:8081";

        public ApiClient() {
                client = HttpClient.newHttpClient();
        }

        public String listarProjetos() throws Exception {

                HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(baseUrl + "/projetos"))
                                .GET()
                                .build();

                HttpResponse<String> response = client.send(
                                request,
                                HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                        throw new RuntimeException(
                                        "Erro ao listar projetos. HTTP: "
                                                        + response.statusCode());
                }

                return response.body();
        }

        public List<Projeto> listarProjetosComoObjetos() throws Exception {
                String json = listarProjetos();

                return mapper.readValue(
                                json,
                                new TypeReference<List<Projeto>>() {
                                });
        }

        public void cadastrarProjeto(Projeto projeto) throws Exception {

                String json = mapper.writeValueAsString(projeto);

                HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(baseUrl + "/projetos"))
                                .header("Content-Type", "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(json))
                                .build();

                HttpResponse<String> response = client.send(
                                request,
                                HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 201) {
                        throw new RuntimeException(
                                        "Erro ao cadastrar projeto. HTTP: "
                                                        + response.statusCode()
                                                        + "\nResposta: " + response.body());
                }
        }

        public void atualizarProjeto(int id, Projeto projeto) throws Exception {

                String json = mapper.writeValueAsString(projeto);

                HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(baseUrl + "/projetos/" + id))
                                .header("Content-Type", "application/json")
                                .PUT(HttpRequest.BodyPublishers.ofString(json))
                                .build();

                HttpResponse<String> response = client.send(
                                request,
                                HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                        throw new RuntimeException(
                                        "Erro ao atualizar projeto. HTTP: "
                                                        + response.statusCode()
                                                        + "\nResposta: " + response.body());
                }
        }

        public void excluirProjeto(int id) throws Exception {
                HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(baseUrl + "/projetos/" + id))
                                .DELETE()
                                .build();

                HttpResponse<String> response = client.send(
                                request,
                                HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                        throw new RuntimeException(
                                        "Erro ao excluir projeto. HTTP: "
                                                        + response.statusCode()
                                                        + "\nResposta: " + response.body());
                }
        }
}
