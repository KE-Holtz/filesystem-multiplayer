import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import backend.Config;
import backend.Session;
import frontend.WrappingLayout;
import gameplay.games.Game;
import gameplay.games.ReadWriteGame;
import gameplay.games.hangout.Hangout;
import gameplay.games.uno.Uno;

public class Main {
    private static JFrame frame = new JFrame();

    public static void main(String[] args) {
        Config.init();

        Path sessionSpacePath = Path.of(Config.getSessionSpace());

        frame.setLayout(new BorderLayout());
        frame.setResizable(true);

        boolean hosting = false;

        JPanel screen = new JPanel();
        JPanel labelPanel = new JPanel();
        JPanel buttons = new JPanel();
        JPanel sessionButtons = new JPanel();
        JPanel input = new JPanel();

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(sessionButtons);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(600, 100));
        scrollPane.setMaximumSize(new Dimension(600, 100));
        scrollPane.setMinimumSize(new Dimension(600, 100));
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);

        Font font = new Font("Arial", Font.PLAIN, 20);

        screen.setLayout(new GridBagLayout());
        screen.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        screen.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttons.setLayout(new FlowLayout());
        buttons.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sessionButtons.setLayout(new WrappingLayout(5, 5, WrappingLayout.CENTER));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;

        frame.add(screen);

        JLabel label = new JLabel();

        JTextField userText = new JTextField();

        JButton yes = new JButton("Yes");
        JButton no = new JButton("No");
        JButton enter = new JButton("Enter");

        label.setFont(font);
        label.setText("Are you hosting?");
        label.setVisible(true);

        yes.setFocusPainted(false);
        yes.setBackground(Color.WHITE);
        yes.setFont(font);

        no.setFocusPainted(false);
        no.setBackground(Color.WHITE);
        no.setFont(font);

        enter.setFocusPainted(false);
        enter.setBackground(Color.WHITE);
        enter.setFont(font);

        labelPanel.add(label);

        buttons.add(yes);
        buttons.add(no);

        input.add(userText);
        input.add(enter);

        gbc.gridx = 0;
        gbc.gridy = 0;
        screen.add(labelPanel, gbc);
        gbc.gridy = 1;
        screen.add(buttons, gbc);
        screen.add(input, gbc);

        userText.setColumns(20);
        userText.setVisible(false);
        userText.setFont(font);
        enter.setVisible(false);

        frame.pack();
        frame.setSize(600, 200);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        final boolean[] hostingTemp = { false };
        yes.addActionListener(e -> {
            hostingTemp[0] = true;
            yes.setVisible(false);
            ;
            no.setVisible(false);
            ;
        });
        no.addActionListener(e -> {
            hostingTemp[0] = false;
            no.setVisible(false);
            ;
            yes.setVisible(false);
            ;
        });
        while (yes.isVisible() || no.isVisible()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        hosting = hostingTemp[0];
        ArrayList<Game> games = new ArrayList<Game>();
        games.add(new ReadWriteGame());
        games.add(new Uno());
        games.add(new Hangout());

        Session session;
        String sessionName;
        if (hosting) {
            label.setText("Enter the session name: ");
            userText.setVisible(true);
            enter.setVisible(true);

            final String[] sessionNameTemp = { "" };
            enter.addActionListener(e -> {
                sessionNameTemp[0] = userText.getText();
            });
            while (sessionNameTemp[0].equals("")) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (sessionSpacePath.resolve(sessionNameTemp[0]).toFile().exists()) {
                label.setText("Session already exists. Please enter a valid session name: ");
                sessionNameTemp[0] = "";
                enter.addActionListener(e -> {
                    sessionNameTemp[0] = userText.getText();
                });
                while (sessionNameTemp[0].equals("")) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            sessionName = sessionNameTemp[0];
            sessionName = encodeString(sessionName);
        } else {
            label.setText("click on the session you would like to join");
            userText.setVisible(false);
            enter.setVisible(false);

            ArrayList<JButton> sessionButtonsList = new ArrayList<>();
            final String[] sessionNameTemp = { "" };
            int numOfFiles = sessionSpacePath.toFile().list().length;
            while (sessionNameTemp[0].equals("")) {
                for (String i : sessionSpacePath.toFile().list()) {
                    final String rawSessionName = decodeString(i);
                    JButton sessionButton = new JButton(i);
                    sessionButton.setName(i);
                    boolean exists = false;
                    for (int j = 0; j < sessionButtonsList.size(); j++) {
                        if (sessionButtonsList.get(j)
                                .getName()
                                .equals(sessionButton.getName())) {
                            exists = true;
                        }
                    }
                    if (!exists) {
                        sessionButton.setVisible(true);
                        sessionButton.setFocusPainted(false);
                        sessionButton.setBackground(Color.WHITE);
                        Icon icon = new ImageIcon("src/images/folder.png");
                        sessionButton.setIcon(icon);
                        sessionButton.setHorizontalTextPosition(JButton.CENTER);
                        sessionButton.setVerticalTextPosition(JButton.BOTTOM);
                        sessionButtonsList.add(sessionButton);
                        sessionButtons.add(sessionButton);
                        sessionButton.addActionListener(e -> {
                            sessionNameTemp[0] = rawSessionName;
                            for (Component component : screen.getComponents()) {
                                if (component instanceof Button) {
                                    component.setVisible(false);
                                }
                            }
                        });
                    }
                }
                gbc.gridx = 0;
                gbc.gridy = 1;
                screen.add(scrollPane, gbc);
                sessionButtons.revalidate();
                sessionButtons.repaint();
                screen.revalidate();
                screen.repaint();
                while (sessionNameTemp[0].equals("")
                        && sessionSpacePath.toFile().list().length == numOfFiles) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                numOfFiles = sessionSpacePath.toFile().list().length;
            }
            sessionName = sessionNameTemp[0];
        }
        screen.remove(scrollPane);
        screen.revalidate();
        screen.repaint();
        userText.setText("");
        label.setText("Enter your name:");
        userText.setVisible(true);
        enter.setVisible(true);
        final String[] nameTemp = { "" };
        enter.addActionListener(e -> {
            nameTemp[0] = userText.getText();
        });
        while (nameTemp[0].equals("")) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (isValidName(nameTemp[0], sessionName, sessionSpacePath) != "") {
            String errorMessage = isValidName(nameTemp[0], sessionName, sessionSpacePath);
            label.setText(errorMessage);
            nameTemp[0] = "";
            enter.addActionListener(e -> {
                nameTemp[0] = userText.getText();
            });
            while (nameTemp[0].equals("")) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        String name = nameTemp[0];
        session = new Session(sessionName, name, games, hosting);
        frame.dispose();
        if (hosting) {
            session.host("");
        } else {
            session.join();
        }
    }

    public static String isValidName(String name, String sessionName, Path sessionSpacePath) {
        if (name.length() < 3 || name.length() > 16) {
            return "Name must be between 3 and 16 characters.";
        }
        String invalidChars = "#\\/:*?\"<>|()[]{};=+`~$&%";
        for (char c : invalidChars.toCharArray()) {
            if (name.indexOf(c) != -1) {
                return "Invalid character: " + c;
            }
        }
        if (sessionSpacePath.resolve(sessionName).resolve("players").resolve(name).toFile().exists()) {
            return "Name already exists.";
        }
        return "";
    }

    public static String encodeString(String s) {
        String delimiter = Config.getDelimiter();
        if (delimiter.equals("\\")) {
            delimiter = "\\\\";
        }
        s = s.replaceAll("#", "#" + "#");
        s = s.replaceAll(delimiter, "#" + "1");
        s = s.replaceAll("/", "#" + "2");
        s = s.replaceAll(":", "#" + "3");
        s = s.replaceAll("\\*", "#" + "4");
        s = s.replaceAll("\\?", "#" + "5");
        s = s.replaceAll("\"", "#" + "6");
        s = s.replaceAll("<", "#" + "7");
        s = s.replaceAll(">", "#" + "8");
        s = s.replaceAll("\\|", "#" + "9");
        return s;
    }

    public static String decodeString(String s) {
        String delimiter = Config.getDelimiter();
        if (delimiter.equals("\\")) {
            delimiter = "\\\\";
        }
        s = s.replaceAll("#" + "#", "#");
        s = s.replaceAll("#" + "1", delimiter);
        s = s.replaceAll("#" + "2", "/");
        s = s.replaceAll("#" + "3", ":");
        s = s.replaceAll("#" + "4", "*");
        s = s.replaceAll("#" + "5", "?");
        s = s.replaceAll("#" + "6", "\"");
        s = s.replaceAll("#" + "7", "<");
        s = s.replaceAll("#" + "8", ">");
        s = s.replaceAll("#" + "9", "|");
        return s;
    }
}
