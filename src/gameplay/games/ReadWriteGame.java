package gameplay.games;

import java.util.ArrayList;
import java.util.Scanner;

import backend.Lobby;
import backend.Session;
import backend.globalvars.GlobalString;
import backend.publicvars.PublicString;
import gameplay.Player;

enum State {
    READ_BIO,
    WRITE_BIO,
    READ_ONCE,
    WRITE_ONCE,
    READ_CONTINUOUS,
    WRITE_CONTINUOUS,
    EXITING,
}

public class ReadWriteGame extends Game {

    private State state;
    private final Scanner scanner = new Scanner(System.in);

    private Lobby lobby;
    private ArrayList<Player> partners;
    private Player self;
    private PublicString bio;

    private GlobalString globalString;

    public ReadWriteGame() {

    }

    @Override
    public void initialize(Session session) {
        lobby = session.getLobby();
        self = lobby.getClientPlayer();
        globalString = new GlobalString(session, "globalString");
    }

    @Override
    public void startGame() {
        System.out.println("Starting ReadWrite game");
        bio = new PublicString(self, "bio", "default value");
        while (setState())
            ;
    }

    @Override
    public boolean periodic() {
        switch (state) {
            case READ_BIO:
                System.out.println("Your bio is: " + bio.getValue());
                partners = lobby.getPlayers();
                partners.remove(self);
                for (Player other : partners) {
                    if (other.getVariable("bio").isPresent()) {
                        System.out.println(other.getName() + "'s bio is: "
                                + ((PublicString) (other.getVariable("bio").get())).getValue());
                    } else {
                        System.out.println(other.getName() + " has no bio");
                    }
                }
                setState();
                break;
            case WRITE_BIO:
                System.out.println("Enter your new bio");
                String newBio = scanner.nextLine();
                bio.setValue(newBio);
                setState();
                break;
            case READ_ONCE:
                System.out.println("Reading once: " + globalString.getValue().orElse("Nothing"));
                setState();
                break;
            case WRITE_ONCE:
                System.out.println("Enter a value to write: ");
                globalString.setValue(scanner.nextLine());
                setState();
                break;
            case READ_CONTINUOUS:
                do {
                    System.out.println("Reading continuously: " + globalString.getValue().orElse("Nothing"));
                    System.out.println("Enter 'stop' to stop reading, anything else to continue");
                } while (scanner.next() != "stop");
                break;
            case WRITE_CONTINUOUS:
                do {
                    System.out.println("Enter a value to write continuously: ");
                    globalString.setValue(scanner.nextLine());
                    System.out.println("Enter 'stop' to stop writing, anything else to continue");
                } while (!scanner.next().equals("stop"));
                break;
            case EXITING:
                return false;
            default:
                break;
        }
        return true;
    }

    @Override
    public void endGame() {
        System.out.println("Ending ReadWrite game");
    }

    @Override
    public String getName() {
        return "Read/Write Game";
    }

    // True == failed
    public boolean setState() {
        System.out.println("Select a state: ");
        System.out.println("1. Read_BIO -> rb");
        System.out.println("2. WRITE_BIO -> wb");
        System.out.println("3. READ_ONCE -> ro");
        System.out.println("4. WRITE_ONCE -> wo");
        System.out.println("5. READ_CONTINUOUS -> rc");
        System.out.println("6. WRITE_CONTINUOUS -> wc");
        System.out.println("7. EXITING -> exit");
        String input = scanner.nextLine();
        switch (input) {
            case "rb":
                state = State.READ_BIO;
                break;
            case "wb":
                state = State.WRITE_BIO;
                break;
            case "ro":
                state = State.READ_ONCE;
                break;
            case "wo":
                state = State.WRITE_ONCE;
                break;
            case "rc":
                state = State.READ_CONTINUOUS;
                break;
            case "wc":
                state = State.WRITE_CONTINUOUS;
                break;
            case "exit":
                state = State.EXITING;
                break;
            default:
                System.out.println("Invalid input. Please try again.");
                return true;
        }
        return false;
    }
}
