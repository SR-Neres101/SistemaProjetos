
package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import model.Projeto;
import service.ProjetoService;

public class TelaProjetos extends JFrame {

    private ProjetoService service;

    private JTextField campoNome;
    private JTextField campoDescricao;

    private JComboBox<String> comboCategoria;
    private JComboBox<String> comboStatus;

    private JTable tabela;
    private DefaultTableModel modelo;

    private JButton botaoCadastrar;
    private JButton botaoLimpar;

    public TelaProjetos() {
        setTitle("Sistema de Projetos");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        service = new ProjetoService();

        try {
            service.carregar();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Erro ao carregar projetos: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
        }

        criarComponentes();
        criarEventos();
        carregarTabela();
    }

    private void criarComponentes() {
        campoNome = new JTextField(20);
        campoDescricao = new JTextField(20);

        comboCategoria = new JComboBox<>(new String[]{
            "Web", "Software", "Mobile", "Outro"
        });

        comboStatus = new JComboBox<>(new String[]{
            "Planejado",
            "Em desenvolvimento",
            "Concluído"
        });

        botaoCadastrar = new JButton("Cadastrar");
        botaoLimpar = new JButton("Limpar");

        modelo = new DefaultTableModel(
            new String[]{"ID", "Nome", "Categoria", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modelo);

        JPanel painelFormulario = new JPanel(
            new GridLayout(0, 2, 8, 8)
        );

        painelFormulario.setBorder(
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );

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

        setLayout(new BorderLayout());

        add(painelFormulario, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
    }

    private void criarEventos() {
        botaoLimpar.addActionListener(
            e -> limparFormulario()
        );

        botaoCadastrar.addActionListener(
            e -> cadastrar()
        );
    }

    private void limparFormulario() {
        campoNome.setText("");
        campoDescricao.setText("");
        comboCategoria.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        campoNome.requestFocusInWindow();
    }

    private void cadastrar() {
        String nome = campoNome.getText().trim();
        String descricao = campoDescricao.getText().trim();
        String categoria =
            (String) comboCategoria.getSelectedItem();
        String status =
            (String) comboStatus.getSelectedItem();

        if (nome.isBlank()) {
            JOptionPane.showMessageDialog(
                this, "Informe o nome do projeto."
            );
            campoNome.requestFocusInWindow();
            return;
        }

        int proximoId = 1;

        for (Projeto p : service.listar()) {
            if (p.getId() >= proximoId) {
                proximoId = p.getId() + 1;
            }
        }

        Projeto projeto = new Projeto(
            proximoId, nome, descricao, categoria, status
        );

        if (!service.adicionar(projeto)) {
            JOptionPane.showMessageDialog(
                this, "Não foi possível cadastrar o projeto."
            );
            return;
        }

        try {
            service.salvar();

            JOptionPane.showMessageDialog(
                this, "Projeto cadastrado com sucesso!"
            );

            limparFormulario();
            carregarTabela();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Erro ao salvar o projeto: " + e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void carregarTabela() {
        modelo.setRowCount(0);

        for (Projeto projeto : service.listar()) {
            modelo.addRow(new Object[]{
                projeto.getId(),
                projeto.getNome(),
                projeto.getCategoria(),
                projeto.getStatus()
            });
        }
    }
    public static void main(String[] args) {

        TelaProjetos tela = new TelaProjetos();

        tela.setVisible(true);
    }
}

