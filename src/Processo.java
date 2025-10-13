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

    // Métodos sincronizados para garantir exclusão mútua
    public synchronized void pronto() {
        this.estado = Estado.PRONTO;
        System.out.println("Processo " + id + " pronto para execução.");
    }

    private synchronized void executar() {
        this.estado = Estado.EXECUCAO;
        System.out.println("Processo " + id + " em execução...");
    }

    public synchronized void suspender() {
        this.estado = Estado.SUSPENSO;
        System.out.println("Processo " + id + " suspenso.");
    }

    public synchronized void finalizar() {
        this.estado = Estado.FINALIZADO;
        System.out.println("Processo " + id + " finalizado.\n");
    }

    @Override
    public synchronized void run() {
        executar();
        try {
            Thread.sleep(tempoExecucao);
            tempoExecutado = tempoExecucao;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finalizar();
    }

    public synchronized void executarQuantum(int quantum) {
        executar();
        int tempoRestante = tempoExecucao - tempoExecutado;
        int tempoParaExecutar = Math.min(quantum, tempoRestante);
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

    public int getIdProcesso() { return this.id; }
    public int getPrioridade() { return this.prioridade; }
    public int getTempoExecucao() { return this.tempoExecucao; }
    public int getTempoExecutado() { return this.tempoExecutado; }
    public Estado getEstado() { return this.estado; }
}
