
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
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().setBackground(new Color(244, 240, 232));

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
        iniciarAtualizacaoAutomatica();

    }

    private void iniciarAtualizacaoAutomatica() {
        javax.swing.Timer timer = new javax.swing.Timer(3000, e -> {
            if (campoNome.getText().isBlank()
                    && campoDescricao.getText().isBlank()
                    && tabela.getSelectedRow() == -1) {
                carregarTabela();
            }
        });

        timer.start();
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

        Color fundoCampo = new Color(255, 253, 249);
        Color corTexto = new Color(53, 54, 47);
        Color corBorda = new Color(233, 227, 217);

        JTextField[] camposTexto = { campoNome, campoDescricao };

        for (JTextField campo : camposTexto) {
            campo.setBackground(fundoCampo);
            campo.setForeground(corTexto);
            campo.setCaretColor(corTexto);
            campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(corBorda),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            campo.setFont(new Font("Arial", Font.PLAIN, 14));
        }

        JComboBox<?>[] combos = { comboCategoria, comboStatus };

        for (JComboBox<?> combo : combos) {
            combo.setBackground(fundoCampo);
            combo.setForeground(corTexto);
            combo.setFont(new Font("Arial", Font.PLAIN, 14));
        }

        botaoCadastrar = new JButton("Cadastrar");
        botaoEditar = new JButton("Editar");
        botaoExcluir = new JButton("Excluir");
        botaoLimpar = new JButton("Limpar");

        botaoCadastrar.setBackground(new Color(184, 111, 82));
        botaoCadastrar.setForeground(Color.WHITE);

        botaoEditar.setBackground(new Color(126, 146, 152));
        botaoEditar.setForeground(Color.WHITE);

        botaoExcluir.setBackground(new Color(184, 92, 80));
        botaoExcluir.setForeground(Color.WHITE);

        botaoLimpar.setBackground(new Color(137, 135, 126));
        botaoLimpar.setForeground(Color.WHITE);

        for (JButton botao : new JButton[] {
                botaoCadastrar, botaoEditar, botaoExcluir, botaoLimpar
        }) {
            botao.setFocusPainted(false);
            botao.setOpaque(true);
            botao.setBorderPainted(false);
            botao.setMargin(new Insets(10, 16, 10, 16));
        }

        modelo = new DefaultTableModel(
                new String[] { "ID", "Nome", "Categoria", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modelo);

        JPanel painelFormulario = new JPanel(
                new BorderLayout(0, 14));

        painelFormulario.setBackground(new Color(255, 253, 249));
        painelFormulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(233, 227, 217)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));

        JLabel tituloFormulario = new JLabel("Dados do projeto");
        tituloFormulario.setFont(new Font("Arial", Font.BOLD, 18));
        tituloFormulario.setForeground(new Color(53, 54, 47));

        JPanel painelCampos = new JPanel(new GridLayout(2, 4, 12, 8));
        painelCampos.setBackground(new Color(255, 253, 249));

        JLabel[] rotulos = {
                new JLabel("Nome:"),
                new JLabel("Descrição:"),
                new JLabel("Categoria:"),
                new JLabel("Status:")
        };

        for (JLabel rotulo : rotulos) {
            rotulo.setForeground(new Color(53, 54, 47));
            rotulo.setFont(new Font("Arial", Font.BOLD, 12));
            painelCampos.add(rotulo);
        }

        painelCampos.add(campoNome);
        painelCampos.add(campoDescricao);
        painelCampos.add(comboCategoria);
        painelCampos.add(comboStatus);

        JPanel painelBotoes = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 0));
        painelBotoes.setBackground(new Color(255, 253, 249));

        painelBotoes.add(botaoCadastrar);
        painelBotoes.add(botaoEditar);
        painelBotoes.add(botaoExcluir);
        painelBotoes.add(botaoLimpar);

        painelFormulario.add(tituloFormulario, BorderLayout.NORTH);
        painelFormulario.add(painelCampos, BorderLayout.CENTER);
        painelFormulario.add(painelBotoes, BorderLayout.SOUTH);

        getContentPane().setBackground(new Color(244, 240, 232));

        setLayout(new BorderLayout(0, 12));

        JPanel painelTabela = new JPanel(new BorderLayout());
        painelTabela.setBackground(new Color(255, 253, 249));
        painelTabela.setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12));

        tabela.setBackground(new Color(255, 253, 249));
        tabela.setForeground(new Color(53, 54, 47));
        tabela.setRowHeight(28);
        tabela.setGridColor(new Color(233, 227, 217));
        tabela.setSelectionBackground(new Color(234, 213, 138));
        tabela.setSelectionForeground(new Color(53, 54, 47));

        tabela.getTableHeader().setBackground(new Color(248, 245, 239));
        tabela.getTableHeader().setForeground(new Color(53, 54, 47));

        JScrollPane rolagem = new JScrollPane(tabela);
        rolagem.getViewport().setBackground(new Color(255, 253, 249));
        rolagem.setBorder(BorderFactory.createEmptyBorder());

        painelTabela.add(rolagem, BorderLayout.CENTER);

        add(painelFormulario, BorderLayout.NORTH);
        add(painelTabela, BorderLayout.CENTER);
    }

    private void criarEventos() {
        botaoLimpar.addActionListener(
                e -> limparFormulario());

        botaoCadastrar.addActionListener(
                e -> cadastrar());

        botaoEditar.addActionListener(
                e -> editarProjeto());

        botaoExcluir.addActionListener(
                e -> excluirProjeto());

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
        tabela.clearSelection();
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
                    "Selecione um projeto na tabela para excluir.");
            return;
        }

        int id = (int) modelo.getValueAt(linha, 0);

        int confirmacao = JOptionPane.showConfirmDialog(
                this,
                "Tem certeza de que deseja excluir o projeto selecionado?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacao != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            apiClient.excluirProjeto(id);

            JOptionPane.showMessageDialog(
                    this,
                    "Projeto excluído com sucesso!");

            idProjetoSelecionado = -1;
            limparFormulario();
            carregarTabela();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Não foi possível excluir o projeto.\n\n"
                            + e.getMessage(),
                    "Erro ao excluir",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    public static void main(String[] args) {

        TelaProjetos tela = new TelaProjetos();

        tela.setVisible(true);
    }
}
