package gameplay.games.uno;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import backend.publicvars.PublicInt;
import frontend.WrappingLayout;
import gameplay.Player;

public class UnoWindow {
    private JFrame frame;
    private JPanel mainPanel = new JPanel();
    private JPanel cardPanel = new JPanel();
    private JPanel handPanel = new JPanel();
    private JPanel playerPanel = new JPanel();
    private JPanel backgroundPanel = new JPanel();
    private JPanel container = new JPanel();
    private JScrollPane scrollPane = new JScrollPane();
    private Card topCard;
    private ArrayList<Card> hand = new ArrayList<>();
    private GridBagConstraints gbc = new GridBagConstraints();
    private CardLayout cl = new CardLayout();

    private Uno uno;

    public UnoWindow(Uno uno) {
        this.uno = uno;
        frame = new JFrame("Uno Game");
        initialize();
    }

    public void initialize() {
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setOpaque(false);
        cardPanel.setLayout(new GridBagLayout());
        cardPanel.setOpaque(false);
        handPanel.setLayout(new GridBagLayout());
        handPanel.setOpaque(false);
        playerPanel.setLayout(new WrappingLayout(10, 5, WrappingLayout.CENTER));
        playerPanel.setOpaque(false);
        scrollPane.setViewportView(handPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(1600, 230));
        scrollPane.setMaximumSize(new Dimension(1600, 230));
        scrollPane.setMinimumSize(new Dimension(1600, 230));
        scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        mainPanel.add(playerPanel, BorderLayout.NORTH);
        backgroundPanel = new JPanel(cl);
        backgroundPanel.add(new JLabel(new ImageIcon("src\\gameplay\\games\\uno\\assets\\Green_Table.png")), "Card1");
        backgroundPanel.add(new JLabel(new ImageIcon("src\\gameplay\\games\\uno\\assets\\Red_Table.png")), "Card2");
        container.setLayout(null);
        gbc.gridx = 0;
        gbc.gridy = 0;
        backgroundPanel.setBounds(0, 0, 1600, 900);
        mainPanel.setBounds(0, 0, 1600, 900);
        container.add(backgroundPanel);
        container.add(mainPanel);
        container.setOpaque(false);
        container.setComponentZOrder(backgroundPanel, 1);
        container.setComponentZOrder(mainPanel, 0);
        Dimension size = new Dimension(1600, 900);
        container.setPreferredSize(size);
        container.setMaximumSize(size);
        container.setMinimumSize(size);
        frame.setContentPane(container);
        frame.pack();
    }

    public void winScreen(String winnerName) {
        JDialog winScreen = new JDialog(frame, true);
        winScreen.setLayout(new BorderLayout());
        winScreen.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        winScreen.setSize(600, 200);
        winScreen.setLocationRelativeTo(frame);
        JPanel winPanel = new JPanel();
        winPanel.setLayout(new GridBagLayout());
        winScreen.add(winPanel, BorderLayout.CENTER);
        JLabel winLabel = new JLabel(winnerName + " wins!!!!");
        winLabel.setFont(new Font("Arial", Font.BOLD, 40));
        gbc.gridx = 0;
        gbc.gridy = 0;
        winPanel.add(winLabel, gbc);
        gbc.gridy++;

        JButton returnButton = new JButton("Return to Lobby");
        returnButton.addActionListener(e -> {
            winScreen.dispose();
            frame.dispose();
            return;
        });
        returnButton.setBackground(Color.WHITE);
        returnButton.setFocusPainted(false);
        returnButton.setVisible(true);
        winPanel.add(returnButton, gbc);
        winPanel.revalidate();
        winPanel.repaint();

        winScreen.setVisible(true);
    }

