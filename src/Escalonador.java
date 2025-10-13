import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

    public void adicionarProcesso(Processo processo) { listaProcessos.add(processo); }

    // Sincronizando escalonamento: evita múltiplos escalonadores agindo simultaneamente
    public synchronized void escalonar() {
        switch (algoritmoSelecionado) {
            case PRIORIDADE: escalonarPorPrioridade(); break;
            case ROUND_ROBIN: escalonarRoundRobin(); break;
        }
    }

    private void escalonarPorPrioridade() {
        System.out.println("Iniciando escalonamento por PRIORIDADE...\n");
        while (!listaProcessos.isEmpty()) {
            Collections.sort(listaProcessos, (p1, p2) -> Integer.compare(p2.getPrioridade(), p1.getPrioridade()));
            Processo processoSelecionado = listaProcessos.remove(0);
            processoSelecionado.pronto();

            Thread t = new Thread(processoSelecionado);
            t.start();
            try {
                t.join(); // aguarda o término de cada processo ANTES de iniciar o próximo
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Escalonamento por prioridade concluído.\n");
    }


    private void escalonarRoundRobin() {
        System.out.println("Iniciando escalonamento ROUND ROBIN...\n");
        Queue<Processo> fila = new LinkedList<>(listaProcessos);

        List<Thread> threads = new ArrayList<>();
        while (!fila.isEmpty()) {
            Processo processoAtual = fila.poll();
            if (processoAtual.getEstado() == Processo.Estado.SUSPENSO ||
                    processoAtual.getEstado() == Processo.Estado.PRONTO) {
                processoAtual.pronto();
                // Executa quantum em thread sincronizada
                Thread t = new Thread(() -> processoAtual.executarQuantum(quantum));
                threads.add(t);
                t.start();

                try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }

                if (processoAtual.getTempoExecutado() < processoAtual.getTempoExecucao()) {
                    fila.add(processoAtual);
                }
            }
        }
        // Já aguarda cada thread terminar nos joins acima.
        System.out.println("Escalonamento Round Robin concluído.\n");
    }
}
