import model.Projeto;

public class Main{
    public static void main(String[] args){
        System.out.println("===========================");
        System.out.println("    SYSTEMA DE PROJETOS");
        System.out.println("============================\n");
        System.out.println("Projeto: Portifólio acadêmico\nDesenvolvido em java\nVersão: 1.0");

        Projeto projeto1 = new Projeto(1, "Sistema Acadêmico", "Sistema para gerenciamento acadêmico", "Software", "Concluído");
        
        Projeto projeto2 = new Projeto(2, "Site institucional", "Website de uma instituição", "Web", "Concluído");
        
        Projeto projeto3 = new Projeto(3, "Aplicativo Mobile", "Um aplicativo para Mobile", "Mobile", "Planejado");

        projeto1.exibirDados();
        projeto2.exibirDados();
        projeto3.exibirDados();

        System.out.print(projeto1.getStatus());
    }
}
