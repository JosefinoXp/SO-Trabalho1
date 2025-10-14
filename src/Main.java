public class Main {
    public static void main(String[] args) {
        // Invoca a interface gráfica na Event Dispatch Thread (EDT) do Swing
        javax.swing.SwingUtilities.invokeLater(() ->
                new InterfaceGrafica().setVisible(true)
        );
    }
}