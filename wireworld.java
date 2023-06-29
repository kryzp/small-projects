import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Main {
	
	public static final int CELL_STATE_EMPTY = 0;
	public static final int CELL_STATE_CONDUCTOR = 1;
	public static final int CELL_STATE_HEAD = 2;
	public static final int CELL_STATE_TAIL = 3;
	public static final int CELL_STATE_MAX = 4;

	public static final int WIDTH     = 64;
	public static final int HEIGHT    = 64;
	public static final int CELL_SIZE = 16;
	public static final int DELAY_BETWEEN_UPDATE_MS = 75;

	public static JFrame frame = null;

	public static boolean isRunning = false;

	public static int[] states = new int[WIDTH * HEIGHT];

	public static void draw(Graphics g) {
		var lineColour = new Color(39, 42, 51);
		var emptyColour = new Color(22, 26, 31);
		var electronHead = new Color(107, 208, 226);
		var electronTail = new Color(255, 0, 58);
		var conductor = new Color(242, 248, 64);

		// clear screen
		g.setColor(emptyColour);
		g.fillRect(0, 0, WIDTH * CELL_SIZE, HEIGHT * CELL_SIZE);

		// draw horizontal grid lines
		for (int y = 0; y < HEIGHT; y++) {
			g.setColor(lineColour);
			g.drawLine(0, y * CELL_SIZE, WIDTH * CELL_SIZE, y * CELL_SIZE);
		}

		// drw vertical grid lines
		for (int x = 0; x < WIDTH; x++) {
			g.setColor(lineColour);
			g.drawLine(x * CELL_SIZE, 0, x * CELL_SIZE, HEIGHT * CELL_SIZE);
		}

		// draw points
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				int st = states[(y * WIDTH) + x];
				if (st == CELL_STATE_HEAD) {
					g.setColor(electronHead);
				} else if (st == CELL_STATE_TAIL) {
					g.setColor(electronTail);
				} else if (st == CELL_STATE_CONDUCTOR) {
					g.setColor(conductor);
				} else {
					continue;
				}
				g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
			}
		}
	}

	public static void tick() {
		int[] newState = new int[WIDTH * HEIGHT];
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = 0; x < WIDTH; x++) {
				int idx = (y * WIDTH) + x;
				int here = states[idx];
				if (here == CELL_STATE_EMPTY) {
					newState[idx] = CELL_STATE_EMPTY;
				} else if (here == CELL_STATE_HEAD) {
					newState[idx] = CELL_STATE_TAIL;
				} else if (here == CELL_STATE_TAIL) {
					newState[idx] = CELL_STATE_CONDUCTOR;
				} else if (here == CELL_STATE_CONDUCTOR) {
					int n = 0;
					for (int dy = -1; dy <= 1; dy++) {
						for (int dx = -1; dx <= 1; dx++) {
							if (dx == 0 && dy == 0) {
								continue;
							}
							int px = x + dx;
							int py = y + dy;
							if (px < 0 || py < 0 || px >= WIDTH || py >= HEIGHT) {
								continue;
							}
							int idx2 = (py * WIDTH) + px;
							int st2 = states[idx2];
							if (st2 == CELL_STATE_HEAD) {
								n++;
							}
						}
					}
					if (n == 1 || n == 2) {
						newState[idx] = CELL_STATE_HEAD;
					} else {
						newState[idx] = CELL_STATE_CONDUCTOR;
					}
				}
			}
		}
		for (int i = 0; i < WIDTH * HEIGHT; i++) {
			states[i] = newState[i];
		}
	}

	public static void main(String[] args) throws InterruptedException {
		frame = new JFrame("Wireworld");
		frame.setVisible(true);
		frame.setFocusable(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(WIDTH * CELL_SIZE, 28 /* mystery constant because java swing doesn't play nice with the top of the macos window for some reason ??? */ + HEIGHT * CELL_SIZE);

		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int)((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int)((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);

		var content = new JComponent() {
			@Override
			public void paintComponent(Graphics g) {
				draw(g);
			}
		};

		content.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					var mp = frame.getContentPane().getMousePosition();
					int px = (int)(mp.getX() / CELL_SIZE);
					int py = (int)(mp.getY() / CELL_SIZE);
					int idx = (py * WIDTH) + px;
					states[idx] = (states[idx] + 1) % CELL_STATE_MAX;
					Main.frame.repaint();
				}
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

		frame.setContentPane(content);

		while (true) {
			if (isRunning) {
				tick();
				frame.repaint();
			}
			Thread.sleep(isRunning ? DELAY_BETWEEN_UPDATE_MS : 10);
		}
	}
}