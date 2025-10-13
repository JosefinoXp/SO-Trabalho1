public class Main {
    public static void main(String[] args) {
        // Escolha do algoritmo (troque para PRIORIDADE se quiser)
        Escalonador escalonador = new Escalonador(Escalonador.Algoritmo.ROUND_ROBIN, 1000);

        // Criação dos processos simulados
        escalonador.adicionarProcesso(new Processo(1, 3, 3000));
        escalonador.adicionarProcesso(new Processo(2, 1, 5000));
        escalonador.adicionarProcesso(new Processo(3, 2, 2500));
        escalonador.adicionarProcesso(new Processo(4, 6, 2500));

        // Executa o escalonamento
        escalonador.escalonar();
    }
}
