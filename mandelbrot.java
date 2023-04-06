import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class Main {

	private static int MAX_ITERATIONS = 20;
	private static final int ORBIT_LIMIT_SQUARED = 100;

	// size is 2x drawing size because this increases the resolution on my retina display.
	private static final double SIZE = 1024;

	private static double camX = 0.0;
	private static double camY = 0.0;
	private static double camSize = SIZE;
	private static double camOrigX = (double)SIZE * 2.0 / 3.0;
	private static double camOrigY = (double)SIZE / 2.0;

	private static JFrame frame;

	private static boolean isMouseThreadRunning = false;
	private static boolean doRepaint = true;
	private static boolean drawingRect = false;
	private static Point rectZoomPointInitial;
	private static Point rectZoomPointFinal;

	private static BufferedImage mandelOut;

	public static void main(String[] args) throws UnsupportedLookAndFeelException {
		mandelOut = new BufferedImage((int)SIZE, (int)SIZE, BufferedImage.TYPE_INT_RGB);
		setupWindow();
	}

	private static double baseChannel(double t) {
		return Math.max(0, 1.0 - (5.0 * t * t));
	}

	private static Color gradient(double t) {
		return new Color(
			(int)(255.0 * baseChannel(t - 0.0)),
			(int)(255.0 * baseChannel(t - 0.8)),
			(int)(255.0 * baseChannel(t - 0.5))
		);
	}

	private static void applyDrawnRect() {
		camX += 2 * rectZoomPointInitial.x * camSize / SIZE;
		camY += 2 * rectZoomPointInitial.y * camSize / SIZE;
		camSize = Math.abs(rectZoomPointFinal.x - rectZoomPointInitial.x) * 2 * camSize / SIZE;
	}

	private static void drawMandelbrot(Graphics g) {
		if (!drawingRect) {
			for (int y = 0; y < SIZE; y++) {
				for (int x = 0; x < SIZE; x++) {

					double rc = (double)((x / SIZE * camSize) + camX - camOrigX) / (SIZE * 0.3);
					double ic = (double)((y / SIZE * camSize) + camY - camOrigY) / (SIZE * 0.3);
					double rz = 0.0;
					double iz = 0.0;

					int i = 0;
					for (; i < MAX_ITERATIONS; i++) {
						double rzz = rc + ((rz * rz) - (iz * iz));
						double izz = ic + (2.0 * rz * iz);
						rz = rzz;
						iz = izz;
						if (((rz * rz) + (iz * iz)) > ORBIT_LIMIT_SQUARED) {
							break;
						}
					}

					Color colour = Color.BLACK;

					if (i + 1 < MAX_ITERATIONS) {
						colour = gradient((double)i / (double)MAX_ITERATIONS);
					} else {
						colour = Color.BLACK;
					}

					mandelOut.setRGB(x, y, colour.getRGB());
				}
			}
		}

		g.drawImage(mandelOut, 0, 0, (int)(SIZE / 2.0), (int)(SIZE / 2.0), null);
	}

	private static void drawZoomRectangle(Graphics g) {
		if (rectZoomPointInitial == null || rectZoomPointFinal == null) {
			return;
		}
		g.drawRect(
			Math.min(rectZoomPointInitial.x, rectZoomPointFinal.x),
			Math.min(rectZoomPointInitial.y, rectZoomPointFinal.y),
			Math.abs(rectZoomPointFinal.x - rectZoomPointInitial.x),
			Math.abs(rectZoomPointFinal.x - rectZoomPointInitial.x)
		);
	}

	private static void setupWindow() throws UnsupportedLookAndFeelException {

		UIManager.setLookAndFeel(new NimbusLookAndFeel());

		// initialize
		frame = new JFrame("Mandelbrot Set");
		frame.setVisible(true);
		frame.setFocusable(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize((int)(SIZE / 2.0), (int)(SIZE / 2.0));

		// centre window
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int)((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int)((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);

		frame.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (!drawingRect) {
					rectZoomPointInitial = frame.getMousePosition();
					rectZoomPointInitial.y -= 30.0;
					drawingRect = true;
				}
				initMouseRectThread();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				drawingRect = false;
				applyDrawnRect();
			}
		});

		var content = new JComponent() {
			@Override
			public void paintComponent(Graphics g) {
				drawMandelbrot(g);
				if (drawingRect) {
					drawZoomRectangle(g);
				}
			}
		};

		JButton decBtn = new JButton("-");
		decBtn.addActionListener(a -> new Thread(() -> {
			doRepaint = false;
			MAX_ITERATIONS -= 5;
			frame.repaint();
		}).start());
		content.add(decBtn);

		JButton resetBtn = new JButton("Reset");
		resetBtn.addActionListener(a -> new Thread(() -> {
			camX = 0.0;
			camY = 0.0;
			camSize = SIZE;
			frame.repaint();
		}).start());
		content.add(resetBtn);

		JButton incBtn = new JButton("+");
		incBtn.addActionListener(a -> new Thread(() -> {
			doRepaint = false;
			MAX_ITERATIONS += 5;
			frame.repaint();
		}).start());
		content.add(incBtn);

		content.setLayout(new FlowLayout());

		frame.setContentPane(content);
		frame.repaint();
	}

	private static boolean checkAndMarkMouseRectThread() {
		if (isMouseThreadRunning) {
			return false;
		}
		isMouseThreadRunning = true;
		return true;
	}

	private static void initMouseRectThread() {
		if (checkAndMarkMouseRectThread()) {
			new Thread(() -> {
				do {
					rectZoomPointFinal = frame.getMousePosition();
					rectZoomPointFinal.y -= 30.0;
					frame.repaint();
				} while(drawingRect);
				isMouseThreadRunning = false;
			}).start();
		}
	}
}
