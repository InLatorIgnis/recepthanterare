package main;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import Butikslayout.ButiksLayout;
import ENUMS.Kategori;
import Recept.FileReceptRepository;
import Recept.ReceptFabrik;
import Veckomeny.Veckomeny;
import Veckomeny.Veckomeny.VeckomenyPost;

public class Inköpslista {
    public static final Path DEFAULT_SHOPPINGLIST_DIR = Path.of("./inköpslistor");
    
    private String namn;
    private ButiksLayout layout;
    private Veckomeny menu;
    private Map<Kategori, List<ReceptFabrik.ReceptIngrediens>> ingredienserPerKategori;

    public Inköpslista(String namn, ButiksLayout layout, Veckomeny menu) {
        this.namn = namn;
        this.layout = layout;
        this.menu = menu;
        this.ingredienserPerKategori = new HashMap<>();
    }

    /**
     * Aggregates ingredients from all recipes in the menu,
     * grouped by category and sorted according to layout's category order
     */
    public void aggregateIngredients(FileReceptRepository receptRepository) throws Exception {
        // Initialize kategori map in layout order
        for (Kategori kategori : layout.getKategoriOrdning()) {
            ingredienserPerKategori.put(kategori, new ArrayList<>());
        }

        // Go through each day in the menu
        for (Entry<DayOfWeek, VeckomenyPost> dayEntry : menu.getDagar().entrySet()) {
            String receptNamn = dayEntry.getValue().getNamn();
            
            // Skip empty days
            if (receptNamn == null || receptNamn.isEmpty()) {
                continue;
            }

            try {
                // Load/resolve the recipe and scale ingredients by the selected portion count
                List<ReceptFabrik.ReceptIngrediens> ingredienser = resolveIngredienserForPost(dayEntry.getValue(), receptRepository);

                // Add each ingredient to its category
                for (ReceptFabrik.ReceptIngrediens ingrediens : ingredienser) {
                    try {
                        Kategori kategori = Kategori.valueOf(ingrediens.getKategori());
                        if (ingredienserPerKategori.containsKey(kategori)) {
                            ingredienserPerKategori.get(kategori).add(ingrediens);
                        }
                    } catch (IllegalArgumentException e) {
                        System.err.println("Varning: Okänd kategori '" + ingrediens.getKategori() + "' för ingrediens '" + ingrediens.getNamn() + "'");
                    }
                }
            } catch (Exception e) {
                System.err.println("Varning: Kunde inte ladda recept '" + receptNamn + "': " + e.getMessage());
            }
        }
    }

    private List<ReceptFabrik.ReceptIngrediens> resolveIngredienserForPost(VeckomenyPost post, FileReceptRepository receptRepository) throws Exception {
        ReceptFabrik recept = post.getRecept();
        String receptNamn = post.getNamn();

        if ((recept == null || recept.getIngrediensList() == null || recept.getIngrediensList().isEmpty())
                && receptNamn != null && !receptNamn.isEmpty() && receptRepository.exists(receptNamn)) {
            recept = receptRepository.ladda(receptNamn);
        }

        if (recept == null) {
            throw new IllegalArgumentException("Receptobjektet är tomt för recept '" + receptNamn + "'");
        }

        return recept.skaleraIngredienser(post.getPortioner());
    }



    /**
     * Groups ingredients by name and sums quantities
     */
    private Map<String, IngrediensTotal> aggregateByName() {
        Map<String, IngrediensTotal> totals = new LinkedHashMap<>();

        for (Map.Entry<Kategori, List<ReceptFabrik.ReceptIngrediens>> entry : ingredienserPerKategori.entrySet()) {
            for (ReceptFabrik.ReceptIngrediens ingrediens : entry.getValue()) {
                String key = ingrediens.getNamn().toLowerCase();
                
                if (totals.containsKey(key)) {
                    IngrediensTotal existing = totals.get(key);
                    existing.addMängd(ingrediens.getMängd());
                } else {
                    totals.put(key, new IngrediensTotal(
                        ingrediens.getNamn(),
                        ingrediens.getKategori(),
                        ingrediens.getMängd(),
                        ingrediens.getEnhet()
                    ));
                }
            }
        }

        return totals;
    }

    public String output() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔══════════════════════════════════════════════════════╗\n");
        sb.append("║                     INKÖPSLISTA").append(String.format("%21s","")).append(" ║\n");
        sb.append("║           för veckomeny: ").append(String.format("%-27s", menu.getNamn())).append(" ║\n");
        sb.append("║           Butik: ").append(String.format("%-35s", layout.getButiksNamn())).append(" ║\n");
        sb.append("╚══════════════════════════════════════════════════════╝\n");

        Map<String, IngrediensTotal> totals = aggregateByName();

        for (Kategori kategori : layout.getKategoriOrdning()) {
            List<ReceptFabrik.ReceptIngrediens> ingredienser = ingredienserPerKategori.get(kategori);
            
            if (ingredienser == null || ingredienser.isEmpty()) {
                continue;
            }

            sb.append(String.format("%-11s", "\n")).append("━━━ ").append(kategori).append(" ━━━\n");

            // Get unique ingredients for this category and sum quantities
            Map<String, IngrediensTotal> categoryTotals = new LinkedHashMap<>();
            for (ReceptFabrik.ReceptIngrediens ing : ingredienser) {
                String key = ing.getNamn().toLowerCase();
                if (categoryTotals.containsKey(key)) {
                    categoryTotals.get(key).addMängd(ing.getMängd());
                } else {
                    categoryTotals.put(key, new IngrediensTotal(
                        ing.getNamn(),
                        ing.getKategori(),
                        ing.getMängd(),
                        ing.getEnhet()
                    ));
                }
            }

            for (IngrediensTotal total : categoryTotals.values()) {
                sb.append(String.format("%-13s %-1s %-18s %6.1f %4s\n",
                        "",
                        "☐",
                    total.namn, 
                    total.totalMängd, 
                    total.enhet));
            }
        }

        sb.append("\n");

        try {
            listToFIle(sb.toString(), menu.getNamn() + "_inköpslista.txt");
        } catch (IOException e) {
            System.err.println("Kunde inte spara inköpslista till fil: " + e.getMessage());
        }

        return sb.toString();
    }

    private File listToFIle(String lista, String fileName) throws IOException {
        try {
            // Create directory if it doesn't exist
            Files.createDirectories(DEFAULT_SHOPPINGLIST_DIR);
            
            Path outputPath = DEFAULT_SHOPPINGLIST_DIR.resolve(fileName);
            Files.writeString(outputPath, lista);
            System.out.println("Inköpslista sparad till: " + outputPath.toAbsolutePath());
            return outputPath.toFile();    
        } catch (IOException e) {
            throw new IOException("Kunde inte skriva till fil: " + e.getMessage());
        }
    }

    /**
     * Inner class to track ingredient totals
     */
    private static class IngrediensTotal {
        String namn;
        String kategori;
        Double totalMängd;
        String enhet;

        IngrediensTotal(String namn, String kategori, Double mängd, String enhet) {
            this.namn = namn;
            this.kategori = kategori;
            this.totalMängd = mängd;
            this.enhet = enhet;
        }

        void addMängd(Double mängd) {
            this.totalMängd += mängd;
        }
    }
}
