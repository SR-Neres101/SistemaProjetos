package service;

import dao.ProjetoCSV;
import java.util.ArrayList;
import java.util.List;
import model.Projeto;

public class ProjetoService {
    private final ProjetoCSV dao;
    private List<Projeto> projetos;
    
    public ProjetoService(){
        projetos = new ArrayList<>();
        dao = new ProjetoCSV();
    }

    public void carregar() throws Exception {
        projetos = dao.listar();
    }

    public void salvar()
        throws Exception {

        dao.salvar(projetos);

    }

    public boolean adicionar(Projeto projeto){

        if(projeto.getNome() == null || projeto.getNome().isBlank()){
            return false;
        }
        
        Projeto existente = buscaPorId(projeto.getId());
        if(existente != null){
            return false;
        }
        projetos.add(projeto);
        return true;
    }

    public List<Projeto> listar(){
        return projetos;
    }

    public Projeto buscaPorId(int id){
        for(Projeto projeto : projetos){
            if(projeto.getId() == id){
                return projeto;
            }
        }

        return null;
    }

    public List<Projeto> buscarPorCategoria(String categoria) {
        List<Projeto> resultado = new ArrayList<>();
        for(Projeto projeto : projetos){
            if(projeto.getCategoria().equalsIgnoreCase(categoria)){
                resultado.add(projeto);
            }
        }
        return resultado;
    }
    public List<Projeto> buscarPorStatus(String status) {
        List<Projeto> resultado = new ArrayList<>();
        for(Projeto projeto : projetos){
            if(projeto.getStatus().equalsIgnoreCase(status)){
                resultado.add(projeto);
            }
        }
        return resultado;
    }

    public boolean removePorId(int id){
        Projeto projeto = buscaPorId(id);

        if(projeto != null) {
            projetos.remove(projeto);
            return true;
        }
        return false;
    }

    public boolean alterarStatus(int id, String novoStatus) {

    Projeto projeto = buscaPorId(id);

        if (projeto == null) {

            return false;

        }

        projeto.setStatus(novoStatus);

        return true;
    }
}
