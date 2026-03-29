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
    private Parser parser;
    private Room currentRoom;
    private GameGUI gui;

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

        // 2. Inicializa o parser
        parser = new Parser();

        // 3. Inicializa a GUI com imagens
        gui = new GameGUI(this);

        // Exibe o estado inicial
        gui.updateImage(currentRoom.getImagePath());
        gui.updatePath("Bem-vindo ao World of Zuul!");
        gui.updatePath(currentRoom.getLongDescription());
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


        // Inicialização dos itens (Missão)
        Item chaveAuditorium = new Item("Chave do auditório", "Usada para encontrar com o professor.");
        pub.setItem(chaveAuditorium); // A chave do auditorio está no pub

        Item chaveOffice = new Item("Chave da sala de administração", "Usada para entrar na sala da admnistração.");
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
     *  Main play routine.  Loops until end of play.
     */
    public void play()
    {
        printWelcome();

        // Enter the main command loop.  Here we repeatedly read commands and
        // execute them until the game is over.

        boolean finished = false;
        while (! finished) {
            Command command = parser.getCommand();
            finished = processCommand(command);
        }
        gui.printMessage("Thank you for playing.  Good bye.");
    }

    /**
     * Print out the opening message for the player.
     */
    private void printWelcome()
    {
        gui.printMessage("\n");
        gui.printMessage("Welcome to the World of Zuul!");
        gui.printMessage("World of Zuul is a new, incredibly boring adventure game.");
        gui.printMessage("Type 'help' if you need help.");
        gui.printMessage("\n");
        gui.printMessage(currentRoom.getLongDescription());
    }

    /**
     * Given a command, process (that is: execute) the command.
     * @param command The command to be processed.
     * @return true If the command ends the game, false otherwise.
     */
    private boolean processCommand(Command command)
    {
        boolean wantToQuit = false;

        CommandWord commandWord = command.getCommandWord();

        if(commandWord == CommandWord.UNKNOWN) {
            gui.printMessage("I don't know what you mean...");
            return false;
        }

        if (commandWord == CommandWord.HELP) {
            printHelp();
        }
        else if (commandWord == CommandWord.GO) {
            goRoom(command);
        }
        else if (commandWord == CommandWord.QUIT) {
            wantToQuit = quit(command);
        }
        // else command not recognised.
        return wantToQuit;
    }

    // implementations of user commands:

    /**
     * Print out some help information.
     * Here we print some stupid, cryptic message and a list of the
     * command words.
     */
    private void printHelp()
    {
        gui.printMessage("You are lost. You are alone. You wander");
        gui.printMessage("around at the University.");
        gui.printMessage("\n");
        gui.printMessage("Your command words are: " + parser.getCommands());

    }

    /**
     * Try to go to one direction. If there is an exit, enter the new
     * room, otherwise print an error message.
     */
    private void goRoom(Command command)
    {
        if(!command.hasSecondWord()) {
            gui.updatePath("Ir para onde?");
            return;
        }

        String direction = command.getSecondWord();
        Room nextRoom = currentRoom.getExit(direction);

        if (nextRoom == null) {
            gui.updatePath("Não há uma porta!");
        }
        else if(nextRoom.getShortDescription().contains("office") && !hasKeyAdmin){
            gui.updatePath("A porta está trancada, ache o professor antes de ir para essa sala");
        }
        else if(nextRoom.getShortDescription().contains("theatre") && !hasKeyAuditorium){
            gui.updatePath("A porta está trancada, a chave pode estar no barzinho");
        }
        else if(nextRoom.getShortDescription().contains("home") && !hasKeyGate){
            gui.updatePath("O portão parece estar trancado, pegue a chave na sala de administração para abrir.");
        }
        else {
            currentRoom = nextRoom;



            // ATUALIZAÇÃO GRÁFICA: Imagem e Texto
            gui.updateImage(currentRoom.getImagePath());
            gui.updatePath(currentRoom.getLongDescription());

            // Lógica da Missão Integrada à Movimentação
            checkMissionEvents();
        }
    }

    /**
     * "Quit" was entered. Check the rest of the command to see
     * whether we really quit the game.
     * @return true, if this command quits the game, false otherwise.
     */
    private boolean quit(Command command)
    {
        if(command.hasSecondWord()) {
            gui.printMessage("Quit what?");
            return false;
        }
        else {
            return true;  // signal that we want to quit
        }
    }

    public void processInputFromUI(String input) {
        if (input == null || input.trim().isEmpty()) { return; }

        // Ecoa o comando na tela
        gui.updatePath("Comando: " + input);

        Command command = parser.parseString(input);
        boolean wantToQuit = processCommand(command);

        if (wantToQuit) {
            gui.updatePath("Obrigado por jogar. Tchau!");
            Timer timer = new Timer(2000, e -> System.exit(0));
            timer.setRepeats(false);
            timer.start();
        }
    }

    private void checkMissionEvents() {
        // Evento 1: Encontrar a chave no Pub
        if(currentRoom.getShortDescription().contains("pub") && currentRoom.getItem() != null) {
            if(currentRoom.getItem().getNome().equals("Chave do auditório")) {
                currentRoom.takeItem(); // Remove da sala
                hasKeyAuditorium = true;
                gui.updatePath("Você encontrou a chave do auditório");
            }
        }

        // Evento 2: Falar com o professor
        if(currentRoom.getShortDescription().contains("outside") && hasKeyAuditorium && !hasKeyAdmin) {
            gui.updatePath("Vá para o auditório para pegar a chave com o professor");
        }

        //Evento 3: Pegar chave com o professor
        if(currentRoom.getShortDescription().contains("theatre") && currentRoom.getItem() != null) {
            if(currentRoom.getItem().getNome().equals("Chave da sala de administração")){
                currentRoom.takeItem();
                hasKeyAdmin = true;
                gui.updatePath("Você falou com o professor e pegou a chave da sala de administração");
            }
        }

        //Evento 4: Pegar a chave na sala de administração

        if(currentRoom.getShortDescription().contains("office") && currentRoom.getItem() != null) {
            if(currentRoom.getItem().getNome().equals("Chave do portão principal")){
                currentRoom.takeItem();
                hasKeyGate = true;
                gui.updatePath("Agora que você possui a chave do portão, pode abri-lo para ir embora");
            }
        }

        //Evento 5: Abrir o portão e ir embora
        if(currentRoom.getShortDescription().contains("outside") && hasKeyGate) {
            gui.updatePath("Agora você pode abrir o portão e ir embora.");
        }

        //Evento 6: EOG
        if(currentRoom.getShortDescription().contains("home")){
           gui.updatePath("Parabêns, você finalizou o jogo.");
        }
    }
}

