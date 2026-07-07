package main;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class initAppData {
    public static Path userDir;

    public static void initAppData() {
        Path dir = getUserDir();

        if (isFirstRun(dir)) {
            copyDefaultRecipes(dir);
            copyDefaultShoppingLists(dir);
            copyDefaultMenus(dir);
            copyDefaultLayouts(dir);
        }
    }



    



    public static Path getUserDir() {
        userDir = main.PathLogic.USER_DIR;
        return userDir;
    }
    


    public static void copyDefaultRecipes(Path dir) {
        copyDefaultDirectory(dir, "recept", "recept");
    }

    private static void copyDefaultLayouts(Path dir) {
        copyDefaultDirectory(dir, "butikslayouter", "layouts");
    }

    private static void copyDefaultMenus(Path dir) {
        copyDefaultDirectory(dir, "veckomenyrer", "menus");
    }

    private static void copyDefaultShoppingLists(Path dir) {
        copyDefaultDirectory(dir, "inköpslistor", "shoppinglists");
    }

    private static void copyDefaultDirectory(Path dir, String sourceName, String targetName) {
        Path targetDir = dir.resolve(targetName);
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create target directory: " + targetDir, e);
        }

        List<Path> candidates = new ArrayList<>();
        Path installedResourceDir = getInstalledResourceBaseDir();
        if (installedResourceDir != null) {
            candidates.add(installedResourceDir.resolve(sourceName));
        }
        candidates.add(Path.of("resources", sourceName));
        candidates.add(Path.of("src", "dist", "resources", sourceName));
        candidates.add(Path.of("build", "install", "recepthanterare", "resources", sourceName));
        candidates.add(Path.of("install", "recepthanterare", "resources", sourceName));
        candidates.add(Path.of("build", "jpackage", "recepthanterare", "resources", sourceName));
        candidates.add(Path.of("build", "jpackage", "recepthanterare", "lib", "app", "resources", sourceName));
        candidates.add(Path.of("/opt", "recepthanterare", "lib", "app", "resources", sourceName));

        boolean copiedAny = false;
        for (Path sourceDir : candidates) {
            if (sourceDir != null && Files.exists(sourceDir) && Files.isDirectory(sourceDir)) {
                try {
                    Files.walk(sourceDir).forEach(source -> {
                        try {
                            Path relative = sourceDir.relativize(source);
                            Path destination = targetDir.resolve(relative);
                            if (Files.isDirectory(source)) {
                                Files.createDirectories(destination);
                            } else {
                                Files.createDirectories(destination.getParent());
                                if (!Files.exists(destination)) {
                                    Files.copy(source, destination);
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Error copying file: " + source + " to " + targetDir + ": " + e.getMessage());
                        }
                    });
                    copiedAny = true;
                } catch (IOException e) {
                    System.err.println("Failed walking source dir: " + sourceDir + " - " + e.getMessage());
                }
            }
        }

        if (!copiedAny) {
            System.out.println("No distribution resources found for: " + sourceName);
        }
    }

    private static Path getInstalledResourceBaseDir() {
        Path candidate = Path.of("/opt", "recepthanterare", "lib", "app", "resources");
        if (Files.isDirectory(candidate)) {
            return candidate;
        }

        try {
            URI codeSourceUri = PathLogic.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path codeSourcePath = Path.of(codeSourceUri);
            if (Files.isRegularFile(codeSourcePath)) {
                Path appFolder = codeSourcePath.getParent();
                if (appFolder != null) {
                    Path appRoot = appFolder.getParent();
                    if (appRoot != null) {
                        Path directResourceDir = appRoot.resolve("resources");
                        if (Files.isDirectory(directResourceDir)) {
                            return directResourceDir;
                        }
                        Path packagedResourceDir = appRoot.resolve("lib").resolve("app").resolve("resources");
                        if (Files.isDirectory(packagedResourceDir)) {
                            return packagedResourceDir;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static boolean isFirstRun(Path dir) {
        if (!Files.exists(dir)) {
            return true;
        }

        Path[] requiredDirs = {
            dir.resolve("recept"),
            dir.resolve("layouts"),
            dir.resolve("menus"),
            dir.resolve("shoppinglists")
        };

        for (Path requiredDir : requiredDirs) {
            if (!Files.isDirectory(requiredDir)) {
                return true;
            }

            try {
                if (Files.list(requiredDir).findAny().isPresent()) {
                    return false;
                }
            } catch (IOException e) {
                return true;
            }
        }

        return true;
    }
}
