import java.io.*;

public class Temp {
    public static void main(String[] args) {
        String filename = args[0];
        File file = new File(filename);
        String outputNames = file.getAbsolutePath();
        System.out.println(outputNames);
    }
}
