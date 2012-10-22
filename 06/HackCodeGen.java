public class HackCodeGen {
    public String dest(String destIn) {
        String destOut = null;
        if (destIn == null) { destOut = "000"; }
        else if (destIn.equals("M")) { destOut = "001"; }
        else if (destIn.equals("D")) { destOut = "010"; }
        else if (destIn.equals("MD")) { destOut = "011"; }
        else if (destIn.equals("A")) { destOut = "100"; }
        else if (destIn.equals("AM")) { destOut = "101"; }
        else if (destIn.equals("AD")) { destOut = "110"; }
        else if (destIn.equals("AMD")) { destOut = "111"; }
        else { }
        return destOut;
    }

    public String comp(String compIn) {
        String compOut = null;
        if (compIn == null) { return null; }
        else if (compIn.equals("0")) { compOut = "0101010"; }
        else if (compIn.equals("1")) { compOut = "0111111"; }
        else if (compIn.equals("-1")) { compOut = "0111010"; }
        else if (compIn.equals("D")) { compOut = "0001100"; }
        else if (compIn.equals("A")) { compOut = "0110000"; }
        else if (compIn.equals("!D")) { compOut = "0001101"; }
        else if (compIn.equals("!A")) { compOut = "0110001"; }
        else if (compIn.equals("-D")) { compOut = "0001111"; }
        else if (compIn.equals("-A")) { compOut = "0110011"; }
        else if (compIn.equals("D+1")) { compOut = "0011111"; }
        else if (compIn.equals("A+1")) { compOut = "0110111"; }
        else if (compIn.equals("D-1")) { compOut = "0001110"; }
        else if (compIn.equals("A-1")) { compOut = "0110010"; }
        else if (compIn.equals("D+A")) { compOut = "0000010"; }
        else if (compIn.equals("D-A")) { compOut = "0010011"; }
        else if (compIn.equals("A-D")) { compOut = "0000111"; }
        else if (compIn.equals("D&A")) { compOut = "0000000"; }
        else if (compIn.equals("D|A")) { compOut = "0010101"; }
        else if (compIn.equals("M")) { compOut = "1110000"; }
        else if (compIn.equals("!M")) { compOut = "1110001"; }
        else if (compIn.equals("-M")) { compOut = "1110011"; }
        else if (compIn.equals("M+1")) { compOut = "1110111"; }
        else if (compIn.equals("M-1")) { compOut = "1110010"; }
        else if (compIn.equals("D+M")) { compOut = "1000010"; }
        else if (compIn.equals("D-M")) { compOut = "1010011"; }
        else if (compIn.equals("M-D")) { compOut = "1000111"; }
        else if (compIn.equals("D&M")) { compOut = "1000000"; }
        else if (compIn.equals("D|M")) { compOut = "1010101"; }
        else { }
        return compOut;
    }

    public String jump(String jumpIn) {
        String jumpOut;
        if (jumpIn == null) {jumpOut = "000"; }
        else if (jumpIn.equals("JGT")) { jumpOut = "001"; }
        else if (jumpIn.equals("JEQ")) { jumpOut = "010"; }
        else if (jumpIn.equals("JGE")) { jumpOut = "011"; }
        else if (jumpIn.equals("JLT")) { jumpOut = "100"; }
        else if (jumpIn.equals("JNE")) { jumpOut = "101"; }
        else if (jumpIn.equals("JLE")) { jumpOut = "110"; }
        else if (jumpIn.equals("JMP")) { jumpOut = "111"; }
        else { jumpOut = "000"; }

        return jumpOut;
    }
}