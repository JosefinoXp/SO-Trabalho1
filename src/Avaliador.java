import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Avaliador {
    private List<Processo> processosConcluidos;
    private double tempoTotalSimulacao; // em segundos
    private int trocasDeContexto;

    public Avaliador(List<Processo> processosConcluidos, double tempoTotalSimulacao, int trocasDeContexto) {
        this.processosConcluidos = processosConcluidos;
        this.tempoTotalSimulacao = tempoTotalSimulacao / 1000.0;
        this.trocasDeContexto = trocasDeContexto;
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
        return processosConcluidos.size() / tempoTotalSimulacao;
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

    public int getTrocasDeContexto() {
        return trocasDeContexto;
    }
}