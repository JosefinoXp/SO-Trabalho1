public class Processo {
    private final int id;
    private final int prioridade;
    private final int tempoExecucao; // Tempo total necessário (burst time)
    private int tempoExecutado;
    private Estado estado;

    // Métricas
    private long tempoChegada;
    private long tempoInicioPrimeiraExecucao = -1;
    private long tempoFinalizacao;

    public enum Estado { NOVO, PRONTO, EXECUCAO, SUSPENSO, FINALIZADO }

    public Processo(int id, int prioridade, int tempoExecucao) {
        this.id = id;
        this.prioridade = prioridade;
        this.tempoExecucao = tempoExecucao;
        this.tempoExecutado = 0;
        this.estado = Estado.NOVO;
        this.tempoChegada = System.currentTimeMillis();
    }

    public synchronized void pronto() {
        this.estado = Estado.PRONTO;
    }

    public synchronized void executar(int quantum) {
        if (estado == Estado.FINALIZADO) return;

        if (tempoInicioPrimeiraExecucao == -1) {
            this.tempoInicioPrimeiraExecucao = System.currentTimeMillis();
        }

        this.estado = Estado.EXECUCAO;
        int tempoRestante = tempoExecucao - tempoExecutado;
        int tempoParaExecutar = Math.min(quantum, tempoRestante);

        try {
            Thread.sleep(tempoParaExecutar);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }

        tempoExecutado += tempoParaExecutar;
        if (tempoExecutado >= tempoExecucao) {
            finalizar();
        } else {
            suspender();
        }
    }

    public synchronized void suspender() {
        this.estado = Estado.SUSPENSO;
    }

    public synchronized void finalizar() {
        this.estado = Estado.FINALIZADO;
        this.tempoFinalizacao = System.currentTimeMillis();
    }

    // Getters para informações básicas
    public int getIdProcesso() { return id; }
    public int getPrioridade() { return prioridade; }
    public int getTempoExecucao() { return tempoExecucao; }
    public synchronized int getTempoExecutado() { return tempoExecutado; }
    public synchronized Estado getEstado() { return estado; }

    // Getters para métricas
    public long getTempoChegada() { return tempoChegada; }
    public long getTempoInicioPrimeiraExecucao() { return tempoInicioPrimeiraExecucao; }
    public long getTempoFinalizacao() { return tempoFinalizacao; }
    public long getTempoDeRetorno() { return tempoFinalizacao - tempoChegada; }
    public long getTempoDeEspera() { return getTempoDeRetorno() - tempoExecucao; }
    public long getTempoDeResposta() { return tempoInicioPrimeiraExecucao - tempoChegada; }
}