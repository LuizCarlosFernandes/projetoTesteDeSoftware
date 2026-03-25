package st.project;

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
    private boolean hasKey = false;


    /**
     * Create the game and initialise its internal map.
     */
    public Game() {
        createRooms();

        this.parser = new Parser();
        this.gui = new GameGUI(this);


        gui.printMessage("Bem-vindo ao World of Zuul!");
        gui.printMessage(currentRoom.getLongDescription());
    }

    /**
     * Create all the rooms and link their exits together.
     */
    private void createRooms()
    {
        Room outside, theatre, pub, lab, office;

        // create the rooms
        outside = new Room("outside the main entrance of the University");
        theatre = new Room("in a lecture theatre");
        pub = new Room("in the campus pub");
        lab = new Room("in a computing lab");
        office = new Room("in the computing admin office");

        // initialise room exits
        outside.setExit("east", theatre);
        outside.setExit("south", lab);
        outside.setExit("west", pub);

        theatre.setExit("west", outside);

        pub.setExit("east", outside);

        lab.setExit("north", outside);
        lab.setExit("east", office);

        office.setExit("west", lab);

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
            // if there is no second word, we don't know where to go...
            gui.printMessage("Go where?");
            return;
        }

        String direction = command.getSecondWord();

        // Try to leave current room.
        Room nextRoom = currentRoom.getExit(direction);

        if (nextRoom == null) {
            gui.printMessage("There is no door!");
        }
        else {
            currentRoom = nextRoom;
            gui.updatePath(currentRoom.getShortDescription());

            // Lógica da Missão
            if(currentRoom.getShortDescription().contains("pub") && !hasKey) {
                hasKey = true;
                gui.printMessage("Você encontrou a Chave Secreta!");
            }

            if(currentRoom.getShortDescription().contains("admin office") && hasKey) {
                gui.printMessage("MISSÃO CUMPRIDA! Você abriu o cofre com a chave.");
                System.exit(0);
            }

            gui.printMessage(currentRoom.getLongDescription());
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
        // Transformamos a String da interface em um objeto Command
        Command command = parser.parseString(input);
        boolean wantToQuit = processCommand(command);

        if (wantToQuit) {
            gui.printMessage("Obrigado por jogar. Tchau!");
            System.exit(0);
        }
    }
}
