package client;

import java.util.List;
import model.Projeto;

public class TesteApiClient {

    public static void main(String[] args) {
        try {
            ApiClient apiClient = new ApiClient();

            List<Projeto> projetos = apiClient.listarProjetosComoObjetos();

            for (Projeto projeto : projetos) {
                projeto.exibirDados();
            }

        } catch (Exception e) {
            System.out.println(
                    "Falha ao consultar a API: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
