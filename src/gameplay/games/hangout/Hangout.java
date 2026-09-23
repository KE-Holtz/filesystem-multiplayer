package gameplay.games.hangout;

import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import backend.Lobby;
import backend.Session;
import backend.publicvars.PublicInt;
import gameplay.Player;
import gameplay.games.Game;

public class Hangout extends Game {
    public final int frameWidth = 200;
    public final int frameHeight = 200;
    public final int screenHeight = 1080;
    public final int headBoxHeight = 60;
    public final int footBoxHeight = 60;
    // public final Color[] colorChoices = {Color.BLACK,Color.BLUE,Color.CYAN,Color.GREEN,Color.MAGENTA,Color.ORANGE,Color.PINK,Color.RED,Color.WHITE,Color.YELLOW,Color.GRAY};

    public Lobby lobby;

    public Player self;
    public JFrame myFrame;
    public ArrayList<Player> players;

    public long lastTimestamp;
    public long deltaT;
    public boolean grounded;
    public boolean riding;
    public int ridingPlayerIndex;
    public int x = 0;
    public double vx = 0;
    public double vy = 0;
    public final double gravityConstant = 0.02;
    public int y = 0;

    public PublicInt publicX;
    public PublicInt publicY;
    // public PublicInt colorIndex;

    public PublicInt[] xs;
    public PublicInt[] ys;
    public JFrame[] frames;

    @Override
    public void initialize(Session session) {
        this.lobby = session.getLobby();
        this.self = lobby.getClientPlayer();
        players = lobby.getPlayers();
        publicX = new PublicInt(self, "x", 0);
        publicY = new PublicInt(self, "y", 0);
        // colorIndex = new PublicInt(self, "colorIndex", (int)(colorChoices.length*Math.random()));
        frames = new JFrame[lobby.getPlayers().size()];
        xs = new PublicInt[lobby.getPlayers().size()];
        ys = new PublicInt[lobby.getPlayers().size()];
    }

    @Override
    public void startGame() {
        for (int i = 0; i < players.size(); i++) {
            JFrame frame = new JFrame(players.get(i).getName());
            JLabel name = new JLabel(players.get(i).getName(),  SwingConstants.CENTER);
            frame.setUndecorated(true);
            frame.add(name);
            if (players.get(i).equals(self)) {
                myFrame = frame;
                myFrame.addKeyListener(new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        int keyCode = e.getKeyCode();
                        if (keyCode == KeyEvent.VK_UP) {
                            if (grounded) {
                                vy = -5;
                                grounded = false;
                                riding = false;
                            }
                        }
                        if (keyCode == KeyEvent.VK_LEFT) {
                            vx = -1.5;
                        }
                        if (keyCode == KeyEvent.VK_RIGHT) {
                            vx = 1.5;
                        }
                    }

                    public void keyReleased(KeyEvent e) {
                        int keyCode = e.getKeyCode();
                        if (keyCode == KeyEvent.VK_LEFT) {
                            vx = 0;
                        }
                        if (keyCode == KeyEvent.VK_RIGHT) {
                            vx = 0;
                        }
                    }
                });
            }
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.setResizable(false);
            frame.setVisible(true);
            frame.setLocationRelativeTo(null);
            frame.setSize(frameWidth, frameHeight);
            frames[i] = frame;
            xs[i] = (PublicInt) players.get(i).getVariable("x").get();
            ys[i] = (PublicInt) players.get(i).getVariable("y").get();
            lastTimestamp = System.currentTimeMillis();
        }
    }

    @Override
    public boolean periodic() {
        deltaT = System.currentTimeMillis() - lastTimestamp;
        lastTimestamp = System.currentTimeMillis();
        if (!riding) {
            if (!grounded) {
                vy += gravityConstant * deltaT;
                y += vy * deltaT;
            }
            x += vx * deltaT;
            if (y >= screenHeight - frameHeight) {
                grounded = true;
                y = screenHeight - frameHeight;
                vy = 0;
            }
        }
        for (int i = 0; i < players.size(); i++) {
            Optional<Integer> optionalX = xs[i].getValue();
            Optional<Integer> optionalY = ys[i].getValue();
            if (players.get(i).equals(self)) {
                if (riding) {
                    Optional<Integer> ridingOptionalX = xs[ridingPlayerIndex].getValue();
                    Optional<Integer> ridingOptionalY = ys[ridingPlayerIndex].getValue();
                    if (ridingOptionalX.isPresent() && ridingOptionalY.isPresent()) {
                        x = ridingOptionalX.get();
                        y = ridingOptionalY.get() - frameHeight;
                        myFrame.setLocation(x, y);
                        publicX.setValue(x);
                        publicY.setValue(y);
                    }
                } else {
                    publicX.setValue(x);
                    publicY.setValue(y);
                    myFrame.setLocation(x, y);
                }
            } else if (optionalX.isPresent() && optionalY.isPresent()) {
                int otherX = optionalX.get();
                int otherY = optionalY.get();
                frames[i].setLocation(otherX, otherY);
                if (vy > 0 && areRectanglesIntersecting(x, y + frameHeight - footBoxHeight, frameWidth,
                        footBoxHeight, otherX, otherY + frameHeight - headBoxHeight, frameHeight, headBoxHeight)
                        && !riding) {
                    y = otherY - frameHeight;
                    grounded = true;
                    riding = true;
                    ridingPlayerIndex = i;
                    vy = 0;
                    System.out.println("Getting head");
                }
            }

        }
        System.out.println(x + " " + y);
        return true;
    }

    @Override
    public void endGame() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'endGame'");
    }

    @Override
    public String getName() {
        return "Hangout";
    }

    private static boolean areRectanglesIntersecting(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        if (x1 + w1 >= x2 && // r1 right edge past r2 left
                x1 <= x2 + w2 && // r1 left edge past r2 right
                y1 + h2 >= y2 && // r1 top edge past r2 bottom
                y1 <= y2 + h2) { // r1 bottom edge past r2 top
            return true;
        }
        return false;
    }
}
