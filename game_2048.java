import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class Main {
    public static int[][] board = new int[4][4];

    public static void main(String[] args) {
        final int Y_BORDER_EXTRA = 30; // macos??

        board[1][1] = 2;

        JFrame frame = new JFrame("2048");
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setFocusable(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(512, 512 + Y_BORDER_EXTRA);
        frame.setBackground(new Color(232, 225, 197));

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int k = e.getKeyCode();
                switch (k) {
                    case KeyEvent.VK_LEFT:
                        for (int y = 0; y < 4; y++) {
                            int x = 1;
                            while (x < 4 && x >= 0) {
                                int here = board[y][x];
                                int nxt = board[y][x - 1];
                                if (here != 0) {
                                    if (nxt == 0) {
                                        board[y][x - 1] = here;
                                        board[y][x] = 0;
                                        if (x >= 2)
                                            x -= 2;
                                    } else if (nxt == here) {
                                        board[y][x - 1] = here * 2;
                                        board[y][x] = 0;
                                    }
                                }
                                x++;
                            }
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                        for (int y = 0; y < 4; y++) {
                            int x = 2;
                            while (x < 4 && x >= 0) {
                                int here = board[y][x];
                                int nxt = board[y][x + 1];
                                if (here != 0) {
                                    if (nxt == 0) {
                                        board[y][x + 1] = here;
                                        board[y][x] = 0;
                                        if (x < 2)
                                            x += 2;
                                    } else if (nxt == here) {
                                        board[y][x + 1] = here * 2;
                                        board[y][x] = 0;
                                    }
                                }
                                x--;
                            }
                        }
                        break;
                    case KeyEvent.VK_UP:
                        for (int x = 0; x < 4; x++) {
                            int y = 1;
                            while (y < 4 && y >= 0) {
                                int here = board[y][x];
                                int nxt = board[y - 1][x];
                                if (here != 0) {
                                    if (nxt == 0) {
                                        board[y - 1][x] = here;
                                        board[y][x] = 0;
                                        if (y >= 2)
                                            y -= 2;
                                    } else if (nxt == here) {
                                        board[y - 1][x] = here * 2;
                                        board[y][x] = 0;
                                    }
                                }
                                y++;
                            }
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                        for (int x = 0; x < 4; x++) {
                            int y = 2;
                            while (y < 4 && y >= 0) {
                                int here = board[y][x];
                                int nxt = board[y + 1][x];
                                if (here != 0) {
                                    if (nxt == 0) {
                                        board[y + 1][x] = here;
                                        board[y][x] = 0;
                                        if (y < 2)
                                            y += 2;
                                    } else if (nxt == here) {
                                        board[y + 1][x] = here * 2;
                                        board[y][x] = 0;
                                    }
                                }
                                y--;
                            }
                        }
                        break;
                }
                Random rng = new Random();
                int cx = -1;
                int cy = -1;
                do {
                    cx = rng.nextInt(0, 4);
                    cy = rng.nextInt(0, 4);
                } while (board[cy][cx] == 0);
                int emptyX = -1;
                int emptyY = -1;
                do {
                    emptyX = rng.nextInt(0, 4);
                    emptyY = rng.nextInt(0, 4);
                } while (board[emptyY][emptyX] != 0);
                board[emptyY][emptyX] = board[cy][cx];
                frame.repaint();
                boolean anyEmpty = false;
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        if (board[i][j] == 0) {
                            anyEmpty = true;
                            break;
                        }
                    }
                    if (anyEmpty) {
                        break;
                    }
                }
                if (!anyEmpty) {
                    System.out.println("Finished!");
                    System.exit(0);
                }
            }
        });

        var content = new JComponent() {
            @Override
            public void paintComponent(Graphics g) {
                final Font FONT = g.getFont().deriveFont(30f);
                g.setFont(FONT);

                final int PADDING_OUTSIDE = 20;
                g.setColor(new Color(161, 101, 88));
                g.fillRoundRect(PADDING_OUTSIDE, PADDING_OUTSIDE, frame.getWidth() - 2*PADDING_OUTSIDE, frame.getHeight() - 2*PADDING_OUTSIDE - Y_BORDER_EXTRA, 20, 20);

                final int PADDING_INSIDE = 10;
                final int BLOCK_SIZE = (frame.getWidth() - 2*PADDING_OUTSIDE - 5*PADDING_INSIDE) / 4;
                final int OFF = PADDING_OUTSIDE + PADDING_INSIDE;
                final int DT = PADDING_INSIDE + BLOCK_SIZE;
                for (int y = 0; y < 4; y++) {
                    for (int x = 0; x < 4; x++) {
                        int px = OFF + x*DT;
                        int py = OFF + y*DT;

                        g.setColor(new Color(213, 190, 163));
                        g.fillRoundRect(px, py, BLOCK_SIZE, BLOCK_SIZE, 20, 20);

                        if (board[y][x] != 0) {
                            String str = String.valueOf(board[y][x]);
                            int xo = BLOCK_SIZE/2 - (int)g.getFontMetrics().getStringBounds(str, null).getWidth()/2;
                            int yo = BLOCK_SIZE/2 + (int)g.getFontMetrics().getStringBounds(str, null).getHeight()/3;

                            g.setColor(new Color(161, 101, 88));
                            g.drawString(str, px + xo, py + yo);
                        }
                    }
                }
            }
        };

        frame.setContentPane(content);
    }
}
