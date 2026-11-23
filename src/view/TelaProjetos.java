
package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import model.Projeto;
import client.ApiClient;
import java.util.List;

public class TelaProjetos extends JFrame {

    private ApiClient apiClient;

    private boolean dadosCarregados = false;

    private JTextField campoNome;
    private JTextField campoDescricao;

    private JComboBox<String> comboCategoria;
    private JComboBox<String> comboStatus;

    private JTable tabela;
    private DefaultTableModel modelo;

    private JButton botaoCadastrar;
    private JButton botaoEditar;
    private JButton botaoExcluir;
    private JButton botaoLimpar;

    private int idProjetoSelecionado = -1;

    public TelaProjetos() {
        setTitle("Sistema de Projetos");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        apiClient = new ApiClient();

        try {
            apiClient.listarProjetosComoObjetos();
            dadosCarregados = true;

        } catch (Exception e) {
            dadosCarregados = false;

            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível carregar os projetos pela API.\n" +
                            "Verifique se o servidor está funcionando.\n\n" +
                            "Detalhes: " + e.getMessage(),
                    "Erro ao carregar projetos",
                    JOptionPane.ERROR_MESSAGE);
        }

        criarComponentes();
        criarEventos();
        carregarTabela();

        atualizarEstadoFormulario();
    }

    private void criarComponentes() {
        campoNome = new JTextField(20);
        campoDescricao = new JTextField(20);

        comboCategoria = new JComboBox<>(new String[] {
                "Web", "Software", "Mobile", "Outro"
        });

        comboStatus = new JComboBox<>(new String[] {
                "Planejado",
                "Em desenvolvimento",
                "Concluído"
        });

        botaoCadastrar = new JButton("Cadastrar");
        botaoEditar = new JButton("Editar");
        botaoExcluir = new JButton("Excluir");
        botaoLimpar = new JButton("Limpar");

        modelo = new DefaultTableModel(
                new String[] { "ID", "Nome", "Categoria", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modelo);

        JPanel painelFormulario = new JPanel(
                new GridLayout(0, 2, 8, 8));

        painelFormulario.setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12));

        painelFormulario.add(new JLabel("Nome:"));
        painelFormulario.add(campoNome);

        painelFormulario.add(new JLabel("Descrição:"));
        painelFormulario.add(campoDescricao);

        painelFormulario.add(new JLabel("Categoria:"));
        painelFormulario.add(comboCategoria);

        painelFormulario.add(new JLabel("Status:"));
        painelFormulario.add(comboStatus);

        painelFormulario.add(botaoCadastrar);
        painelFormulario.add(botaoLimpar);
        painelFormulario.add(botaoEditar);
        painelFormulario.add(botaoExcluir);

        setLayout(new BorderLayout());

        add(painelFormulario, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
    }

    private void criarEventos() {
        botaoLimpar.addActionListener(
                e -> limparFormulario());

        botaoCadastrar.addActionListener(
                e -> cadastrar());

        botaoEditar.addActionListener(
                e -> editarProjeto());

                botaoExcluir.addActionListener(
    e -> excluirProjeto()
);

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarProjetoSelecionado();
            }
        });
    }

    private void atualizarEstadoFormulario() {
        campoNome.setEnabled(dadosCarregados);
        campoDescricao.setEnabled(dadosCarregados);

        comboCategoria.setEnabled(dadosCarregados);
        comboStatus.setEnabled(dadosCarregados);

        botaoCadastrar.setEnabled(dadosCarregados);
        botaoLimpar.setEnabled(dadosCarregados);
    }

    private void limparFormulario() {
        campoNome.setText("");
        campoDescricao.setText("");
        comboCategoria.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        campoNome.requestFocusInWindow();
    }

    private void cadastrar() {
        if (!dadosCarregados) {
            JOptionPane.showMessageDialog(
                    this,
                    "Cadastro bloqueado: a API não está disponível.",
                    "Dados indisponíveis",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nome = campoNome.getText().trim();
        String descricao = campoDescricao.getText().trim();
        String categoria = (String) comboCategoria.getSelectedItem();
        String status = (String) comboStatus.getSelectedItem();

        if (nome.isBlank()) {
            JOptionPane.showMessageDialog(
                    this, "Informe o nome do projeto.");
            campoNome.requestFocusInWindow();
            return;
        }

        try {
            int proximoId = 1;

            for (Projeto p : apiClient.listarProjetosComoObjetos()) {
                if (p.getId() >= proximoId) {
                    proximoId = p.getId() + 1;
                }
            }

            Projeto projeto = new Projeto(
                    proximoId, nome, descricao, categoria, status);

            apiClient.cadastrarProjeto(projeto);

            JOptionPane.showMessageDialog(
                    this,
                    "Projeto cadastrado e salvo com sucesso!");

            limparFormulario();
            carregarTabela();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível cadastrar o projeto pela API.\n\n" +
                            "Detalhes: " + e.getMessage(),
                    "Erro ao cadastrar",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarTabela() {
        modelo.setRowCount(0);

        try {
            List<Projeto> projetos = apiClient.listarProjetosComoObjetos();

            for (Projeto projeto : projetos) {
                modelo.addRow(new Object[] {
                        projeto.getId(),
                        projeto.getNome(),
                        projeto.getCategoria(),
                        projeto.getStatus()
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível atualizar a tabela pela API.\n\n" +
                            "Detalhes: " + e.getMessage(),
                    "Erro ao carregar projetos",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarProjetoSelecionado() {
        int linha = tabela.getSelectedRow();

        if (linha == -1) {
            idProjetoSelecionado = -1;
            return;
        }

        idProjetoSelecionado = (int) modelo.getValueAt(linha, 0);

        try {
            for (Projeto projeto : apiClient.listarProjetosComoObjetos()) {
                if (projeto.getId() == idProjetoSelecionado) {
                    campoNome.setText(projeto.getNome());
                    campoDescricao.setText(projeto.getDescricao());
                    comboCategoria.setSelectedItem(projeto.getCategoria());
                    comboStatus.setSelectedItem(projeto.getStatus());
                    return;
                }
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Projeto não encontrado na API.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao carregar projeto: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarProjeto() {
        if (!dadosCarregados) {
            return;
        }

        if (idProjetoSelecionado == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecione um projeto na tabela para editar.");
            return;
        }

        String nome = campoNome.getText().trim();
        String descricao = campoDescricao.getText().trim();
        String categoria = (String) comboCategoria.getSelectedItem();
        String status = (String) comboStatus.getSelectedItem();

        if (nome.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Informe o nome do projeto.");
            campoNome.requestFocusInWindow();
            return;
        }

        Projeto projeto = new Projeto(
                idProjetoSelecionado,
                nome,
                descricao,
                categoria,
                status);

        try {
            apiClient.atualizarProjeto(idProjetoSelecionado, projeto);

            JOptionPane.showMessageDialog(
                    this,
                    "Projeto atualizado com sucesso!");

            limparFormulario();
            idProjetoSelecionado = -1;
            tabela.clearSelection();
            carregarTabela();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível atualizar o projeto.\n\n"
                            + e.getMessage(),
                    "Erro ao editar",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


private void excluirProjeto() {
    int linha = tabela.getSelectedRow();

    if (linha == -1) {
        JOptionPane.showMessageDialog(
            this,
            "Selecione um projeto na tabela para excluir."
        );
        return;
    }

    int id = (int) modelo.getValueAt(linha, 0);

    int confirmacao = JOptionPane.showConfirmDialog(
        this,
        "Tem certeza de que deseja excluir o projeto selecionado?",
        "Confirmar exclusão",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE
    );

    if (confirmacao != JOptionPane.YES_OPTION) {
        return;
    }

    try {
        apiClient.excluirProjeto(id);

        JOptionPane.showMessageDialog(
            this,
            "Projeto excluído com sucesso!"
        );

        idProjetoSelecionado = -1;
        limparFormulario();
        carregarTabela();

    } catch (Exception e) {
        JOptionPane.showMessageDialog(
            this,
            "Não foi possível excluir o projeto.\n\n"
                + e.getMessage(),
            "Erro ao excluir",
            JOptionPane.ERROR_MESSAGE
        );
    }

    }

    public static void main(String[] args) {

        TelaProjetos tela = new TelaProjetos();

        tela.setVisible(true);
    }
}

