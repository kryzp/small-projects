import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main extends JComponent {
    private static final String GRADIENT = " .:-=+*#%@";

    private static BufferedImage toGrayScale(BufferedImage in) {
        BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < in.getHeight(); y++) {
            for (int x = 0; x < in.getWidth(); x++) {
                Color incol = new Color(in.getRGB(x, y));
                int avg = (incol.getRed() + incol.getGreen() + incol.getBlue()) / 3;
                out.setRGB(x, y, new Color(avg, avg, avg).getRGB());
            }
        }

        return out;
    }

    private static void convert(BufferedImage in) throws IOException {
        System.out.println("Started Converting...");

        FileOutputStream ff = new FileOutputStream("/Users/kryzp/Documents/output.txt");

        for (int y = 0; y < in.getHeight(); y++) {
            for (int x = 0; x < in.getWidth(); x++) {
                Color incol = new Color(in.getRGB(x, y));
                int idx = (int)((float)((incol.getRed() + incol.getGreen() + incol.getBlue()) / 3) / 255f * (GRADIENT.length() - 1));
                ff.write((GRADIENT.charAt(idx) + " ").getBytes());
            }
            ff.write("\n".getBytes());
        }

        ff.close();

        System.out.println("Converted! Written out to /Users/kryzp/Documents/output.txt");
    }

    public static void main(String[] args) throws IOException, UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        BufferedImage img = ImageIO.read(new File("/Users/kryzp/Downloads/meow.png"));
        final BufferedImage finalImg = toGrayScale(img);

        UIManager.setLookAndFeel(new NimbusLookAndFeel());

        JFrame frame = new JFrame("Image 2 Ascii");
        {
            frame.setVisible(true);
            frame.setResizable(true);
            frame.setFocusable(true);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(1024, 1024);

            var content = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    g.drawImage(finalImg, 17, 40, null);
                }
            };

            JButton conversionBtn = new JButton("Convert");
            conversionBtn.addActionListener(a -> new Thread(() -> {
                try {
                    convert(finalImg);
                    JOptionPane.showMessageDialog(frame, "finidhed conveting!", "finished conversting (burh momen)", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start());
            content.add(conversionBtn);

            content.setLayout(new FlowLayout());

            frame.setContentPane(content);
        }
    }
}
