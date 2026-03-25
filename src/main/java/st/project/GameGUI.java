package st.project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameGUI extends JFrame {
    private JTextArea pathArea;
    private JTextField inputField;
    private Game game; // Referência para enviar comandos de volta

    public GameGUI(Game game) {
        super("World of Zuul - Graphical Edition");
        this.game = game;

        pathArea = new JTextArea(15, 40);
        pathArea.setEditable(false);
        inputField = new JTextField(30);

        // Ao apertar Enter no campo de texto
        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String commandText = inputField.getText();
                inputField.setText("");
                game.processInputFromUI(commandText); // Envia para o motor do jogo
            }
        });

        setLayout(new BorderLayout());
        add(new JScrollPane(pathArea), BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.add(new JLabel("Comando: "));
        southPanel.add(inputField);
        add(southPanel, BorderLayout.SOUTH);

        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void printMessage(String message) {
        pathArea.append(message + "\n");
        pathArea.setCaretPosition(pathArea.getDocument().getLength());
    }

    /**
     * Atualiza o log da interface com o trajeto e eventos do jogo.
     * @param message A mensagem ou descrição da sala a ser exibida.
     */
    public void updatePath(String message) {
        // Adiciona a nova mensagem ao final do histórico atual
        pathArea.append("> " + message + "\n\n");

        // Auto-scroll para garantir que o jogador veja o último comando
        pathArea.setCaretPosition(pathArea.getDocument().getLength());
    }
}