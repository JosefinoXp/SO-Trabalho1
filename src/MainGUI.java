import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainGUI extends JFrame {
    private JComboBox<String> comboAlgoritmo;
    private JTextField campoQuantum, campoId, campoPrioridade, campoTempo;
    private JTextArea areaLog;
    private Escalonador escalonador;
    private JButton botaoAdd, botaoExecutar;

    public MainGUI() {
        setTitle("Simulador de Escalonamento de Processos");
        setSize(700, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel painelPrincipal = new JPanel();
        painelPrincipal.setLayout(new BoxLayout(painelPrincipal, BoxLayout.Y_AXIS));
        painelPrincipal.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(painelPrincipal);

        // Painel de configuração
        JPanel painelConfig = new JPanel(new GridLayout(2, 2, 10, 10));
        painelConfig.setBorder(BorderFactory.createTitledBorder("Configuração"));
        comboAlgoritmo = new JComboBox<>(new String[]{"PRIORIDADE", "ROUND_ROBIN"});
        campoQuantum = new JTextField("1000");
        painelConfig.add(new JLabel("Algoritmo:"));
        painelConfig.add(comboAlgoritmo);
        painelConfig.add(new JLabel("Quantum (ms):"));
        painelConfig.add(campoQuantum);
        painelPrincipal.add(painelConfig);

        // Painel de adição de processos
        JPanel painelProcesso = new JPanel(new GridLayout(2, 4, 10, 10));
        painelProcesso.setBorder(BorderFactory.createTitledBorder("Adicionar Processo"));
        campoId = new JTextField();
        campoPrioridade = new JTextField();
        campoTempo = new JTextField();
        botaoAdd = new JButton("Adicionar Processo");

        painelProcesso.add(new JLabel("ID:"));
        painelProcesso.add(campoId);
        painelProcesso.add(new JLabel("Prioridade:"));
        painelProcesso.add(campoPrioridade);
        painelProcesso.add(new JLabel("Tempo Execução (ms):"));
        painelProcesso.add(campoTempo);
        painelProcesso.add(new JLabel());
        painelProcesso.add(botaoAdd);
        painelPrincipal.add(painelProcesso);

        // Painel de execução e log
        JPanel painelInferior = new JPanel(new BorderLayout());
        painelInferior.setBorder(BorderFactory.createTitledBorder("Execução e Log"));
        botaoExecutar = new JButton("Iniciar Simulação");
        areaLog = new JTextArea();
        areaLog.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaLog);
        painelInferior.add(botaoExecutar, BorderLayout.NORTH);
        painelInferior.add(scroll, BorderLayout.CENTER);
        painelPrincipal.add(painelInferior);

        // Ações
        botaoAdd.addActionListener(e -> adicionarProcesso());
        botaoExecutar.addActionListener(e -> iniciarSimulacao());
    }

    private void adicionarProcesso() {
        try {
            int id = Integer.parseInt(campoId.getText());
            int prioridade = Integer.parseInt(campoPrioridade.getText());
            int tempo = Integer.parseInt(campoTempo.getText());

            if (id < 0 || prioridade < 0 || tempo <= 0) {
                JOptionPane.showMessageDialog(this, "Os valores devem ser positivos!");
                return;
            }

            if (escalonador == null)
                inicializarEscalonador();

            escalonador.adicionarProcesso(new Processo(id, prioridade, tempo));
            areaLog.append(String.format("✔ Processo adicionado: P%d (prioridade %d, %d ms)\n", id, prioridade, tempo));

            campoId.setText("");
            campoPrioridade.setText("");
            campoTempo.setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos com números válidos.");
        }
    }

    private void inicializarEscalonador() {
        String algoritmoSelecionado = (String) comboAlgoritmo.getSelectedItem();
        Escalonador.Algoritmo algoritmo = algoritmoSelecionado.equals("PRIORIDADE")
                ? Escalonador.Algoritmo.PRIORIDADE
                : Escalonador.Algoritmo.ROUND_ROBIN;

        int quantum;
        try {
            quantum = Integer.parseInt(campoQuantum.getText());
            if (quantum <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantum deve ser um número positivo!");
            return;
        }

        escalonador = new Escalonador(algoritmo, quantum);
    }

    private void iniciarSimulacao() {
        if (escalonador == null || escalonador.listaVazia()) {
            JOptionPane.showMessageDialog(this, "Adicione pelo menos um processo antes de iniciar!");
            return;
        }

        bloquearCampos(true);
        areaLog.append("\n=== 🧩 Iniciando simulação ===\n");

        Thread simThread = new Thread(() -> {
            escalonador.setOutputHandler(text ->
                    SwingUtilities.invokeLater(() -> areaLog.append(text + "\n")));

            escalonador.escalonar();

            SwingUtilities.invokeLater(() -> {
                areaLog.append("\n✅ Simulação finalizada!\n");
                bloquearCampos(false);
            });
        });
        simThread.start();
    }

    private void bloquearCampos(boolean bloqueado) {
        comboAlgoritmo.setEnabled(!bloqueado);
        campoQuantum.setEnabled(!bloqueado);
        campoId.setEnabled(!bloqueado);
        campoPrioridade.setEnabled(!bloqueado);
        campoTempo.setEnabled(!bloqueado);
        botaoAdd.setEnabled(!bloqueado);
        botaoExecutar.setEnabled(!bloqueado);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}
