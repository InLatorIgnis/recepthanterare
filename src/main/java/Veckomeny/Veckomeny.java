package Veckomeny;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonProperty;

import Recept.receptfabrik;


public class Veckomeny {
    @JsonProperty
    private String namn; // e.g., "V19-2026" or "Week19-2026"

    @JsonProperty
    // faktorera om till Map<DayOFWeek, Menupost> där menupost innehåller receptfabrik och portioner
    private Map<DayOfWeek, VeckomenyPost> dagar; // Day -> Recipe name mapping (Monday -> recipe name, etc.)

    // No-arg constructor for JSON deserialization
    public Veckomeny() {
        this.dagar = new TreeMap<>();
    }

    public Veckomeny(String namn) {
        this.namn = namn;
        this.dagar = new TreeMap<>();
        // Initialize with empty days
        this.dagar.put(DayOfWeek.MONDAY, new VeckomenyPost(new receptfabrik(), 4)); // Default 4 portions
        this.dagar.put(DayOfWeek.TUESDAY, new VeckomenyPost(new receptfabrik(), 4));
        this.dagar.put(DayOfWeek.WEDNESDAY, new VeckomenyPost(new receptfabrik(), 4));
        this.dagar.put(DayOfWeek.THURSDAY, new VeckomenyPost(new receptfabrik(), 4));
        this.dagar.put(DayOfWeek.FRIDAY, new VeckomenyPost(new receptfabrik(), 4));
        this.dagar.put(DayOfWeek.SATURDAY, new VeckomenyPost(new receptfabrik(), 4));
        this.dagar.put(DayOfWeek.SUNDAY, new VeckomenyPost(new receptfabrik(), 4));
    }

    public String getNamn() {
        return namn;
    }

    public void setNamn(String namn) {
        this.namn = namn;
    }

    public Map<DayOfWeek, VeckomenyPost> getDagar() {
        return dagar;
    }

    public void setDagar(Map<DayOfWeek, VeckomenyPost> dagar) {
        this.dagar = dagar;
    }

    public void setReceptForDag(DayOfWeek dag, VeckomenyPost recept) {
        this.dagar.put(dag, recept);
    }


    public String getReceptForDag(DayOfWeek dag) {
        try {
            VeckomenyPost post = this.dagar.get(dag);
            if (post != null && post.recept != null) {
                return post.getNamn();
            }
        } catch (Exception e) {
            System.err.println("Fel vid hämtning av recept för " + dag + ": " + e.getMessage());
        }
        return null;
    }

    public String getReceptForDag(String dag) {
        DayOfWeek dayOfWeek = parseDayOfWeek(dag);
        return getReceptForDag(dayOfWeek);
    }

    private DayOfWeek parseDayOfWeek(String dag) {
        if (dag == null) {
            throw new IllegalArgumentException("Dag kan inte vara null");
        }

        switch (dag.trim().toLowerCase()) {
            case "måndag":
            case "mandag":
                return DayOfWeek.MONDAY;
            case "tisdag":
                return DayOfWeek.TUESDAY;
            case "onsdag":
                return DayOfWeek.WEDNESDAY;
            case "torsdag":
                return DayOfWeek.THURSDAY;
            case "fredag":
                return DayOfWeek.FRIDAY;
            case "lördag":
            case "loradag":
                return DayOfWeek.SATURDAY;
            case "söndag":
            case "sondag":
                return DayOfWeek.SUNDAY;
            default:
                throw new IllegalArgumentException("Okänd veckodag: '" + dag + "'");
        }
    }

    public String output() {
        StringBuilder sb = new StringBuilder();
        sb.append("Veckomeny: ").append(this.namn).append("\n");
        sb.append("================\n");
        for (Entry<DayOfWeek, VeckomenyPost> entry : this.dagar.entrySet()) {
            sb.append(entry.getKey()).append(": ");
            if (entry.getValue().getNamn().isEmpty()) {
                sb.append("(inget recept)");
            } else {
                sb.append(entry.getValue());
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    public static class VeckomenyPost {
        
        @JsonProperty
        private receptfabrik recept;

        @JsonProperty
        private int portioner;

        public VeckomenyPost() {
            this.recept = new receptfabrik();
            this.portioner = 4; // Default portioner
        }
        
        public VeckomenyPost(receptfabrik recept, int portioner) {
            this.recept = recept;
            this.portioner = portioner;
        }
        public String getNamn() {
            return this.recept.getNamn();
        }

        public void setNamn(String namn) {
            this.recept.setNamn(namn);
        }

        public receptfabrik getRecept() {
            return this.recept;
        }

        public void setRecept(receptfabrik recept) {
            this.recept = recept;
        }
        public int getPortioner() {
            return this.portioner;
        }
        public void setPortioner(int portioner) {
            this.portioner = portioner;
        }

        @Override
        public String toString() {
            return this.recept.getNamn() + " (" + this.portioner + " portioner)";
        }

    }
}
