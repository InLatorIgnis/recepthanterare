package Butikslayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map; 
import com.fasterxml.jackson.databind.ObjectMapper;


public class FileButiksLayoutRepository implements ButiksLayoutRepository {

     public static final Path DEFAULT_LAYOUTS_DIR = Path.of("./butikslayouter");
    
    private Map<String, ButiksLayout> layoutMap = new HashMap<>();
    private final Path folderPath;
    private ObjectMapper mapper = new ObjectMapper();

        public FileButiksLayoutRepository() throws IOException {
            this(DEFAULT_LAYOUTS_DIR);
        }

        public FileButiksLayoutRepository(Path folderPath) throws IOException {
            this.folderPath = folderPath;

            if (Files.exists(folderPath) && !Files.isDirectory(folderPath)) {
                throw new IOException("Target path exists but is not a directory: " + folderPath);
            }

            Files.createDirectories(folderPath);
            System.out.println("Using folder: " + folderPath.toAbsolutePath());
        }

    @Override
    public void spara(ButiksLayout layout) throws IOException {
        
        mapper.writeValue(
            folderPath.resolve(layout.getButiksNamn() + ".json").toFile(),
            layout
        );

        layoutMap.put(layout.getButiksNamn(), layout);
    }

    @Override
    public ButiksLayout ladda(String namn) throws IOException {
        ButiksLayout layout = mapper.readValue(
            folderPath.resolve(namn + ".json").toFile(),
            ButiksLayout.class
        );

        layoutMap.put(namn, layout);
        return layout;
    }

    public boolean exists(String namn) {
        return Files.exists(folderPath.resolve(namn + ".json"));
    }

    @Override
    public void ta_bort(String namn) throws IOException {
        Path filePath = folderPath.resolve(namn + ".json");
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            layoutMap.remove(namn);
        } else {
            throw new IOException("Recipe not found: " + namn);
        }
    }
}

        
    