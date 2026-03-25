package st.project;

import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        // Criar o frame (janela)
        JFrame frame = new JFrame("Minha Primeira Janela");

        // Configurar o tamanho
        frame.setSize(400, 300);

        // Configurar ação de fechamento (para encerrar o programa ao clicar no X)
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Tornar visível
        frame.setVisible(true);
    }
}
