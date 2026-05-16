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

    private int fase = 1;

        //VARIAVEIS DA MISSÃO 1
    private boolean hasKeyAdmin = false;
    private boolean hasKeyAuditorium = false;
    private boolean hasKeyGate =  false;



    /**
     * Create the game and initialise its internal map.
     */
    public Game()
    {
        // 1. Cria as salas primeiro
        phaseOne();

        // 2. Inicializa a GUI
        gui = new GameGUI(this);

        printWelcome();
    }


    /**
     * Create all the rooms and link their exits together.
     */
    private void phaseOne()
    {

        //Salas da fase 1
        Room outside, theatre, pub, lab, office, gate;

        outside = new Room("outside the main entrance", "src/images/outside.jpeg");
        theatre = new Room("in a lecture theatre", "src/images/auditorium.png");
        pub = new Room("in the campus pub", "src/images/pub.jpeg");
        lab = new Room("in a computing lab", "src/images/computing_lab.png");
        office = new Room("in the admin office", "src/images/admin.png");
        gate = new Room("gate of the campus", "src/images/home.png");

        //Salas da fase 2

        Room outsideGate, pharmacy, bakery, friendsHouse, apartments, streetOne, streetTwo;

        outsideGate= new Room("Fora do campus", "");
        pharmacy = new Room("Farmácia", "");
        bakery = new Room("Padaria","");
        friendsHouse = new Room("Casa do seu amigo","");
        apartments = new Room("Complexo de apartamentos","");
        streetOne = new Room("Rua comum", "");
        streetTwo = new Room("Outra rua comum", "");


        // ---------------------------------------------------------------------
        // FASE 1
        // ---------------------------------------------------------------------

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
        outside.setExit("baixo", outsideGate);

        theatre.setExit("cima", outside);

        pub.setExit("baixo", outside);

        lab.setExit("baixo", outside);
        lab.setExit("cima", office);

        office.setExit("baixo", lab);

        currentRoom = outside;  // start game outside



        // ---------------------------------------------------------------------
        // FASE 2
        // ---------------------------------------------------------------------

        /*
          Caminho fase 2:
          Gate -> farmácia (remédio) <-> padaria -> casa de amigo (pegar chave de casa)
          prédio (sem chave, precisa passar na casa de amigo) -> fim fase 2;
        * */
        //Inicializando items

        Item chaveDeCasa = new Item("Chave do seu prédio","Utilizada para entrar no seu prédio");
        friendsHouse.setItem(chaveDeCasa);

        Item pao = new Item("Pão para o seu amigo","Se você não levar o pao para seu amigo, ele " +
                "vai te bater.");
        bakery.setItem(pao);

        //Inicializando saidas
        outsideGate.setExit("cima", outside);
        outsideGate.setExit("direita", pharmacy);
        outsideGate.setExit("baixo ", streetOne);

        pharmacy.setExit("esquerda", outsideGate);

        streetOne.setExit("cima", outsideGate);
        streetOne.setExit("esquerda", bakery);
        streetOne.setExit("baixo", streetTwo);

        streetTwo.setExit("cima", streetOne);
        streetTwo.setExit("esquerda", friendsHouse);
        streetTwo.setExit("baixo", apartments);
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

        //Evento 2: Pegar chave com o professor
        if(currentRoom.getShortDescription().contains("theatre") && currentRoom.getItem() != null) {
                gui.updatePath(currentRoom.getItem().getDescricao());
                currentRoom.takeItem();
                hasKeyAdmin = true;
        }

        //Evento 3: Pegar a chave na sala de administração

        if(currentRoom.getShortDescription().contains("office") && currentRoom.getItem() != null) {
                gui.updatePath(currentRoom.getItem().getDescricao());
                currentRoom.takeItem();
                hasKeyGate = true;
        }

        //Evento 4: Abrir o portão e ir embora
        if(currentRoom.getShortDescription().contains("outside") && hasKeyGate) {
            gui.updatePath("Agora você pode abrir o portão e ir embora.");
        }

        //Evento 5: FIM DE FASE 1
        if(currentRoom.getShortDescription().contains("gate")){
           gui.updatePath("Parabêns, você conseguiu sair do campus, agora o objetivo é chegar em casa." +
                   "Não esqueça do seu amigo, ele ta com fome, e com sua chave...");
           fase = 2;
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
        else if(nextRoom.getShortDescription().contains("gate") && !hasKeyGate){
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
            gui.updatePath("Você falhou em terminar a fase....");
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

