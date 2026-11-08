import edu.fiu.cop3337.calendar.app.CalendarApp;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Test helper (not part of the application): runs CalendarApp on a display
 * (for example Xvfb), feeds it scripted console input, saves a PNG of every
 * JOptionPane dialog that appears, and answers it automatically.
 *
 * Usage: java -cp out:tests DialogCapture outputDir answer inputLine...
 * answer = yes or no, used for confirm dialogs.
 */
public class DialogCapture {
    public static void main(String[] args) throws Exception {
        File outDir = new File(args[0]);
        boolean yes = args[1].equalsIgnoreCase("yes");
        StringBuilder input = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            input.append(args[i]).append('\n');
        }
        System.setIn(new ByteArrayInputStream(input.toString().getBytes(StandardCharsets.UTF_8)));

        Thread watcher = new Thread(() -> watch(outDir, yes));
        watcher.setDaemon(true);
        watcher.start();

        CalendarApp.main(new String[0]);
        System.exit(0);
    }

    private static void watch(File outDir, boolean yes) {
        int count = 0;
        while (true) {
            try {
                Thread.sleep(400);
                for (Window w : Window.getWindows()) {
                    if (w instanceof JDialog && w.isShowing()) {
                        JDialog dialog = (JDialog) w;
                        Thread.sleep(300);
                        count++;
                        final int n = count;
                        SwingUtilities.invokeAndWait(() -> {
                            try {
                                BufferedImage img = new BufferedImage(dialog.getWidth() * 2,
                                        dialog.getHeight() * 2, BufferedImage.TYPE_INT_RGB);
                                java.awt.Graphics2D g = img.createGraphics();
                                g.scale(2, 2);
                                dialog.printAll(g);
                                g.dispose();
                                String name = String.format("dialog-%02d-%s.png", n,
                                        dialog.getTitle().replaceAll("[^A-Za-z]+", "-"));
                                ImageIO.write(img, "png", new File(outDir, name));
                                System.out.println("[captured " + name + "]");
                                JOptionPane pane = findPane(dialog.getContentPane());
                                Object value = isConfirm(pane)
                                        ? Integer.valueOf(yes ? JOptionPane.YES_OPTION : JOptionPane.NO_OPTION)
                                        : Integer.valueOf(JOptionPane.OK_OPTION);
                                pane.setValue(value);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private static boolean isConfirm(JOptionPane pane) {
        return pane.getOptionType() == JOptionPane.YES_NO_OPTION;
    }

    private static JOptionPane findPane(Container c) {
        for (Component child : c.getComponents()) {
            if (child instanceof JOptionPane) {
                return (JOptionPane) child;
            }
            if (child instanceof Container) {
                JOptionPane p = findPane((Container) child);
                if (p != null) {
                    return p;
                }
            }
        }
        return null;
    }
}
