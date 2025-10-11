public class Processo extends Thread {
    private int id;
    private int prioridade;
    private int tempoExecucao;
    private int tempoExecutado;
    private Estado estado;

    public enum Estado {
        PRONTO, EXECUCAO, SUSPENSO
    }

    public Processo(int id, int prioridade, int tempoExecucao) {
        this.id = id;//id deve ser informado
        this.prioridade = prioridade;//prioridade deve ser informada
        this.tempoExecucao = tempoExecucao;//tempo informado
        this.tempoExecutado = 0; // Inicialmente, o tempo executado é zero
        this.estado = Estado.SUSPENSO;//Processo entra suspenso na fila
    }

    public void suspender() {//suspenede um processo
        this.estado = Estado.SUSPENSO;
        System.out.println("Processo " + id + " suspenso.");
    }

    public void pronto() {
        this.estado = Estado.PRONTO;
        System.out.println("Processo " + id + " retomado.");
    }

    public long getId() {
        return this.id;
    }

    public int getPrioridade() {
        return this.prioridade;
    }

    public int getTempoExecucao() {
        return this.tempoExecucao;
    }

    public int getTempoExecutado() {
        return this.tempoExecutado;
    }

    public Estado getEstado() {
        return this.estado;
    }

    public void setTempoExecucao(int tempoExecucao) {
        this.tempoExecucao = tempoExecucao;
    }

}
