package dk.pfrandsen;

import java.io.File;
import java.nio.file.Path;

public class Utilities {

    public static void deleteFolder(Path folder) {
        if (folder.toFile().exists() && folder.toFile().isDirectory()) {
            System.out.println("deleting " + folder.toAbsolutePath());
            deleteRecursive(folder.toFile());
        }
    }

    static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteRecursive(f);
                }
                if (!file.delete()) {
                    System.out.println("Could not delete folder " + file);
                }
            }
        } else {
            if (!file.delete()) {
                System.out.println("Could not delete file " + file);
            }
        }
    }

}
