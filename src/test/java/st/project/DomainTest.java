package st.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class DomainTest {

    private Game game;
    private GameGUI gui;


    @BeforeEach
    public void setUp() {
        game = new Game();
        gui = new GameGUI(game);
        game.setGui(gui); // Aplicação do Dublê de Teste
    }


    @Test
    @DisplayName("Testa progressão completa da missão do jogo")
    public void testFullMissionProgression() {


        // 1. Ir ao Pub pegar a primeira chave
        game.processDirection("esquerda");
        // Internamente, checkMissionEvents() deve ter setado hasKeyAuditorium como true

        // 2. Tentar entrar no Theatre (Auditório)
        game.processDirection("baixo"); // volta pro outside
        boolean enteredTheatre = game.processDirection("direita");

        assertThat(enteredTheatre).isTrue();

        // 3. Verificar se ao entrar no Theatre, pegou a chave do Admin
        game.processDirection("cima"); // volta pro outside
        game.processDirection("cima"); // vai pro lab
        boolean enteredOffice = game.processDirection("cima");

        assertThat(enteredOffice).isTrue();

        // 4. Sair do campus e ir embora.
        game.processDirection("baixo");
        game.processDirection("baixo");
        boolean openGate = game.processDirection("baixo");

        assertThat(openGate).isTrue();
    }



}