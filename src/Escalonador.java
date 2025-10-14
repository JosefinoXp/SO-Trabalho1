import java.util.*;
import java.util.function.Consumer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class Escalonador {
    private List<Processo> listaProcessos;
    private int quantum;
    private Algoritmo algoritmoSelecionado;
    private int trocasContexto;
    private long inicioExecucao, fimExecucao;

    // Custos e métricas adicionais
    private final int overheadTrocaMs;      // custo por troca de contexto (em ms)
    private long tempoOverheadTotalMs = 0;  // total acumulado
    private long tempoOciosoTotalMs = 0;    // reservado p/ futuras chegadas escalonadas

    // Medição de CPU real do ESCALONADOR (thread atual)
    private final ThreadMXBean medidor;
    private long cpuTotalNs = 0L; // CPU “de verdade” gasta executando fatias dos processos

    public enum Algoritmo { PRIORIDADE, ROUND_ROBIN }

    public interface EscalonadorCallback {
        void onLog(String message);
        void onProcessoIniciado(Processo p);
        void onProcessoFinalizado(Processo p);
        void onConcluido();
    }

    private EscalonadorCallback callback;

    // Construtor padrão com overhead default de 5ms
    public Escalonador(Algoritmo algoritmo, int quantum, List<Processo> processos, EscalonadorCallback callback) {
        this(algoritmo, quantum, processos, callback, 5);
    }

    // Construtor com overhead customizável
    public Escalonador(Algoritmo algoritmo, int quantum, List<Processo> processos,
                       EscalonadorCallback callback, int overheadTrocaMs) {
        this.algoritmoSelecionado = algoritmo;
        this.quantum = quantum;
        this.listaProcessos = new ArrayList<>(processos);
        this.callback = callback;
        this.trocasContexto = 0;
        this.overheadTrocaMs = Math.max(0, overheadTrocaMs);

        this.medidor = ManagementFactory.getThreadMXBean();
        if (medidor.isThreadCpuTimeSupported() && !medidor.isThreadCpuTimeEnabled()) {
            medidor.setThreadCpuTimeEnabled(true);
        }
    }

    /**
     * Função do método: Inicia o escalonamento dos processos conforme o algoritmo selecionado.
     * Entrada: Nenhuma
     * Saída: Nenhuma (executa escalonamento)
     */
    public void escalonar() {
        inicioExecucao = System.currentTimeMillis();
        callback.onLog(String.format(
                "INICIANDO ESCALONAMENTO COM %s (Quantum: %dms, Overhead: %dms)\n",
                algoritmoSelecionado, quantum, overheadTrocaMs
        ));

        switch (algoritmoSelecionado) {
            case PRIORIDADE -> escalonarPorPrioridade();
            case ROUND_ROBIN -> escalonarRoundRobin();
        }

        fimExecucao = System.currentTimeMillis();
        callback.onConcluido();
    }

    /**
     * Função do método: Executa o escalonamento por prioridade.
     * Entrada: Nenhuma
     * Saída: Nenhuma (executa escalonamento)
     */
    private void escalonarPorPrioridade() {
        listaProcessos.sort(Comparator.comparingInt(Processo::getPrioridade).reversed());
        for (int i = 0; i < listaProcessos.size(); i++) {
            Processo p = listaProcessos.get(i);
            executarProcesso(p, p.getTempoExecucao());
            if (i < listaProcessos.size() - 1) aplicarOverheadTroca();
        }
    }

    /**
     * Função do método: Executa o escalonamento Round Robin.
     * Entrada: Nenhuma
     * Saída: Nenhuma (executa escalonamento)
     */
    private void escalonarRoundRobin() {
        Queue<Processo> fila = new LinkedList<>(listaProcessos);
        while (!fila.isEmpty()) {
            Processo atual = fila.poll();
            if (atual.getEstado() != Processo.Estado.FINALIZADO) {
                executarProcesso(atual, quantum);

                // overhead entre fatias (sempre que há uma decisão de troca)
                if (atual.getEstado() != Processo.Estado.FINALIZADO || !fila.isEmpty()) {
                    aplicarOverheadTroca();
                }
                if (atual.getEstado() != Processo.Estado.FINALIZADO) {
                    fila.add(atual);
                }
            }
        }
    }

    /**
     * Função do método: Executa uma fatia de tempo de um processo.
     * Entrada: Processo, tempo de execução
     * Saída: Nenhuma (atualiza estado do processo)
     */
    private void executarProcesso(Processo p, int tempoExecucao) {
        p.pronto();
        callback.onLog(String.format("[P%d] PRONTO | Prioridade: %d", p.getIdProcesso(), p.getPrioridade()));
        callback.onProcessoIniciado(p);

        int antes = p.getTempoExecutado();
        long cpuNs = p.executar(tempoExecucao, medidor); // mede CPU real
        cpuTotalNs += Math.max(0L, cpuNs);

        int executado = p.getTempoExecutado() - antes;
        trocasContexto++;

        callback.onLog(String.format(
                "[P%d] EXECUTOU por %dms | Total: %d/%dms",
                p.getIdProcesso(), executado, p.getTempoExecutado(), p.getTempoExecucao()
        ));

        if (p.getEstado() == Processo.Estado.FINALIZADO) {
            callback.onLog(String.format("[P%d] FINALIZADO\n", p.getIdProcesso()));
            callback.onProcessoFinalizado(p);
        } else {
            callback.onLog(String.format("[P%d] SUSPENSO", p.getIdProcesso()));
        }
    }

    /**
     * Função do método: Aplica o overhead de troca de contexto.
     * Entrada: Nenhuma
     * Saída: Nenhuma (atualiza métricas)
     */
    private void aplicarOverheadTroca() {
        if (overheadTrocaMs <= 0) return;
        try {
            long inicio = System.currentTimeMillis();
            Thread.sleep(overheadTrocaMs);
            long fim = System.currentTimeMillis();
            tempoOverheadTotalMs += (fim - inicio);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Getters de métricas
    /**
     * Função do método: Retorna o tempo total de execução da simulação.
     * Entrada: Nenhuma
     * Saída: Tempo total em ms
     */
    public double getTempoTotal() { return fimExecucao - inicioExecucao; }
    /**
     * Função do método: Retorna o número de trocas de contexto realizadas.
     * Entrada: Nenhuma
     * Saída: Número de trocas de contexto
     */
    public int getTrocasContexto() { return trocasContexto; }
    /**
     * Função do método: Retorna a lista de processos gerenciados.
     * Entrada: Nenhuma
     * Saída: Lista de processos
     */
    public List<Processo> getProcessos() { return listaProcessos; }

    /**
     * Função do método: Retorna o tempo total de overhead de trocas de contexto.
     * Entrada: Nenhuma
     * Saída: Tempo de overhead em ms
     */
    public long getTempoOverheadTotalMs() { return tempoOverheadTotalMs; }
    /**
     * Função do método: Retorna o tempo total ocioso reservado para futuras chegadas.
     * Entrada: Nenhuma
     * Saída: Tempo ocioso em ms
     */
    public long getTempoOciosoTotalMs() { return tempoOciosoTotalMs; }

    /** CPU real total gasta executando as "partes CPU" das fatias (em nanos) */
    /**
     * Função do método: Retorna o tempo total de CPU real gasto pelo escalonador.
     * Entrada: Nenhuma
     * Saída: Tempo de CPU em nanossegundos
     */
    public long getCpuTotalNs() { return cpuTotalNs; }
}
