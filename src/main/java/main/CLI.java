package main;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import Butikslayout.ButiksLayout;
import Butikslayout.FileButiksLayoutRepository;
import ENUMS.Kategori;
import Recept.FileReceptRepository;
import Recept.ReceptFabrik;
import Veckomeny.FileVeckomenyRepository;
import Veckomeny.Veckomeny;
import Veckomeny.Veckomeny.VeckomenyPost; 
public class CLI {
    
    public static void main(String[] args) {
        System.out.println("Välkommen till Recepthanterare!");
        
        mainMenu();

        // Här kan du implementera logiken för att hantera användarens input och interaktion
    }

    public static void mainMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n========== HUVUDMENY ==========");
            System.out.println("1. Recepthantering");
            System.out.println("2. Butikslayouter");
            System.out.println("3. Veckomeny");
            System.out.println("4. Inköpslista");
            System.out.println("5. Avsluta");
            System.out.print("Välj ett alternativ: ");

            try {
                if (!scanner.hasNextInt()) {
                System.out.println("Ingen input hittades (CLI avbryts)");
                return;
                }

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        receptMenu(scanner);
                        break;
                    case 2:
                        butikslayoutMenu(scanner);
                        break;
                    case 3:
                        veckomentyMenu(scanner);
                        break;
                    case 4:
                        inköpslistaMenu(scanner);
                        break;
                    case 5:
                        running = false;
                        System.out.println("Tack för att du använde Recepthanterare!");
                        break;
                    default:
                        System.out.println("Ogiltigt val.");
                }
            } catch (Exception e) {
                System.out.println("Fel: " + e.getMessage());
                scanner.nextLine(); // Clear invalid input
            }
        }

        scanner.close();
    }

    public static void receptMenu(Scanner scanner) {
        FileReceptRepository repository;
        System.out.println("\n========== RECEPTMENY ==========");

        try {
            repository = new FileReceptRepository(FileReceptRepository.DEFAULT_RECIPES_DIR);

            boolean inMenu = true;
            while (inMenu) {
                System.out.println("\n1. Skapa nytt recept");
                System.out.println("2. Visa recept");
                System.out.println("3. Ta bort recept");
                System.out.println("4. Tillbaka till huvudmeny");
                System.out.print("Välj ett alternativ: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        skapaRecept(scanner, repository);
                        break;
                    case 2:
                        visaRecept(scanner, repository);
                        break;
                    case 3:
                        tabortRecept(scanner, repository);
                        break;
                    case 4:
                        inMenu = false;
                        break;
                    default:
                        System.out.println("Ogiltigt val.");
                }
            }
        } catch (IOException e) {
            System.err.println("Kunde inte komma åt receptskåpet: " + e.getMessage());
        }
    }

    private static void skapaRecept(Scanner scanner, FileReceptRepository repository) throws IOException {
        System.out.println("\n--- Skapa nytt recept ---");
        System.out.print("Receptets namn: ");
        String namn = SanitizeNamn(scanner);

        List<ReceptFabrik.ReceptIngrediens> ingrediensList = new ArrayList<>();
        boolean addingIngredients = true;

        while (addingIngredients) {
            System.out.println("\nIngrediens " + (ingrediensList.size() + 1));
            System.out.print("Namn (eller 'klart' för att sluta): ");
            String ingrediensNamn = SanitizeNamn(scanner);

            if (ingrediensNamn.equalsIgnoreCase("klart")) {
                addingIngredients = false;
                continue;
            }

            String kategori = selectKategori(scanner);

            System.out.print("Mängd: ");
            Double mängd = scanner.nextDouble();
            scanner.nextLine(); // Consume newline

            System.out.print("Enhet (t.ex. st, dl, g): ");
            String enhet = selectEnhet(scanner);

            try {
                ingrediensList.add(new ReceptFabrik.ReceptIngrediens(ingrediensNamn, kategori, mängd, enhet));
                System.out.println("Ingrediens tillagd.");
            } catch (IllegalArgumentException e) {
                System.out.println("Fel: " + e.getMessage());
            }
        }

        if (ingrediensList.isEmpty()) {
            System.out.println("Receptet måste ha minst en ingrediens.");
            return;
        }

        ReceptFabrik nyRecept = new ReceptFabrik(namn, ingrediensList);

        // Check if recipe already exists
        if (repository.exists(namn)) {
            System.out.println("Ett recept med namnet '" + namn + "' finns redan. Vill du ersätta det? (y/n)");
            String replaceChoice = scanner.nextLine().trim().toLowerCase();
            if (!replaceChoice.equals("y")) {
                System.out.println("Recept inte sparat.");
                return;
            }
        }

        repository.spara(nyRecept);
        System.out.println("Recept sparat!");
        System.out.println(nyRecept.output());
    }

    private static void visaRecept(Scanner scanner, FileReceptRepository repository) throws IOException {
        System.out.print("\nRecepts namn: ");
        String namn = SanitizeNamn(scanner);

        if (!repository.exists(namn)) {
            System.out.println("Receptet '" + namn + "' hittades inte.");
            return;
        }

        ReceptFabrik recept = repository.ladda(namn);
        System.out.println("\n" + recept.output());
    }

    private static void tabortRecept(Scanner scanner, FileReceptRepository repository) throws IOException {
        System.out.print("\nRecepts namn att ta bort: ");
        String namn = SanitizeNamn(scanner);

        if (!repository.exists(namn)) {
            System.out.println("Receptet '" + namn + "' hittades inte.");
            return;
        }

        System.out.println("Är du säker? (y/n)");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("y")) {
            repository.ta_bort(namn);
            System.out.println("Receptet borttaget.");
        } else {
            System.out.println("Avbruten.");
        }
    }

    public static void veckomentyMenu(Scanner scanner) {
        FileVeckomenyRepository menyRepository;
        FileReceptRepository receptRepository;
        System.out.println("\n========== VECKOMENY ==========");

        try {
            menyRepository = new FileVeckomenyRepository(FileVeckomenyRepository.DEFAULT_WEEKMENUS_DIR);
            receptRepository = new FileReceptRepository(FileReceptRepository.DEFAULT_RECIPES_DIR);

            boolean inMenu = true;
            while (inMenu) {
                System.out.println("1. Skapa ny veckomeny");
                System.out.println("2. Visa/Redigera en veckomeny");
                System.out.println("3. Ta bort veckomeny");
                System.out.println("4. Tillbaka till huvudmeny");
                System.out.print("Välj ett alternativ: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {

                    case 1:
                        skapaVeckomeny(scanner, menyRepository, receptRepository);
                        break;
                    case 2:
                        visaVeckomeny(scanner, menyRepository, receptRepository);
                        break;
                    case 3:
                        tabortVeckomeny(scanner, menyRepository);
                        break;
                    case 4:
                        inMenu = false;
                        break;
                    default:
                        System.out.println("Ogiltigt val.");
                }
            }
        } catch (IOException e) {
            System.err.println("Kunde inte komma åt veckomenyer: " + e.getMessage());
        }
    }

    private static void skapaVeckomeny(Scanner scanner, FileVeckomenyRepository repository, FileReceptRepository receptRepository) throws IOException {
        System.out.println("\n--- Skapa ny veckomeny ---");
        System.out.println("Namnkonvention: V##-YYYY (t.ex. V19-2026 för vecka 19, 2026)");
        System.out.print("Veckomenys namn: ");
        String namn = SanitizeNamn(scanner);

        if (repository.exists(namn)) {
            System.out.println("En veckomeny med namnet '" + namn + "' finns redan. Vill du ersätta den? (y/n)");
            String replaceChoice = scanner.nextLine().trim().toLowerCase();
            if (!replaceChoice.equals("y")) {
                System.out.println("Veckomeny inte skapad.");
                return;
            }
        }

        Veckomeny nyVeckomeny = new Veckomeny(namn);
        String[] dagar = {"Måndag", "Tisdag", "Onsdag", "Torsdag", "Fredag", "Lördag", "Söndag"};
        ReceptFabrik recept = new ReceptFabrik(); // Tom receptfabrik för att ladda recept i VeckomenyPost
        
        System.out.println("\nLägg till recept för varje dag (skriv receptets namn eller lämna tomt för att hoppa över):");
        for (String dag : dagar) {
            System.out.print(dag + ": ");
            String namn1 = scanner.nextLine().trim();
            if (!namn1.isEmpty()) {
                recept = receptRepository.ladda(namn1); // Försök ladda receptet för att validera att det finns
                
                System.out.print("Antal portioner: ");
                int portioner = scanner.nextInt(); // Default portioner
                scanner.nextLine(); // Consume newline
                nyVeckomeny.setReceptForDag(toDayOfWeek(dag), new VeckomenyPost(recept, portioner));
            }
        }

        repository.spara(nyVeckomeny);
        System.out.println("Veckomeny sparad!");
        System.out.println(nyVeckomeny.output());
    }

    private static void visaVeckomeny(Scanner scanner, FileVeckomenyRepository repository, FileReceptRepository receptRepository) throws IOException {
        System.out.print("\nVeckomenys namn: ");
        String namn = SanitizeNamn(scanner);

        if (!repository.exists(namn)) {
            System.out.println("Veckomenyn '" + namn + "' hittades inte.");
            return;
        }

        Veckomeny veckomeny = repository.ladda(namn);
        System.out.println("\n" + veckomeny.output());

        System.out.println("\nVill du redigera denna veckomeny? (y/n)");
        String editChoice = scanner.nextLine().trim().toLowerCase();
        if (editChoice.equals("y")) {
            redigeraVeckomeny(scanner, veckomeny, repository, receptRepository);
        }
    }

    private static void redigeraVeckomeny(Scanner scanner, Veckomeny veckomeny, FileVeckomenyRepository repository, FileReceptRepository receptRepository) throws IOException {
        String[] dagar = {"Måndag", "Tisdag", "Onsdag", "Torsdag", "Fredag", "Lördag", "Söndag"};

        System.out.println("\nRedigera veckomeny:");
        for (int i = 0; i < dagar.length; i++) {
            System.out.println((i + 1) + ". " + dagar[i] + " (" + veckomeny.getReceptForDag(dagar[i]) + ")");
        }
        System.out.println("0. Spara och avsluta");

        System.out.print("Välj dag att redigera: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (choice == 0) {
            repository.spara(veckomeny);
            System.out.println("Veckomeny sparad.");
        } else if (choice >= 1 && choice <= dagar.length) {
            String dag = dagar[choice - 1];
            System.out.print("Nytt recept för " + dag + ": ");
            String nyttRecept = scanner.nextLine().trim();
                ReceptFabrik recept = receptRepository.ladda(nyttRecept); // Försök ladda receptet för att validera att det finns
            System.out.print("Antal portioner: ");
                int portioner = scanner.nextInt(); 
            veckomeny.setReceptForDag(toDayOfWeek(dag), new VeckomenyPost(recept, portioner));
            System.out.println("Uppdaterat.");
            redigeraVeckomeny(scanner, veckomeny, repository, receptRepository);
        } else {
            System.out.println("Ogiltigt val.");
            redigeraVeckomeny(scanner, veckomeny, repository, receptRepository);
        }
    }

    private static DayOfWeek toDayOfWeek(String dag) {
        return switch (dag.toLowerCase(Locale.ROOT).trim()) {
            case "måndag", "mandag" -> DayOfWeek.MONDAY;
            case "tisdag" -> DayOfWeek.TUESDAY;
            case "onsdag" -> DayOfWeek.WEDNESDAY;
            case "torsdag" -> DayOfWeek.THURSDAY;
            case "fredag" -> DayOfWeek.FRIDAY;
            case "lördag", "loradag" -> DayOfWeek.SATURDAY;
            case "söndag", "sondag" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException("Okänd veckodag: " + dag);
        };
    }

    private static void tabortVeckomeny(Scanner scanner, FileVeckomenyRepository repository) throws IOException {
        System.out.print("\nVeckomenys namn att ta bort: ");
        String namn = SanitizeNamn(scanner);

        if (!repository.exists(namn)) {
            System.out.println("Veckomenyn '" + namn + "' hittades inte.");
            return;
        }

        System.out.println("Är du säker? (y/n)");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("y")) {
            repository.ta_bort(namn);
            System.out.println("Veckomenyn borttagen.");
        } else {
            System.out.println("Avbruten.");
        }
    }

    public static void inköpslistaMenu(Scanner scanner) {
        FileButiksLayoutRepository layoutRepository;
        FileVeckomenyRepository veckomentyRepository;
        FileReceptRepository receptRepository;

        System.out.println("\n========== INKÖPSLISTA ==========");

        try {
            layoutRepository = new FileButiksLayoutRepository(FileButiksLayoutRepository.DEFAULT_LAYOUTS_DIR);
            veckomentyRepository = new FileVeckomenyRepository(FileVeckomenyRepository.DEFAULT_WEEKMENUS_DIR);
            receptRepository = new FileReceptRepository(FileReceptRepository.DEFAULT_RECIPES_DIR);

            // Select butikslayout
            System.out.print("\nButikens namn: ");
            String butikNamn = SanitizeNamn(scanner);

            if (!layoutRepository.exists(butikNamn)) {
                System.out.println("Butiken '" + butikNamn + "' hittades inte.");
                return;
            }

            ButiksLayout layout = layoutRepository.ladda(butikNamn);

            // Select veckomeny
            System.out.print("\nVeckomenys namn: ");
            String veckoNamn = SanitizeNamn(scanner);

            if (!veckomentyRepository.exists(veckoNamn)) {
                System.out.println("Veckomenyn '" + veckoNamn + "' hittades inte.");
                return;
            }

            Veckomeny veckomeny = veckomentyRepository.ladda(veckoNamn);
 
            // Generate shopping list
            Inköpslista inköpslista = new Inköpslista("Inköpslista", layout, veckomeny);
            inköpslista.aggregateIngredients(receptRepository);

            // Display
            System.out.println(inköpslista.output());


            // Option to save to file (future feature)
            System.out.println("Inköpslistan är nu redo.");

        } catch (Exception e) {
            System.err.println("Ett oväntat fel uppstod: " + e.getMessage());
        }
    }

    public static void butikslayoutMenu(Scanner scanner) {
        FileButiksLayoutRepository repository;
        
            try {
                repository = new FileButiksLayoutRepository(FileButiksLayoutRepository.DEFAULT_LAYOUTS_DIR);
                
                boolean inMenu = true;
                while (inMenu) {
                    System.out.print("Välj ett alternativ: ");
                    System.out.println("\n========== BUTIKSLAYOUT MENY ==========");
                    System.out.println("1. Skapa ny butikslayout");
                    System.out.println("2. Visa/Redigera butikslayout");
                    System.out.println("3. Ta bort butikslayout");
                    System.out.println("4. Tillbaka till huvudmeny");
                    int choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    switch (choice) {
                        case 1:
                            createLayout(scanner, repository);
                        break;

                        case 2:
                        System.out.println("Visa butikslayout:");
                        System.out.println("Ange butikens namn:");
                            String namn = SanitizeNamn(scanner);
                        ButiksLayout layout = repository.ladda(namn);
                        System.out.println("Butik: " + layout.getButiksNamn());
                        System.out.println("Kategoriordning:");
                        for (Kategori kategori : layout.getKategoriOrdning()) {
                            System.out.println("- " + kategori);
                        }
                        System.out.println("Vill du redigera denna layout? (y/n)");
                        String editChoice = scanner.nextLine().trim().toLowerCase();
                        if (editChoice.equals("y")) {
                            createLayout(scanner, namn, repository);
                        } else {
                            System.out.println("Layout inte redigerad.");
                         
                        }
                        break;
                        
                        case 3:
                            System.out.println("Ange butikens namn:");
                            String deleteName = SanitizeNamn(scanner);
                            try {
                                repository.ta_bort(deleteName);
                                System.out.println("Butikslayout borttagen.");
                            } catch (IOException e) {
                                System.err.println("Kunde inte ta bort butikslayout: " + e.getMessage());
                            }
                            break;

                        case 4:
                            inMenu = false;
                            break;
                    
                        default:
                            System.out.println("Ogiltigt val.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Kunde inte ladda butikslayout: " + e.getMessage());
                e.printStackTrace();
            }
    }

    public static String SanitizeNamn(Scanner scanner) {
        String namn = scanner.nextLine().trim();
        namn = namn.substring(0, 1).toUpperCase() + namn.substring(1);
        return namn;
    }

    public static String selectKategori(Scanner scanner) {
        String[] kategorier = ReceptFabrik.ReceptIngrediens.getValidKategorier().split(", ");
        System.out.println("\nVälj kategori:");
        for (int i = 0; i < kategorier.length; i++) {
            System.out.println((i + 1) + ". " + kategorier[i]);
        }
        System.out.print("Val (nummer): ");
        
        try {
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            if (choice > 0 && choice <= kategorier.length) {
                return kategorier[choice - 1];
            } else {
                System.out.println("Ogiltigt val. Försök igen.");
                return selectKategori(scanner);
            }
        } catch (Exception e) {
            scanner.nextLine(); // Consume invalid input
            System.out.println("Ogiltigt val. Försök igen.");
            return selectKategori(scanner);
        }
    }

    public static String selectEnhet(Scanner scanner) {
        String[] enheter = ReceptFabrik.ReceptIngrediens.getValidEnheter().split(", ");
        System.out.println("\nVälj enhet:");
        for (int i = 0; i < enheter.length; i++) {
            System.out.println((i + 1) + ". " + enheter[i]);
        }
        System.out.print("Val (nummer): ");
        
        try {
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            if (choice > 0 && choice <= enheter.length) {
                return enheter[choice - 1];
            } else {
                System.out.println("Ogiltigt val. Försök igen.");
                return selectEnhet(scanner);
            }
        } catch (Exception e) {
            scanner.nextLine(); // Consume invalid input
            System.out.println("Ogiltigt val. Försök igen.");
            return selectEnhet(scanner);
        }
    }

    private static void createLayout(Scanner scanner, FileButiksLayoutRepository repository) throws IOException {
        createLayout(scanner, "", repository);
    }

    private static void createLayout(Scanner scanner, String butiksNamn, FileButiksLayoutRepository repository) throws IOException {
               System.out.println("Skapa ny butikslayout:");
               String angivetNamn;
               if (!butiksNamn.isEmpty()) {
               System.out.println("Ange butikens namn:");
               angivetNamn = SanitizeNamn(scanner);
                angivetNamn =  butiksNamn; // Använd det befintliga namnet istället för att fråga användaren
                } else {
                 angivetNamn = SanitizeNamn(scanner);
                }
               System.out.println("Ange kategorier i den ordning de påträffas butiken:");
               
               
               StringBuilder sb = new StringBuilder();
               int index = 1;

               for (Kategori kategori : Kategori.values()) {
        sb.append("- " + kategori + " (" + index + ")\n");
        index++;
               }
               System.out.println(sb.toString());

               List<Kategori> available = new ArrayList<>(List.of(Kategori.values()));
               List<Kategori> kategoriOrdning = new ArrayList<>();
               boolean done = false;
        while (!done) {

            // --- "Clear screen" --- ANSI escape code för att rensa terminalen, fungerar i de flesta terminaler som stöder ANSI-koder
            System.out.print("\033[H\033[2J");
            System.out.flush();
            System.out.println();
            int maxRows = Math.max(available.size(), kategoriOrdning.size());

            System.out.printf("%-30s | %-30s\n", "Tillgängliga", "Valda");
            System.out.println("----------------------------------------------------------");

            for (int i = 0; i < maxRows; i++) {
                String left = "";
                String right = "";

                if (i < available.size()) {
                    left = (i + 1) + ". " + available.get(i);
                }

                if (i < kategoriOrdning.size()) {
                    right = (i + 1) + " -> " + kategoriOrdning.get(i);
                }

                System.out.printf("%-30s | %-30s\n", left, right);
            }

            System.out.println("Ange kategori nummer (eller 0 för att avsluta):");    
            int input = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            if (input == 0) {
                done = true;
                continue; 
            }

                if (input >= 1 && input <= available.size()) {
                    Kategori vald = available.remove(input - 1);
                    kategoriOrdning.add(vald);

                    System.out.println("Kategori " + vald + " tillagd.");
                } else {
                    System.out.println("Ogiltigt val.");
                }
                
            }
               
               ButiksLayout nyLayout = new ButiksLayout(angivetNamn, kategoriOrdning);
               System.out.println("Ny butikslayout skapad: " + nyLayout.getButiksNamn());
               System.out.println("Kategoriordning:");
               for (Kategori kategori : nyLayout.getKategoriOrdning()) {
                System.out.println("- " + kategori);

               }
               System.out.println("Vill du spara denna layout till fil? (y/n)");
               String saveChoice = scanner.nextLine().trim().toLowerCase();
               if (saveChoice.equals("y")) {
                // Check if layout already exists
                if (repository.exists(nyLayout.getButiksNamn())) {
                    System.out.println("En layout med namnet '" + nyLayout.getButiksNamn() + "' finns redan. Vill du ersätta den? (y/n)");
                    String replaceChoice = scanner.nextLine().trim().toLowerCase();
                    if (!replaceChoice.equals("y")) {
                        System.out.println("Layout inte sparad.");
                        return;
                    }
                }
                repository.spara(nyLayout);
                System.out.println("Layout sparad till fil.");
                } else {
                System.out.println("Layout inte sparad.");
                }
    } 
}
     








