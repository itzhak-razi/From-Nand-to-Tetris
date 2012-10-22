import java.lang.StringBuilder;

public class HackAssembler {
    static int A_COMMAND = 0;
    static int C_COMMAND = 1;
    static int L_COMMAND = 2;
    static int VAR = 16;

    public static void firstPass(HackParser parser,
                                 HackST st, String fileName) {
        int counter = -1;
        String symbol = null;
        while (parser.hasMoreCommands()) {
            parser.advance();
            if (parser.commandType() == L_COMMAND) {
                symbol = parser.symbol();
                st.addEntry(symbol, counter + 1);
            } else { counter++; }
        }
    }

    public static void secondPass(HackParser parser, HackCodeGen codeGen,
                                  HackST st, Out output) {
        while (parser.hasMoreCommands()) {
            parser.advance();
            if (parser.commandType() == A_COMMAND) {
                String a_symbol = parser.symbol();
                String a_comm = generateACommand(a_symbol, st);
                output.println(a_comm);
            } else if (parser.commandType() == C_COMMAND) {
                String dest = parser.dest();
                String comp = parser.comp();
                String jump = parser.jump();
                String c_comm = generateCCommand(dest, comp, jump, codeGen);
                output.println(c_comm);
            }
        }
    }

    private static String generateACommand(String symbol, HackST st) {
        String code = null;
        int addr;
        if (isNumeric(symbol)) {
            addr = Integer.parseInt(symbol);
        } else {
            if (st.contains(symbol)) {
                addr = st.getAddress(symbol);
            } else {
                addr = VAR++;
                st.addEntry(symbol, addr);
            }
        }
        String binary = Integer.toBinaryString(addr);
        code = changeLength(binary, 16);
        return code;
    }

    private static boolean isNumeric(String str) {
        if (str.length() == 0) { return false; }

        for (char c : str.toCharArray())
        {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    private static String changeLength(String str, int number) {
        int zeroNum = number - str.length();
        StringBuilder zeros = new StringBuilder();
        for (int i = zeroNum; i > 0; i--)
            zeros.append("0");
        zeros.append(str);
        return zeros.toString();
    }

    public static String generateCCommand(String dest, String comp,
                                          String jump, HackCodeGen codeGen) {
        StringBuilder code = new StringBuilder();
        code.append("111");
        String compCode = codeGen.comp(comp);
        code.append(compCode);
        String destCode = codeGen.dest(dest);
        code.append(destCode);
        String jumpCode = codeGen.jump(jump);
        code.append(jumpCode);
        return code.toString();
    }

    public static void main(String[] args) {
        String fileName = args[0];
        HackParser parser = new HackParser(fileName);
        HackCodeGen codeGen = new HackCodeGen();
        HackST st = new HackST();

        firstPass(parser, st, fileName);

        parser = new HackParser(fileName);
        String outName = fileName.split("[.]")[0] + "_YX.hack";
        Out output = new Out(outName);
        secondPass(parser, codeGen, st, output);

        output.close();
    }
}