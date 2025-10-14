import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public class PainelMetricas extends JPanel {

    private JTextArea areaMetricas;
    private JPanel painelGrafico;
    private GanttChartPanel ganttChartPanel;

    public PainelMetricas() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new TitledBorder("Métricas de Desempenho"));

        areaMetricas = new JTextArea();
        areaMetricas.setEditable(false);
        areaMetricas.setFont(new Font("Monospaced", Font.PLAIN, 14));
        areaMetricas.setBorder(new TitledBorder("Resumo"));
        areaMetricas.setPreferredSize(new Dimension(300, 150));

        // O painel do gráfico agora conterá nosso componente customizado
        painelGrafico = new JPanel(new BorderLayout());
        painelGrafico.setBorder(new TitledBorder("Timeline de Execução (Gráfico de Gantt)"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, areaMetricas, painelGrafico);
        splitPane.setResizeWeight(0.2);

        add(splitPane, BorderLayout.CENTER);
    }

    public void exibirMetricas(Avaliador avaliador, List<Processo> processos) {
        // Exibição das métricas textuais (sem alteração)
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Throughput: %.2f processos/s\n", avaliador.getThroughput()));
        sb.append(String.format("Trocas de Contexto: %d\n", avaliador.getTrocasDeContexto()));
        sb.append(String.format("Tempo Médio de Retorno: %.2f ms\n", avaliador.getTempoMedioDeRetorno()));
        sb.append(String.format("Tempo Médio de Espera: %.2f ms\n", avaliador.getTempoMedioDeEspera()));
        sb.append(String.format("Tempo Médio de Resposta: %.2f ms\n", avaliador.getTempoMedioDeResposta()));

        areaMetricas.setText(sb.toString());

        // Criar e exibir o novo Gráfico de Gantt
        ganttChartPanel = new GanttChartPanel(processos);
        painelGrafico.removeAll();
        painelGrafico.add(new JScrollPane(ganttChartPanel), BorderLayout.CENTER);
        painelGrafico.revalidate();
        painelGrafico.repaint();
    }

    public void limpar() {
        areaMetricas.setText("Aguardando simulação...");
        painelGrafico.removeAll();
        painelGrafico.revalidate();
        painelGrafico.repaint();
    }

    /**
     * Componente customizado que desenha um Gráfico de Gantt.
     */
    private static class GanttChartPanel extends JPanel {
        private final List<Processo> processos;
        private long tempoMinimo = -1;
        private long tempoMaximo = -1;

        // Paleta de cores para as barras
        private final Color[] cores = {
                new Color(110, 178, 255), new Color(111, 219, 146), new Color(255, 178, 110),
                new Color(255, 110, 110), new Color(178, 110, 255), new Color(245, 245, 122)
        };

        public GanttChartPanel(List<Processo> processos) {
            this.processos = new ArrayList<>(processos);
            // Ordena os processos para exibição consistente no gráfico
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

            // Definições de layout do gráfico
            int paddingEsquerda = 80;
            int paddingDireita = 30;
            int paddingTop = 30;
            int paddingBottom = 40;
            int alturaBarra = 30;
            int espacoEntreBarras = 15;

            int areaDesenhoWidth = getWidth() - paddingEsquerda - paddingDireita;
            long duracaoTotal = tempoMaximo - tempoMinimo;

            // Desenha as barras dos processos
            int y = paddingTop;
            for (int i = 0; i < processos.size(); i++) {
                Processo p = processos.get(i);

                // Desenha o rótulo do processo
                g2d.setColor(Color.BLACK);
                g2d.drawString("Processo " + p.getIdProcesso(), 10, y + alturaBarra / 2 + 5);

                if (p.getTempoInicioPrimeiraExecucao() != -1) {
                    long inicioRelativo = p.getTempoInicioPrimeiraExecucao() - tempoMinimo;
                    long fimRelativo = p.getTempoFinalizacao() - tempoMinimo;

                    int xBarra = paddingEsquerda + (int) (areaDesenhoWidth * inicioRelativo / duracaoTotal);
                    int larguraBarra = (int) (areaDesenhoWidth * (fimRelativo - inicioRelativo) / duracaoTotal);
                    if (larguraBarra == 0) larguraBarra = 1; // Garante visibilidade mínima

                    // Desenha a barra
                    g2d.setColor(cores[i % cores.length]);
                    g2d.fillRect(xBarra, y, larguraBarra, alturaBarra);
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.drawRect(xBarra, y, larguraBarra, alturaBarra);
                }

                y += alturaBarra + espacoEntreBarras;
            }

            // Define a altura total do painel para que o JScrollPane funcione
            setPreferredSize(new Dimension(getWidth(), y + paddingBottom));

            // Desenha o eixo do tempo
            int yEixo = y;
            g2d.setColor(Color.BLACK);
            g2d.drawLine(paddingEsquerda, yEixo, getWidth() - paddingDireita, yEixo);

            // Adiciona marcadores de tempo ao eixo
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
}