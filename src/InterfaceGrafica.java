import javax.swing.*;
import java.awt.*;
import java.util.List;

public class InterfaceGrafica extends JFrame {
    private PainelSimulacao painelSimulacao;
    private PainelMetricas painelMetricas;
    private JComboBox<Escalonador.Algoritmo> comboAlgoritmo;
    private JSpinner spinnerQuantum;
    private JButton btnIniciar;
    private JButton btnLimparHistorico;

    public InterfaceGrafica() {
        setTitle("Simulador de Escalonador de Processos");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Painel de Controle
        JPanel painelControle = new JPanel(new FlowLayout(FlowLayout.LEFT));

        painelControle.add(new JLabel("Algoritmo:"));
        comboAlgoritmo = new JComboBox<>(Escalonador.Algoritmo.values());
        painelControle.add(comboAlgoritmo);

        painelControle.add(new JLabel("Quantum (ms):"));
        spinnerQuantum = new JSpinner(new SpinnerNumberModel(1000, 100, 5000, 100));
        painelControle.add(spinnerQuantum);

        btnIniciar = new JButton("Iniciar Simulação");
        painelControle.add(btnIniciar);

        btnLimparHistorico = new JButton("Limpar Histórico");
        painelControle.add(btnLimparHistorico);

        // Painéis Principais
        painelSimulacao = new PainelSimulacao();
        painelMetricas = new PainelMetricas();

        JSplitPane splitPanePrincipal = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                painelSimulacao,
                painelMetricas
        );
        splitPanePrincipal.setResizeWeight(0.6);

        add(painelControle, BorderLayout.NORTH);
        add(splitPanePrincipal, BorderLayout.CENTER);

        // Eventos
        btnIniciar.addActionListener(e -> iniciarSimulacao());
        btnLimparHistorico.addActionListener(e -> painelMetricas.limparHistorico());
    }

    private void iniciarSimulacao() {
        btnIniciar.setEnabled(false);
        comboAlgoritmo.setEnabled(false);
        spinnerQuantum.setEnabled(false);

        List<Processo> cargaDeTrabalho = Avaliador.gerarCargaDeTrabalho(5);
        painelSimulacao.prepararParaSimulacao(cargaDeTrabalho);

        Escalonador.Algoritmo algoritmo = (Escalonador.Algoritmo) comboAlgoritmo.getSelectedItem();
        int quantum = (int) spinnerQuantum.getValue();

        // Nome do cenário para identificação
        String nomeCenario = algoritmo.toString() + " (Q=" + quantum + "ms)";

        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                Escalonador.EscalonadorCallback callback = new Escalonador.EscalonadorCallback() {
                    @Override
                    public void onLog(String message) {
                        SwingUtilities.invokeLater(() -> painelSimulacao.adicionarLog(message));
                    }

                    @Override
                    public void onProcessoIniciado(Processo p) {
                        SwingUtilities.invokeLater(() -> painelSimulacao.atualizarProcesso(p));
                    }

                    @Override
                    public void onProcessoFinalizado(Processo p) {
                        SwingUtilities.invokeLater(() -> painelSimulacao.atualizarProcesso(p));
                    }

                    @Override
                    public void onConcluido() {
                        // Implementação vazia
                    }
                };

                Escalonador escalonador = new Escalonador(algoritmo, quantum, cargaDeTrabalho, callback);
                // ou: new Escalonador(algoritmo, quantum, cargaDeTrabalho, callback, 8)  // overhead 8ms

                escalonador.escalonar();

                Avaliador avaliador = new Avaliador(
                        escalonador.getProcessos(),
                        escalonador.getTempoTotal(),           // wall time total
                        escalonador.getTrocasContexto(),
                        escalonador.getTempoOverheadTotalMs(),
                        escalonador.getCpuTotalNs()            // CPU real medida
                );

                SwingUtilities.invokeLater(() ->
                        painelMetricas.exibirMetricas(avaliador, escalonador.getProcessos(), nomeCenario)
                );


                return null;
            }

            @Override
            protected void done() {
                btnIniciar.setEnabled(true);
                comboAlgoritmo.setEnabled(true);
                spinnerQuantum.setEnabled(true);
                painelSimulacao.adicionarLog("\n✓ SIMULAÇÃO CONCLUÍDA");
                painelSimulacao.adicionarLog("Execute outro cenário para comparação\n");
            }
        };

        worker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InterfaceGrafica().setVisible(true));
    }
}