import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.Collections;

public class PainelMetricas extends JPanel {

    private JTabbedPane tabbedPane;
    private JTextArea areaResumo;
    private JPanel painelGantt;
    private JPanel painelHistogramas;
    private JPanel painelComparativo;

    public PainelMetricas() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Métricas de Desempenho"));

        tabbedPane = new JTabbedPane();

        // Aba 1: Resumo
        areaResumo = new JTextArea("Aguardando simulação...");
        areaResumo.setEditable(false);
        areaResumo.setFont(new Font("Monospaced", Font.PLAIN, 14));
        tabbedPane.addTab("Resumo", new JScrollPane(areaResumo));

        // Aba 2: Gráfico de Gantt
        painelGantt = new JPanel(new BorderLayout());
        tabbedPane.addTab("Gráfico de Gantt", painelGantt);

        // Aba 3: Histogramas
        painelHistogramas = new JPanel();
        painelHistogramas.setLayout(new BoxLayout(painelHistogramas, BoxLayout.Y_AXIS));
        tabbedPane.addTab("Histogramas de Tempo", new JScrollPane(painelHistogramas));

        // Aba 4: Comparativo
        painelComparativo = new JPanel(new BorderLayout());
        tabbedPane.addTab("Comparativo de Métricas", painelComparativo);

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void exibirMetricas(Avaliador avaliador, List<Processo> processos) {
        // 1. Preencher Resumo
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Throughput: %.2f processos/s\n", avaliador.getThroughput()));
        sb.append(String.format("Trocas de Contexto: %d\n", avaliador.getTrocasDeContexto()));
        sb.append(String.format("Utilização da CPU: %.2f %%\n\n", avaliador.getUtilizacaoCPU()));
        sb.append(String.format("Tempo Médio de Retorno: %.2f ms\n", avaliador.getTempoMedioDeRetorno()));
        sb.append(String.format("Tempo Médio de Espera: %.2f ms\n", avaliador.getTempoMedioDeEspera()));
        sb.append(String.format("Tempo Médio de Resposta: %.2f ms\n", avaliador.getTempoMedioDeResposta()));
        areaResumo.setText(sb.toString());

        // 2. Preencher Gráfico de Gantt
        painelGantt.removeAll();
        painelGantt.add(new JScrollPane(new GanttChartPanel(processos)), BorderLayout.CENTER);

        // 3. Preencher Histogramas
        painelHistogramas.removeAll();
        List<Long> temposRetorno = processos.stream().map(Processo::getTempoDeRetorno).collect(Collectors.toList());
        List<Long> temposEspera = processos.stream().map(Processo::getTempoDeEspera).collect(Collectors.toList());
        List<Long> temposResposta = processos.stream().map(Processo::getTempoDeResposta).collect(Collectors.toList());

        painelHistogramas.add(new HistogramPanel(temposRetorno, "Histograma - Tempo de Retorno (ms)"));
        painelHistogramas.add(new HistogramPanel(temposEspera, "Histograma - Tempo de Espera (ms)"));
        painelHistogramas.add(new HistogramPanel(temposResposta, "Histograma - Tempo de Resposta (ms)"));

        // 4. Preencher Gráfico Comparativo
        painelComparativo.removeAll();
        painelComparativo.add(new BarChartPanel(avaliador), BorderLayout.CENTER);


        revalidate();
        repaint();
    }

    public void limpar() {
        areaResumo.setText("Aguardando simulação...");
        painelGantt.removeAll();
        painelHistogramas.removeAll();
        painelComparativo.removeAll();
        revalidate();
        repaint();
    }

    // --- PAINÉIS DE GRÁFICOS CUSTOMIZADOS ---

    /** Gráfico de Gantt (sem alterações, apenas movido para dentro da classe) */
    private static class GanttChartPanel extends JPanel {
        private final List<Processo> processos;
        private long tempoMinimo = -1;
        private long tempoMaximo = -1;

