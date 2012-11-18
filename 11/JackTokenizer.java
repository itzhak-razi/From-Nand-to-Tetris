import java.lang.StringBuilder;
import java.util.*;

public class JackTokenizer {
    private static String[] symbolArray = {"{", "}", "(", ")", "[", "]", ".",
        ",", ";", "+", "-", "*", "/", "&", "|", "<", ">", "=", "~"};
    private static List symbols = Arrays.asList(symbolArray);
    
    //private static String symbolPattern = "(.*)(\\{|\\}|\\(|\\)|\\[|\\]|\\.|\\,|;|\\+|-|\\*|/|&|\\||<|>|=|~)(.*)";
    private static String symbolPattern = "(^[^\"]*)(\\{|\\}|\\(|\\)|\\[|\\]|\\.|\\,|;|\\+|-|\\*|/|&|\\||<|>|=|~)(.*)";
    private static String commentPattern = "(^//.*)|(^/\\*.*)|^\\*.*";
    private static String keywordPattern = "class|constructor|function|method|field|static|var|int|char|boolean|void|true|false|null|this|let|do|if|else|while|return";
    private static String identifierPattern = "^[^\\d\\W]\\w*\\Z";
    private static String intPattern = "\\d+";
    private static String stringPattern = "^\"[^\"]+\"$";


    private In input; 
    private String currentLine; // the current line of the input
    private String[] currentWords; // the current words of the line. splitted by whitespace
    private int position;  // the current position of word in currentWords
    private String currentWord;  // the current processing word
    private String token;  // = currentWord if no symbol in it.  Else, split around symbol

    public JackTokenizer(String filename) {
        input = new In(filename);
        setCurrentWords(); // initiate currentWords
        position = 0;  // set initial position to 0
    }

    public boolean hasMoreTokens() {
        if (input.hasNextLine() || position < currentWords.length) {
            return true;
        } else {
            input.close();
            return false;
        }
    }
    
    public void advance() {
        if (!hasMoreTokens()) { return; }

        if (position >= currentWords.length) {
           setCurrentWords();  // change line
           position = 0;  // reset position
       }
        
       token = currentWords[position++];
    }

    private void setCurrentWords() {
        do {
            currentLine = input.readLine().trim();
        } while (currentLine.equals("") ||
                 isComment());  // skip empty and commenting lines
        String[] words = currentLine.split("//");  // strip trailing comments
        currentLine = words[0];
        // replace space of string literals " " with _ so that they will not be separated
        if (currentLine.length() > 1 && 
            (currentLine.contains("\"") || currentLine.matches(symbolPattern))) { replaceStringLiteral(); }
        currentWords = currentLine.split("\\s+");
    }

    private void replaceStringLiteral() {
        StringBuilder builder = new StringBuilder();
        boolean replace = false;

        for (int i = 0; i < currentLine.length(); i++) {
            String c = currentLine.substring(i, i + 1);
            if (c.equals("\"")) {
                if (replace) { replace = false; }
                else { replace = true; }
            }

            if (c.equals(" ")) {
                if (replace) {
                    c = "_";
                }
            }

            if (symbols.contains(c)) {
                if (!replace) {
                    c = " " + c + " ";
                }
            }

            builder.append(c);
        }
        currentLine = builder.toString();
        // nice debugging tool. see exactly the line that the compliler failed
        System.out.println(currentLine);
    }

    private boolean isComment() {
        return currentLine.matches(commentPattern);
    }

    public int tokenType() {
        if (token.matches(keywordPattern)) { return JackTokens.KEYWORD; }
        else if (token.matches(symbolPattern)) { return JackTokens.SYMBOL; }
        else if (token.matches(identifierPattern)) { return JackTokens.IDENTIFIER; }
        else if (token.matches(intPattern)) { return JackTokens.INT_CONST; }
        else if (token.matches(stringPattern)) { return JackTokens.STRING_CONST; }
        else { return JackTokens.ERROR; }
    }

