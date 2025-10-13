import java.util.*;

public class Escalonador {
    private List<Processo> listaProcessos;
    private int quantum;
    private Algoritmo algoritmoSelecionado;

    public enum Algoritmo { PRIORIDADE, ROUND_ROBIN }

    public Escalonador(Algoritmo algoritmo, int quantum) {
        this.listaProcessos = new ArrayList<>();
        this.algoritmoSelecionado = algoritmo;
        this.quantum = quantum;
    }

    public void adicionarProcesso(Processo processo) {
        listaProcessos.add(processo);
    }

    // Método principal de escalonamento
    public synchronized void escalonar() {
        switch (algoritmoSelecionado) {
            case PRIORIDADE:
                escalonarPorPrioridade();
                break;
            case ROUND_ROBIN:
                escalonarRoundRobin();
                break;
        }
    }

    // Escalonamento por prioridade
    private void escalonarPorPrioridade() {
        System.out.println("\n===== ESCALONAMENTO POR PRIORIDADE =====\n");

        // Ordena pela prioridade (maior primeiro)
        listaProcessos.sort((p1, p2) -> Integer.compare(p2.getPrioridade(), p1.getPrioridade()));

        // Inicia todas as threads (concorrência real)
        for (Processo p : listaProcessos) {
            p.pronto();
            new Thread(() -> p.executarQuantum(p.getTempoExecucao())).start();
        }

        // Aguarda todos finalizarem
        aguardarConclusao();
        System.out.println("Escalonamento por PRIORIDADE concluído.\n");
    }

    // Escalonamento Round Robin com quantum
    private void escalonarRoundRobin() {
        System.out.println("\n===== ESCALONAMENTO ROUND ROBIN =====\n");

        Queue<Processo> fila = new LinkedList<>(listaProcessos);

        // Continua até todos os processos finalizarem
        while (!fila.isEmpty()) {
            Processo processoAtual = fila.poll();

            if (processoAtual.getEstado() != Processo.Estado.FINALIZADO) {
                processoAtual.pronto();

                // Cria thread para executar um quantum
                Thread t = new Thread(() -> processoAtual.executarQuantum(quantum));
                t.start();

                try {
                    t.join(); // espera o quantum terminar
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Se ainda resta tempo, retorna o processo à fila
                if (processoAtual.getEstado() != Processo.Estado.FINALIZADO) {
                    fila.add(processoAtual);
                }
            }

            // Log visual do estado da fila
            mostrarFila(fila);
        }

        System.out.println("\nEscalonamento ROUND ROBIN concluído.\n");
    }

    // Mostra a fila atual
    private void mostrarFila(Queue<Processo> fila) {
        System.out.print("Fila atual: [ ");
        for (Processo p : fila) {
            System.out.print("P" + p.getIdProcesso() + " ");
        }
        System.out.println("]");
    }

    // Aguarda até que todos os processos estejam finalizados
    private void aguardarConclusao() {
        boolean todosFinalizados;
        do {
            todosFinalizados = listaProcessos.stream()
                    .allMatch(p -> p.getEstado() == Processo.Estado.FINALIZADO);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!todosFinalizados);
    }
}