        private final Color[] cores = {
                new Color(110, 178, 255), new Color(111, 219, 146), new Color(255, 178, 110),
                new Color(255, 110, 110), new Color(178, 110, 255), new Color(245, 245, 122)
        };

        public GanttChartPanel(List<Processo> processos) {
            this.processos = new ArrayList<>(processos);
            this.processos.sort(Comparator.comparingInt(Processo::getIdProcesso));
            setBackground(Color.WHITE);
            calcularLimitesDeTempo();
        }

        private void calcularLimitesDeTempo() {
            if (processos.isEmpty()) return;

            tempoMinimo = processos.stream()
                    .mapToLong(Processo::getTempoChegada)
                    .min()
                    .orElse(0);

            tempoMaximo = processos.stream()
                    .filter(p -> p.getEstado() == Processo.Estado.FINALIZADO)
                    .mapToLong(Processo::getTempoFinalizacao)
                    .max()
                    .orElse(tempoMinimo);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (processos.isEmpty() || tempoMaximo <= tempoMinimo) {
                g2d.drawString("Não há dados para exibir.", 20, 30);
                return;
            }

            int paddingEsquerda = 80;
            int paddingDireita = 30;
            int paddingTop = 30;
            int paddingBottom = 40;
            int alturaBarra = 30;
            int espacoEntreBarras = 15;

            int areaDesenhoWidth = getWidth() - paddingEsquerda - paddingDireita;
            long duracaoTotal = tempoMaximo - tempoMinimo;

            int y = paddingTop;
            for (int i = 0; i < processos.size(); i++) {
                Processo p = processos.get(i);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Processo " + p.getIdProcesso(), 10, y + alturaBarra / 2 + 5);

                if (p.getTempoInicioPrimeiraExecucao() != -1) {
                    long inicioRelativo = p.getTempoInicioPrimeiraExecucao() - tempoMinimo;
                    long fimRelativo = p.getTempoFinalizacao() - tempoMinimo;

                    int xBarra = paddingEsquerda + (int) (areaDesenhoWidth * inicioRelativo / duracaoTotal);
                    int larguraBarra = (int) (areaDesenhoWidth * (fimRelativo - inicioRelativo) / duracaoTotal);
                    if (larguraBarra == 0) larguraBarra = 1;

                    g2d.setColor(cores[i % cores.length]);
                    g2d.fillRect(xBarra, y, larguraBarra, alturaBarra);
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.drawRect(xBarra, y, larguraBarra, alturaBarra);
                }
                y += alturaBarra + espacoEntreBarras;
            }
            setPreferredSize(new Dimension(getWidth(), y + paddingBottom));

            int yEixo = y;
            g2d.setColor(Color.BLACK);
            g2d.drawLine(paddingEsquerda, yEixo, getWidth() - paddingDireita, yEixo);

            int numMarcadores = 5;
            for (int i = 0; i <= numMarcadores; i++) {
                long tempoMarcador = tempoMinimo + (i * duracaoTotal / numMarcadores);
                int xMarcador = paddingEsquerda + (int) (i * (double)areaDesenhoWidth / numMarcadores);

                g2d.drawLine(xMarcador, yEixo, xMarcador, yEixo + 5);
                String labelTempo = String.format("%.1fs", (tempoMarcador - tempoMinimo) / 1000.0);
                g2d.drawString(labelTempo, xMarcador - 15, yEixo + 20);
            }
        }
    }

    /** CORRIGIDO: Painel para desenhar Histogramas */
    private static class HistogramPanel extends JPanel {
        private final List<Long> data;
        private final String title;
        private final int BINS = 10;

