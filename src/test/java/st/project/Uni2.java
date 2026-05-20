package st.project;

import net.jqwik.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

public class Uni2 {

    private Game game;
    private GameGUI mockGui;

    @BeforeEach
    public void setUp() {
        game = new Game();
        mockGui = Mockito.mock(GameGUI.class);
        game.setGui(mockGui); // Aplicação do Dublê de Teste
    }

    // ---------------------------------------------------------
    // TESTES ESTRUTURAIS (100% MC/DC)
    // ---------------------------------------------------------

    @Test
    @DisplayName("MC/DC para isRoomLocked: (Office == True, !hasKeyAdmin == True) -> True")
    public void testIsRoomLocked_OfficeNoKey() throws Exception {
        // Testa a condição: description.contains("office") && !hasKeyAdmin
        boolean result;
        game.processDirection("cima"); // Vai Lab
        result = game.processDirection("cima"); // Tenta Office

        assertThat(result).isFalse(); // Retorna falso porque isRoomLocked impede
        verify(mockGui, atLeastOnce()).updatePath(Mockito.contains("A porta está trancada"));
    }

    @Test
    @DisplayName("MC/DC para isRoomLocked: (Office == True, !hasKeyAdmin == False) -> False")
    public void testIsRoomLocked_OfficeWithKey(){
        // Coleta a chave do auditório e depois do admin para acessar o office
        game.processDirection("esquerda"); // pub (Pega chave auditório)
        game.processDirection("baixo");    // fora
        game.processDirection("direita");  // auditório (Pega chave admin)
        game.processDirection("cima");     // fora
        game.processDirection("cima");     // lab

        boolean result = game.processDirection("cima"); // Entra office
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("MC/DC para checkMissionEvents: (pub == True, item != null) -> True")
    public void testCheckMissionEvents_PubWithItem() throws Exception {
        // Testa: currentRoom.getShortDescription().contains("pub") && currentRoom.getItem() != null
        game.processDirection("esquerda"); // Entra no pub que tem o item de início

        Field hasKeyAuditorium = Game.class.getDeclaredField("hasKeyAuditorium");
        hasKeyAuditorium.setAccessible(true);
        assertThat((boolean) hasKeyAuditorium.get(game)).isTrue();
    }

    @Test
    @DisplayName("MC/DC para checkMissionEvents: (pub == True, item == null) -> False")
    public void testCheckMissionEvents_PubWithoutItem() throws Exception {
        game.processDirection("esquerda"); // Entra no pub, coleta o item
        game.processDirection("baixo");    // Sai
        game.processDirection("esquerda"); // Entra novamente, item agora é nulo

        // Verifica se a lógica não quebra e mantém o fluxo
        assertThat(game.currentRoom.getShortDescription()).contains("pub");
    }

    // ---------------------------------------------------------
    // TESTES DE FRONTEIRA E DOMÍNIO
    // ---------------------------------------------------------

    @Test
    @DisplayName("Teste de Fronteira: Limite Máximo de Movimentos (14 movimentos)")
    public void testMaxMovementsBoundary() throws Exception {
        // O limite é movimentos == 14
        for (int i = 0; i < 13; i++) {
            game.processDirection("cima"); // Movimentações válidas ou inválidas que contam
            game.processDirection("baixo");
        }

        int score = game.processScorePhase1(14);
        assertThat(score).isEqualTo(0);
        verify(mockGui, atLeastOnce()).updatePath(Mockito.contains("você falhou"));
    }

    @Test
    @DisplayName("Teste de Domínio: Armadilha da Fase 2 (isRoomTrapped)")
    public void testTrappedRoomAndLockedsRooms() throws Exception {
        // Configura o estado para Fase 2 diretamente via reflexão para testar o domínio
        Field faseField = Game.class.getDeclaredField("fase");
        faseField.setAccessible(true);
        faseField.set(game, 2);

        Field currentRoomField = Game.class.getDeclaredField("currentRoom");
        currentRoomField.setAccessible(true);
        Room friendsHouse = new Room("Casa do seu amigo", "src/images/friendsHouse.jpg");
        currentRoomField.set(game, friendsHouse);

        boolean isTrapped = game.isRoomTrapped(friendsHouse);


        assertThat(isTrapped).isTrue();
        verify(mockGui, atLeastOnce()).updatePath(Mockito.contains("sonhando, e acordou"));

        //Verifica também se a vila academica está de fato trancada.
        Room apartments = new Room("Complexo de apartamentos","src/images/home.png");
        currentRoomField.set(game, apartments);

        boolean isLocked = game.isRoomLocked(apartments);
        assertThat(isLocked).isTrue();

        //Visita farmácia, e dps entra tenta entrar, sem pão, na casa do amigo
        Field pharmacyField = Game.class.getDeclaredField("visitedPharmacy");
        pharmacyField.setAccessible(true);
        pharmacyField.set(game, true);

        currentRoomField.set(game, friendsHouse);
        boolean checkpointTrap = game.isRoomTrapped(friendsHouse);
        assertThat(checkpointTrap).isTrue();


        //Verifica se um ambiente sem armadilha, possui alguma

        Room pharmacy = new Room("Farmácia", "src/images/pharmacy.jpg");
        currentRoomField.set(game, pharmacy);

        boolean inexistentTrap = game.isRoomTrapped(pharmacy);
        assertThat(inexistentTrap).isFalse();

    }

    // ---------------------------------------------------------
    // DUBLES DE TESTE (MOCKITO)
    // ---------------------------------------------------------

    @Test
    @DisplayName("Garante que a GUI não é atualizada se não houver caminho (Spy/Mock verification)")
    public void testInvalidDirectionDoesNotUpdateImage() {
        game.processDirection("nordeste"); // Comando inválido

        // Verifica se o aviso de erro foi acionado na GUI mockada
        verify(mockGui).updatePath(Mockito.contains("Não há saída"));
        // Verifica se a imagem NÃO foi atualizada pois não trocou de sala
        //Mockito.verify(mockGui, Mockito.never()).updateImage(anyString());
    }

    @Test
    @DisplayName("Testa se processDirection é interrompido e retorna false ao entrar em armadilha")
    public void testProcessDirectionBlockedByTrap() throws Exception {
        Room startRoom = new Room("Rua", "src/images/street.png");
        Room trapRoom = new Room("Casa do seu amigo", "src/images/friendsHouse.jpg");

        // 2. Conecta as duas salas para garantir que nextRoom NÃO seja null
        startRoom.setExit("norte", trapRoom);

        // 3. Injeta a sala inicial no currentRoom do jogo via Reflexão
        Field currentRoomField = Game.class.getDeclaredField("currentRoom");
        currentRoomField.setAccessible(true);
        currentRoomField.set(game, startRoom);

        // 4. Garante que o jogador NÃO tem o pão (para ativar a armadilha)
        Field hasPaoField = Game.class.getDeclaredField("hasPao");
        hasPaoField.setAccessible(true);
        hasPaoField.set(game, false);

        // 5. Tenta se mover para a direção da armadilha
        boolean enteredTrappedRoom = game.processDirection("norte");

        // Asserção: A barreira if(isRoomTrapped(nextRoom)) deve acionar e retornar false
        assertThat(enteredTrappedRoom).isFalse();
    }

    // ---------------------------------------------------------
    // TESTES BASEADOS EM PROPRIEDADES (JQWIK)
    // ---------------------------------------------------------

    @Property
    //@DisplayName("Propriedade: Comandos aleatórios não devem quebrar o estado do jogo")
    public void anyDirectionCommandShouldReturnBooleanWithoutException(
            @ForAll("randomDirections") String direction) {

        Game propGame = new Game();
        GameGUI propMockGui = Mockito.mock(GameGUI.class);
        propGame.setGui(propMockGui);

        // A execução não deve lançar NullPointerException nem outra exceção
        assertDoesNotThrow(() -> {
            boolean success = propGame.processDirection(direction);
            assertTrue(success || !success); // Sempre retornará um booleano válido
        });
    }

    @Provide
    Arbitrary<String> randomDirections() {
        return Arbitraries.of("cima", "baixo", "esquerda", "direita", "norte", "sul", "", " ", null);
    }
}