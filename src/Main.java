public class Main {
    public static void main(String[] args) {
        // Escolha o algoritmo (ROUND_ROBIN ou PRIORIDADE)
        Escalonador.Algoritmo algoritmo = Escalonador.Algoritmo.PRIORIDADE;

        // Quantum de 1000 ms (1 segundo)
        int quantum = 1000;

        Escalonador escalonador = new Escalonador(algoritmo, quantum);

        // Criação dos processos simulados
        escalonador.adicionarProcesso(new Processo(1, 3, 3000));
        escalonador.adicionarProcesso(new Processo(2, 1, 5000));
        escalonador.adicionarProcesso(new Processo(3, 2, 2500));
        escalonador.adicionarProcesso(new Processo(4, 6, 2500));

        // Executa o escalonamento
        escalonador.escalonar();

        System.out.println("Simulação finalizada com sucesso!");
    }
}
