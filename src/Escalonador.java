import java.util.*;
import java.util.function.Consumer;

public class Escalonador {
    private List<Processo> listaProcessos;
    private int quantum;
    private Algoritmo algoritmoSelecionado;
    private int trocasContexto;
    private long inicioExecucao, fimExecucao;

    // Callback para notificar a GUI
    private Consumer<String> logCallback;
    private Consumer<Processo> processoAtualCallback;

    public enum Algoritmo { PRIORIDADE, ROUND_ROBIN }

    public interface EscalonadorCallback {
        void onLog(String message);
        void onProcessoIniciado(Processo p);
        void onProcessoFinalizado(Processo p);
        void onConcluido();
    }
    private EscalonadorCallback callback;


    public Escalonador(Algoritmo algoritmo, int quantum, List<Processo> processos, EscalonadorCallback callback) {
        this.algoritmoSelecionado = algoritmo;
        this.quantum = quantum;
        this.listaProcessos = new ArrayList<>(processos);
        this.callback = callback;
        this.trocasContexto = 0;
    }

    public void escalonar() {
        inicioExecucao = System.currentTimeMillis();
        callback.onLog(String.format("INICIANDO ESCALONAMENTO COM %s (Quantum: %dms)\n", algoritmoSelecionado, quantum));

        switch (algoritmoSelecionado) {
            case PRIORIDADE -> escalonarPorPrioridade();
            case ROUND_ROBIN -> escalonarRoundRobin();
        }

        fimExecucao = System.currentTimeMillis();
        callback.onConcluido();
    }

    private void escalonarPorPrioridade() {
        listaProcessos.sort(Comparator.comparingInt(Processo::getPrioridade).reversed());

        for (Processo p : listaProcessos) {
            executarProcesso(p, p.getTempoExecucao());
        }
    }

    private void escalonarRoundRobin() {
        Queue<Processo> fila = new LinkedList<>(listaProcessos);

        while (!fila.isEmpty()) {
            Processo atual = fila.poll();
            if (atual.getEstado() != Processo.Estado.FINALIZADO) {
                executarProcesso(atual, quantum);

                if (atual.getEstado() != Processo.Estado.FINALIZADO) {
                    fila.add(atual);
                }
            }
        }
    }

    private void executarProcesso(Processo p, int tempoExecucao) {
        p.pronto();
        callback.onLog(String.format("[P%d] PRONTO | Prioridade: %d", p.getIdProcesso(), p.getPrioridade()));
        callback.onProcessoIniciado(p);

        int tempoExecutadoAntes = p.getTempoExecutado();
        p.executar(tempoExecucao);
        int tempoExecutadoAgora = p.getTempoExecutado() - tempoExecutadoAntes;
        trocasContexto++;

        callback.onLog(String.format("[P%d] EXECUTOU por %dms | Total: %d/%dms",
                p.getIdProcesso(), tempoExecutadoAgora, p.getTempoExecutado(), p.getTempoExecucao()));

        if (p.getEstado() == Processo.Estado.FINALIZADO) {
            callback.onLog(String.format("[P%d] FINALIZADO\n", p.getIdProcesso()));
            callback.onProcessoFinalizado(p);
        } else {
            callback.onLog(String.format("[P%d] SUSPENSO", p.getIdProcesso()));
        }
    }

    // Getters
    public double getTempoTotal() { return fimExecucao - inicioExecucao; }
    public int getTrocasContexto() { return trocasContexto; }
    public List<Processo> getProcessos() { return listaProcessos; }
}