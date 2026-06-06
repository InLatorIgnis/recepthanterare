package Butikslayout;
import java.util.List;

import ENUMS.Kategori;

public class ButiksLayout {
    //laddar in butikslayout, ska ta emot en lista med ingredienser och sortera lista baserat på kategori och ordning i butiken
    private String butiksNamn;
    private List<Kategori> kategoriOrdning;



    public ButiksLayout() {
        // Jackson needs a no-arg constructor for deserialization
    }

    public ButiksLayout(String butiksNamn, List<Kategori> kategoriOrdning) {
        this.butiksNamn = butiksNamn;
        this.kategoriOrdning = kategoriOrdning;
    }
    
    public String getButiksNamn() {
        return this.butiksNamn;
    }

    

    public void setButiksNamn(String butiksNamn) {
        this.butiksNamn = butiksNamn;
    }

    public List<Kategori> getKategoriOrdning() {
        return this.kategoriOrdning;
    }

    public void setKategoriOrdning(List<Kategori> kategoriOrdning) {
        this.kategoriOrdning = kategoriOrdning;
    }
    
}
