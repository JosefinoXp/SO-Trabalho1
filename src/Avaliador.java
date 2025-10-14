import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Avaliador {
    private List<Processo> processosConcluidos;
    private double tempoTotalSimulacaoMs; // wall time da simulação
    private int trocasDeContexto;
    private long tempoOverheadTotalMs;
    private long cpuTotalNs; // CPU real medida pelo ThreadMXBean

    public Avaliador(List<Processo> processosConcluidos,
                     double tempoTotalSimulacaoMs,
                     int trocasDeContexto,
                     long tempoOverheadTotalMs,
                     long cpuTotalNs) {
        this.processosConcluidos = processosConcluidos;
        this.tempoTotalSimulacaoMs = tempoTotalSimulacaoMs;
        this.trocasDeContexto = trocasDeContexto;
        this.tempoOverheadTotalMs = tempoOverheadTotalMs;
        this.cpuTotalNs = cpuTotalNs;
    }

    public static List<Processo> gerarCargaDeTrabalho(int totalProcessos) {
        Random rand = new Random(42);
        return java.util.stream.IntStream.rangeClosed(1, totalProcessos)
                .mapToObj(i -> {
                    int prioridade = rand.nextInt(10) + 1;
                    int tempoExec = (rand.nextInt(5) + 2) * 1000; // 2–6s
                    Processo p = new Processo(i, prioridade, tempoExec);
                    // espalha intensidades de CPU: 30% a 90%
                    p.setCpuIntensidade(0.3 + rand.nextDouble() * 0.6);
                    return p;
                })
                .collect(Collectors.toList());
    }

    // Retorna a soma do "burst" concluído (útil p/ timeline, não p/ CPU real)
    private long getTempoTotalBurstMs() {
        return processosConcluidos.stream().mapToLong(Processo::getTempoExecutado).sum();
    }

    public double getThroughput() {
        if (tempoTotalSimulacaoMs <= 0) return 0;
        return processosConcluidos.size() / (tempoTotalSimulacaoMs / 1000.0);
    }

    public double getTempoMedioDeRetorno() {
        return processosConcluidos.stream().mapToLong(Processo::getTempoDeRetorno).average().orElse(0.0);
    }

    public double getTempoMedioDeEspera() {
        return processosConcluidos.stream().mapToLong(Processo::getTempoDeEspera).average().orElse(0.0);
    }

    public double getTempoMedioDeResposta() {
        return processosConcluidos.stream().mapToLong(Processo::getTempoDeResposta).average().orElse(0.0);
    }

    /**
     * Utilização “realista”: CPU_time / wall_time.
     * cpuTotalNs vem do ThreadMXBean; wall time = tempoTotalSimulacaoMs.
     */
    public double getUtilizacaoCPU() {
        if (tempoTotalSimulacaoMs <= 0) return 0.0;
        double cpuMs = cpuTotalNs / 1_000_000.0;
        double utiliz = (cpuMs / tempoTotalSimulacaoMs) * 100.0;
        // limita para evitar ruídos de alta resolução
        return Math.max(0.0, Math.min(utiliz, 100.0));
    }

    public int getTrocasDeContexto() { return trocasDeContexto; }

    public long getTempoOverheadTotalMs() { return tempoOverheadTotalMs; }

    // Diagnósticos adicionais (se quiser exibir)
    public long getCpuTotalNs() { return cpuTotalNs; }
}
