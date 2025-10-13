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

    // Marca o processo como pronto
    public synchronized void pronto() {
        this.estado = Estado.PRONTO;
        System.out.println("[Processo " + id + "] PRONTO para execução.");
    }

    // Simula execução parcial (quantum)
    public synchronized void executarQuantum(int quantum) {
        if (estado == Estado.FINALIZADO) return;

        this.estado = Estado.EXECUCAO;
        int tempoRestante = tempoExecucao - tempoExecutado;
        int tempoParaExecutar = Math.min(quantum, tempoRestante);

        System.out.println("[Processo " + id + "] EXECUTANDO por " + tempoParaExecutar + " ms (restam " + tempoRestante + " ms)");

        try {
            Thread.sleep(tempoParaExecutar); // simula o tempo de execução
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        tempoExecutado += tempoParaExecutar;

        // Atualiza o estado após o quantum
        tempoRestante = tempoExecucao - tempoExecutado;
        if (tempoRestante > 0) {
            suspender();
        } else {
            finalizar();
        }
    }

    // Marca como suspenso
    public synchronized void suspender() {
        this.estado = Estado.SUSPENSO;
        System.out.println("[Processo " + id + "] SUSPENSO (executou " + tempoExecutado + " / " + tempoExecucao + " ms)");
    }

    // Marca como finalizado
    public synchronized void finalizar() {
        this.estado = Estado.FINALIZADO;
        System.out.println("[Processo " + id + "] FINALIZADO ✅\n");
    }

    // Getters
    public int getIdProcesso() { return this.id; }
    public int getPrioridade() { return this.prioridade; }
    public int getTempoExecucao() { return this.tempoExecucao; }
    public int getTempoExecutado() { return this.tempoExecutado; }
    public Estado getEstado() { return this.estado; }
}
