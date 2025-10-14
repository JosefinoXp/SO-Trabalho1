import java.util.function.Consumer;

public class Processo {
    private int id;
    private int prioridade;
    private int tempoExecucao;
    private int tempoExecutado;
    private Estado estado;
    private Consumer<String> outputHandler = System.out::println;

    public enum Estado { PRONTO, EXECUCAO, SUSPENSO, FINALIZADO }

    public Processo(int id, int prioridade, int tempoExecucao) {
        this.id = id;
        this.prioridade = prioridade;
        this.tempoExecucao = tempoExecucao;
        this.tempoExecutado = 0;
        this.estado = Estado.SUSPENSO;
    }

    public void setOutputHandler(Consumer<String> handler) {
        this.outputHandler = handler;
    }

    public synchronized void pronto() {
        this.estado = Estado.PRONTO;
        outputHandler.accept(String.format("[P%d] PRONTO para execução.", id));
    }

    public synchronized void executarQuantum(int quantum) {
        if (estado == Estado.FINALIZADO) return;

        this.estado = Estado.EXECUCAO;
        int tempoRestante = tempoExecucao - tempoExecutado;
        int tempoParaExecutar = Math.min(quantum, tempoRestante);

        outputHandler.accept(String.format("[P%d] EXECUTANDO por %d ms (restam %d ms)", id, tempoParaExecutar, tempoRestante));

        try {
            Thread.sleep(tempoParaExecutar);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
        outputHandler.accept(String.format("[P%d] SUSPENSO (executou %d/%d ms)", id, tempoExecutado, tempoExecucao));
    }

    public synchronized void finalizar() {
        this.estado = Estado.FINALIZADO;
        outputHandler.accept(String.format("[P%d] FINALIZADO ✅\n", id));
    }

    public int getIdProcesso() { return id; }
    public int getPrioridade() { return prioridade; }
    public int getTempoExecucao() { return tempoExecucao; }
    public int getTempoExecutado() { return tempoExecutado; }
    public Estado getEstado() { return estado; }
}
