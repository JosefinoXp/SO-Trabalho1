import javax.swing.*;
import java.awt.*;

public class InterfaceGrafica extends JFrame {
    private JTextArea logArea;
    private JComboBox<String> comboAlgoritmo;
    private JButton btnIniciar;

    public InterfaceGrafica() {
        setTitle("Simulador de Escalonamento");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.add(new JLabel("Algoritmo: "));
        comboAlgoritmo = new JComboBox<>(new String[]{"ROUND_ROBIN", "PRIORIDADE"});
        top.add(comboAlgoritmo);
        btnIniciar = new JButton("Iniciar Simulação");
        top.add(btnIniciar);
        add(top, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(logArea);
        add(scroll, BorderLayout.CENTER);

        btnIniciar.addActionListener(e -> iniciarSimulacao());
    }

    private void iniciarSimulacao() {
        logArea.setText("");
        new Thread(() -> {
            try {
                Escalonador.Algoritmo algoritmo = comboAlgoritmo.getSelectedItem().toString().equals("ROUND_ROBIN")
                        ? Escalonador.Algoritmo.ROUND_ROBIN
                        : Escalonador.Algoritmo.PRIORIDADE;

                Escalonador esc = new Escalonador(algoritmo, 1000);
                Avaliador av = new Avaliador(esc);
                av.gerarCargaDeTrabalho();

                // Redireciona System.out também para o JTextArea
                System.setOut(new java.io.PrintStream(new java.io.OutputStream() {
                    @Override public void write(int b) {
                        logArea.append(String.valueOf((char)b));
                    }
                }, true));

                av.avaliar();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InterfaceGrafica().setVisible(true));
    }
}
