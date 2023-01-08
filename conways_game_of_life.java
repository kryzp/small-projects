import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Main {
    public static final int SIM_SIZE = 64;
    public static final int CELL_SIZE = 16;
    public static final int DELAY_BETWEEN_UPDATE_MS = 25;

    private static boolean isRunning = false;
    private static boolean isPlacing = false;

    private static boolean[][] cells = new boolean[SIM_SIZE][SIM_SIZE];

    public static void main(String[] args) throws InterruptedException {

        JFrame frame = new JFrame("Conways Game of Life");
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setFocusable(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(SIM_SIZE * CELL_SIZE, SIM_SIZE * CELL_SIZE);

        frame.setContentPane(new JComponent() {
            @Override
            public void paintComponent(Graphics g) {
                draw(g);
            }
        });

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    isRunning = !isRunning;
                }
            }
        });

        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isPlacing = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPlacing = false;
            }
        });

        record Update(int x, int y, boolean state) { };

        while (true) {
            if (isPlacing) {
                var point = frame.getMousePosition();
                if (point != null) {
                    setCell(
                        point.x / CELL_SIZE,
                        (point.y - 29) / CELL_SIZE
                    );
                }
            }

            if (isRunning) {
                ArrayList<Update> updates = new ArrayList<Update>();

                for (int y = 0; y < SIM_SIZE; y++) {
                    for (int x = 0; x < SIM_SIZE; x++) {
                        int s = aliveState(x, y);
                        int n = aliveNeighbours(x, y);
                        if ((n < 2 || n > 3) && s == 1) {
                            updates.add(new Update(x, y, false));
                        } else if (n == 3 && s == 0) {
                            updates.add(new Update(x, y, true));
                        }
                    }
                }

                for (var update : updates) {
                    cells[update.y][update.x] = update.state;
                }
            }

            frame.repaint();
            Thread.sleep(isRunning ? DELAY_BETWEEN_UPDATE_MS : 10);
        }
    }

    private static void setCell(int x, int y) {
        cells[y][x] = true;
    }

    private static void draw(Graphics g) {
        var linecol = new Color(39, 42, 51);
        var oncol = new Color(255, 0, 86);
        var offcol = new Color(22, 26, 31);

        g.setColor(offcol);
        g.fillRect(0, 0, SIM_SIZE * CELL_SIZE, SIM_SIZE * CELL_SIZE);

        if (!isRunning) {
            for (int y = 0; y < SIM_SIZE; y++) {
                g.setColor(linecol);
                g.drawLine(0, y * CELL_SIZE, SIM_SIZE * CELL_SIZE, y * CELL_SIZE);
            }

            for (int x = 0; x < SIM_SIZE; x++) {
                g.setColor(linecol);
                g.drawLine(x * CELL_SIZE, 0, x * CELL_SIZE, SIM_SIZE * CELL_SIZE);
            }
        }

        for (int y = 0; y < SIM_SIZE; y++) {
            for (int x = 0; x < SIM_SIZE; x++) {
                if (!cells[y][x]) {
                    continue;
                }
                g.setColor(oncol);
                g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private static int aliveNeighbours(int x, int y) {
        return (
            aliveState(x - 1, y - 1) +
            aliveState(x + 0, y - 1) +
            aliveState(x + 1, y - 1) +
            aliveState(x - 1, y + 0) +
            aliveState(x + 1, y + 0) +
            aliveState(x - 1, y + 1) +
            aliveState(x + 0, y + 1) +
            aliveState(x + 1, y + 1)
        );
    }

    private static int aliveState(int x, int y) {
        if (x < 0 || x >= SIM_SIZE || y < 0 || y >= SIM_SIZE) {
            return 0;
        }
        return cells[y][x] ? 1 : 0;
    }
}
