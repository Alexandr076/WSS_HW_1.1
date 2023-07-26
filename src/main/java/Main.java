import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static List<String> initializeFiles() {
        List<String> fileNames = new ArrayList<>();
        File folder = new File(System.getProperty("user.dir")+"\\public");
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles.length > 0) {
            for (File file: listOfFiles) {
                fileNames.add("/" + file.getName());
            }
        }
        return fileNames;
    }

    public static void main(String[] args) {
        Server server = new Server(initializeFiles());
    }
}