    public void updatePlayers() {
        playerPanel.removeAll();
        boolean isThisTurn = true;
        ArrayList<Player> players = uno.getPlayers();
        Player self = uno.getSelf();
        for (int i = players.indexOf(self) + 1; i < players.size(); i++) {
            Player p = players.get(i);
            int handSize = ((PublicInt) (p.getVariable("handSize").get())).getValue().orElse(7);
            // System.out.println(handSize);
            String playerInfo = p.getName() + "\nCards: " + handSize;
            JLabel playerLabel = new JLabel("", SwingConstants.CENTER);
            playerLabel.setText("<html>"
                    + playerInfo.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>") + "</html>");
            if (i == uno.getTurnIndex()) {
                playerLabel.setForeground(Color.RED);
                isThisTurn = false;
            } else {
                playerLabel.setForeground(Color.WHITE);
            }
            playerLabel.setFont(new Font("Arial", Font.PLAIN, 40));
            playerPanel.add(playerLabel);
        }
        for (int i = 0; i < players.indexOf(self); i++) {
            Player p = players.get(i);
            int handSize = ((PublicInt) (p.getVariable("handSize").get())).getValue().orElse(7);
            String playerInfo = p.getName() + "\nCards: " + handSize;
            JLabel playerLabel = new JLabel("", SwingConstants.CENTER);
            playerLabel.setText("<html>"
                    + playerInfo.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>") + "</html>");
            if (i == uno.getTurnIndex()) {
                playerLabel.setForeground(Color.RED);
                isThisTurn = false;
            } else {
                playerLabel.setForeground(Color.WHITE);
            }
            playerLabel.setFont(new Font("Arial", Font.PLAIN, 40));
            playerPanel.add(playerLabel);
        }
        if (isThisTurn) {
            cl.last(backgroundPanel);
            backgroundPanel.revalidate();
            backgroundPanel.repaint();
        } else {
            cl.first(backgroundPanel);
            backgroundPanel.revalidate();
            backgroundPanel.repaint();
        }
        playerPanel.revalidate();
        playerPanel.repaint();
    }

    public void updateHand(ArrayList<Card> newHand) {
        handPanel.removeAll();
        hand = (ArrayList<Card>) newHand.clone();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        for (Card card : hand) {
            JButton cardButton = new JButton();
            cardButton.addActionListener(e -> uno.playCard(card));
            cardButton.setIcon(scaleImage(new ImageIcon(card.getImageFile().getAbsolutePath()), 150));
            cardButton.setContentAreaFilled(false);
            cardButton.setFocusPainted(false);
            Dimension size = new Dimension(cardButton.getIcon().getIconWidth(), cardButton.getIcon().getIconHeight());
            cardButton.setPreferredSize(size);
            cardButton.setMaximumSize(size);
            cardButton.setMinimumSize(size);
            handPanel.add(cardButton, gbc);
            gbc.gridx++;
        }
        handPanel.revalidate();
        handPanel.repaint();
    }

    public void updateTopCard(Card card) {
        cardPanel.removeAll();
        topCard = card;
        JLabel cardLabel = new JLabel(new ImageIcon(topCard.getImageFile().getAbsolutePath()));
        JButton deck = new JButton();
        deck.setIcon(new ImageIcon("src\\gameplay\\games\\uno\\assets\\Deck.png"));
        deck.setContentAreaFilled(false);
        deck.setFocusPainted(false);
        Dimension size = new Dimension(deck.getIcon().getIconWidth(), deck.getIcon().getIconHeight());
        deck.setPreferredSize(size);
        deck.setMaximumSize(size);
        deck.setMinimumSize(size);
        deck.addActionListener(e -> uno.drawFromDeck());
        gbc.gridx = 0;
        gbc.gridy = 0;
        cardPanel.add(deck, gbc);
        gbc.gridx = 1;
        cardPanel.add(cardLabel, gbc);
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    public Card pickWildColor() {
        JDialog colorDialog = new JDialog(frame, "Pick a Color", true);
        colorDialog.setLayout(new BorderLayout());
        colorDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        colorDialog.setSize(600, 200);
        colorDialog.setLocationRelativeTo(frame);
        JPanel colorPanel = new JPanel();
        colorPanel.setLayout(new GridBagLayout());
        colorDialog.add(colorPanel, BorderLayout.CENTER);

        gbc.gridy = 0;
        gbc.gridx = 0;
        String[] colors = { "Red", "Green", "Blue", "Yellow" };
        Color[] colorValues = { Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW };
        for (int i = 0; i < 4; i++) {
            JButton colorButton = new JButton();
            int index = i;
            colorButton.addActionListener(e -> {
                topCard.setColor(colors[index]);
                colorDialog.dispose();
            });
            colorButton.setFocusPainted(false);
            colorButton.setVisible(true);
            colorButton.setPreferredSize(new Dimension(100, 50));
            colorButton.setMaximumSize(new Dimension(100, 50));
            colorButton.setMinimumSize(new Dimension(100, 50));
            colorButton.setForeground(colorValues[i]);
            colorButton.setBackground(colorValues[i].darker());
            colorButton.setText(colors[i]);
            gbc.gridx = i;
            colorPanel.add(colorButton, gbc);
        }

        colorPanel.revalidate();
        colorPanel.repaint();

        colorDialog.setVisible(true);
        return topCard;
    }

    public void reDraw() {
        updatePlayers();
        updateHand(uno.getHand());
        updateTopCard(uno.getTopCard());
        container.revalidate();
        container.repaint();
    }

    public ImageIcon scaleImage(ImageIcon icon, int width) {
        return new ImageIcon(icon.getImage().getScaledInstance(width, -1, Image.SCALE_SMOOTH));
    }
}
