public class Processo extends Thread {
    private int id;
    private int prioridade;
    private int tempoExecucao;
    private int tempoExecutado;
    private Estado estado;

    public enum Estado { PRONTO, EXECUCAO, SUSPENSO, FINALIZADO }

    public Processo(int id, int prioridade, int tempoExecucao) {
        this.id = id;
        this.prioridade = prioridade;
        this.tempoExecucao = tempoExecucao;
        this.tempoExecutado = 0;
        this.estado = Estado.SUSPENSO;
    }

    // Define como PRONTO
    public synchronized void pronto() {
        this.estado = Estado.PRONTO;
        System.out.printf("[Processo %d] PRONTO | Prioridade: %d | Tempo Total: %d ms%n",
                id, prioridade, tempoExecucao);
    }

    // Executa um quantum
    public synchronized void executarQuantum(int quantum) {
        if (estado == Estado.FINALIZADO) return;

        this.estado = Estado.EXECUCAO;
        int tempoRestante = tempoExecucao - tempoExecutado;
        int tempoParaExecutar = Math.min(quantum, tempoRestante);

        System.out.printf("[Processo %d] EXECUCAO | Prioridade: %d | Quantum: %d ms | Restante: %d ms%n",
                id, prioridade, tempoParaExecutar, tempoRestante);

        try {
            Thread.sleep(tempoParaExecutar);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        tempoExecutado += tempoParaExecutar;

        tempoRestante = tempoExecucao - tempoExecutado;
        if (tempoRestante > 0) {
            suspender();
        } else {
            finalizar();
        }
    }

    public synchronized void suspender() {
        this.estado = Estado.SUSPENSO;
        System.out.printf("[Processo %d] SUSPENSO | Executado: %d/%d ms%n",
                id, tempoExecutado, tempoExecucao);
    }

    public synchronized void finalizar() {
        this.estado = Estado.FINALIZADO;
        System.out.printf("[Processo %d] FINALIZADO | Tempo total executado: %d ms%n%n",
                id, tempoExecutado);
    }

    // Getters
    public int getIdProcesso() { return id; }
    public int getPrioridade() { return prioridade; }
    public int getTempoExecucao() { return tempoExecucao; }
    public int getTempoExecutado() { return tempoExecutado; }
    public Estado getEstado() { return estado; }
}
