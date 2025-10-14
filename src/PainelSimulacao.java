import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class PainelSimulacao extends JPanel {

    private JPanel painelProcessos;
    private JTextArea logArea;
    private Map<Integer, JProgressBar> barrasDeProgresso;
    private Map<Integer, JLabel> labelsProcessos;
    private Map<Integer, Color> coresProcessos; // ← nova estrutura para guardar cores únicas

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
        coresProcessos = new HashMap<>();
    }

    /**
     * Função do método: Prepara o painel para uma nova simulação com a lista de processos.
     * Entrada: Lista de processos
     * Saída: Nenhuma (atualiza interface)
     */
    public void prepararParaSimulacao(List<Processo> processos) {
        painelProcessos.removeAll();
        barrasDeProgresso.clear();
        labelsProcessos.clear();
        coresProcessos.clear();
        logArea.setText("");

        gerarCoresUnicas(processos);

        for (Processo p : processos) {
            JPanel painelProcesso = new JPanel(new BorderLayout(5, 5));

            JLabel label = new JLabel(String.format("P%d (Prioridade: %d)", p.getIdProcesso(), p.getPrioridade()));
            label.setForeground(Color.BLACK);

            JProgressBar progressBar = new JProgressBar(0, p.getTempoExecucao());
            progressBar.setStringPainted(true);

            // aplica a cor única de fundo
            Color cor = coresProcessos.get(p.getIdProcesso());
            progressBar.setForeground(cor);
            progressBar.setBackground(new Color(240, 240, 240)); // cor de fundo neutra

            painelProcesso.add(label, BorderLayout.WEST);
            painelProcesso.add(progressBar, BorderLayout.CENTER);

            painelProcessos.add(painelProcesso);
            barrasDeProgresso.put(p.getIdProcesso(), progressBar);
            labelsProcessos.put(p.getIdProcesso(), label);
        }
        revalidate();
        repaint();
    }

    /** Gera uma cor única, suave e fixa para cada processo **/
    /**
     * Função do método: Gera cores únicas para cada processo.
     * Entrada: Lista de processos
     * Saída: Nenhuma (atualiza estrutura de cores)
     */
    private void gerarCoresUnicas(List<Processo> processos) {
        Random rand = new Random();
        for (Processo p : processos) {
            float hue = rand.nextFloat();                      // tonalidade aleatória
            float saturation = 0.5f + rand.nextFloat() * 0.4f; // saturação média
            float brightness = 0.8f + rand.nextFloat() * 0.2f; // brilho alto
            Color cor = Color.getHSBColor(hue, saturation, brightness);
            coresProcessos.put(p.getIdProcesso(), cor);
        }
    }

    /**
     * Função do método: Atualiza a interface do processo informado.
     * Entrada: Processo
     * Saída: Nenhuma (atualiza interface)
     */
    public void atualizarProcesso(Processo p) {
        if (p == null) return;

        // Reseta todas as labels para preto
        labelsProcessos.values().forEach(label -> label.setForeground(Color.BLACK));

        // Destaca o processo em execução
        if (p.getEstado() == Processo.Estado.EXECUCAO) {
            JLabel label = labelsProcessos.get(p.getIdProcesso());
            if (label != null) {
                label.setForeground(Color.RED);
            }
        }

        // Atualiza barra de progresso
        JProgressBar progressBar = barrasDeProgresso.get(p.getIdProcesso());
        if (progressBar != null) {
            progressBar.setValue(p.getTempoExecutado());
            progressBar.setString(String.format("%d / %d ms", p.getTempoExecutado(), p.getTempoExecucao()));

            // Mantém a cor única associada ao processo
            Color cor = coresProcessos.get(p.getIdProcesso());
            if (cor != null) {
                progressBar.setForeground(cor);
            }
        }
    }

    /**
     * Função do método: Adiciona uma mensagem ao log de execução.
     * Entrada: Mensagem (String)
     * Saída: Nenhuma (atualiza log)
     */
    public void adicionarLog(String mensagem) {
        logArea.append(mensagem + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
