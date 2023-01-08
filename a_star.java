import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class Main {
    public record Point(int x, int y) { }

    public static final Point START     = new Point(0, 0);
    public static final Point END       = new Point(25, 15);
    public static final int   WIDTH     = Math.abs(END.x - START.x + 5);
    public static final int   HEIGHT    = Math.abs(END.y - START.y + 6);
    public static final int   CELL_SIZE = 32;

    public static ArrayList<Point> getNeighbours(Point p, Point goal, ArrayList<Point> walls) {
        ArrayList<Point> result = new ArrayList<Point>();
        var left  = new Point(p.x - 1, p.y);
        var right = new Point(p.x + 1, p.y);
        var down  = new Point(p.x, p.y - 1);
        var up    = new Point(p.x, p.y + 1);
        if (left.x >= 0 && !walls.contains(left))
            result.add(left);
        if (right.x >= 0 && !walls.contains(right))
            result.add(right);
        if (up.y >= 0 && !walls.contains(up))
            result.add(up);
        if (down.y >= 0 && !walls.contains(down))
            result.add(down);
        return result;
    }

    public static ArrayList<Point> getPath(Map<Point, Point> cameFrom, Point to) {
        ArrayList<Point> result = new ArrayList<Point>();
        result.add(to);
        while (cameFrom.containsKey(to)) {
            to = cameFrom.get(to);
            result.add(0, to);
        }
        return result;
    }

    public static ArrayList<Point> aStar(Point start, Point goal, ArrayList<Point> walls) {
        Set<Point> discovered = new HashSet<Point>();
        discovered.add(start);
        Map<Point, Point> cameFrom = new HashMap<Point, Point>();
        Map<Point, Integer> cost = new HashMap<Point, Integer>();
        for (int y = start.y; y <= goal.y + 5; y++) {
            for (int x = start.x; x <= goal.x + 4; x++) {
                cost.put(new Point(x, y), Integer.MAX_VALUE);
            }
        }
        cost.replace(start, 0);
        while (!discovered.isEmpty()) {
            Point curr = null;
            int minFScore = Integer.MAX_VALUE;
            for (var node : discovered) {
                int val = cost.get(node) + 1;
                if (val < minFScore) {
                    minFScore = val;
                    curr = node;
                }
            }
            if (curr.equals(goal)) {
                return getPath(cameFrom, goal);
            }
            discovered.remove(curr);
            for (var n : getNeighbours(curr, goal, walls)) {
                int neighbourCost = cost.get(curr) + 1;
                var myCost = cost.get(n);
                if (myCost == null) {
                    continue;
                }
                if (neighbourCost < myCost) {
                    cameFrom.put(n, curr);
                    cost.replace(n, neighbourCost);
                    discovered.add(n);
                }
            }
        }
        return new ArrayList<Point>();
    }

    public static void draw(Graphics g, ArrayList<Point> points, ArrayList<Point> walls) {
        var lineColour = new Color(39, 42, 51);
        var mainColour = new Color(255, 0, 58);
        var accentColour0 = new Color(242, 248, 64);
        var accentColour1 = new Color(107, 226, 116);
        var backColour = new Color(22, 26, 31);

        g.setColor(backColour);
        g.fillRect(0, 0, WIDTH * CELL_SIZE, HEIGHT * CELL_SIZE);

        for (int y = 0; y < HEIGHT; y++) {
            g.setColor(lineColour);
            g.drawLine(0, y * CELL_SIZE, WIDTH * CELL_SIZE, y * CELL_SIZE);
        }

        for (int x = 0; x < WIDTH; x++) {
            g.setColor(lineColour);
            g.drawLine(x * CELL_SIZE, 0, x * CELL_SIZE, HEIGHT * CELL_SIZE);
        }

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                if (new Point(x, y).equals(START) || new Point(x, y).equals(END)) {
                    g.setColor(accentColour0);
                } else if (walls.contains(new Point(x, y))) {
                    g.setColor(accentColour1);
                } else if (points.contains(new Point(x, y))) {
                    g.setColor(mainColour);
                } else {
                    continue;
                }
                g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    public static ArrayList<Point> path = null;
    public static ArrayList<Point> walls = null;

    public static JFrame frame = null;

    volatile public static boolean isPlacing = false;
    volatile public static boolean isRemoving = false;
    volatile public static boolean isRunning = false;

    public static void recompute() {
        path = aStar(START, END, walls);
    }

    private static boolean checkAndMark() {
        if (isRunning)
            return !isRunning;
        isRunning = true;
        return isRunning;
    }

    private static void initThread() {
        if (checkAndMark()) {
            new Thread(() -> {
                do {
                    int px = (int)(frame.getContentPane().getMousePosition().getX() / CELL_SIZE);
                    int py = (int)(frame.getContentPane().getMousePosition().getY() / CELL_SIZE);
                    Main.walls.add(new Point(px, py));
                    Main.recompute();
                    Main.frame.repaint();
                } while (isPlacing);
                do {
                    int px = (int)(frame.getContentPane().getMousePosition().getX() / CELL_SIZE);
                    int py = (int)(frame.getContentPane().getMousePosition().getY() / CELL_SIZE);
                    Main.walls.remove(new Point(px, py));
                    Main.recompute();
                    Main.frame.repaint();
                } while (isRemoving);
                isRunning = false;
            }).start();
        }
    }

    public static void main(String[] args) {
        walls = new ArrayList<Point>();

        recompute();

        frame = new JFrame("A* Visualizer");
        frame.setVisible(true);
        frame.setFocusable(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(WIDTH * CELL_SIZE, 28 + HEIGHT * CELL_SIZE);

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int)((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int)((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);

        var content = new JComponent() {
            @Override
            public void paintComponent(Graphics g) {
                draw(g, Main.path, Main.walls);
            }
        };

        content.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    isPlacing = true;
                } else {
                    isRemoving = true;
                }
                initThread();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    isPlacing = false;
                } else {
                    isRemoving = false;
                }
            }
        });

        frame.setContentPane(content);
    }
}
