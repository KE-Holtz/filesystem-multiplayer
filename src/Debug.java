import java.io.File;

public class Debug {
    public static void main(String[] args) {
        File all = new File("S:\\High School\\WuestC\\Drop Box\\Multiplayer");
        deleteContents(all);
    }

    private static void deleteContents(File file) {
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                deleteContents(f);
            }
            f.delete();
        }
    }
}
