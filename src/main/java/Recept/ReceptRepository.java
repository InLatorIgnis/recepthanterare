package Recept;
import java.io.IOException;

public interface ReceptRepository {
    void spara(receptfabrik recept) throws IOException;
    
    receptfabrik ladda(String namn) throws IOException;
    
    boolean exists(String namn);
    
    void ta_bort(String namn) throws IOException;
}
