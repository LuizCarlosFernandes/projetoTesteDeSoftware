package st.project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameGUI extends JFrame {
    private JLabel imageLabel;       // Exibe a imagem da sala
    private JTextArea pathArea;      // Registro visual do trajeto
    private JTextField inputField;   // Entrada de comandos
    private Game game;

    public GameGUI(Game game) {
        super("World of Zuul - Missão Gráfica");
        this.game = game;

        // --- Painel Superior: Imagem da Sala ---
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setPreferredSize(new Dimension(500, 300));
        imageLabel.setBorder(BorderFactory.createEtchedBorder());

        // --- Painel Central: Log do Trajeto ---
        pathArea = new JTextArea(10, 50);
        pathArea.setEditable(false);
        pathArea.setLineWrap(true);
        pathArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(pathArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // --- Painel Inferior: Entrada de Comando ---
        inputField = new JTextField(35);
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String commandText = inputField.getText();
                inputField.setText("");
                game.processInputFromUI(commandText);
            }
        });
            // --- Montagem do Layout ---
        setLayout(new BorderLayout(10, 10));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(imageLabel, BorderLayout.NORTH);
        centerPanel.add(new JLabel("  Histórico do Trajeto e Eventos:"), BorderLayout.CENTER);
        centerPanel.add(scrollPane, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel();
        southPanel.add(new JLabel("Comando:"));
        southPanel.add(inputField);
        add(southPanel, BorderLayout.SOUTH);

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void printMessage(String message) {
        pathArea.append(message + "\n");
        pathArea.setCaretPosition(pathArea.getDocument().getLength());
    }

    public void updateImage(String imagePath) {
        // Tenta carregar a imagem. Certifique-se de que a pasta 'images' está no classpath.
        ImageIcon icon = new ImageIcon(imagePath);

        // Redimensiona a imagem para caber no label mantendo a proporção (opcional)
        Image img = icon.getImage();
        Image newImg = img.getScaledInstance(500, 300, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(newImg));
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