package Butikslayout;
import java.io.IOException;

public interface ButiksLayoutRepository {
    void spara(ButiksLayout layout) throws IOException;
    ButiksLayout ladda(String namn) throws IOException;
    boolean exists(String namn) throws IOException;
    void ta_bort(String namn) throws IOException;
}

