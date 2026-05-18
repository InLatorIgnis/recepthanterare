package main;

import GUI.RecepthanterareGUI;

public class Main {

    public static void main(String[] args) {
        //TODO: Lägg till argumenthantering för att välja mellan CLI och GUI, se till att terminal passas med och CLI exportera Jar fil som normal användare kan starta.
        String mode = (args.length > 0) ? args[0] : "gui";

        switch (mode.toLowerCase()) {

            case "cli" -> CLI.main(new String[0]);

            case "gui" -> javax.swing.SwingUtilities.invokeLater(
                    RecepthanterareGUI::new
            );

            default -> {
                System.out.println("Använd: cli eller gui");
            }
        }
    }
}