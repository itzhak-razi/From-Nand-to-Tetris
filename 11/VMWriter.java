public class VMWriter {
    Out output;

    public VMWriter(String filename) {
        output = new Out(filename);
    }

    public void writePush(String segment, int index) {
        output.printf("push %s %d\n", segment, index);
    }

    public void writePop(String segment, int index) {
        output.printf("pop %s %d\n", segment, index);
    }

    public void writeArithmetic(String command) {
        output.println(command);
    }

    public void writeLabel(String label, int counter) {
        output.printf("label %s\n", label + Integer.toString(counter));
    }

    public void writeGoto(String label, int counter) {
        output.printf("goto %s\n", label + Integer.toString(counter));
    }

    public void writeIf(String label, int counter) {
        output.printf("if-goto %s\n", label + Integer.toString(counter));
    }

    public void writeCall(String name, int nArgs) {
        output.printf("call %s %d\n", name, nArgs);
    } 

    public void writeFunction(String name, int nLocals) {
        output.printf("function %s %d\n", name, nLocals);
    }

    public void writeReturn() {
        output.println("return");
    }

    public void close() {
        output.close();
    }
}
