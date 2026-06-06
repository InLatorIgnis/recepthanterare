package Recept;
import java.io.IOException;

public interface ReceptRepository {
    void spara(ReceptFabrik recept) throws IOException;
    
    ReceptFabrik ladda(String namn) throws IOException;
    
    boolean exists(String namn);
    
    void ta_bort(String namn) throws IOException;
}
