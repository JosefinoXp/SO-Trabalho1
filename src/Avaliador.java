import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Avaliador {
    private List<Processo> processosConcluidos;
    private double tempoTotalSimulacao; // em milissegundos
    private int trocasDeContexto;
    private long tempoTotalBurst; // Soma do tempo de execução de todos os processos

    public Avaliador(List<Processo> processosConcluidos, double tempoTotalSimulacao, int trocasDeContexto, long tempoTotalBurst) {
        this.processosConcluidos = processosConcluidos;
        this.tempoTotalSimulacao = tempoTotalSimulacao;
        this.trocasDeContexto = trocasDeContexto;
        this.tempoTotalBurst = tempoTotalBurst;
    }

    public static List<Processo> gerarCargaDeTrabalho(int totalProcessos) {
        Random rand = new Random(42); // Seed fixa para reprodutibilidade
        return java.util.stream.IntStream.rangeClosed(1, totalProcessos)
                .mapToObj(i -> {
                    int prioridade = rand.nextInt(10) + 1;
                    int tempoExec = (rand.nextInt(5) + 2) * 1000;
                    return new Processo(i, prioridade, tempoExec);
                })
                .collect(Collectors.toList());
    }

    public double getThroughput() {
        if (tempoTotalSimulacao == 0) return 0;
        return processosConcluidos.size() / (tempoTotalSimulacao / 1000.0);
    }

    public double getTempoMedioDeRetorno() {
        return processosConcluidos.stream()
                .mapToLong(Processo::getTempoDeRetorno)
                .average()
                .orElse(0.0);
    }

    public double getTempoMedioDeEspera() {
        return processosConcluidos.stream()
                .mapToLong(Processo::getTempoDeEspera)
                .average()
                .orElse(0.0);
    }
    public double getTempoMedioDeResposta() {
        return processosConcluidos.stream()
                .mapToLong(Processo::getTempoDeResposta)
                .average()
                .orElse(0.0);
    }

    public double getUtilizacaoCPU() {
        if (tempoTotalSimulacao == 0) return 0.0;
        return (double) tempoTotalBurst / tempoTotalSimulacao * 100.0;
    }

    public int getTrocasDeContexto() {
        return trocasDeContexto;
    }
}