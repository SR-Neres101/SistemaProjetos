import model.Projeto;
import service.ProjetoService;

public class Main{
    public static void main(String[] args) throws Exception{
        
        ProjetoService service = new ProjetoService();

        service.carregar();

        System.out.println("===========================");
        System.out.println("    SYSTEMA DE PROJETOS");
        System.out.println("============================\n");
        System.out.println("Projeto: Portifólio acadêmico\nDesenvolvido em java\nVersão: 1.2");

        System.out.println("Projetos carregados"+service.listar().size());

        Projeto p1 = new Projeto(1, "Sistema Acadêmico", "Sistema para gerenciamento acadêmico", "Software", "Concluído");

         boolean cadastrado = service.adicionar(p1);

         if(cadastrado) {
            service.salvar();

            System.out.println("Projeto cadastrado e salvo.");
         } else {
            
            System.out.println("Projeto incapaz de ser cadastrado");
         }
         
        Projeto projeto = service.buscaPorId(4);
        if (projeto == null) {

            System.out.println("Projeto não encontrado");
        }

        boolean removido = service.removePorId(7);
        if(removido){
            service.salvar();
            System.out.print("Projeto removido com sucesso!");
        } else {
            System.out.print("Projeto incapaz de ser removido ou inezistente!");
        }

        boolean statusnovo = service.alterarStatus(1, "Concluído");

        if(statusnovo){
            service.salvar();
        }
    }
}
