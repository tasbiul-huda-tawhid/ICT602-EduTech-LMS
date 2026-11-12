package edutech.view;

import java.io.Console;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * InputHelper
 *
 * Purpose: The ONLY class that reads keyboard input. Every view receives the
 * same InputHelper, so the program never opens a second Scanner on System.in.
 * It also re-asks the user when input is empty or not a valid number, so each
 * slice does not need to repeat this checking.
 *
 * @author Tasbiul Huda Tawhid
 */
public class InputHelper {

    private final Scanner scanner;

    /**
     * Creates the helper around one Scanner.
     *
     * @param scanner the Scanner reading System.in, created once in Main
     */
    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Shows a prompt and returns the trimmed line typed by the user.
     * The line may be empty.
     *
     * @param prompt the text shown before the cursor
     * @return the trimmed input line
     * @throws IllegalStateException if the input stream has closed
     */
    public String readText(String prompt) {
        System.out.print(prompt);
        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException e) {
            // Happens when input ends (for example Ctrl+D); stops endless re-asking
            throw new IllegalStateException("Input stream closed.");
        }
    }

    /**
     * Keeps asking until the user types something that is not blank.
     *
     * @param prompt the text shown before the cursor
     * @return a non-empty, trimmed input line
     */
    public String readNonEmptyText(String prompt) {
        String value = readText(prompt);
        while (value.isEmpty()) {
            System.out.println("This field cannot be empty. Please try again.");
            value = readText(prompt);
        }
        return value;
    }

    /**
     * Keeps asking until the user types a whole number between min and max.
     *
     * @param prompt the text shown before the cursor
     * @param min    the smallest accepted number
     * @param max    the largest accepted number
     * @return a number from min to max (inclusive)
     */
    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            String value = readText(prompt);
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) {
                    return number;
                }
            } catch (NumberFormatException e) {
                // Not a number: fall through to the same message as out-of-range
            }
            System.out.println("Please enter a number from " + min + " to " + max + ".");
        }
    }

    /**
     * Reads a password without showing it on screen when the program runs in a
     * real terminal (NFR-02). When no terminal is available, for example when
     * input is piped from a file or an IDE console is used, it falls back to
     * normal visible input so the program still works.
     *
     * @param prompt the text shown before the cursor
     * @return a non-empty password
     */
    public String readPassword(String prompt) {
        Console console = System.console();
        if (console == null || !isInteractive(console)) {
            return readNonEmptyText(prompt);
        }
        char[] typed = console.readPassword("%s", prompt);
        while (typed != null && typed.length == 0) {
            System.out.println("This field cannot be empty. Please try again.");
            typed = console.readPassword("%s", prompt);
        }
        if (typed == null) {
            // Input ended (for example Ctrl+D), same behaviour as readText
            throw new IllegalStateException("Input stream closed.");
        }
        return new String(typed);
    }

    /**
     * Checks whether the console is attached to a real terminal. Java 22 added
     * Console.isTerminal(); on earlier versions a non-null console always means
     * a terminal. Reflection keeps the code compatible with Java 17 and newer.
     *
     * @param console the system console
     * @return true when keyboard input comes from a terminal
     */
    private static boolean isInteractive(Console console) {
        try {
            return (Boolean) Console.class.getMethod("isTerminal").invoke(console);
        } catch (ReflectiveOperationException e) {
            return true;
        }
    }
}
