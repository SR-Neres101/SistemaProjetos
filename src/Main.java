import model.Projeto;
import service.ProjetoService;

public class Main{
    public static void main(String[] args){

        ProjetoService service = new

        ProjetoService();
        System.out.println("===========================");
        System.out.println("    SYSTEMA DE PROJETOS");
        System.out.println("============================\n");
        System.out.println("Projeto: Portifólio acadêmico\nDesenvolvido em java\nVersão: 1.1");

        Projeto p1 = new Projeto(1, "Sistema Acadêmico", "Sistema para gerenciamento acadêmico", "Software", "Concluído");
        
        Projeto p2 = new Projeto(2, "Site institucional", "Website de uma instituição", "Web", "Concluído");
        
        Projeto p3 = new Projeto(3, "Aplicativo Mobile", "Um aplicativo para Mobile", "Mobile", "Planejado");

        service.adicionar(p1);
        service.adicionar(p2);
        service.adicionar(p3);

        for(Projeto projeto : service.listar()) {
            projeto.exibirDados();
        }
        System.out.println("Total de projetos: "+service.listar().size());

        Projeto encontrado = service.buscaPorId(1);

        if(encontrado != null){
            System.out.println("Projeto encontrado:");
            encontrado.exibirDados();
        } else {
            System.out.println("Projeto não encontrado.");
        }

        System.out.println("Projetos Web");
        for(Projeto projeto : service.buscarPorCategoria("web")){
            projeto.exibirDados();
        }

        System.out.println("Projetos concuídos");
        for(Projeto projeto : service.buscarPorStatus("concluído")){
            projeto.exibirDados();
        }

        boolean removido = service.removePorId(2);

        if(removido){
            System.out.println("Projeto removido com sucesso.");
        }else{
            System.out.println("Projeto não encontrado.");
        }
    }

}
