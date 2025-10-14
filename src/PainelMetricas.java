import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;

public class PainelMetricas extends JPanel {
    private JTabbedPane tabbedPane;
    private JTextArea areaResumo;
    private JPanel painelGantt;
    private JPanel painelGraficosDeBarra;
    private JPanel painelComparativo;

    // Armazena resultados de múltiplas simulações para comparação
    // AQUI ESTAVA O ERRO -> Corrigido para "ResultadoSimulacao"
    private Map<String, ResultadoSimulacao> resultadosAcumulados = new HashMap<>();

    public PainelMetricas() {
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Métricas de Desempenho"));

        tabbedPane = new JTabbedPane();

        areaResumo = new JTextArea("Aguardando simulação...");
        areaResumo.setEditable(false);
        areaResumo.setFont(new Font("Monospaced", Font.PLAIN, 14));
        tabbedPane.addTab("Resumo", new JScrollPane(areaResumo));

        painelGantt = new JPanel(new BorderLayout());
        tabbedPane.addTab("Timeline/Gantt", painelGantt);

        painelGraficosDeBarra = new JPanel();
        painelGraficosDeBarra.setLayout(new BoxLayout(painelGraficosDeBarra, BoxLayout.Y_AXIS));
        tabbedPane.addTab("Gráficos de Tempo", new JScrollPane(painelGraficosDeBarra));

        painelComparativo = new JPanel(new BorderLayout());
        tabbedPane.addTab("Comparativo de Cenários", painelComparativo);

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void exibirMetricas(Avaliador avaliador, List<Processo> processos, String cenario) {
        // Armazena resultado do cenário atual (CORRIGIDO)
        resultadosAcumulados.put(cenario, new ResultadoSimulacao(avaliador, processos));

        // 1. Resumo
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== CENÁRIO: %s ===\n\n", cenario));
        sb.append("--- MÉTRICAS GERAIS ---\n");
        sb.append(String.format("Throughput.........: %.2f processos/s\n", avaliador.getThroughput()));
        sb.append(String.format("Trocas de Contexto...: %d\n", avaliador.getTrocasDeContexto()));
        sb.append(String.format("Utilização da CPU....: %.2f %%\n\n", avaliador.getUtilizacaoCPU()));

        sb.append("--- MÉTRICAS DE TEMPO (MÉDIAS) ---\n");
        double retornoMedioMs = avaliador.getTempoMedioDeRetorno();
        sb.append(String.format("Tempo Médio de Retorno.: %-8.2f ms (%-6.2fs)\n",
                retornoMedioMs, retornoMedioMs / 1000.0));
        double esperaMediaMs = avaliador.getTempoMedioDeEspera();
        sb.append(String.format("Tempo Médio de Espera..: %-8.2f ms (%-6.2fs)\n",
                esperaMediaMs, esperaMediaMs / 1000.0));
        double respostaMediaMs = avaliador.getTempoMedioDeResposta();
        sb.append(String.format("Tempo Médio de Resposta: %-8.2f ms (%-6.2fs)\n",
                respostaMediaMs, respostaMediaMs / 1000.0));

        sb.append("\n\n--- MÉTRICAS INDIVIDUAIS POR PROCESSO ---\n");
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("%-5s | %-25s | %-25s | %-25s\n", "ID", "T. Retorno", "T. Espera", "T. Resposta"));
        sb.append("--------------------------------------------------------------------------------\n");

        processos.sort(Comparator.comparingInt(Processo::getIdProcesso));

        for (Processo p : processos) {
            long retornoMs = p.getTempoDeRetorno();
            double retornoS = retornoMs / 1000.0;
            long esperaMs = p.getTempoDeEspera();
            double esperaS = esperaMs / 1000.0;
            long respostaMs = p.getTempoDeResposta();
            double respostaS = respostaMs / 1000.0;

            String linha = String.format("P%-4d| %-8d ms (%-6.2fs) | %-8d ms (%-6.2fs) | %-8d ms (%-6.2fs)\n",
                    p.getIdProcesso(),
                    retornoMs, retornoS,
                    esperaMs, esperaS,
                    respostaMs, respostaS);
            sb.append(linha);
        }
        sb.append("--------------------------------------------------------------------------------\n");

        areaResumo.setText(sb.toString());
        areaResumo.setCaretPosition(0);

        // 2. Gantt
        painelGantt.removeAll();
        painelGantt.add(new JScrollPane(new GanttChartPanel(processos)), BorderLayout.CENTER);

        // 3. Gráficos de Barra
        painelGraficosDeBarra.removeAll();
        painelGraficosDeBarra.add(new GraficoBarrasPanel(processos, "Gráfico - Tempo de Retorno", Processo::getTempoDeRetorno));
        painelGraficosDeBarra.add(new GraficoBarrasPanel(processos, "Gráfico - Tempo de Espera", Processo::getTempoDeEspera));
        painelGraficosDeBarra.add(new GraficoBarrasPanel(processos, "Gráfico - Tempo de Resposta", Processo::getTempoDeResposta));

        // 4. Comparativo entre cenários
        painelComparativo.removeAll();
        if (resultadosAcumulados.size() > 1) {
            painelComparativo.add(new ComparativoCenariosPanel(resultadosAcumulados), BorderLayout.CENTER);
        } else {
            JLabel lblAguardando = new JLabel("Execute múltiplos cenários para comparação", SwingConstants.CENTER);
            painelComparativo.add(lblAguardando, BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    public void limpar() {
        areaResumo.setText("Aguardando simulação...");
        painelGantt.removeAll();
        painelGraficosDeBarra.removeAll();
        painelComparativo.removeAll();
        revalidate();
        repaint();
    }

    public void limparHistorico() {
        resultadosAcumulados.clear();
        limpar();
    }

    // ========== CLASSES INTERNAS ==========

    // AQUI TAMBÉM ESTAVA O ERRO -> Corrigido para "ResultadoSimulacao"
    private static class ResultadoSimulacao {
        Avaliador avaliador;
        List<Processo> processos;

        ResultadoSimulacao(Avaliador av, List<Processo> procs) {
            this.avaliador = av;
            this.processos = new ArrayList<>(procs);
        }
    }

    private static class GanttChartPanel extends JPanel {
        private final List<Processo> processos;
        private long tempoMin, tempoMax;
        private final Color[] cores = {
                new Color(110, 178, 255), new Color(111, 219, 146),
                new Color(255, 178, 110), new Color(255, 110, 110),
                new Color(178, 110, 255), new Color(245, 245, 122)
        };

        public GanttChartPanel(List<Processo> processos) {
            this.processos = new ArrayList<>(processos);
            this.processos.sort(Comparator.comparingInt(Processo::getIdProcesso));
            setBackground(Color.WHITE);
            calcularLimites();
        }

        private void calcularLimites() {
            if (processos.isEmpty()) return;
            tempoMin = processos.stream().mapToLong(Processo::getTempoChegada).min().orElse(0);
            tempoMax = processos.stream().mapToLong(Processo::getTempoFinalizacao).max().orElse(tempoMin);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (processos.isEmpty() || tempoMax <= tempoMin) {
                g2d.drawString("Sem dados", 20, 30);
                return;
            }

            int padL = 80, padR = 30, padT = 30, padB = 40;
            int alturaBarra = 30, espaco = 15;
            int larguraUtil = getWidth() - padL - padR;
            long duracao = tempoMax - tempoMin;

            int y = padT;
            for (int i = 0; i < processos.size(); i++) {
                Processo p = processos.get(i);
                g2d.setColor(Color.BLACK);
                g2d.drawString("P" + p.getIdProcesso(), 10, y + alturaBarra/2 + 5);

                if (p.getTempoInicioPrimeiraExecucao() != -1) {
                    long inicio = p.getTempoInicioPrimeiraExecucao() - tempoMin;
                    long fim = p.getTempoFinalizacao() - tempoMin;

                    int x = padL + (int)(larguraUtil * inicio / duracao);
                    int w = Math.max(1, (int)(larguraUtil * (fim - inicio) / duracao));

                    g2d.setColor(cores[i % cores.length]);
                    g2d.fillRect(x, y, w, alturaBarra);
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.drawRect(x, y, w, alturaBarra);
                }
                y += alturaBarra + espaco;
            }

            setPreferredSize(new Dimension(getWidth(), y + padB));

            int yEixo = y;
            g2d.setColor(Color.BLACK);
            g2d.drawLine(padL, yEixo, getWidth() - padR, yEixo);

            for (int i = 0; i <= 5; i++) {
                long tempo = tempoMin + (i * duracao / 5);
                int x = padL + (int)(i * larguraUtil / 5.0);
                g2d.drawLine(x, yEixo, x, yEixo + 5);
                g2d.drawString(String.format("%.1fs", (tempo - tempoMin)/1000.0), x - 15, yEixo + 20);
            }
        }
    }

    private static class GraficoBarrasPanel extends JPanel {
        private final List<Processo> processos;
        private final String titulo;
        private final Function<Processo, Long> extratorDeValor;

        public GraficoBarrasPanel(List<Processo> processos, String titulo, Function<Processo, Long> extratorDeValor) {
            this.processos = processos;
            this.titulo = titulo;
            this.extratorDeValor = extratorDeValor;
            setPreferredSize(new Dimension(500, 300));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (processos == null || processos.isEmpty()) {
                g2d.drawString(titulo + " (sem dados)", 10, 20);
                return;
            }

            long maxValor = processos.stream()
                    .mapToLong(extratorDeValor::apply)
                    .max()
                    .orElse(1L);

            int padL = 60, padR = 40, padT = 50, padB = 60;
            int larguraUtil = getWidth() - padL - padR;
            int alturaUtil = getHeight() - padT - padB;
            double larguraBarra = (double) larguraUtil / processos.size();

            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2d.drawString(titulo, padL, 30);

            g2d.setColor(Color.BLACK);
            g2d.drawLine(padL, getHeight() - padB, getWidth() - padR, getHeight() - padB); // X
            g2d.drawLine(padL, padT, padL, getHeight() - padB); // Y

            for (int i = 0; i < processos.size(); i++) {
                Processo p = processos.get(i);
                long valor = extratorDeValor.apply(p);

                int altBarra = (int) (((double) valor / maxValor) * alturaUtil);
                int x = (int) (padL + i * larguraBarra);
                int y = getHeight() - padB - altBarra;

                g2d.setColor(new Color(70, 130, 180));
                g2d.fillRect(x + 2, y, (int) larguraBarra - 4, altBarra);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x + 2, y, (int) larguraBarra - 4, altBarra);

                String labelProcesso = "P" + p.getIdProcesso();
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2d.drawString(labelProcesso, x + (int)larguraBarra/2 - 5, getHeight() - padB + 15);

                String labelValor = String.valueOf(valor);
                g2d.drawString(labelValor, x + (int)larguraBarra/2 - 10, y - 5);
            }

            g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2d.drawString("Processos", getWidth()/2 - 30, getHeight() - 10);
            g2d.rotate(-Math.PI/2);
            g2d.drawString("Tempo (ms)", -getHeight()/2 - 30, 20);
        }
    }

    // AQUI TAMBÉM ESTAVA O ERRO -> Corrigido para "ResultadoSimulacao"
    private static class ComparativoCenariosPanel extends JPanel {
        private final Map<String, ResultadoSimulacao> resultados;

        public ComparativoCenariosPanel(Map<String, ResultadoSimulacao> resultados) {
            this.resultados = resultados;
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (resultados.size() < 2) {
                g2d.drawString("Execute ao menos 2 cenários para comparação", 20, 30);
                return;
            }

            String[] metricas = {"Throughput", "Retorno (ms)", "Espera (ms)", "Resposta (ms)", "Trocas Ctx"};
            int numMetricas = metricas.length;
            int numCenarios = resultados.size();

            int padL = 80, padR = 40, padT = 60, padB = 80;
            int larguraUtil = getWidth() - padL - padR;
            int alturaUtil = getHeight() - padT - padB;

            double larguraGrupo = (double)larguraUtil / numMetricas;
            double larguraBarra = larguraGrupo / (numCenarios + 1);

            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2d.drawString("Comparação de Métricas por Cenário", padL, 30);

            g2d.setColor(Color.BLACK);
            g2d.drawLine(padL, getHeight() - padB, getWidth() - padR, getHeight() - padB);
            g2d.drawLine(padL, padT, padL, getHeight() - padB);

            Color[] coresCenarios = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA};
            List<String> nomeCenarios = new ArrayList<>(resultados.keySet());

            for (int m = 0; m < numMetricas; m++) {
                double maxValor = 0;

                for (String cenario : nomeCenarios) {
                    double valor = obterValorMetrica(resultados.get(cenario).avaliador, m);
                    if (valor > maxValor) maxValor = valor;
                }
                if (maxValor == 0) maxValor = 1;

                for (int c = 0; c < numCenarios; c++) {
                    String cenario = nomeCenarios.get(c);
                    double valor = obterValorMetrica(resultados.get(cenario).avaliador, m);

                    int altBarra = (int)((valor / maxValor) * alturaUtil);
                    int x = (int)(padL + m * larguraGrupo + c * larguraBarra);
                    int y = getHeight() - padB - altBarra;

                    g2d.setColor(coresCenarios[c % coresCenarios.length]);
                    g2d.fillRect(x, y, (int)larguraBarra - 2, altBarra);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x, y, (int)larguraBarra - 2, altBarra);

                    if (altBarra > 10) {
                        g2d.setFont(new Font("SansSerif", Font.BOLD, 9));
                        String valorStr = String.format("%.1f", valor);
                        FontMetrics fm = g2d.getFontMetrics();
                        int larguraTexto = fm.stringWidth(valorStr);
                        int xTexto = x + ((int)larguraBarra - 2 - larguraTexto) / 2;
                        int yTexto = y - 3;

                        g2d.setColor(Color.BLACK);
                        g2d.drawString(valorStr, xTexto, yTexto);
                    }
                }

                int xLabel = (int)(padL + m * larguraGrupo + larguraGrupo/2 - 20);
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
                g2d.drawString(metricas[m], xLabel, getHeight() - padB + 20);
            }

            int yLegenda = getHeight() - 20;
            for (int c = 0; c < nomeCenarios.size(); c++) {
                g2d.setColor(coresCenarios[c % coresCenarios.length]);
                g2d.fillRect(padL + c * 150, yLegenda, 15, 15);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(padL + c * 150, yLegenda, 15, 15);
                g2d.drawString(nomeCenarios.get(c), padL + c * 150 + 20, yLegenda + 12);
            }
        }

        private double obterValorMetrica(Avaliador av, int indice) {
            switch (indice) {
                case 0: return av.getThroughput();
                case 1: return av.getTempoMedioDeRetorno();
                case 2: return av.getTempoMedioDeEspera();
                case 3: return av.getTempoMedioDeResposta();
                case 4: return av.getTrocasDeContexto();
                default: return 0;
            }
        }
    }
}