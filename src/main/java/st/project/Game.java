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


        //Checkpoints
    private Room outside = new Room("outside the main entrance", "src/images/outside.jpeg");
    private Room pharmacy = new Room("Farmácia", "src/images/pharmacy.jpg");

    private int reset = 0;


        //SCORE
    private int movimentos = 0;
    private int movimentosMaximos = 14;
    private Room failed = new Room("fail", "src/images/failed.png");
    public boolean finished = false;
    public boolean gameOver = false;

        //Nivel
    private int fase = 1;

        //VARIAVEIS DA MISSÃO 1
    private boolean hasKeyAdmin = false;
    private boolean hasKeyAuditorium = false;
    private boolean hasKeyGate =  false;

        //VARIAVEIS DA MISSAO 2
    private boolean hasPao = false;
    private boolean visitedPharmacy = false;
    private boolean hasApartmentKey = false;

    /**
     * Create the game and initialise its internal map.
     */
    public Game()
    {

        init();
    }

    public void setGui(GameGUI gui){
        this.gui = gui;
        printWelcome();
    }


    /**
     * Create all the rooms and link their exits together.
     */
    private void init()
    {

        //Salas da fase 1
        Room theatre, pub, lab, office, gate;

        theatre = new Room("in a lecture theatre", "src/images/auditorium.png");
        pub = new Room("in the campus pub", "src/images/pub.jpeg");
        lab = new Room("in a computing lab", "src/images/computing_lab.png");
        office = new Room("in the admin office", "src/images/admin.png");
        //gate = new Room("gate of the campus", "src/images/home.png"); não mais usado.

        //Salas da fase 2

        Room outsideGate, bakery, friendsHouse, apartments, streetOne, streetTwo;

        outsideGate= new Room("Fora do campus", "src/images/outsideGate.png");
        bakery = new Room("Padaria","src/images/bakery.png");
        friendsHouse = new Room("Casa do seu amigo","src/images/friendsHouse.jpg");
        apartments = new Room("Complexo de apartamentos","src/images/home.png");
        streetOne = new Room("Rua comum", "src/images/StreetOne.png");
        streetTwo = new Room("Outra rua comum", "src/images/streetTwo.png");


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
        outsideGate.setExit("baixo", streetOne);

        pharmacy.setExit("direita", outsideGate);

        streetOne.setExit("cima", outsideGate);
        streetOne.setExit("direita", bakery);
        streetOne.setExit("baixo", streetTwo);

        bakery.setExit("direita", streetOne);

        streetTwo.setExit("cima", streetOne);
        streetTwo.setExit("esquerda", friendsHouse);
        streetTwo.setExit("direita", apartments);

        friendsHouse.setExit("baixo",streetTwo);


        apartments.setExit("",streetTwo);


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

    private void checkMissionEvents(int fase) {
        // ---------------------------------------------------------------------
        // FASE 1
        // ---------------------------------------------------------------------
        if(fase == 1){
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
            if(currentRoom.getShortDescription().contains("Fora")){
                gui.updatePath("Parabêns, você conseguiu sair do campus, agora o objetivo é chegar em casa." +
                        "Não esqueça do seu amigo, ele ta com fome, e com sua chave...");
                this.fase = 2;
                finished = true;
            }
        }

        // ---------------------------------------------------------------------
        // FASE 2
        // ---------------------------------------------------------------------
        //Não precisa de if(fase == 2) pois não tem mais de 2 fases.
        else{
            //Saiu do campos
            if(currentRoom.getShortDescription().contains("Fora")){
                gui.updatePath("Você saiu do campus, agora o objetivo é chegar no seu apartamento\n" +
                        "lembre que a chave está com teu amigo, que mora na sua rua, e ele está com MUITA FOME.");
            }
            //Foi na farmácia CHECKPOINT.
            if(currentRoom.getShortDescription().contains("Farmácia")){
                gui.updatePath("Você chegou na farmácia, não tem muito o que ver aqui\n" +
                        "pelo menos agora você sabe onde comprar remédios....");
                visitedPharmacy = true;
            }
            //Pegou o pão na padaria.
            if(currentRoom.getShortDescription().contains("Padaria")){
                gui.updatePath("Ainda bem que você pegou o pão pro seu amigo, ninguem sabe oque poderia acontecer sem isso.......");
                hasPao = true;
            }
            //Entrou na casa do amigo, considerando que tem o pão, se não tiver é barrado antes.
            if(currentRoom.getShortDescription().contains("amigo")){
                gui.updatePath("Você entregou o pão, seu amigo estava com mt fome, e ele entrega sua chave de volta");
                hasApartmentKey = true;
            }
        }



        //processScorePhase1(movimentos);
    }

    //Retorna falso caso movimentação seja falha
    public boolean processDirection(String direction) {

        Room nextRoom = currentRoom.getExit(direction);

        if (nextRoom == null) {
            gui.updatePath("Não há saída para " + direction + "!");
            return false;
        }

        if(isRoomLocked(nextRoom)){
            return false;
        }

        if(isRoomTrapped(nextRoom)){
            return false;
        }


        currentRoom = nextRoom;
        // Atualiza Interface
        gui.updateImage(currentRoom.getImagePath());
        gui.updatePath("Você foi para: " + currentRoom.getShortDescription());
        gui.updatePath(currentRoom.getExitString()); // Mostra saídas disponíveis no log


        checkMissionEvents(fase); // Verifica se venceu ou achou item
        movimentos++;
        return true;
    }

    /*
    Verifica se o ambiente está trancado
    true se estiver trancado, false se aberto.
     */
    public boolean isRoomLocked(Room room) {
        String description = room.getShortDescription();

        if (description.contains("office") && !hasKeyAdmin) {
            gui.updatePath("A porta está trancada, ache o professor antes de ir para essa sala");
            return true;
        }
        if (description.contains("theatre") && !hasKeyAuditorium) {
            gui.updatePath("A porta está trancada, a chave pode estar no barzinho");
            return true;
        }
        if (description.contains("Fora") && !hasKeyGate) {
            gui.updatePath("O portão parece estar trancado, pegue a chave na sala de administração para abrir.");
            return true;
        }

        if(description.contains("apartamentos") && !hasApartmentKey){
            gui.updatePath("Você precisa pegar a chave com seu amigo, cuidado que ele está com fome.");
            return true;
        }

        return false;
    }

    /*
    Verifica se o proximo ambiente possui armadilha, e se ela ainda está ativa.
     */
    public boolean isRoomTrapped(Room room){
        if(room.getShortDescription().contains("amigo")){
            if(hasPao){
                //Possui pão, tudo certo.
                return false;
            }
            else{
                //Se não tem pao.
                if(visitedPharmacy){
                    //Funciona como checkpoint, retorna até a farmácia
                    gui.updatePath("Você esqueceu do pão do seu amigo, ele te deu murro, você acordou na farmácia.");
                    currentRoom = pharmacy;
                    gui.updateImage(currentRoom.getImagePath());
                }
                else{
                    gui.updatePath("Seu amigo te bateu tão forte que você percebeu que estava sonhando, e acordou na faculdade novamente..");
                    gameReset();
                }
                return true;
            }
        }
        else {
            return false;
        }
    }

    //Reseta o progresso do jogo, caso reset mais de 3 vezes GAME OVER.
    public void gameReset(){
        if(reset < 3){
            movimentos = 0;
            finished = false;
            hasKeyAuditorium = hasKeyGate = hasKeyAdmin = false;
            hasPao = visitedPharmacy = hasApartmentKey = false;

            this.init();
            fase = 1;
            gui.updateImage(currentRoom.getImagePath());
            reset++;
        }
        else{
            failMission();
        }

    }

    //Processa pontuação em relação aos movimentos, retorno apenas para questões de teste.
    public int processScorePhase1(int movimentos) {
        if (movimentos == movimentosMaximos) {
            failMission();
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

    public void failMission(){
        currentRoom = failed;
        gui.updateImage(currentRoom.getImagePath());
        gui.updatePath("você falhou em terminar a fase....");
        gameOver = true;
    }
}

