import java.util.Hashtable;

public class SymbolTable {
    // a way to initialize static hashtable
    private static final Hashtable<Integer, String> map;
    static {
        map = new Hashtable<Integer, String>();
        map.put(JackTokens.STATIC, "static");
        map.put(JackTokens.FIELD, "this");
        map.put(JackTokens.ARG, "argument");
        map.put(JackTokens.VAR, "local");
        //map.put(JackTokens., "local");
        //map.put(JackTokens.VAR, "local");
        //map.put(JackTokens.VAR, "local");
        //map.put(JackTokens.VAR, "local");
    }

    private Hashtable<String, Info> classTable;
    private Hashtable<String, Info> subTable;
    private int staticIndex, fieldIndex, argIndex, varIndex;

    private class Info {
        private String type;
        private String kind;
        private int index;

        public Info(String t, int k, int i) {
            type = t;
            if (k >= 0 && k <= 27) kind = map.get(k);
            else System.out.println("Illegal kind");

            index = i;
        }
    }

    public SymbolTable() {
        classTable = new Hashtable<String, Info>();
        subTable = new Hashtable<String, Info>();
    }

    public void startSubroutine() {
        // start a new subroutine scope. i.e. reset the subTable
        subTable = new Hashtable<String, Info>();
        argIndex = 0;
        varIndex = 0;
    }

    public void define(String n, String t, int k) {
        // n: name, t: type(int, string ...), k: kind(static, field ...)
        Info newInfo;
        if (k == JackTokens.STATIC || k == JackTokens.FIELD) {
            if (k == JackTokens.STATIC) newInfo = new Info(t, k, staticIndex++);
            else                        newInfo = new Info(t, k, fieldIndex++);
            classTable.put(n, newInfo);

        } else if (k == JackTokens.ARG || k == JackTokens.VAR) {
            if (k == JackTokens.ARG) newInfo = new Info(t, k, argIndex++);
            else                     newInfo = new Info(t, k, varIndex++);
            subTable.put(n, newInfo);
        }
    }

    public int varCount(int k) {
        // k: kind
        switch (k) {
            case JackTokens.STATIC: return staticIndex; 
            case JackTokens.FIELD : return fieldIndex;  
            case JackTokens.ARG   : return argIndex;    
            case JackTokens.VAR   : return varIndex;    
            default               : return -1;          
        }
    }

    public String kindOf(String name) {
        if (subTable.containsKey(name)) {
            return subTable.get(name).kind;
        }

        if (classTable.containsKey(name)) {
            return classTable.get(name).kind;
        }

        return "KIND_ERROR";
    }

    public String typeOf(String name) {
        if (subTable.containsKey(name)) {
            return subTable.get(name).type;
        }

        if (classTable.containsKey(name)) {
            return classTable.get(name).type;
        }

        return "TYPE_ERROR";
    }

    public int indexOf(String name) {
        if (subTable.containsKey(name)) {
            return subTable.get(name).index;
        }

        if (classTable.containsKey(name)) {
            return classTable.get(name).index;
        }

        return JackTokens.ERROR;
    }

}
