package edu.fiu.cop3337.calendar.view;

import java.awt.GraphicsEnvironment;
import javax.swing.JOptionPane;

/**
 * Small JOptionPane helpers for the console application's Swing feedback.
 *
 * The console stays the main interface. Each helper also echoes to the
 * console. If no display is available (headless test runs), the dialog text
 * is printed with a [dialog] prefix instead, and an overwrite confirmation is
 * answered from the system property calendar.confirmOverwrite (yes/no,
 * default no), so a headless run can never overwrite a file by accident.
 */
public final class Dialogs {
    public static final String TITLE = "Calendar Organizer";

    private Dialogs() {
    }

    /** Prints the message and shows one ERROR_MESSAGE dialog. */
    public static void error(String message) {
        System.out.println("Error: " + message);
        show(message, "Calendar Organizer - Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Prints the message and shows one INFORMATION_MESSAGE dialog. */
    public static void info(String message) {
        System.out.println(message);
        show(message, TITLE, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Asks whether an existing file may be replaced. Only YES_OPTION returns
     * true; NO or closing the dialog returns false.
     */
    public static boolean confirmOverwrite(String path) {
        String message = "The file\n" + path + "\nalready exists. Overwrite it?";
        int choice;
        if (GraphicsEnvironment.isHeadless()) {
            boolean yes = "yes".equalsIgnoreCase(System.getProperty("calendar.confirmOverwrite", "no"));
            System.out.println("[dialog WARNING confirm] " + message.replace('\n', ' ')
                    + " -> " + (yes ? "Yes" : "No"));
            choice = yes ? JOptionPane.YES_OPTION : JOptionPane.NO_OPTION;
        } else {
            choice = JOptionPane.showConfirmDialog(null, message, "Confirm Overwrite",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        }
        return choice == JOptionPane.YES_OPTION;
    }

    private static void show(String message, String title, int type) {
        if (GraphicsEnvironment.isHeadless()) {
            String label = type == JOptionPane.ERROR_MESSAGE ? "ERROR" : "INFORMATION";
            System.out.println("[dialog " + label + "] " + message.replace('\n', ' '));
        } else {
            JOptionPane.showMessageDialog(null, message, title, type);
        }
    }
}
