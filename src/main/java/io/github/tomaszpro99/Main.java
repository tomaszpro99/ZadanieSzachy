package io.github.tomaszpro99;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import javax.swing.*;

class Team {
    String name;
    int speed;

    public Team(String name, int speed) {
        this.name = name;
        this.speed = speed;
    }
}
class Figure {
    int x, y;

    public Figure(int x, int y) {
        this.x = x;
        this.y = y;
    }
}

public class Main extends JFrame
{
    public Main() throws IOException {
        //PrintWriter plik = new PrintWriter(new FileWriter("input.txt"/*, true*/)); //stworzenie pliku
        BufferedReader reader = new BufferedReader(new FileReader("input.txt")); //odczyt pliku

        // Input parametry
        String teamAName = reader.readLine();
        int teamASpeed = Integer.parseInt(reader.readLine());
        String teamBName = reader.readLine();
        int teamBSpeed = Integer.parseInt(reader.readLine());
        int sizeX = Integer.parseInt(reader.readLine());
        int sizeY = Integer.parseInt(reader.readLine());
        reader.close();

        // Walidacja parametrów
        // Program powinien być odporny na błędne dane wejściowe i w przypadku błędnych danych wejściowych, program powinien wypisać wyłącznie słowo "error"
        if (!isValidTeamName(teamAName) || !isValidTeamName(teamBName) || !isValidSpeed(teamASpeed) || !isValidSpeed(teamBSpeed)
                || !isValidSize(sizeX) || !isValidSize(sizeY)) {
            System.out.println("error");
            return;
        }

        // Drużyny
        Team teamA = new Team(teamAName, teamASpeed);
        Team teamB = new Team(teamBName, teamBSpeed);

//        System.out.println(teamAName);
//        System.out.println(teamASpeed);
//        System.out.println(teamBName);
//        System.out.println(teamBSpeed);
//        System.out.println(sizeX);
//        System.out.println(sizeY);

        this.setTitle("Symulacja poruszania się figur po planszy");
        this.setBounds(300, 10, (50 * sizeX) + 14, (50 * sizeY) + 73);

        JPanel panelButtonow = new JPanel(); //panel przycisków
        PanelAnimacji panelAnimacji = new PanelAnimacji(teamA, teamB, sizeX, sizeY); //panel animacji

        // Przyciski START STOP
        JButton bStart = (JButton)panelButtonow.add(new JButton("Start")); //dodajemy przycisk rzutowany na JBuuton
        bStart.addActionListener(new ActionListener() { //akcja - start animacji
            public void actionPerformed(ActionEvent e) //metoda abstract
            {
                try {
                    panelAnimacji.startAnimation(); // wywolaj animacje
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                bStart.setEnabled(false);
            }
        });
        JButton bStop = (JButton)panelButtonow.add(new JButton("Stop")); //dodajemy przycisk rzutowany na JBuuton
        bStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                panelAnimacji.stopAnimation();
                bStart.setEnabled(true);
            }
        });

        this.getContentPane().add(panelAnimacji); //ekran
        this.getContentPane().add(panelButtonow, BorderLayout.SOUTH); //panel z przyciskiem na dole
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //operacja zamkniecia
    }

    // Validacja parametrów
    private static boolean isValidTeamName(String name) { return name.matches("[a-zA-Z0-9]{1,10}"); }
    private static boolean isValidSpeed(int speed) { return speed >= 1 && speed <= 3; }
    private static boolean isValidSize(int size) { return size >= 1 && size <= 1000; }

    //public static void main(String[] args)
    public static void main(String[] args) throws IOException {
        new Main().setVisible(true);
    }

    //Panel Animacji
    static class PanelAnimacji extends JPanel {
        private final Team teamA;
        private final Team teamB;
        private final ArrayList<Figure> teamAList = new ArrayList<>();
        private final ArrayList<Figure> teamBList = new ArrayList<>();
        private final int sizeX;
        private final int sizeY;
        private final int cellSize = 50; // Rozmiar kratek (px)
        private Timer timer; // Deklaracja timera

        public PanelAnimacji(Team teamA, Team teamB, int sizeX, int sizeY) {
            this.teamA = teamA;
            this.teamB = teamB;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
        }
        // Rysowanie komponentów
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Rysowanie szachownicy
            drawSzachownica(g);
            // Rysowanie pionków
            drawTeamA(g);
            drawTeamB(g);
        }
        private void drawSzachownica(Graphics g) {
            for (int row = 0; row < sizeY; row++) {
                for (int col = 0; col < sizeX; col++) {
                    if ((row + col) % 2 == 0) {
                        g.setColor(Color.gray);
                    } else {
                        g.setColor(Color.white);
                    }
                    int x = col * cellSize;
                    int y = row * cellSize;
                    g.fillRect(x, y, cellSize, cellSize);
                }
            }
        }
        private void drawTeamA(Graphics g) {
            g.setColor(Color.black);
            for (Figure figure : teamAList) {
                int centerX = figure.x * cellSize + cellSize / 2;
                int centerY = figure.y * cellSize + cellSize / 2;
                int radius = cellSize / 4;
                g.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            }
        }
        private void drawTeamB(Graphics g) {
            g.setColor(Color.lightGray);
            for (Figure figure : teamBList) {
                int centerX = figure.x * cellSize + cellSize / 2;
                int centerY = figure.y * cellSize + cellSize / 2;
                int radius = cellSize / 4;
                g.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            }
        }

        // START
        public void startAnimation() throws InterruptedException {
            teamAList.clear();
            teamBList.clear();

            newFigures();
            repaint();

            // Stworzenie Timera 1s
            timer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    moveFigures();
                    checkCollisions();
                    repaint();
                }
            });
            timer.start();
        }
        // STOP
        public void stopAnimation() {
            timer.stop();
        }
        // Figury
        // Nowe figury
        private void newFigures() {
            for (int i = 0; i < sizeX; i++) {
                addFigureToTeamA(i, 0); // Drużyna A zaczyna z góry planszy
                addFigureToTeamB(i, sizeY - 1); // Drużyna B zaczyna z dołu planszy
            }
        }
        // Dodawanie figur do drużyn
        public void addFigureToTeamA(int x, int y) { teamAList.add(new Figure(x, y)); }
        public void addFigureToTeamB(int x, int y) { teamBList.add(new Figure(x, y)); }


        // Ruch figur
        private void moveFigures() {
            for (Figure figure : teamAList) {
                int speedA;
                if (figure.x % 2 == 0) {
                    // Odwrotnie ponieważ uwzględniamy x=0! (np pozycja druga to x=1 (nieparzysta))
                    // Dla figur o nieparzystej współrzędnej x
                    // v = 2 ^ <współczynnik_prędkości_drużyny>
                    speedA = (int) Math.pow(2, teamA.speed);
                } else {
                    // Dla figur o parzystej współrzędnej x
                    // v = 1 * <współczynnik_prędkości_drużyny>
                    speedA = 1 * teamA.speed;
                }
                figure.y += speedA;
            }

            for (Figure figure : teamBList) {
                int speedB;
                // Odwrotnie ponieważ uwzględniamy x=0! (np pozycja druga to x=1 (nieparzysta))
                if (figure.x % 2 == 0) {
                    // Dla figur o nieparzystej współrzędnej x
                    // v = 2 ^ <współczynnik_prędkości_drużyny>
                    speedB = (int) Math.pow(2, teamB.speed);
                } else {
                    // Dla figur o parzystej współrzędnej x
                    // v = 1 * <współczynnik_prędkości_drużyny>
                    speedB = 1 * teamB.speed;
                }
                figure.y -= speedB;
            }
        }

        // Kolizje figur
        private void checkCollisions() {
            ArrayList<Figure> toRemove = new ArrayList<>();
            for (Figure a : teamAList) {
                for (Figure b : teamBList) {
                    if (a.x == b.x && a.y == b.y) {
                        // Jeśli dwie figury pojawią się w tym samym miejscu, figura o mniejszej wartości bezwzględnej prędkości jest likwidowana.
                        if (Math.abs(teamA.speed) < Math.abs(teamB.speed)) {
                            toRemove.add(a);
                        } else if (Math.abs(teamA.speed) > Math.abs(teamB.speed)) {
                            toRemove.add(b);
                        } else {
                            // W przypadku remisu, usuwane są obydwie figury.
                            toRemove.add(a);
                            toRemove.add(b);
                        }
                    }
                }
            }
            teamAList.removeAll(toRemove);
            teamBList.removeAll(toRemove);

            // Usuwane są również figury opuszczające planszę.
            for (Figure figure : new ArrayList<>(teamAList)) {
                if (figure.y >= sizeY || figure.y < 0) {
                    teamAList.remove(figure);
                }
            }
            for (Figure figure : new ArrayList<>(teamBList)) {
                if (figure.y >= sizeY || figure.y < 0) {
                    teamBList.remove(figure);
                }
            }

            // Symulacja kończy się w momencie kiedy jedna z drużyn nie posiada już żadnych figur.
            if (teamAList.isEmpty() && teamBList.isEmpty()) {
                System.out.println("remis");
                timer.stop();
            } else if (teamAList.isEmpty()) {
                System.out.println(teamB.name);
                timer.stop();
            } else if (teamBList.isEmpty()) {
                System.out.println(teamA.name);
                timer.stop();
            }
        }
    }
}