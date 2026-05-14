package st.project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;




public class StructuralTest {

    Game game = new Game();
    GameGUI gui = new GameGUI(game);


    @Test
    @DisplayName("Testa funcionamento das saidas")
    public void testRoomExitMapping() {
        Room salaA = new Room("sala A", "imgA.png");
        Room salaB = new Room("sala B", "imgB.png");
        salaA.setExit("norte", salaB);

        assertThat(salaB).isEqualTo(salaA.getExit("norte"));

        assertThat(salaA.getExit("sul")).isNull();

    }

    @Test
    @DisplayName("Testa gerenciamento de itens")
    public void testItemManagement() {
        Room sala = new Room("laboratorio", "lab.png");
        Item chave = new Item("Chave", "Uma chave antiga");
        sala.setItem(chave);

        //Verifica se item foi colocado corretamente
        assertThat(sala.getItem().getNome()).isEqualTo("Chave");

        //Verifica coleta de item
        Item coletado = sala.takeItem();
        assertThat(coletado.getNome()).isEqualTo("Chave");

        //Verifica se item sumiu da sala
        assertThat(sala.getItem()).isNull();
    }

    @Test
    @DisplayName("Testa reconhecimento de comandos")
    public void testCommandRecognition() {
        CommandWords commands = new CommandWords();

        assertThat(CommandWord.GO).isEqualTo(commands.getCommandWord("go"));

        assertThat(CommandWord.UNKNOWN).isEqualTo(commands.getCommandWord("teste"));
    }



    //FEITO POR AI
    @Test
    public void testButtonNorthClickMovesUp() {
        // 1. Localizar o botão de "cima" (Norte)
        // Como os botões são locais no construtor de GameGUI, o ideal seria
        // torná-los atributos da classe GameGUI para facilitar o teste.
        // Se eles forem privados na classe, use o nome da variável:

        JButton btnNorth = findButtonByText(gui, "↑");
        JButton btnSouth = findButtonByText(gui, "↓");
        JButton btnEast = findButtonByText(gui, "→");
        JButton btnWest = findButtonByText(gui, "←");

        assertNotNull(btnNorth);
        assertNotNull(btnSouth);
        assertNotNull(btnEast);
        assertNotNull(btnWest);

        // 2. Simular o clique
        btnNorth.doClick();
        assertThat(game.currentRoom.getShortDescription()).contains("lab");

        btnSouth.doClick();
        assertThat(game.currentRoom.getShortDescription()).contains("outside");

        btnEast.doClick();
        assertThat(game.currentRoom.getShortDescription()).contains("outside");
        //Continua outside pois não consegue entrar no auditorium por não possuir a chave

        btnWest.doClick();
        assertThat(game.currentRoom.getShortDescription()).contains("pub");


    }

    /**
     * Método auxiliar para encontrar botões dentro do JFrame pelo texto (ActionCommand)
     */
    private JButton findButtonByText(GameGUI gui, String text) {
        for (java.awt.Component comp : gui.getContentPane().getComponents()) {
            if (comp instanceof javax.swing.JPanel) {
                for (java.awt.Component subComp : ((javax.swing.JPanel) comp).getComponents()) {
                    if (subComp instanceof JButton) {
                        JButton btn = (JButton) subComp;
                        if (btn.getText().equals(text)) return btn;
                    }
                }
            }
        }
        return null;
    }

    @Test
    @DisplayName("Testa processamento de pontuação do jogo")
    public void testScores() {
        Game game = new Game();
        assertThat(game.processScorePhase1(16)).isEqualTo(0);

        game.finished = true;
        assertThat(game.processScorePhase1(9)).isEqualTo(10); //Nota máxima
        assertThat(game.processScorePhase1(11)).isEqualTo(7);
        assertThat(game.processScorePhase1(13)).isEqualTo(5);
        assertThat(game.processScorePhase1(14)).isEqualTo(0);
    }

}