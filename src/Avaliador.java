import java.util.*;

public class Avaliador {
    private Escalonador escalonador;
    private int totalProcessos;
    private Random rand;

    public Avaliador(Escalonador escalonador) {
        this.escalonador = escalonador;
        this.totalProcessos = 5;
        this.rand = new Random(42); // seed fixa
    }

    public void gerarCargaDeTrabalho() {
        for (int i = 1; i <= totalProcessos; i++) {
            int prioridade = rand.nextInt(10) + 1;
            int tempoExec = (rand.nextInt(5) + 2) * 1000;
            escalonador.adicionarProcesso(new Processo(i, prioridade, tempoExec));
        }
    }

    public void avaliar() {
        long inicio = System.currentTimeMillis();
        escalonador.escalonar();
        long fim = System.currentTimeMillis();

        double tempoTotal = (fim - inicio);
        int trocas = escalonador.getTrocasContexto();
        double throughput = (double) totalProcessos / (tempoTotal / 1000);

        System.out.println("\n=== METRICAS ===");
        System.out.printf("Throughput: %.2f processos/s%n", throughput);
        System.out.println("Trocas de contexto: " + trocas);
        System.out.println("Tempo total: " + tempoTotal + " ms");
    }
}
