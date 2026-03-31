package st.project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


public class BoundaryTest {

    @Test
    @DisplayName("Testa comandos inválidos de movimento")
    public void testInvalidDirectionMovement() {
        Game game = new Game();

        boolean result = game.processDirection("atras");
        assertFalse(result);
    }

    @Test
    @DisplayName("Testa acesso a locais trancados.")
    public void testLockedDoorAccess() {
        Game game = new Game();

        // Caminho: outside -> lab -> office
        game.processDirection("cima");
        boolean locked = game.processDirection("cima");

        assertThat(locked).isFalse();

        game = new Game(); //Reinicia o jogo
        locked = game.processDirection("baixo"); //Tenta sair sem a chave

        assertThat(locked).isFalse();

        game = new Game();
        locked = game.processDirection("direita"); //Tenta entrar direto no auditório
        assertThat(locked).isFalse();
    }

    @Test
    @DisplayName("Testa acesso a local trancado independente de possuir outras chaves")
    public void testLockedDoorAccessWithKeys() {
        Game game = new Game();
        //Caminho: outside -> pub -> outside -> lab -> office
        game.processDirection("esquerda");
        game.processDirection("baixo");
        game.processDirection("cima");
        boolean locked  = game.processDirection("cima");

        assertThat(locked).isFalse();
    }

    @Test
    @DisplayName("Testa comandos válidos de movimento")
    public void testvalidDirectionMovement() {
        Game game = new Game();

        boolean result = game.processDirection("cima");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Testa acesso a locais trancados possuindo a chave.")
    public void testUnlockedDoorAccess() {
        Game game = new Game();
        // Caminho: outside -> pub -> outside -> auditorium
        game.processDirection("esquerda");
        game.processDirection("baixo");
        boolean locked = game.processDirection("direita");

        assertThat(locked).isTrue();


    }



}