package Veckomeny;
import java.io.IOException;

public interface VeckomentyRepository {
    void spara(Veckomeny veckomeny) throws IOException;
    
    Veckomeny ladda(String namn) throws IOException;
    
    boolean exists(String namn);
    
    void ta_bort(String namn) throws IOException;
}
