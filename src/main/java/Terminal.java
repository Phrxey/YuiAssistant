import java.util.Scanner;
import java.lang.System;
public class Terminal {
    public static void runTerminal(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Console Application Started. Enter exit to terminate the program.");
        while (true) {
            System.out.print("> ");

            String command = scanner.nextLine().trim();

            if (command.equalsIgnoreCase("exit")) {
                System.out.println("Exiting the console...");
                break;
            }
        }
    }
}
