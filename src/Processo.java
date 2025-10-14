import java.util.concurrent.ThreadLocalRandom;

public class Processo {
    private final int id;
    private final int prioridade;
    private final int tempoExecucao; // Tempo total necessário (burst time em ms)
    private int tempoExecutado;
    private Estado estado;

    // Métricas de timeline (em wall-clock)
    private long tempoChegada;
    private long tempoInicioPrimeiraExecucao = -1;
    private long tempoFinalizacao;

    // Modelo: fração do quantum que é CPU-bound; o resto simula I/O (sleep)
    // Ex.: 0.7 => 70% CPU, 30% I/O
    private double cpuIntensidade;

    public enum Estado { NOVO, PRONTO, EXECUCAO, SUSPENSO, FINALIZADO }

    public Processo(int id, int prioridade, int tempoExecucao) {
        this.id = id;
        this.prioridade = prioridade;
        this.tempoExecucao = tempoExecucao;
        this.tempoExecutado = 0;
        this.estado = Estado.NOVO;
        this.tempoChegada = System.currentTimeMillis();
        // intensidade padrão razoável; pode ser alterada com setCpuIntensidade
        this.cpuIntensidade = 0.6 + ThreadLocalRandom.current().nextDouble() * 0.3; // 0.6–0.9
    }

    public synchronized void pronto() {
        this.estado = Estado.PRONTO;
    }

    /**
     * Executa uma fatia (quantum). Retorna o tempo de CPU real gasto nesta chamada (em nanos),
     * medido via ThreadMXBean no thread que chama este método (o escalonador).
     */
    public synchronized long executar(int quantum, java.lang.management.ThreadMXBean medidor) {
        if (estado == Estado.FINALIZADO) return 0L;

        if (tempoInicioPrimeiraExecucao == -1) {
            this.tempoInicioPrimeiraExecucao = System.currentTimeMillis();
        }

        this.estado = Estado.EXECUCAO;
        int tempoRestante = tempoExecucao - tempoExecutado;
        int tempoParaExecutar = Math.min(quantum, tempoRestante);

        // parcela CPU-bound (busy loop) + parcela I/O-bound (sleep)
        long cpuAlvoMs = Math.max(0, Math.round(tempoParaExecutar * cpuIntensidade));
        long ioAlvoMs  = Math.max(0, tempoParaExecutar - cpuAlvoMs);

        // Medição de CPU real do thread atual (escalonador)
        long cpuAntesNs = medidor != null && medidor.isCurrentThreadCpuTimeSupported()
                ? medidor.getCurrentThreadCpuTime() : -1L;

        // Busy work por ~cpuAlvoMs (usa operações para evitar eliminação pelo JIT)
        long inicioWall = System.currentTimeMillis();
        double sink = 0.0;
        while (System.currentTimeMillis() - inicioWall < cpuAlvoMs) {
            // lote de operações numéricas
            for (int i = 0; i < 50_000; i++) {
                sink += Math.sqrt(i + sink);
            }
            // impede otimização agressiva
            if (sink > Double.MAX_VALUE / 2) sink = 0.0;
        }

        long cpuDepoisNs = medidor != null && medidor.isCurrentThreadCpuTimeSupported()
                ? medidor.getCurrentThreadCpuTime() : -1L;

        long cpuGastoNs = 0L;
        if (cpuAntesNs >= 0 && cpuDepoisNs >= cpuAntesNs) {
            cpuGastoNs = cpuDepoisNs - cpuAntesNs;
        }

        // Simula parte I/O-bound (sleep não consome CPU)
        if (ioAlvoMs > 0) {
            try { Thread.sleep(ioAlvoMs); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        tempoExecutado += tempoParaExecutar;

        if (tempoExecutado >= tempoExecucao) {
            finalizar();
        } else {
            suspender();
        }
        return cpuGastoNs;
    }

    public synchronized void suspender() {
        this.estado = Estado.SUSPENSO;
    }

    public synchronized void finalizar() {
        this.estado = Estado.FINALIZADO;
        this.tempoFinalizacao = System.currentTimeMillis();
    }

    // Getters básicos
    public int getIdProcesso() { return id; }
    public int getPrioridade() { return prioridade; }
    public int getTempoExecucao() { return tempoExecucao; }
    public synchronized int getTempoExecutado() { return tempoExecutado; }
    public synchronized Estado getEstado() { return estado; }

    // Métricas de timeline
    public long getTempoChegada() { return tempoChegada; }
    public long getTempoInicioPrimeiraExecucao() { return tempoInicioPrimeiraExecucao; }
    public long getTempoFinalizacao() { return tempoFinalizacao; }
    public long getTempoDeRetorno() { return tempoFinalizacao - tempoChegada; }
    public long getTempoDeEspera() { return getTempoDeRetorno() - tempoExecucao; }
    public long getTempoDeResposta() { return tempoInicioPrimeiraExecucao - tempoChegada; }

    // Intensidade de CPU (0.0 a 1.0)
    public double getCpuIntensidade() { return cpuIntensidade; }
    public void setCpuIntensidade(double cpuIntensidade) {
        this.cpuIntensidade = Math.max(0.0, Math.min(1.0, cpuIntensidade));
    }
}
