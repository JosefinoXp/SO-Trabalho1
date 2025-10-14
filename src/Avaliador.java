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

    // --- MÉTODO ATUALIZADO ---
    /**
     * Função do método: Gera uma lista de processos simulados com parâmetros aleatórios.
     * Entrada: totalProcessos, seed
     * Saída: Lista de processos
     */
    public static List<Processo> gerarCargaDeTrabalho(int totalProcessos, int seed) {
        Random rand = new Random(seed); // Usa a semente (seed) fornecida
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

    /**
     * Função do método: Calcula o throughput da simulação.
     * Entrada: Nenhuma
     * Saída: Valor de throughput
     */
    public double getThroughput() {
        if (tempoTotalSimulacaoMs <= 0) return 0;
        return processosConcluidos.size() / (tempoTotalSimulacaoMs / 1000.0);
    }

    /**
     * Função do método: Calcula o tempo médio de retorno dos processos.
     * Entrada: Nenhuma
     * Saída: Tempo médio de retorno
     */
    public double getTempoMedioDeRetorno() {
        return processosConcluidos.stream().mapToLong(Processo::getTempoDeRetorno).average().orElse(0.0);
    }

    /**
     * Função do método: Calcula o tempo médio de espera dos processos.
     * Entrada: Nenhuma
     * Saída: Tempo médio de espera
     */
    public double getTempoMedioDeEspera() {
        return processosConcluidos.stream().mapToLong(Processo::getTempoDeEspera).average().orElse(0.0);
    }

    /**
     * Função do método: Calcula o tempo médio de resposta dos processos.
     * Entrada: Nenhuma
     * Saída: Tempo médio de resposta
     */
    public double getTempoMedioDeResposta() {
        return processosConcluidos.stream().mapToLong(Processo::getTempoDeResposta).average().orElse(0.0);
    }

    /**
     * Utilização “realista”: CPU_time / wall_time.
     * cpuTotalNs vem do ThreadMXBean; wall time = tempoTotalSimulacaoMs.
     */
    /**
     * Função do método: Calcula a utilização da CPU durante a simulação.
     * Entrada: Nenhuma
     * Saída: Percentual de utilização da CPU
     */
    public double getUtilizacaoCPU() {
        if (tempoTotalSimulacaoMs <= 0) return 0.0;
        double cpuMs = cpuTotalNs / 1_000_000.0;
        double utiliz = (cpuMs / tempoTotalSimulacaoMs) * 100.0;
        // limita para evitar ruídos de alta resolução
        return Math.max(0.0, Math.min(utiliz, 100.0));
    }

    /**
     * Função do método: Retorna o número de trocas de contexto.
     * Entrada: Nenhuma
     * Saída: Número de trocas de contexto
     */
    public int getTrocasDeContexto() { return trocasDeContexto; }

    /**
     * Função do método: Retorna o tempo total de overhead de trocas de contexto.
     * Entrada: Nenhuma
     * Saída: Tempo de overhead em ms
     */
    public long getTempoOverheadTotalMs() { return tempoOverheadTotalMs; }

    // Diagnósticos adicionais (se quiser exibir)
    /**
     * Função do método: Retorna o tempo total de CPU real gasto.
     * Entrada: Nenhuma
     * Saída: Tempo de CPU em nanossegundos
     */
    public long getCpuTotalNs() { return cpuTotalNs; }
}