package ericholbert.databaze.database;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ericholbert.databaze.core.CorePreferences;

public final class Database {

    public static final String ROOT_NAME = "databaze";
    public static final String ITEMS_NAME = "zaznamy.txt";

    private static final Path rootPath = CorePreferences.getRootPath();

    private final String databaseName;
    private final Path databasePath;
    private final Path itemsPath;
    private final Path filesPath;
    private final DatabasePreferences preferences;

    Database(String databaseName) {
        this.databaseName = databaseName;
        databasePath = Path.of(rootPath + "/" + databaseName);
        itemsPath = Path.of(databasePath + "/" + ITEMS_NAME);
        filesPath = Path.of(databasePath + "/" + "soubory");
        preferences = new DatabasePreferences(databaseName);
    }

    public static String[] getDatabaseNames() {
        String[] allFolderNames = new File(String.valueOf(rootPath)).list();
        if (allFolderNames == null)
            return null;
        ArrayList<String> databaseNames = new ArrayList<>();
        for (String folder : allFolderNames)
            if (itemsFileExists(folder))
                databaseNames.add(folder);
        return databaseNames.toArray(String[]::new);
    }

    public static boolean itemsFileExists(String databaseName) {
        return (Files.exists(Path.of(rootPath + "/" + databaseName + "/" + ITEMS_NAME)));
    }

    String loadDatabase() {
        try {
            if (Files.exists(itemsPath))
                return Files.readString(itemsPath, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
        return null;
    }

    boolean saveDatabase(String content) {
        try {
            Files.createDirectories(databasePath);
            if (!Files.exists(itemsPath))
                Files.createFile(itemsPath);
            Files.writeString(itemsPath, content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    DatabasePreferences getDatabasePreferences() {
        return preferences;
    }

    String getDatabaseName() {
        return databaseName;
    }

    String transferFile(String input, boolean renameFile) {
        Path sourcePath = Path.of(input);
        String fileName = sourcePath.getFileName().toString();
        if (renameFile) {
            fileName = renameFile(fileName);
        }
        try {
            Files.createDirectories(filesPath);
            Files.move(sourcePath, Path.of(filesPath + "/" + fileName));
        } catch (Exception ignored) {
            return null;
        }
        return fileName;
    }

    boolean openFile(String fileName) {
        try {
            Desktop.getDesktop().open(new File(filesPath + "/" + fileName));
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    boolean deleteFile(String fileName) {
        File f = new File(filesPath + "/" + fileName);
        try {
            Desktop.getDesktop().moveToTrash(f);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private String renameFile(String fileName) {
        String p = "yy_MM_dd_HH_mm_ss";
        SimpleDateFormat sdf = new SimpleDateFormat(p);
        Date d = new Date();
        return fileName.replaceAll(".+(?=\\.)", sdf.format(d));
    }

}
