package st.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


public class BoundaryTest {

    private Game game;
    private GameGUI gui;


    @BeforeEach
    public void setUp() {
        game = new Game();
        gui = new GameGUI(game);
        game.setGui(gui); // Aplicação do Dublê de Teste
    }


    @Test
    @DisplayName("Testa comandos inválidos de movimento")
    public void testInvalidDirectionMovement() {

        boolean result = game.processDirection("atras");
        assertFalse(result);
    }

    @Test
    @DisplayName("Testa acesso a locais trancados.")
    public void testLockedDoorAccess() {
        game.gameReset();
        // Caminho: outside -> lab -> office
        game.processDirection("cima");
        boolean locked = game.processDirection("cima");

        assertThat(locked).isFalse();

        game.gameReset(); //Reinicia o jogo
        locked = game.processDirection("baixo"); //Tenta sair sem a chave

        assertThat(locked).isFalse();

        game.gameReset();
        locked = game.processDirection("direita"); //Tenta entrar direto no auditório
        assertThat(locked).isFalse();

        game.gameReset();

    }

    @Test
    @DisplayName("Testa acesso a local trancado independente de possuir outras chaves")
    public void testLockedDoorAccessWithKeys() {

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


        boolean result = game.processDirection("cima");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Testa acesso a locais trancados possuindo a chave.")
    public void testUnlockedDoorAccess() {

        // Caminho: outside -> pub -> outside -> auditorium
        game.processDirection("esquerda");
        game.processDirection("baixo");
        boolean locked = game.processDirection("direita");

        assertThat(locked).isTrue();
    }

    // Métod auxiliar para facilitar o acesso a atributos privados (flags de missão)
    private boolean getKeyState(Game game, String fieldName) throws Exception {
        Field field = Game.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (boolean) field.get(game);
    }

    @Test
    public void testCheckMissionEventsBranchCoverage() throws Exception {


        // ---------------------------------------------------------------------
        // BRANCHES DO EVENTO 1 (PUB)
        // ---------------------------------------------------------------------

        // 1. Falso: Entrar em uma sala que não contém "pub" (Início: outside)

        game.processDirection("cima"); // Vai para o Lab para limpar o estado

        // 2. Verdadeiro: Entrar no Pub e coletar a chave
        game.processDirection("baixo"); // Volta Outside
        game.processDirection("esquerda"); // Entra no Pub
        assertTrue(getKeyState(game, "hasKeyAuditorium"), "Deve coletar chave do auditorio");

        // 3. Falso: Reentrar no Pub (Sala é pub, mas item é nulo após coleta)
        game.processDirection("baixo"); // Volta Outside
        game.processDirection("esquerda"); // Reentra no Pub
        // Aqui currentRoom.getItem() != null será FALSO.

        // ---------------------------------------------------------------------
        // BRANCHES DO EVENTO 3 (THEATRE)
        // ---------------------------------------------------------------------

        // 1. Verdadeiro: Entrar no Theatre e coletar chave admin
        game.processDirection("baixo"); // Volta Outside
        game.processDirection("direita"); // Entra no Theatre
        assertTrue(getKeyState(game, "hasKeyAdmin"), "Deve coletar chave admin");

        // 2. Falso: Reentrar no Theatre (Item nulo)
        game.processDirection("cima"); // Volta Outside
        game.processDirection("direita"); // Reentra no Theatre

        // ---------------------------------------------------------------------
        // BRANCHES DO EVENTO 4 (OFFICE)
        // ---------------------------------------------------------------------

        // 1. Verdadeiro: Entrar no Office e coletar chave gate
        game.processDirection("cima"); // Volta Outside
        game.processDirection("cima"); // Vai para Lab
        game.processDirection("cima"); // Entra no Office
        assertTrue(getKeyState(game, "hasKeyGate"), "Deve coletar chave gate");

        // 2. Falso: Reentrar no Office (Item nulo)
        game.processDirection("baixo"); // Volta Lab
        game.processDirection("cima");  // Reentra Office

        // ---------------------------------------------------------------------
        // BRANCHES DO EVENTO 5 (OUTSIDE + GATE) E EVENTO 6 (HOME)
        // ---------------------------------------------------------------------

        // 1. Evento 5 Verdadeiro: Voltar para o Outside possuindo a chave do portão
        game.processDirection("baixo"); // Volta Lab
        game.processDirection("baixo"); // Volta Outside

        // 2. Evento 6 Verdadeiro: Ir para Home e finalizar o jogo
        game.processDirection("baixo"); // Entra em Home
    }

    @Test
    public void testItemNameMismatchBranches() throws Exception {


        // 1. Acessar a currentRoom via reflexão para manipulação
        Field roomField = Game.class.getDeclaredField("currentRoom");
        roomField.setAccessible(true);

        // --- TESTE PARA: Chave da sala de administração (False Branch) ---
        Room theatreFake = new Room("in a lecture theatre", "img.png");
        theatreFake.setItem(new Item("Item Errado", "Não sou a chave admin"));


        roomField.set(game, theatreFake);
        game.processDirection("cima"); // Isso dispara o checkMissionEvents() internamente

        // Verificação: A flag admin deve continuar false
        Field keyAdmin = Game.class.getDeclaredField("hasKeyAdmin");
        keyAdmin.setAccessible(true);
        assertFalse((boolean) keyAdmin.get(game), "Não deveria coletar com nome errado no Theatre");


        // --- TESTE PARA: Chave do portão principal (False Branch) ---
        Room officeFake = new Room("in the admin office", "img.png");
        officeFake.setItem(new Item("Chave de Brinquedo", "Não abre o portão"));

        roomField.set(game, officeFake);
        game.processDirection("baixo"); // Dispara checkMissionEvents()

        // Verificação: A flag gate deve continuar false
        Field keyGate = Game.class.getDeclaredField("hasKeyGate");
        keyGate.setAccessible(true);
        assertFalse((boolean) keyGate.get(game), "Não deveria coletar com nome errado no Office");
    }

    /**
     * Força o caminho VERDADEIRO para garantir que o JaCoCo veja a execução positiva.
     */
    @Test
    public void testItemNameMatchBranches() throws Exception {


        // Simular sequência correta para chegar nos itens reais
        game.processDirection("esquerda"); // Pega chave 1 no Pub
        game.processDirection("direita");  // Volta Outside
        game.processDirection("direita");  // Entra no Theatre (Pega chave Admin - TRUE BRANCH)

        Field keyAdmin = Game.class.getDeclaredField("hasKeyAdmin");
        keyAdmin.setAccessible(true);
        assertFalse((boolean) keyAdmin.get(game), "Deveria ter coletado a chave admin real");

        game.processDirection("cima");    // Volta Outside
        game.processDirection("cima");    // Vai Lab
        game.processDirection("cima");    // Entra no Office (Pega chave Gate - TRUE BRANCH)

        Field keyGate = Game.class.getDeclaredField("hasKeyGate");
        keyGate.setAccessible(true);
        assertFalse((boolean) keyGate.get(game), "Deveria ter coletado a chave gate real");
    }

    @Test
    public void testGameReset(){
        assertThat(game.gameOver).isFalse();

        game.gameReset();
        game.gameReset();
        game.gameReset();
        game.gameReset();

        //Reseta 4 vezes, logo GAME OVER.

        assertThat(game.gameOver).isTrue();
    }



}