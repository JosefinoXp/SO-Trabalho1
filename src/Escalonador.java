import java.util.*;

public class Escalonador {
    private List<Processo> listaProcessos;
    private int quantum;
    private Algoritmo algoritmoSelecionado;
    private int trocasContexto;
    private long inicioExecucao, fimExecucao;

    public enum Algoritmo { PRIORIDADE, ROUND_ROBIN }

    public Escalonador(Algoritmo algoritmo, int quantum) {
        this.listaProcessos = new ArrayList<>();
        this.algoritmoSelecionado = algoritmo;
        this.quantum = quantum;
        this.trocasContexto = 0;
    }

    public void adicionarProcesso(Processo p) {
        listaProcessos.add(p);
    }

    public synchronized void escalonar() {
        inicioExecucao = System.currentTimeMillis();

        System.out.println("\n==============================");
        System.out.println(" INICIANDO ESCALONAMENTO");
        System.out.println("==============================");
        System.out.println("Algoritmo: " + algoritmoSelecionado);
        System.out.println("Quantum: " + quantum + " ms");
        System.out.println("Processos carregados:");

        for (Processo p : listaProcessos) {
            System.out.printf(" - P%d | Prioridade: %d | Tempo Execucao: %d ms%n",
                    p.getIdProcesso(), p.getPrioridade(), p.getTempoExecucao());
        }
        System.out.println("--------------------------------\n");

        switch (algoritmoSelecionado) {
            case PRIORIDADE -> escalonarPorPrioridade();
            case ROUND_ROBIN -> escalonarRoundRobin();
        }

        fimExecucao = System.currentTimeMillis();

        System.out.println("\n========================================");
        System.out.println("  ESCALONAMENTO CONCLUIDO");
        System.out.println("========================================");
        System.out.printf("Tempo total: %.2f s%n", (fimExecucao - inicioExecucao) / 1000.0);
        System.out.println("Trocas de contexto: " + trocasContexto);
        System.out.println("Processos finalizados:");
        for (Processo p : listaProcessos) {
            System.out.printf(" - P%d | Executado: %d/%d ms | Estado final: %s%n",
                    p.getIdProcesso(), p.getTempoExecutado(), p.getTempoExecucao(), p.getEstado());
        }
        System.out.println("========================================\n");
    }

    private void escalonarPorPrioridade() {
        System.out.println("=== ESCALONAMENTO POR PRIORIDADE ===");
        listaProcessos.sort((p1, p2) -> Integer.compare(p2.getPrioridade(), p1.getPrioridade()));

        for (Processo p : listaProcessos) {
            p.pronto();
            Thread t = new Thread(() -> p.executarQuantum(p.getTempoExecucao()));
            t.start();
            try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }
            trocasContexto++;
        }
    }

    private void escalonarRoundRobin() {
        System.out.println("=== ESCALONAMENTO ROUND ROBIN ===");
        Queue<Processo> fila = new LinkedList<>(listaProcessos);

        while (!fila.isEmpty()) {
            Processo atual = fila.poll();
            if (atual.getEstado() != Processo.Estado.FINALIZADO) {
                atual.pronto();
                Thread t = new Thread(() -> atual.executarQuantum(quantum));
                t.start();
                try { t.join(); } catch (InterruptedException e) { e.printStackTrace(); }
                trocasContexto++;
                if (atual.getEstado() != Processo.Estado.FINALIZADO)
                    fila.add(atual);
            }
        }
    }

    public double getTempoTotal() { return fimExecucao - inicioExecucao; }
    public int getTrocasContexto() { return trocasContexto; }
    public List<Processo> getProcessos() { return listaProcessos; }
}
