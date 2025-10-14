import java.util.*;
import java.util.function.Consumer;

public class Escalonador {
    private List<Processo> listaProcessos;
    private int quantum;
    private Algoritmo algoritmoSelecionado;
    private Consumer<String> outputHandler = System.out::println;

    public enum Algoritmo { PRIORIDADE, ROUND_ROBIN }

    public Escalonador(Algoritmo algoritmo, int quantum) {
        this.listaProcessos = new ArrayList<>();
        this.algoritmoSelecionado = algoritmo;
        this.quantum = Math.max(quantum, 1);
    }

    public void setOutputHandler(Consumer<String> handler) {
        this.outputHandler = handler;
    }

    public void adicionarProcesso(Processo processo) {
        listaProcessos.add(processo);
    }

    public boolean listaVazia() {
        return listaProcessos.isEmpty();
    }

    public synchronized void escalonar() {
        switch (algoritmoSelecionado) {
            case PRIORIDADE -> escalonarPorPrioridade();
            case ROUND_ROBIN -> escalonarRoundRobin();
        }
    }

    private void escalonarPorPrioridade() {
        outputHandler.accept("\n===== ESCALONAMENTO POR PRIORIDADE =====\n");
        listaProcessos.sort((p1, p2) -> Integer.compare(p2.getPrioridade(), p1.getPrioridade()));

        for (Processo p : listaProcessos) {
            p.setOutputHandler(outputHandler);
            p.pronto();

            Thread t = new Thread(() -> p.executarQuantum(p.getTempoExecucao()));
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        aguardarConclusao();
        outputHandler.accept("Escalonamento por PRIORIDADE concluído.\n");
    }

    private void escalonarRoundRobin() {
        outputHandler.accept("\n===== ESCALONAMENTO ROUND ROBIN =====\n");
        Queue<Processo> fila = new LinkedList<>(listaProcessos);

        while (!fila.isEmpty()) {
            Processo processoAtual = fila.poll();

            if (processoAtual.getEstado() != Processo.Estado.FINALIZADO) {
                processoAtual.setOutputHandler(outputHandler);
                processoAtual.pronto();

                Thread t = new Thread(() -> processoAtual.executarQuantum(quantum));
                t.start();

                try {
                    t.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                if (processoAtual.getEstado() != Processo.Estado.FINALIZADO) {
                    fila.add(processoAtual);
                }
            }
            mostrarFila(fila);
        }

        outputHandler.accept("\nEscalonamento ROUND ROBIN concluído.\n");
    }

    private void mostrarFila(Queue<Processo> fila) {
        StringBuilder sb = new StringBuilder("Fila atual: [ ");
        for (Processo p : fila) {
            sb.append("P").append(p.getIdProcesso()).append(" ");
        }
        sb.append("]");
        outputHandler.accept(sb.toString());
    }

    private void aguardarConclusao() {
        boolean todosFinalizados;
        do {
            todosFinalizados = listaProcessos.stream()
                    .allMatch(p -> p.getEstado() == Processo.Estado.FINALIZADO);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } while (!todosFinalizados);
    }
}
