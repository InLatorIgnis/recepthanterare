package Recept;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.javatuples.Triplet;



public class ReceptFörslag {
private     Set<String> ingredienser = new TreeSet<>(); 
    
    
    
    public Set<String> läsInIngredienser(FileReceptRepository rc) {

        try {
                    
            Files.list(FileReceptRepository.DEFAULT_RECIPES_DIR)
                .filter(p -> p.toString().endsWith(".json"))
                .forEach(p -> {
                    String recipeName = p.getFileName().toString().replace(".json", "");

                    try {
                            ReceptFabrik rf = rc.ladda(recipeName);
                            for (int i = 0; i < rf.getIngrediensList().size(); i++) {
                                String namn = rf.getIngrediensList().get(i).getNamn();
                            ingredienser.add(namn);
                        }

                    } catch (IOException e) {
                        System.err.println("Kunde inte läsa recept: " + recipeName);
                        e.printStackTrace();
                    }
                    
                });
        } catch (IOException e) {
            System.err.println("Kunde inte läsa recept: " + e.getMessage());
        }

        return ingredienser;
    }
    
    public List<Triplet<String, Set<String>, Set<String>>> matchandeRecept(Set<String> valdaIngredienser, FileReceptRepository rc) {

        List<Triplet<String, Set<String>, Set<String>>> rekommenderadeRecept = new ArrayList<>();

        for (String receptNamn : rc.hämtaLaddadeRecept().keySet()) {
            ReceptFabrik rf = rc.hämtaLaddadeRecept().get(receptNamn);
            Set<String> matchade = new HashSet<>();
            Set<String> saknade = new HashSet<>();

            for (int i = 0; i < rf.getIngrediensList().size(); i++) {
                String namn = rf.getIngrediensList().get(i).getNamn();
                if (valdaIngredienser.contains(namn)) {
                    matchade.add(namn);
                } else {
                    saknade.add(namn);
                }
            }

            if (!matchade.isEmpty()) {
                rekommenderadeRecept.add(new Triplet<>(receptNamn, matchade, saknade));
            }
        }

        rekommenderadeRecept.sort((t1, t2) -> Integer.compare(t2.getValue1().size(), t1.getValue1().size()));

        return rekommenderadeRecept;
    }



    
    
















}
