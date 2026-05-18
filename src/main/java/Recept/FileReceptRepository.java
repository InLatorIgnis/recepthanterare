package Recept;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileReceptRepository implements ReceptRepository {
    
    public static final Path DEFAULT_RECIPES_DIR = Path.of("./recept");
    
    private Map<String, receptfabrik> receptMap = new HashMap<>();
    private final Path folderPath;
    private ObjectMapper mapper = new ObjectMapper();

    public FileReceptRepository() throws IOException {
        this(DEFAULT_RECIPES_DIR);
    }

    public FileReceptRepository(Path folderPath) throws IOException {
        this.folderPath = folderPath;

        if (Files.exists(folderPath) && !Files.isDirectory(folderPath)) {
            throw new IOException("Target path exists but is not a directory: " + folderPath);
        }

        Files.createDirectories(folderPath);
        System.out.println("Using recipes folder: " + folderPath.toAbsolutePath());
    }

    @Override
    public void spara(receptfabrik recept) throws IOException {
        mapper.writeValue(
            folderPath.resolve(recept.getNamn() + ".json").toFile(),
            recept
        );
        receptMap.put(recept.getNamn(), recept);
    }

    @Override
    public receptfabrik ladda(String namn) throws IOException {
        receptfabrik recept = mapper.readValue(
            folderPath.resolve(namn + ".json").toFile(),
            receptfabrik.class
        );
        receptMap.put(namn, recept);
        return recept;
    }

    @Override
    public boolean exists(String namn) {
        return Files.exists(folderPath.resolve(namn + ".json"));
    }

    @Override
    public void ta_bort(String namn) throws IOException {
        Path filePath = folderPath.resolve(namn + ".json");
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            receptMap.remove(namn);
        } else {
            throw new IOException("Recipe not found: " + namn);
        }
    }
}
