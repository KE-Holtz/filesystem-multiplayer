package gameplay.games.uno;

import java.util.ArrayList;

import javax.swing.SwingUtilities;

import backend.Lobby;
import backend.Session;
import backend.globalvars.GlobalBoolean;
import backend.globalvars.GlobalInt;
import backend.globalvars.GlobalString;
import backend.globalvars.GlobalVar;
import backend.publicvars.PublicInt;
import gameplay.Player;
import gameplay.games.Game;

enum State {
    WAITING,
    TURN,
    SPECIAL,
    WIN,
}

// TODO: 1) fix skip 2) add end message
public class Uno extends Game {
    private UnoWindow uwu;

    private Lobby lobby;
    private Player self;
    private ArrayList<Player> players;

    private GlobalVar<Card> topCard;
    private Card localTopCard;

    private PublicInt handSize;
    private ArrayList<Card> hand;

    private GlobalInt turnIndex;
    private GlobalInt numTurns;
    private int localTurnIndex = 0;
    private int localNumTurns = 0;

    private GlobalInt drawCounter;
    private boolean skip;
    private GlobalBoolean reverse;

    private GlobalString winner;
    private State state;

    private int safeTurnNum = -1;

    public Uno() {
        
    }

    @Override
    public void initialize(Session session) {
        lobby = session.getLobby();
        self = lobby.getClientPlayer();

        topCard = new GlobalVar<Card>(session, "card", Card::fromString, Card.randomNonWild());

        handSize = new PublicInt(self, "handSize");
        self.addVariable(handSize);

        turnIndex = new GlobalInt(session, "turnNum", 0);
        numTurns = new GlobalInt(session, "numTurns", 0);

        drawCounter = new GlobalInt(session, "drawCounter", 0);
        reverse = new GlobalBoolean(session, "reverse", false);

        winner = new GlobalString(session, "winner", "");
    }

    @Override
    public void startGame() {
        // System.out.println("This player is " + self.getName());
        uwu = new UnoWindow(this);
        uwu.updateTopCard(topCard.getValue().orElse(Card.randomNonWild()));
        hand = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            hand.add(Card.random());
        }
        handSize.setValue(hand.size());
        uwu.updateHand(hand);
        players = lobby.getPlayers();
        players.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        uwu.updatePlayers();
        // System.out.println(players);
    }

    @Override
    public boolean periodic() {
        syncVars();
        if (numTurns.getValue().orElse(0) > localNumTurns) {
            localNumTurns = numTurns.getValue().orElse(localNumTurns);
            uwu.reDraw();
        }
        Player currentPlayer = players.get(localTurnIndex);
        // System.out.println("Current Player = " + currentPlayer.getName());
        // System.out.println("State = " + state);
        if (!winner.getValue().orElse("").equals("")) {
            state = State.WIN;
        } else if (!currentPlayer.equals(self)) {
            state = State.WAITING;
        } else if (drawCounter.getValue().orElse(0) > 0 && safeTurnNum < localNumTurns) {
            state = State.SPECIAL;
        } else {
            state = State.TURN;
        }
        // if(state != lastState)
        //     uwu.reDraw();
        switch (state) {
            case WAITING:
                // Wait for it to be your turn
                // System.out.println("Waiting");
                break;
            case TURN:
                // System.out.println("Taking turn");
                break;
            case SPECIAL:
                // System.out.println("Special case:");
                // System.out.println("  drawCounter = " + drawCounter.getValue().orElse(-1));
                for (int i = 0; i < drawCounter.getValue().orElse(0); i++) {
                    drawCard();
                    // System.out.println(self.getName() + ": Drew");
                    wait(500);
                }
                drawCounter.setValue(0);
                passTurn();
                break;
            case WIN:
                System.out.println("Game over, " + winner.getValue() + " won!");
                return false;
            default:
                break;
        }
        if (hand.size() <= 0) {
            winner.setValue(self.getName());
        }
        return true;
    }

    @Override
    public void endGame() {
        uwu.winScreen(winner.getValue().get());
    }

    @Override
    public String getName() {
        return "Uno";
    }

    public void syncVars() {
        if (topCard.getValue().isEmpty()) {
            // System.out.println("Top card is empty");
        } else {
            localTopCard = topCard.getValue().orElse(localTopCard);
        }

        if (turnIndex.getValue().isEmpty()) {
            // System.out.println("Turn index is empty");
        } else {
            localTurnIndex = turnIndex.getValue().orElse(localTurnIndex);
        }
    }

    public void playCard(Card card) {
        // System.out.println("Playing card, state = " + state);
        if (state.equals(State.WAITING)) {
            return;
        } else if (card.isWild()) {
            hand.remove(card);
            handSize.setValue(hand.size());
            topCard.setValue(card);
            uwu.updateTopCard(card);
            // System.out.println(topCard.getValue().get());
            topCard.setValue(uwu.pickWildColor());
            // System.out.println("Wild card played");
            if (card.getValue() == 14) {
                safeTurnNum = localNumTurns;
                drawCounter.setValue(4);
            }
            passTurn();
        } else if (card.matches(localTopCard)) {
            hand.remove(card);
            handSize.setValue(hand.size());
            topCard.setValue(card);
            uwu.updateTopCard(card);
            switch (card.getValue()) {
                case 10:
                    skip = true;
                    break;
                case 11:
                    reverse.setValue(!reverse.getValue().orElse(false));
                    if (players.size() == 2)
                        skip = true;
                    break;
                case 12:
                    safeTurnNum = localNumTurns;
                    drawCounter.setValue(2);
                default:
                    break;
            }
            passTurn();
        } else {
        }
    }

    public void drawFromDeck() {
        if (state.equals(State.TURN)) {
            Card card = drawCard();
            SwingUtilities.invokeLater(() -> {
                wait(200);
                if (card.matches(localTopCard) || card.isWild()) {
                    playCard(card);
                } else {
                    passTurn();
                }
            });
        }
    }

    public Card drawCard() {
        Card card = Card.random();
        if (!state.equals(State.WAITING)) {
            hand.add(card);
            handSize.setValue(hand.size());
        }
        uwu.reDraw();
        return card;
    }

    public void passTurn() {
        int nextTurnIndex;
        if (reverse.getValue().orElse(false)) {
            nextTurnIndex = localTurnIndex - (skip ? 2 : 1);
            if (nextTurnIndex < 0) {
                nextTurnIndex = players.size() + nextTurnIndex;
            } else {
                nextTurnIndex = nextTurnIndex % players.size();
            }
        } else {
            nextTurnIndex = (localTurnIndex + (skip ? 2 : 1)) % players.size();
        }
        skip = false;
        // System.out.println("Passing turn, currentTurnNum = " + localTurnIndex + " nextTurnNum = " + nextTurnIndex
                // + ", numTurns = " + numTurns.getValue().orElse(0) + ", players.size() = " + players.size()
                // + "reversing = " + reverse.getValue());
        turnIndex.setValue(nextTurnIndex);
        localTurnIndex = nextTurnIndex;
        if (numTurns.getValue().isEmpty()) {
            // System.out.println("numTurns is empty");
        } else {
            numTurns.setValue(numTurns.getValue().orElse(0) + 1);
        }
    }

    public void wait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public Card getTopCard() {
        return topCard.getValue().orElse(Card.randomNonWild());
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public Player getSelf() {
        return self;
    }

    public int getLocalTurnIndex() {
        return localTurnIndex;
    }

    public int getTurnIndex() {
        return turnIndex.getValue().orElse(localTurnIndex);
    }
}