    public int keyword() {
        if (tokenType() != JackTokens.KEYWORD) { return JackTokens.ERROR; }
        if (token.equals("class")) { return JackTokens.CLASS; }
        else if (token.equals("method")) { return JackTokens.METHOD; }
        else if (token.equals("function")) { return JackTokens.FUNCTION; }
        else if (token.equals("constructor")) { return JackTokens.CONSTRUCTOR; }
        else if (token.equals("int")) { return JackTokens.INT; }
        else if (token.equals("boolean")) { return JackTokens.BOOLEAN; }
        else if (token.equals("char")) { return JackTokens.CHAR; }
        else if (token.equals("void")) { return JackTokens.VOID; }
        else if (token.equals("var")) { return JackTokens.VAR; }
        else if (token.equals("static")) { return JackTokens.STATIC; }
        else if (token.equals("field")) { return JackTokens.FIELD; }
        else if (token.equals("let")) { return JackTokens.LET; }
        else if (token.equals("do")) { return JackTokens.DO; }
        else if (token.equals("if")) { return JackTokens.IF; }
        else if (token.equals("else")) { return JackTokens.ELSE; }
        else if (token.equals("while")) { return JackTokens.WHILE; }
        else if (token.equals("return")) { return JackTokens.RETURN; }
        else if (token.equals("true")) { return JackTokens.TRUE; }
        else if (token.equals("false")) { return JackTokens.FALSE; }
        else if (token.equals("null")) { return JackTokens.NULL; }
        else if (token.equals("this")) { return JackTokens.THIS; }
        else { return JackTokens.ERROR; }
    }

    public String token() {
        //if (tokenType() != JackTokens.KEYWORD) {
        //    System.out.println("Cann't access non-keyword token through this method");
        //    return "Error";
        //}
        return new String(token);
    }
    
    public String symbol() {
        if (tokenType() != JackTokens.SYMBOL) { return "Error"; }
        else { return new String(token); }
    }

    public String identifier() {
        if (tokenType() != JackTokens.IDENTIFIER) { return "ERROR"; }
        return new String(token);
    }

    public int intVal() {
        if (tokenType() != JackTokens.INT_CONST) { return JackTokens.ERROR; }
        return Integer.parseInt(token);
    }

    public String stringVal() {
        if (tokenType() != JackTokens.STRING_CONST) { return "ERROR"; }
        token = token.replace("_", " ");
        return token.replace("\"", "");
    }

    public void writeXML(Out output) {
        output.println("<tokens>");
        while (hasMoreTokens()) {
            advance();
            int type = tokenType();
            switch (type) {
                case JackTokens.KEYWORD: writeTag(output, token, "keyword"); break;
                case JackTokens.SYMBOL: writeTag(output, symbol(), "symbol"); break;
                case JackTokens.IDENTIFIER: writeTag(output, identifier(), "identifier"); break;
                case JackTokens.INT_CONST: writeTag(output, Integer.toString(intVal()),
                                                   "integerConstant"); break;
                case JackTokens.STRING_CONST: writeTag(output, stringVal(), "stringConstant");
                                              break;
                default: writeTag(output, "ERROR", "error"); break;
            } 
        }
        output.println("</tokens>");
    }

    private void writeTag(Out output, String word, String type) {
        output.println("<" + type + "> " + word + " </" + type + ">");
        System.out.println("<" + type + "> " + word + " </" + type + ">");
    }

    public static void main(String[] args) {
        String symbolTest = "(^[^\"]*)(\\{|\\}|\\(|\\)|\\[|\\]|\\.|\\,|;|\\+|-|\\*|/|&|\\||<|>|=|~)(.*)";
        System.out.printf("match: %b\n", "\"{\"".matches(symbolTest));
        String filename = args[0];
        String outputName = "YX" + filename.split("\\.")[0] + "T.xml";
        Out output = new Out(outputName);
        JackTokenizer tokenizer = new JackTokenizer(filename);
        tokenizer.writeXML(output);
    }
}
