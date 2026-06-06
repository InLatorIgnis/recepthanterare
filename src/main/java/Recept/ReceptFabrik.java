package Recept;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

import ENUMS.Enhet;
import ENUMS.Kategori;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ReceptFabrik {
    @JsonProperty
    private String namn;
    
    @JsonProperty
    private List<ReceptIngrediens> ingrediensList;
    @JsonProperty
    private double standardPortion = 4; // Default portion size for scaling

    // No-arg constructor for JSON deserialization
    public ReceptFabrik() {
        this.ingrediensList = new ArrayList<>();
    }

    public ReceptFabrik(String namn, List<ReceptIngrediens> ingrediensList) {
        this.namn = namn;
        this.ingrediensList = ingrediensList;
        
    }

    // Legacy constructor support
    /* public receptfabrik(String namn, Map<ingrediens, Integer> ingrediensMap) {
        this.namn = namn;
        this.ingrediensList = new ArrayList<>();
        for (Map.Entry<ingrediens, Integer> entry : ingrediensMap.entrySet()) {
            this.ingrediensList.add(new ReceptIngrediens(entry.getKey().getNamn(), entry.getKey().getKategori(), entry.getValue().doubleValue(),
             entry.getKey().getenhet()));
        }
    } */

    public String getNamn() {
        return this.namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public List<ReceptIngrediens> getIngrediensList() {
        return this.ingrediensList;
    }

    public void setIngrediensList(List<ReceptIngrediens> ingrediensList) {
        this.ingrediensList = ingrediensList;
    }

    @JsonIgnore
    public String[] getIngredienser() {
        String[] ingredienserArray = new String[this.ingrediensList.size()];
        for (int i = 0; i < this.ingrediensList.size(); i++) {
            ReceptIngrediens ri = this.ingrediensList.get(i);
            ingredienserArray[i] = ri.getMängd() + " " + ri.getNamn();
        }
        return ingredienserArray;
    }
    public void setStandardPortion(double standardPortion) {
        this.standardPortion = standardPortion;
    }
    public double getStandardPortion() {
        return this.standardPortion;
    }


    public String output() {
        StringBuilder sb = new StringBuilder();
        sb.append("Recept: ").append(this.namn).append("\n");
        sb.append("Ingredienser:\n");
        for (ReceptIngrediens ri : this.ingrediensList) {
            sb.append("- ").append(ri.getMängd()).append(" ").append(ri.getEnhet()).append(" ").append(ri.getNamn()).append(" (").append(ri.getKategori()).append(")\n");
        }
        return sb.toString();
    }

    public List<ReceptIngrediens> skaleraIngredienser(double portioner) {
        List<ReceptIngrediens> skaladeIngredienser = new ArrayList<>();
        double faktor = portioner / standardPortion;
        for (ReceptIngrediens ri : this.ingrediensList) {
            skaladeIngredienser.add(new ReceptIngrediens(ri.getNamn(), ri.getKategori(), ri.getMängd() * faktor, ri.getEnhet()));
        }
        return skaladeIngredienser;
    }

    // Inner class for recipe ingredients (JSON serializable)
    public static class ReceptIngrediens {
        @JsonProperty
        private String namn;

        @JsonProperty
        private String kategori;

        @JsonProperty
        private Double mängd;

        @JsonProperty
        private String enhet; // Optional: could be "st", "dl", "g", etc.

        // No-arg constructor for JSON deserialization
        public ReceptIngrediens() {
        }

        public ReceptIngrediens(String namn, String kategori, Double mängd, String enhet) {
            this.namn = namn;
            validateAndSetKategori(kategori);
            this.mängd = mängd;
            this.enhet = enhet;
        }

        public String getNamn() {
            return namn;
        }

        public void setNamn(String namn) {
            this.namn = namn;
        }

        public String getKategori() {
            return kategori;
        }

        public void setKategori(String kategori) {
            validateAndSetKategori(kategori);
        }

        private void validateAndSetKategori(String kategori) {
            try {
                Kategori.valueOf(kategori.toUpperCase());
                this.kategori = kategori.toUpperCase();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Ogiltig kategori: '" + kategori + "'. Giltiga kategorier är: " + getValidKategorier());
            }
        }

        public static String getValidKategorier() {
            StringBuilder sb = new StringBuilder();
            Kategori[] kategorier = Kategori.values();
            for (int i = 0; i < kategorier.length; i++) {
                sb.append(kategorier[i].toString());
                if (i < kategorier.length - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }

        public Double getMängd() {
            return mängd;
        }

        public void setMängd(Double mängd) {
            this.mängd = mängd;
        }

        public String getEnhet() {


            return enhet;
        }
        
        public void setEnhet(String enhet) {
            validateAndSetEnhet(enhet);;
        }

        private void validateAndSetEnhet(String enhet) {
            try {
                Enhet.valueOf(enhet.toLowerCase());
                this.enhet = enhet.toLowerCase();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Ogiltig enhet: '" + enhet + "'. Giltiga enheter är: " + getValidEnheter());
            }
        }
        public static String getValidEnheter() {
            StringBuilder sb = new StringBuilder();
            Enhet[] enheter = Enhet.values();
            for (int i = 0; i < enheter.length; i++) {
                sb.append(enheter[i].toString());
                if (i < enheter.length - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
    }
}