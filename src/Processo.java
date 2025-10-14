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

    /**
     * Função do método: Define o estado do processo como PRONTO.
     * Entrada: Nenhuma
     * Saída: Nenhuma (atualiza estado)
     */
    public synchronized void pronto() {
        this.estado = Estado.PRONTO;
    }

    /**
     * Executa uma fatia (quantum). Retorna o tempo de CPU real gasto nesta chamada (em nanos),
     * medido via ThreadMXBean no thread que chama este método (o escalonador).
     */
    /**
     * Função do método: Executa uma fatia do processo simulando CPU e I/O.
     * Entrada: quantum, medidor de CPU
     * Saída: Tempo de CPU real gasto (nanossegundos)
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

    /**
     * Função do método: Define o estado do processo como SUSPENSO.
     * Entrada: Nenhuma
     * Saída: Nenhuma (atualiza estado)
     */
    public synchronized void suspender() {
        this.estado = Estado.SUSPENSO;
    }

    /**
     * Função do método: Finaliza o processo e registra o tempo de finalização.
     * Entrada: Nenhuma
     * Saída: Nenhuma (atualiza estado e tempo)
     */
    public synchronized void finalizar() {
        this.estado = Estado.FINALIZADO;
        this.tempoFinalizacao = System.currentTimeMillis();
    }

    // Getters básicos
    /**
     * Função do método: Retorna o ID do processo.
     * Entrada: Nenhuma
     * Saída: ID do processo
     */
    public int getIdProcesso() { return id; }
    /**
     * Função do método: Retorna a prioridade do processo.
     * Entrada: Nenhuma
     * Saída: Prioridade
     */
    public int getPrioridade() { return prioridade; }
    /**
     * Função do método: Retorna o tempo total de execução necessário.
     * Entrada: Nenhuma
     * Saída: Tempo de execução em ms
     */
    public int getTempoExecucao() { return tempoExecucao; }
    /**
     * Função do método: Retorna o tempo já executado do processo.
     * Entrada: Nenhuma
     * Saída: Tempo executado em ms
     */
    public synchronized int getTempoExecutado() { return tempoExecutado; }
    /**
     * Função do método: Retorna o estado atual do processo.
     * Entrada: Nenhuma
     * Saída: Estado do processo
     */
    public synchronized Estado getEstado() { return estado; }

    // Métricas de timeline
    /**
     * Função do método: Retorna o tempo de chegada do processo.
     * Entrada: Nenhuma
     * Saída: Tempo de chegada (ms)
     */
    public long getTempoChegada() { return tempoChegada; }
    /**
     * Função do método: Retorna o tempo de início da primeira execução.
     * Entrada: Nenhuma
     * Saída: Tempo de início (ms)
     */
    public long getTempoInicioPrimeiraExecucao() { return tempoInicioPrimeiraExecucao; }
    /**
     * Função do método: Retorna o tempo de finalização do processo.
     * Entrada: Nenhuma
     * Saída: Tempo de finalização (ms)
     */
    public long getTempoFinalizacao() { return tempoFinalizacao; }
    /**
     * Função do método: Calcula o tempo de retorno do processo.
     * Entrada: Nenhuma
     * Saída: Tempo de retorno (ms)
     */
    public long getTempoDeRetorno() { return tempoFinalizacao - tempoChegada; }
    /**
     * Função do método: Calcula o tempo de espera do processo.
     * Entrada: Nenhuma
     * Saída: Tempo de espera (ms)
     */
    public long getTempoDeEspera() { return getTempoDeRetorno() - tempoExecucao; }
    /**
     * Função do método: Calcula o tempo de resposta do processo.
     * Entrada: Nenhuma
     * Saída: Tempo de resposta (ms)
     */
    public long getTempoDeResposta() { return tempoInicioPrimeiraExecucao - tempoChegada; }

    // Intensidade de CPU (0.0 a 1.0)
    /**
     * Função do método: Retorna a intensidade de CPU do processo.
     * Entrada: Nenhuma
     * Saída: Intensidade de CPU (0.0 a 1.0)
     */
    public double getCpuIntensidade() { return cpuIntensidade; }
    /**
     * Função do método: Define a intensidade de CPU do processo.
     * Entrada: Intensidade de CPU (double)
     * Saída: Nenhuma (atualiza atributo)
     */
    public void setCpuIntensidade(double cpuIntensidade) {
        this.cpuIntensidade = Math.max(0.0, Math.min(1.0, cpuIntensidade));
    }
}
