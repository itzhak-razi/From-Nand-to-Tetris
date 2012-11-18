public class HackParser {
    static int A_COMMAND = 0;
    static int C_COMMAND = 1;
    static int L_COMMAND = 2;

    private In input;
    private String currentCommand;

    public HackParser(String filename) {
        input = new In(filename);
    }

    public boolean hasMoreCommands() {
        if (input.hasNextLine()) {
            return true;
        } else {
            input.close();
            return false;
        }
    }

    public void advance() {
        String currentLine;
        do {
            currentLine = input.readLine().trim();
        } while (currentLine.equals("") ||
                 currentLine.substring(0,2).equals("//"));
        String[] parts = currentLine.split("//");
        currentCommand = parts[0];
        currentCommand = currentCommand.replace(" ", "");
    }

    public int commandType() {
        char firstChar = currentCommand.charAt(0);
        if (firstChar == '@') { return A_COMMAND; }
        else if (firstChar == '(') { return L_COMMAND; }
        else { return C_COMMAND; }
    }

    public String symbol() {
        String symbol = null;
        if (this.commandType() == A_COMMAND ) {
            symbol = currentCommand.substring(1, currentCommand.length());
        } else if (this.commandType() == L_COMMAND) {
            symbol = currentCommand.substring(1, currentCommand.length() - 1);
        }
        else { }
        return symbol;
    }

    public String dest() {
        String dest = null;
        if (this.commandType() == C_COMMAND &&
            currentCommand.indexOf("=") != -1)
                dest = currentCommand.split("=")[0];
        return dest;
    }

    public String comp() {
        String comp = null;
        if (this.commandType() == C_COMMAND) {
            if (currentCommand.indexOf("=") != -1) {
                comp = currentCommand.split("=")[1];
            } else if (currentCommand.indexOf(";") != -1) {
                comp = currentCommand.split(";")[0];
            } else { }
        }
        return comp;
    }

    public String jump() {
        String jump = null;
        if (this.commandType() == C_COMMAND) {
            if (currentCommand.indexOf(";") != -1) {
                jump = currentCommand.split(";")[1];
            } else { }
        }
        return jump;
    }

    public static void main(String[] args) {
        String a = "// just test";
        System.out.println(a.substring(0,2));
    }
}