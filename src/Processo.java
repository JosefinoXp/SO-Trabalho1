public class Processo extends Thread {
    private int id;
    private int prioridade;
    private int tempoExecucao;
    private int tempoExecutado;
    private Estado estado;

    public enum Estado {
        PRONTO, EXECUCAO, SUSPENSO, FINALIZADO
    }

    public Processo(int id, int prioridade, int tempoExecucao) {
        this.id = id; // ID do processo
        this.prioridade = prioridade; // Prioridade definida
        this.tempoExecucao = tempoExecucao; // Tempo total necessário
        this.tempoExecutado = 0; // Nenhum tempo executado inicialmente
        this.estado = Estado.SUSPENSO; // Estado inicial: suspenso
    }

    // Transição para PRONTO
    public void pronto() {
        this.estado = Estado.PRONTO;
        System.out.println("Processo " + id + " pronto para execução.");
    }

    // Transição para EXECUÇÃO
    private void executar() {
        this.estado = Estado.EXECUCAO;
        System.out.println("Processo " + id + " em execução...");
    }

    // Suspende o processo (volta para fila no Round Robin)
    public void suspender() {
        this.estado = Estado.SUSPENSO;
        System.out.println("Processo " + id + " suspenso.");
    }

    // Finaliza o processo
    public void finalizar() {
        this.estado = Estado.FINALIZADO;
        System.out.println("Processo " + id + " finalizado.\n");
    }

    // Método de execução completa (usado no algoritmo por prioridade)
    @Override
    public void run() {
        executar();
        try {
            Thread.sleep(tempoExecucao); // Simula o tempo total de execução
            tempoExecutado = tempoExecucao;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finalizar();
    }

    // Método de execução parcial (usado no Round Robin)
    public void executarQuantum(int quantum) {
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
            suspender(); // Processo ainda não terminou
        } else {
            finalizar(); // Concluído
        }
    }

    // Getters
    public int getIdProcesso() {
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
}
