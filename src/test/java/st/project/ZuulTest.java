package st.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class ZuulTest {
    private Game game = new Game();

    /**
     * TESTES DE DOMÍNIO E FRONTEIRA (CommandWords & Parser)
     */
    @Test
    @DisplayName("Teste dominio: Validez dos comandos")
    public void testCommandValidity() {
        CommandWords cw = new CommandWords();
        // Fronteira: Comando válido
        assertThat(cw.isCommand("go")).isTrue();
        // Fronteira: Comando inválido
        assertThat(cw.isCommand("fly")).isFalse();
        // Fronteira: Case sensitivity (o jogo é case sensitive para comandos no HashMap)
        assertThat(cw.isCommand("GO")).isFalse();
    }

    @Test
    @DisplayName("Teste dominio: Lógica do parser")
    public void testParserLogic() {
        Parser parser = new Parser();
        // Domínio: Comando de uma palavra
        Command cmd = parser.parseString("help");
        assertEquals(CommandWord.HELP, cmd.getCommandWord());
        assertNull(cmd.getSecondWord());

        // Domínio: comando não existente
        cmd = parser.parseString("test");
        assertThat(cmd.isUnknown()).isTrue();

        // Domínio: Comando de duas palavras
        cmd = parser.parseString("go direita");
        assertEquals(CommandWord.GO, cmd.getCommandWord());
        assertThat(cmd.hasSecondWord()).isTrue();
        assertEquals("direita", cmd.getSecondWord());
    }

    @Test
    @DisplayName("Retorna lista de comandos")
    public void testReturnCommands() {
        //help, go, quit
        String commands = "help go quit ";

        CommandWords cw = new CommandWords();
        assertThat(cw.getCommandList()).isEqualTo(commands);


        assertThat(cw.showAll()).isEqualTo(3);
    }


    /**
     * TESTES ESTRUTURAIS (MC/DC - Lógica de Movimentação e Missão
     */

    @Test
    @DisplayName("Teste estrutural: indo para caminho inexistente")
    public void testInexistentPath() {
        // 1. Teste de Saída Inexistente (Condição: nextRoom == null)
        // Partindo do Outside, não há saída "cima" (lab está em cima, mas testaremos "norte")
        assertThat(game.processDirection("norte")).isFalse();
        // Verifica-se visualmente/log via mock se necessário, mas aqui validamos o fluxo
    }
    @Test
    @DisplayName("Teste estrutural: tentando entrar no auditorio antes de pegar a chave")
    public void testLockedAuditorium() {
        //Entrando no auditorio antes de entrar pegar a chave.
        assertThat(game.processDirection("direita")).isFalse();
        // 2. Teste de Bloqueio: Theatre sem chave (Condição: nextRoom.contains("theatre") && !hasKeyAuditorium)
    }
    @Test
    @DisplayName("Teste estrutural: Tentando entrar na sala de administração antes da chave")
    public void testLockedAdministration() {
        game.processDirection("cima");
        assertThat(game.processDirection("cima")).isFalse();
    }

    @Test
    @DisplayName("Teste estrutural: Tentando sair do campus antes de finalizar missão")
    public void testLockedGate() {
        assertThat(game.processDirection("baixo")).isFalse();
    }

    @Test
    @DisplayName("Teste estrutural: Pegar chave e ir para auditório")
    public void testOpenAuditorium() {
        game.processDirection("esquerda"); //Vai ao pub pegar a chave
        game.processDirection("baixo"); // Sai do pub
        assertThat(game.processDirection("direita")).isTrue(); //Tenta entrar no auditório
    }

    @Test
    @DisplayName("Teste estrutural: pegar chave e ir para sala de administração")
    public void testOpenAdministration() {
        game.processDirection("esquerda"); //Pega chave no pub
        game.processDirection("baixo"); //Sai do pub
        game.processDirection("direita"); //Busca chave com professor
        game.processDirection("cima"); //Volta ao campus
        game.processDirection("cima"); //Entra no laboratório de computação
        assertThat(game.processDirection("cima")).isTrue(); // tenta entrar na sala de administração
    }

    @Test
    @DisplayName("Teste estrutural: sair do campus")
    public void testOpenGate() {
        game.processDirection("esquerda");
        game.processDirection("baixo"); //Sai do pub
        game.processDirection("direita"); //Busca chave com professor
        game.processDirection("cima"); //Volta ao campus
        game.processDirection("cima"); //Entra no laboratório de computação
        game.processDirection("cima"); //Entra na sala de administração
        game.processDirection("baixo"); //Sai da sala de admin
        game.processDirection("baixo"); //Sai do lab de computação
        assertThat(game.processDirection("baixo")).isTrue(); // tenta sair do campus
    }

    /**
     * MC/DC: Teste de independência das condições de erro
     */
    @Test
    public void testSpecificLockMessages() {
        // Testa se o bloqueio do Office ocorre independentemente de outras chaves
        // Indo direto para Lab -> Office (sem passar pelo Theatre)
        game.processDirection("cima"); // Lab
        game.processDirection("cima"); // Office
        // Espera-se que a mensagem de bloqueio do Admin apareça (hasKeyAdmin é false)
    }
}