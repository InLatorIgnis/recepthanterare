package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PathLogic {
    public static final Path USER_DIR = Path.of(System.getProperty("user.home"), ".recepthanterare");
    public static final Path RECIPES_DIR = USER_DIR.resolve("recept");
    public static final Path LAYOUTS_DIR = USER_DIR.resolve("layouts");
    public static final Path MENUS_DIR = USER_DIR.resolve("menus");
    public static final Path SHOPPINGLISTS_DIR = USER_DIR.resolve("shoppinglists");

    static {
        try {
            Files.createDirectories(USER_DIR);
            Files.createDirectories(RECIPES_DIR);
            Files.createDirectories(LAYOUTS_DIR);
            Files.createDirectories(MENUS_DIR);
            Files.createDirectories(SHOPPINGLISTS_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Could not create application data directories", e);
        }
        System.out.println("Using user data dir: " + USER_DIR.toAbsolutePath());
    }
}