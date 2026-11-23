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

    public boolean adicionarESalvar(Projeto projeto) throws Exception {

    // Primeiro, valida e adiciona à lista.
    if (!adicionar(projeto)) {
        return false;
    }

    try {
        // Tenta persistir a lista atualizada.
        salvar();
        return true;

    } catch (Exception e) {
        // Se falhar, desfaz a inclusão na memória.
        removePorId(projeto.getId());

        // Repassa o erro para a interface tratar.
        throw e;
    }
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

    public boolean alterar(Projeto projetoAtualizado) {

    Projeto projeto = buscaPorId(projetoAtualizado.getId());

        if (projeto == null) {

            return false;

        }

        projeto.setNome(projetoAtualizado.getNome());
        projeto.setDescricao(projetoAtualizado.getDescricao());
        projeto.setCategoria(projetoAtualizado.getCategoria());
        projeto.setStatus(projetoAtualizado.getStatus());

        return true;
    }
}
