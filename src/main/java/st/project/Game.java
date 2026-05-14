package st.project;

import javax.swing.Timer;

/**
 *  This class is the main class of the "World of Zuul" application. 
 *  "World of Zuul" is a very simple, text based adventure game.  Users 
 *  can walk around some scenery. That's all. It should really be extended 
 *  to make it more interesting!
 * 
 *  To play this game, create an instance of this class and call the "play"
 *  method.
 * 
 *  This main class creates and initialises all the others: it creates all
 *  rooms, creates the parser and starts the game.  It also evaluates and
 *  executes the commands that the parser returns.
 * 
 * @author  Michael Kolling and David J. Barnes
 * @version 2008.03.30
 */

public class Game
{
    protected Room currentRoom;
    private GameGUI gui;

        //SCORE
    private int movimentos = 0;
    private int movimentosMaximos = 14;
    private Room failed = new Room("fail", "src/images/failed.png");
    public boolean finished = false;

        //VARIAVEIS DA MISSÃO
    private boolean hasKeyAdmin = false;
    private boolean hasKeyAuditorium = false;
    private boolean hasKeyGate =  false;


    /**
     * Create the game and initialise its internal map.
     */
    public Game()
    {
        // 1. Cria as salas primeiro
        createRooms();

        // 2. Inicializa a GUI
        gui = new GameGUI(this);

        printWelcome();
    }

    /**
     * Create all the rooms and link their exits together.
     */
    private void createRooms()
    {
        Room outside, theatre, pub, lab, office, home;

        // create the rooms
        outside = new Room("outside the main entrance", "src/images/outside.jpeg");
        theatre = new Room("in a lecture theatre", "src/images/auditorium.png");
        pub = new Room("in the campus pub", "src/images/pub.jpeg");
        lab = new Room("in a computing lab", "src/images/computing_lab.png");
        office = new Room("in the admin office", "src/images/admin.png");
        home = new Room("my home", "src/images/home.png");

        failed = new Room("fail", "src/images/failed.png");


        // Inicialização dos itens (Missão)
        Item chaveAuditorium = new Item("Chave do auditório", "Usada para poder encontrar com o professor.");
        pub.setItem(chaveAuditorium); // A chave do auditorio está no pub

        Item chaveOffice = new Item("Chave da sala de administração", "Usada para abrir a porta da admnistração.");
        theatre.setItem(chaveOffice);

        Item chaveGate = new Item("Chave do portão principal", "Usada para sair da faculdade");
        office.setItem(chaveGate);

        // initialise room exits
        outside.setExit("direita", theatre);
        outside.setExit("cima", lab);
        outside.setExit("esquerda", pub);
        outside.setExit("baixo", home);

        theatre.setExit("cima", outside);

        pub.setExit("baixo", outside);

        lab.setExit("baixo", outside);
        lab.setExit("cima", office);

        office.setExit("baixo", lab);

        currentRoom = outside;  // start game outside
    }

    /**
     * Print out the opening message for the player.
     */
    private void printWelcome()
    {

        // Exibe o estado inicial
        gui.updateImage(currentRoom.getImagePath());
        gui.updatePath("Bem-vindo ao World of Zuul!");
        gui.updatePath("World of Zuul é um novo, e extraordinariamente jogo tedioso de aventura.");
        gui.updatePath(currentRoom.getLongDescription());
    }

    private void checkMissionEvents() {
        // Evento 1: Encontrar a chave no Pub
        if(currentRoom.getShortDescription().contains("pub") && currentRoom.getItem() != null) {
                gui.updatePath(currentRoom.getItem().getDescricao());
                currentRoom.takeItem(); // Remove da sala
                hasKeyAuditorium = true;

        }

//        // Evento 2: Falar com o professor
//        if(currentRoom.getShortDescription().contains("outside") && hasKeyAuditorium && !hasKeyAdmin) {
//            gui.updatePath("Vá para o auditório para pegar a chave com o professor");
//        }

        //Evento 3: Pegar chave com o professor
        if(currentRoom.getShortDescription().contains("theatre") && currentRoom.getItem() != null) {
                gui.updatePath(currentRoom.getItem().getDescricao());
                currentRoom.takeItem();
                hasKeyAdmin = true;
        }

        //Evento 4: Pegar a chave na sala de administração

        if(currentRoom.getShortDescription().contains("office") && currentRoom.getItem() != null) {
                gui.updatePath(currentRoom.getItem().getDescricao());
                currentRoom.takeItem();
                hasKeyGate = true;
        }

        //Evento 5: Abrir o portão e ir embora
        if(currentRoom.getShortDescription().contains("outside") && hasKeyGate) {
            gui.updatePath("Agora você pode abrir o portão e ir embora.");
        }

        //Evento 6: EOG
        if(currentRoom.getShortDescription().contains("home")){
           gui.updatePath("Parabêns, você finalizou o jogo.");
           finished = true;
        }

        processScorePhase1(movimentos);
    }

    //Retorna falso caso movimentação seja falha
    public boolean processDirection(String direction) {

        Room nextRoom = currentRoom.getExit(direction);

        if (nextRoom == null) {
            gui.updatePath("Não há saída para " + direction + "!");
            return false;
        }
        else if(nextRoom.getShortDescription().contains("office") && !hasKeyAdmin){
            gui.updatePath("A porta está trancada, ache o professor antes de ir para essa sala");
            return false;
        }
        else if(nextRoom.getShortDescription().contains("theatre") && !hasKeyAuditorium){
            gui.updatePath("A porta está trancada, a chave pode estar no barzinho");
            return false;
        }
        else if(nextRoom.getShortDescription().contains("home") && !hasKeyGate){
            gui.updatePath("O portão parece estar trancado, pegue a chave na sala de administração para abrir.");
            return false;
        }
        else {
            currentRoom = nextRoom;

            // Atualiza Interface
            gui.updateImage(currentRoom.getImagePath());
            gui.updatePath("Você foi para: " + currentRoom.getShortDescription());
            gui.updatePath(currentRoom.getExitString()); // Mostra saídas disponíveis no log



            checkMissionEvents(); // Verifica se venceu ou achou item
            movimentos++;
            return true;
        }
    }

    //Processa pontuação em relação aos movimentos, retorno apenas para questões de teste.
    public int processScorePhase1(int movimentos) {
        if (movimentos == movimentosMaximos) {
            currentRoom = failed;
            gui.updateImage(currentRoom.getImagePath());
            gui.updatePath("Você falhou em concluir o jogo");
            return 0;
        }
        if(finished){
            if(movimentos <= 9){ //Caminho ótimo.
                gui.updatePath("Você já sabia do caminho não é? Parabens NOTA MÁXIMA");
                return 10;
            }
            else if(movimentos <=11){
                gui.updatePath("Não é muito bom, mas pelo menos você chegou lá");
                return 7;
            }
            else{
                gui.updatePath("Quase não conseguia ein, to ficando decepcionado.");
                return 5;
            }

        }
        return 0;
    }
}

