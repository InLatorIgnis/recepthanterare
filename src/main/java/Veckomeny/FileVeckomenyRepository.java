package Veckomeny;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileVeckomenyRepository implements VeckomentyRepository {
    
    public static final Path DEFAULT_WEEKMENUS_DIR = determineDefaultWeekmenusDir();
    
    private static Path determineDefaultWeekmenusDir() {
        Path distributionDir = Path.of("resources", "veckomenyrer");
        if (Files.exists(distributionDir)) {
            return distributionDir;
        }
        return Path.of("src", "dist", "resources", "veckomenyrer");
    }
    
    private Map<String, Veckomeny> veckomenyCahe = new HashMap<>();
    private final Path folderPath;
    private ObjectMapper mapper = new ObjectMapper();

    public FileVeckomenyRepository() throws IOException {
        this(DEFAULT_WEEKMENUS_DIR);
    }

    public FileVeckomenyRepository(Path folderPath) throws IOException {
        this.folderPath = folderPath;

        if (Files.exists(folderPath) && !Files.isDirectory(folderPath)) {
            throw new IOException("Target path exists but is not a directory: " + folderPath);
        }

        Files.createDirectories(folderPath);
        System.out.println("Using weekly menus folder: " + folderPath.toAbsolutePath());
    }

    @Override
    public void spara(Veckomeny veckomeny) throws IOException {
            String name = veckomeny.getNamn();
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Weekly menu name cannot be null or blank");
            
        }
        

        mapper.writeValue(
            folderPath.resolve(veckomeny.getNamn() + ".json").toFile(),
            veckomeny
        );
        veckomenyCahe.put(veckomeny.getNamn(), veckomeny);
    }

    @Override
    public Veckomeny ladda(String namn) throws IOException {
        Veckomeny veckomeny = mapper.readValue(
            folderPath.resolve(namn + ".json").toFile(),
            Veckomeny.class
        );
        veckomenyCahe.put(namn, veckomeny);
        return veckomeny;
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
            veckomenyCahe.remove(namn);
        } else {
            throw new IOException("Weekly menu not found: " + namn);
        }
    }

    
}
