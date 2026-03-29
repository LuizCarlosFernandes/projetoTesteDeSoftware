package st.project;

import javax.swing.*;
import java.awt.*;

public class GameGUI extends JFrame {
    private JLabel imageLabel;
    private JTextArea pathArea;
    private Game game;

    public GameGUI(Game game) {
        super("World of Zuul - Missão Gráfica");
        this.game = game;

        // 1. Configuração da Imagem
        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(500, 300));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // 2. Configuração do Texto (Trajeto)
        pathArea = new JTextArea(10, 50);
        pathArea.setEditable(false);
        pathArea.setLineWrap(true);
        pathArea.setWrapStyleWord(true);

        // CRIE APENAS UM SCROLLPANE
        JScrollPane scrollPane = new JScrollPane(pathArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // 3. Painel de Navegação (Setas)
        JPanel navPanel = new JPanel(new GridLayout(3, 3, 5, 5));
        JButton btnNorth = new JButton("↑");
        JButton btnSouth = new JButton("↓");
        JButton btnEast = new JButton("→");
        JButton btnWest = new JButton("←");

        navPanel.add(new JLabel("")); navPanel.add(btnNorth); navPanel.add(new JLabel(""));
        navPanel.add(btnWest);        navPanel.add(new JLabel("MOVE", SwingConstants.CENTER)); navPanel.add(btnEast);
        navPanel.add(new JLabel("")); navPanel.add(btnSouth); navPanel.add(new JLabel(""));

        // Listeners corrigidos para as direções que o Room.java entende
        btnNorth.addActionListener(e -> this.game.processDirection("cima"));
        btnSouth.addActionListener(e -> this.game.processDirection("baixo"));
        btnEast.addActionListener(e -> this.game.processDirection("direita"));
        btnWest.addActionListener(e -> this.game.processDirection("esquerda"));

        // 4. Montagem do Layout
        setLayout(new BorderLayout(10, 10));
        add(imageLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER); // Adiciona o scroll que contém o pathArea
        add(navPanel, BorderLayout.EAST);

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void updatePath(String message) {
        pathArea.append("> " + message + "\n\n");
        pathArea.setCaretPosition(pathArea.getDocument().getLength());
    }

    public void updateImage(String imagePath) {
        try {
            ImageIcon icon = new ImageIcon(imagePath);
            Image img = icon.getImage();
            Image newImg = img.getScaledInstance(500, 300, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(newImg));
        } catch (Exception e) {
            updatePath("Erro ao carregar imagem: " + imagePath);
        }
    }
}