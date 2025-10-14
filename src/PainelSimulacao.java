import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PainelSimulacao extends JPanel {

    private JPanel painelProcessos;
    private JTextArea logArea;
    private Map<Integer, JProgressBar> barrasDeProgresso;
    private Map<Integer, JLabel> labelsProcessos;

    public PainelSimulacao() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new TitledBorder("Visualizador de Escalonamento"));

        painelProcessos = new JPanel();
        painelProcessos.setLayout(new BoxLayout(painelProcessos, BoxLayout.Y_AXIS));
        JScrollPane scrollProcessos = new JScrollPane(painelProcessos);
        scrollProcessos.setPreferredSize(new Dimension(300, 200));


        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setBorder(new TitledBorder("Log de Execução"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollProcessos, scrollLog);
        splitPane.setResizeWeight(0.4);

        add(splitPane, BorderLayout.CENTER);

        barrasDeProgresso = new HashMap<>();
        labelsProcessos = new HashMap<>();
    }

    public void prepararParaSimulacao(List<Processo> processos) {
        painelProcessos.removeAll();
        barrasDeProgresso.clear();
        labelsProcessos.clear();
        logArea.setText("");

        for (Processo p : processos) {
            JPanel painelProcesso = new JPanel(new BorderLayout(5, 5));
            JLabel label = new JLabel(String.format("P%d (Prioridade: %d)", p.getIdProcesso(), p.getPrioridade()));
            JProgressBar progressBar = new JProgressBar(0, p.getTempoExecucao());
            progressBar.setStringPainted(true);

            painelProcesso.add(label, BorderLayout.WEST);
            painelProcesso.add(progressBar, BorderLayout.CENTER);

            painelProcessos.add(painelProcesso);
            barrasDeProgresso.put(p.getIdProcesso(), progressBar);
            labelsProcessos.put(p.getIdProcesso(), label);
        }
        revalidate();
        repaint();
    }

    public void atualizarProcesso(Processo p) {
        if (p == null) return;

        // Reseta a cor de todos os labels
        labelsProcessos.values().forEach(label -> label.setForeground(Color.BLACK));

        // Destaca o processo em execução
        if (p.getEstado() == Processo.Estado.EXECUCAO) {
            JLabel label = labelsProcessos.get(p.getIdProcesso());
            if (label != null) {
                label.setForeground(Color.RED);
            }
        }

        JProgressBar progressBar = barrasDeProgresso.get(p.getIdProcesso());
        if (progressBar != null) {
            progressBar.setValue(p.getTempoExecutado());
            progressBar.setString(String.format("%d / %d ms", p.getTempoExecutado(), p.getTempoExecucao()));
        }
    }

    public void adicionarLog(String mensagem) {
        logArea.append(mensagem + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}