        public HistogramPanel(List<Long> data, String title) {
            this.data = data;
            this.title = title;
            setPreferredSize(new Dimension(400, 300));
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Validação dos dados
            if (data == null || data.isEmpty() || Collections.min(data).equals(Collections.max(data))) {
                String message = title + " (Dados insuficientes)";
                FontMetrics metrics = g2d.getFontMetrics();
                int x = (getWidth() - metrics.stringWidth(message)) / 2;
                int y = getHeight() / 2;
                g2d.drawString(message, x, y);
                return;
            }

            long min = Collections.min(data);
            long max = Collections.max(data);

            int[] bins = new int[BINS];
            double binSize = (double) (max - min) / BINS;

            for (Long value : data) {
                int binIndex = (int) ((value - min) / binSize);
                if (value.equals(max)) { // O valor máximo deve cair no último bin
                    binIndex = BINS - 1;
                }
                if (binIndex >= 0 && binIndex < BINS) {
                    bins[binIndex]++;
                }
            }

            int maxCount = 0;
            for (int count : bins) {
                if (count > maxCount) maxCount = count;
            }

            int padding = 40;
            int width = getWidth() - 2 * padding;
            int height = getHeight() - 2 * padding;

            // Desenha eixos
            g2d.drawLine(padding, getHeight() - padding, padding, padding);
            g2d.drawLine(padding, getHeight() - padding, getWidth() - padding, getHeight() - padding);

            // Desenha título e rótulos dos eixos
            g2d.drawString(title, padding, padding - 20);
            g2d.drawString("Contagem", padding - 35, padding - 5);
            g2d.drawString(String.valueOf(maxCount), padding - 25, padding + 5);
            g2d.drawString("0", padding - 15, getHeight() - padding + 5);

            // Desenha barras
            double barWidth = (double) width / BINS;
            for (int i = 0; i < BINS; i++) {
                int barHeight = (maxCount == 0) ? 0 : (int) (((double) bins[i] / maxCount) * height);
                int x = (int) (padding + i * barWidth);
                int y = getHeight() - padding - barHeight;
                g2d.setColor(new Color(70, 130, 180));
                g2d.fillRect(x, y, (int) barWidth - 2, barHeight); // -2 para criar um espaçamento
            }
        }
    }

    /** CORRIGIDO: Painel para Gráfico de Barras Comparativo (movido para dentro da classe) */
    private static class BarChartPanel extends JPanel {
        private final Avaliador avaliador;

        public BarChartPanel(Avaliador avaliador) {
            this.avaliador = avaliador;
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double[] values = {
                    avaliador.getTempoMedioDeRetorno(),
                    avaliador.getTempoMedioDeEspera(),
                    avaliador.getTempoMedioDeResposta()
            };
            String[] labels = {"Retorno", "Espera", "Resposta"};
            Color[] colors = {new Color(255, 99, 132), new Color(54, 162, 235), new Color(255, 206, 86)};

            double maxValue = 0;
            for(double v : values) {
                if (v > maxValue) maxValue = v;
            }

            if (maxValue == 0) {
                g2d.drawString("Não há dados para exibir.", 20, 30);
                return;
            }

            int padding = 40;
            int width = getWidth() - 2 * padding;
            int height = getHeight() - 2 * padding;
            int barWidth = width / values.length / 2;

            for (int i = 0; i < values.length; i++) {
                int barHeight = (int) ((values[i] / maxValue) * height);
                int x = padding + (width / (values.length * 2)) * (i * 2 + 1);
                int y = getHeight() - padding - barHeight;

                g2d.setColor(colors[i]);
                g2d.fillRect(x - barWidth/2, y, barWidth, barHeight);
                g2d.setColor(Color.BLACK);
                FontMetrics metrics = g2d.getFontMetrics();
                int labelWidth = metrics.stringWidth(labels[i]);
                g2d.drawString(labels[i], x - labelWidth/2, getHeight() - padding + 15);
                String valueString = String.format("%.0f ms", values[i]);
                int valueWidth = metrics.stringWidth(valueString);
                g2d.drawString(valueString, x - valueWidth/2, y - 5);
            }
        }
    }
}