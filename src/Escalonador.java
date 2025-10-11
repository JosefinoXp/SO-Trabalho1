import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Escalonador {
    private List<Processo> listaProcessos;
    private int quantum; // tempo de fatia (em ms)
    private Algoritmo algoritmoSelecionado;

    public enum Algoritmo {
        PRIORIDADE,
        ROUND_ROBIN
    }

    public Escalonador(Algoritmo algoritmo, int quantum) {
        this.listaProcessos = new ArrayList<>();
        this.algoritmoSelecionado = algoritmo;
        this.quantum = quantum;
    }

    public void adicionarProcesso(Processo processo) {
        listaProcessos.add(processo);
    }

    public void escalonar() {
        switch (algoritmoSelecionado) {
            case PRIORIDADE:
                escalonarPorPrioridade();
                break;
            case ROUND_ROBIN:
                escalonarRoundRobin();
                break;
        }
    }

    // ======================
    // Escalonamento por Prioridade
    // ======================
    private void escalonarPorPrioridade() {
        System.out.println("Iniciando escalonamento por PRIORIDADE...\n");

        while (!listaProcessos.isEmpty()) {
            // Ordena do maior para o menor valor de prioridade
            Collections.sort(listaProcessos,
                    (p1, p2) -> Integer.compare(p2.getPrioridade(), p1.getPrioridade()));

            // Seleciona e remove o primeiro processo
            Processo processoSelecionado = listaProcessos.remove(0);
            processoSelecionado.pronto();

            if (processoSelecionado.getEstado() == Processo.Estado.PRONTO) {
                processoSelecionado.start(); // inicia execução total
                try {
                    processoSelecionado.join(); // aguarda término da thread
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Escalonamento por prioridade concluído.\n");
    }

    // ======================
    // Escalonamento Round Robin
    // ======================
    private void escalonarRoundRobin() {
        System.out.println("Iniciando escalonamento ROUND ROBIN...\n");

        Queue<Processo> fila = new LinkedList<>(listaProcessos);

        while (!fila.isEmpty()) {
            Processo processoAtual = fila.poll();

            if (processoAtual.getEstado() == Processo.Estado.SUSPENSO
                    || processoAtual.getEstado() == Processo.Estado.PRONTO) {

                processoAtual.pronto();
                processoAtual.executarQuantum(quantum);

                // Verifica se o processo terminou
                if (processoAtual.getTempoExecutado() < processoAtual.getTempoExecucao()) {
                    fila.add(processoAtual); // retorna à fila
                }
            }
        }

        System.out.println("Escalonamento Round Robin concluído.\n");
    }
}
