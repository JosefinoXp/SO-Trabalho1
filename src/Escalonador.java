import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Escalonador {
    //Lista de processos
    private List<Processo> listaProcessos;

    //Construtor da classe
    public Escalonador() {

        this.listaProcessos = new ArrayList<>();
    }

    //insere um processo na lista
    public void adicionarProcesso(Processo processo) {

        listaProcessos.add(processo);
    }

    //retorna a lista de processos
    public List<Processo> getListaProcessos() {

        return this.listaProcessos;
    }



    public void escalonar() {
        // Enquanto houver processos na lista.
        while (!listaProcessos.isEmpty()) {

            // Ordena a lista de processos por prioridade, do maior para o menor.
            Collections.sort(listaProcessos, (p1, p2) -> Integer.compare(p2.getPrioridade(), p1.getPrioridade()));

            // Remove o primeiro processo da lista ordenada.
            Processo processoSelecionado = listaProcessos.remove(0);
            processoSelecionado.pronto();

            if (processoSelecionado.getEstado() == Processo.Estado.PRONTO) {
                // Inicia a execução do processo.
                processoSelecionado.start();

                try {
                    //Sincroniza as threads, faz uma aguardar o término da outra, antes de iniciar.
                    processoSelecionado.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}