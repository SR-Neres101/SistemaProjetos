import java.util.Scanner;
import model.Projeto;
import service.ProjetoService;

public class Main {
    public static void main(String[] args) throws Exception{
        Scanner sc = new Scanner(System.in);
        
        ProjetoService service = new ProjetoService();

        service.carregar();

        //Recepção
        System.out.println("Projeto: Portifólio acadêmico\nDesenvolvido em java\nVersão: 1.3");
        System.out.println("============================\n    SYSTEMA DE PROJETOS\n============================\n");
        int opcao = -1;
        do{
            System.out.println("Total de projetos: "+service.listar().size());
        System.out.println("1 - Listar projetos");
        System.out.println("2 - Buscar projeto");
        System.out.println("3 - Cadastrar projeto");
        System.out.println("4 - Alterar projeto");
        System.out.println("5 - Excluir projeto");
        System.out.println("0 - Sair");
        System.out.print("Escolha: ");
        opcao = sc.nextInt();
        switch (opcao){
            case 1:
                System.out.println("\n--- LISTA DE PROJETOS---");

                for( Projeto projeto : service.listar()){
                    projeto.exibirDados();
                    System.out.println("------------------------");
                }
                break;

            case 2:
                System.out.println("Digite o ID: ");

                int idBusca = sc.nextInt();
                Projeto projeto = service.buscaPorId(idBusca);

                if(projeto != null){
                    projeto.exibirDados();
                } else {
                    System.out.println("Projeto não encontrado.");
                }
                break;

            case 3:
                System.out.println("Digite os seguintes campos para criar. ");

                System.out.print("ID: ");
                int id = sc.nextInt();
                sc.nextLine();

                System.out.print("NOME: ");
                String nome = sc.nextLine();

                if(nome == null || nome.isBlank()){
                    System.out.println("O nome é obrigatório.");

                    break;
                }

                System.out.print("DESCRIÇÃO: ");
                String descricao= sc.nextLine();

                System.out.print("CATEGORIA: ");
                String categoria = sc.nextLine();

                if (categoria.isBlank()) {

                    System.out.println("A categoria é obrigatória.");

                    break;
                }
                
                System.out.print("STATUS: ");
                String status = sc.nextLine();

                Projeto projet = new Projeto(id, nome, descricao, categoria, status);
                boolean cadastrado = service.adicionar(projet);

                if(cadastrado) {
                    service.salvar();

                    System.out.println("Projeto cadastrado e salvo.");
                } else {
                    
                    System.out.println("Projeto incapaz de ser cadastrado");
                }
                break;

            case 4:
                System.out.print("Digite o ID do projeto: ");

                int idAlterar = sc .nextInt();
                sc.nextLine();

                Projeto existente = service.buscaPorId(idAlterar);

                if (existente == null){
                    System.out.println("Projeto não encontrado.");
                } else {
                    System.out.println("\nProjeto atual:");
                    existente.exibirDados();

                    System.out.print("Novo nome: ");
                    String nomeAlt = sc.nextLine();

                    if(nomeAlt == null || nomeAlt.isBlank()){
                        System.out.println("O nome é obrigatório.");

                        break;
                    }

                    System.out.print("Nova descrição: ");
                    String descricaoAlt = sc.nextLine();
                    System.out.print("Nova categoria: ");
                    String categoriaAlt = sc.nextLine();
                    if (categoriaAlt.isBlank()) {

                    System.out.println("A categoria é obrigatória.");

                    break;
                }
                    System.out.print("Novo status: ");
                    String statusAlt = sc.nextLine();

                    
                    Projeto atualizado = new Projeto(idAlterar, nomeAlt, descricaoAlt, categoriaAlt, statusAlt);

                    boolean alterado = service.alterar(atualizado);

                    if(alterado){
                        service.salvar();

                        System.out.println("Projeto alterado com sucesso");
                    } else {
                        System.out.println("Não foi possível alterar");
                    }
                }
                break;

            case 5:
                System.out.print("Digite o ID do projeto: ");

                int idExcluir = sc.nextInt();

                Projeto projetoDel = service.buscaPorId(idExcluir);

                if (projetoDel == null) {
                    System.out.println("Projeto não encontrado.");
                } else {
                    System.out.print("Confirma a exclusão? (S/N): ");

                    sc.nextLine();
                    String confirmar = sc.nextLine();

                    if(confirmar.equalsIgnoreCase("S")) {
                        boolean removido = service.removePorId(idExcluir);

                        if(removido){
                            service.salvar();
                            System.out.println("Projeto excluido.");
                        } else {
                            System.out.println("Exclusão cancelada.");
                        }
                    } else if(confirmar.equalsIgnoreCase("N")){
                        System.out.println("Exclusão cancelada.");
                    }
                }
                break;

            case 0:
                System.out.println("Encerrando o sistema...");
                break;

            default:
                System.out.println("Opção inválida.");
        }
    } while(opcao != 0);
    sc.close();  
    }
}
