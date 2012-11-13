import java.io.File;

public class JackAnalyzer {

    public static void main(String[] args) {
        String filename = args[0];
        File file = new File(filename);
        String[] children = null;
        String outputName = null; 
        boolean isDirectory = false; 

        if (file.isFile()) {
            if (filename.contains(".jack")) {
                children = new String[1];
                children[0] = filename;
                outputName = filename.split("[.]")[0] + ".asm";
            } else {
                System.out.println("Not a jack file");
                return;
            }
        } else if (file.isDirectory()) {
            String[] outputNames = file.getAbsolutePath().split("/");
            outputName = outputNames[outputNames.length - 2] + ".asm";
            children = file.list();
            isDirectory = true;
        } else {
            System.out.println(outputName);
            outputName = null;
            System.out.println("Ilegal name");
            return;
        }

        boolean containsJack = false;
        for (String child : children) {
            if (child.contains(".jack")) {
                containsJack = true;
                break;
            }
        }

        if (!containsJack) {
            System.out.println("No jack file in this directory");
            return;
        }
        
        for (String child : children) {
            if (child.matches("^.*jack$")) {
                System.out.printf("Writing %s\n", child);
                CompilationEngine engine = new CompilationEngine(child);
                engine.compileClass();
            }
        }
    }
}
