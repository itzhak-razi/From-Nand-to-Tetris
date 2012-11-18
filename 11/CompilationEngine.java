public class CompilationEngine {
    private static String whileCON = "WHILE_CON";
    private static String whileEND = "WHILE_END";
    private static String ifTrue = "IF_TRUE";
    private static String ifFalse = "IF_FALSE";

    private JackTokenizer tokenizer;
    private SymbolTable table;
    private VMWriter writer;
    private String className;
    private String currentName;
    private int currentKind;
    private String currentType;
    private int whileCounter, ifCounter;

    public CompilationEngine(String filename) {
        tokenizer = new JackTokenizer(filename);
        table = new SymbolTable();
        String outputName = filename.split("\\.")[0] + ".vm";
        writer = new VMWriter(outputName);
	}

    private void addToTable() { 
        table.define(currentName, currentType, currentKind); 
    }

    public void compileClass() {
        tokenizer.advance();
        if (tokenizer.keyword() == JackTokens.CLASS) {
            // write class name
            tokenizer.advance();
            if (checkIdentifier()) {
                // className is used to generate unique subroutine identifiers
                className = tokenizer.identifier();
            } else {
                System.out.println("illegal class name identifier");
                return;
            }

            // write {
            tokenizer.advance();
            if (!checkSymbol("{")) {
                System.out.println("no openning { for class");
                return;
            }

            // parse potential classVarDec
            tokenizer.advance();
            while ( tokenizer.keyword() == JackTokens.STATIC ||
                    tokenizer.keyword() == JackTokens.FIELD) {
                compileClassVarDec();
                tokenizer.advance();
            }

            // parse potential subroutineDec
            while ( tokenizer.keyword() == JackTokens.CONSTRUCTOR ||
                    tokenizer.keyword() == JackTokens.FUNCTION ||
                    tokenizer.keyword() == JackTokens.METHOD) {
                compileSubRoutine();
                tokenizer.advance();
            }

            // write }
            if (!checkSymbol("}")) {
                System.out.println("no closing } for class");
                return;
            }

            if (tokenizer.hasMoreTokens()) {
                System.out.println("addtional tokens after closing }");
            }
        } else {
            System.out.println("does not start with class");
            return;
        }
    }
    
    public void compileClassVarDec() {
        // we already know the current token is legit, so directy write it out.
        // set kind here
        currentKind = tokenizer.keyword();

        // match type
        tokenizer.advance();

        // type is set in checkAndWriteType
        if (!checkAndWriteType()) {
            System.out.println("illegal type for class var dec");
            return;
        }

        // match varName
        tokenizer.advance();
        
        // currentName is set in checkIdentifier
        if (checkIdentifier()) {
            // put into symbol table
            addToTable();
        } else {
            System.out.println("illegal classVar identifier");
            return;
        }
        

        // match potential ", varName" part
        tokenizer.advance();
        while (tokenizer.symbol().equals(",")) {
            tokenizer.advance();
            if (checkIdentifier()) {
                addToTable();
            } else {
                System.out.println("illegal classVar identifier");
                return;
            }
            tokenizer.advance();
        }

        // match ;
        if (!checkSymbol(";")) {
            System.out.println("no ending ;");
            return;
        }
    }

    public void compileSubRoutine() {
        // clear the previous subroutine symbol table
        table.startSubroutine();
        //isVoid = false;

        // already know that the current token start with constructor, function or method
        int subRoutineKind = tokenizer.keyword();  // is it a function or method or constructor

        // match return type
        tokenizer.advance();
        if (!checkAndWriteType()) {
            System.out.println("Illegal type name for subroutine");
            return;
        }

        String currentSubName = null;

        // match subroutine identifier
        tokenizer.advance();
        if (checkIdentifier()) {
            currentSubName = className + "." + currentName;
        } else {
            System.out.println("illegal subroutine name");
            return;
        }
        
        // if it is a method, the first argument is self
        if (subRoutineKind == JackTokens.METHOD) {
            table.define("this", className, JackTokens.ARG);
        }

        // match parameter list
        tokenizer.advance();
        if (checkSymbol("(")) {
            compileParameterList();
        } else {
            System.out.println("no () after function name");
            return;
        }

        // match the closing ) for the paramater list
        if (!checkSymbol(")")) {
            System.out.println("no () after function name");
            return;
        }

        // match subroutine body
        tokenizer.advance();
        if (checkSymbol("{")) {
            tokenizer.advance();
            while ( tokenizer.tokenType() == JackTokens.KEYWORD &&
                    tokenizer.token().equals("var")) {
                compileVarDec();
                tokenizer.advance();
            }
        } else {
            System.out.println("no { after function parameters");
            return;
        }
         
         // write function
        writer.writeFunction(currentSubName, table.varCount(JackTokens.VAR));

        if (subRoutineKind == JackTokens.CONSTRUCTOR) {
            // allocate space in constructor
            int numOfFields = table.varCount(JackTokens.FIELD);
            if (numOfFields > 0) writer.writePush("constant", numOfFields);
            writer.writeCall("Memory.alloc", 1);
            writer.writePop("pointer", 0);
        } else if (subRoutineKind == JackTokens.METHOD) {
            // set up this pointer in method
            writer.writePush("argument", 0);
            writer.writePop("pointer", 0);
        }

        //compileSubroutineBody();
        compileStatements();

        // match }
        if (!checkSymbol("}")) {
            System.out.println("no } found to close subroutine call");
            System.out.printf("current token is : %s\n", tokenizer.token());
        }

        //if (isVoid) writer.writePush("constant", 0);
        return;
    }

    public int compileParameterList() {
        // we know that the kind is argument
        currentKind = JackTokens.ARG;
        int numberOfArgs = 0;

        // write type
        tokenizer.advance();
        if (checkAndWriteType()) {
            // match varName
            tokenizer.advance();
            if (checkIdentifier()) {
                addToTable();
                numberOfArgs++;
            } else {
                System.out.println("illegal identifier in parameter list");
                return -1;
            }

            // match other arguments
            tokenizer.advance();
            while (tokenizer.symbol().equals(",")) {
                tokenizer.advance();
                if (!checkAndWriteType()) {
                    System.out.println("illegal type name");
                    return -1;
                }
                tokenizer.advance();
                if (checkIdentifier()) {
                    addToTable();
                    numberOfArgs++;
                } else {
                    System.out.println("illegal identifier name");
                    return -1;
                }
                tokenizer.advance();
            }
        }
        return numberOfArgs;
    }

    public void compileVarDec() {
        // set kind and write var
        currentKind = JackTokens.VAR;

        // check type
        tokenizer.advance();
        if (!checkAndWriteType()) {
            System.out.println("illegal type for var");
            return;
        }

        // check varName
        tokenizer.advance();
        if (checkIdentifier()) {
            System.out.printf("adding: %s\n", currentName);
            addToTable();
        } else {
            System.out.println("illegal identifier for var");
            return;
        }

        tokenizer.advance();
        while (tokenizer.symbol().equals(",")) {
            tokenizer.advance();
            if (checkIdentifier()) {
                System.out.printf("adding: %s\n", currentName);
                addToTable();
            } else {
                System.out.println("illegal identifier for var");
                return;
            }

            tokenizer.advance();
        }

        if (!checkSymbol(";")) {
            System.out.println("varDec doesn't end with ;");
            return;
        }
    }

    public void compileStatements() {
        while (tokenizer.tokenType() == JackTokens.KEYWORD) {
            int keyword_type = tokenizer.keyword();
            // compileIf needs to do one token look ahead to check "else",
            // so no more advance here.
            switch(keyword_type) {
                case JackTokens.LET:    compileLet(); tokenizer.advance(); break;
                case JackTokens.IF:     compileIf(); break;
                case JackTokens.WHILE:  compileWhile(); tokenizer.advance(); break;
                case JackTokens.DO:     compileDo(); tokenizer.advance(); break;
                case JackTokens.RETURN: compileReturn(); tokenizer.advance(); break;
                default: System.out.println("illegal statement"); return;
            }
        }
    }

    private boolean checkAndWriteType() {
        if (tokenizer.keyword() == JackTokens.INT ||
            tokenizer.keyword() == JackTokens.CHAR ||
            tokenizer.keyword() == JackTokens.BOOLEAN) {
            // set current type here
            currentType = tokenizer.token();
            return true;
        } else if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
            currentType = tokenizer.token();
            return true;
        } else if (tokenizer.tokenType() == JackTokens.KEYWORD && tokenizer.token().equals("void")) {
            currentType = tokenizer.token();
            return true;
        } else {
            return false;
        }
    }

    private boolean checkIdentifier() {
        if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
            currentName = tokenizer.identifier();
            return true;
        } else {
            return false;
        }
    }

    private boolean checkSymbol(String s) {
        if (tokenizer.symbol().equals(s)) return true; 
        else                              return false;
    }

    private boolean checkKeyword(String k) {
        if (tokenizer.tokenType() == JackTokens.KEYWORD &&
            tokenizer.token().equals(k)) {
            return true;
        } else {
            return false;
        }
    }

    public void compileLet() {
        tokenizer.advance();
        if (!checkIdentifier()) {
            System.out.println("Illegal identifier");
            return;
        }

        String var = currentName;
        boolean isArray = false;
        String kind = table.kindOf(var);
        String type = table.typeOf(var);
        int index = table.indexOf(var);

        tokenizer.advance();
        if (checkSymbol("[")) {
            compileArrayTerm();
            isArray = true;
            // if has [], advance and next should be =

            // the top of stack should be the index
            writer.writePush(kind, index);
            writer.writeArithmetic("add");
            writer.writePop("temp", 2);
            //writer.writePop("pointer", 1);

            tokenizer.advance();
        }

        if (!checkSymbol("=")) {
            System.out.println("No = found");
            return;
        }

        tokenizer.advance();
        compileExpression();
        
        // the result should be on the top of stack now
        // write out pop code
        if (isArray) {
            //writer.writePop("temp", 0);
            //writer.writePop("pointer", 1);
            //writer.writePush("temp", 0);
            //writer.writePop("that", 0);
            writer.writePush("temp", 2);
            writer.writePop("pointer", 1);
            writer.writePop("that", 0);
        } else {
            writer.writePop(kind, index);
        }

        // No need to advance because compileExpression does one token look ahead
        if (!checkSymbol(";")) {
            System.out.println("No ; found at the end of statement");
            return;
        }
    }

    public void compileIf() {
        int localCounter = ifCounter++;

        tokenizer.advance();
        if (!checkSymbol("(")) {
            System.out.println("No openning ( for if statement");
            return;
        }

        tokenizer.advance();
        compileExpression();

        //tokenizer.advance();
        if (!checkSymbol(")")) {
            System.out.println("No closing ) for if statement");
            return;
        }

        // generate ~if
        writer.writeArithmetic("not");

        tokenizer.advance();
        if (!checkSymbol("{")) {
            System.out.println("No { for if statement");
            return;
        }

        // if-goto L1
        writer.writeIf(ifFalse, localCounter);
        tokenizer.advance();
        compileStatements();

        // goto L2
        writer.writeGoto(ifTrue, localCounter);

        if (!checkSymbol("}")) {
            System.out.println("No } for if statement");
            System.out.printf("the current symbol is %s\n", tokenizer.token());
            return;
        }

        // label l1
        writer.writeLabel(ifFalse, localCounter);

        tokenizer.advance();
        if (checkKeyword("else")) {
            tokenizer.advance();
            if (!checkSymbol("{")) {
                System.out.println("No { for else statment");
                return;
            }

            tokenizer.advance();
            compileStatements();

            //tokenizer.advance();
            if (!checkSymbol("}")) {
                System.out.println("No } for if statement");
                return;
            }
            tokenizer.advance();
        }

        // label l2
        writer.writeLabel(ifTrue, localCounter);
    }

    public void compileWhile() {
        // label l1
        int localCounter = whileCounter++;

        writer.writeLabel(whileCON, localCounter);
        
        tokenizer.advance();
        if (!checkSymbol("(")) {
            System.out.println("No ( in while statement");
            return;
        }

        tokenizer.advance();
        compileExpression();

        //tokenizer.advance();
        if (!checkSymbol(")")) {
            System.out.println("No ) in while statement");
            return;
        }
        
        // negate the top of stack
        writer.writeArithmetic("not");

        tokenizer.advance();
        if (!checkSymbol("{")) {
            System.out.println("No { in while statement");
            return;
        }
        // if-goto l2
        writer.writeIf(whileEND, localCounter);

        tokenizer.advance();
        compileStatements();

        // goto l1
        writer.writeGoto(whileCON, localCounter);
        
        //tokenizer.advance();
        if (!checkSymbol("}")) {
            System.out.println("No } in while statement");
            return;
        }

        // label l2
        writer.writeLabel(whileEND, localCounter);
    }

    public void compileDo() {
        tokenizer.advance();
        // Before call compileSubRoutineCall, first check if the current
        // token is valid identifier. Then advance again and check if the it is . or (
        if (checkIdentifier()) {
            String firstHalf = currentName;
            tokenizer.advance();
            if (checkSymbol(".") || checkSymbol("(")) {
                // if it's ".", means currentName now is other class names
                // else, we are calling self methods.
                // if the current symbol is (
                compileSubRoutineCall(firstHalf);
            } else {
                System.out.println("Not valid subroutine call");
                return;
            }
            
            
        } else {
            System.out.printf("%s is not a valid identifier for do statement\n", tokenizer.token());
            return;
        }

        tokenizer.advance();
        if (!checkSymbol(";")) {
            System.out.println("No closing ;");
            return;
        }
        // pop the 0 from void return function
        writer.writePop("temp", 0);
    }

    public void compileReturn() {
        tokenizer.advance();
        // if the following is not ; then try to parse argument
        if (!checkSymbol(";")) {
            compileExpression();

            // after the expresison, it should end with ;
            if (!checkSymbol(";")) {
                System.out.println("return statement not ending with ;");
                return;
            }
        } else {
            writer.writePush("constant", 0);
        }
        // write return
        writer.writeReturn();
    }

    public void compileExpression() {
        compileTerm();

        // compileTerm needs to do one token look ahead, so no advance here.
        while (checkSymbol("+") || checkSymbol("-") || checkSymbol("*") || checkSymbol("/") ||
               checkSymbol("&") || checkSymbol("|") || checkSymbol("<") || checkSymbol(">") ||
               checkSymbol("=")) {
            String localSymbol = tokenizer.symbol(); 
            tokenizer.advance();
            compileTerm();
            // write op vm code here
            if (localSymbol.equals("+")) {
                writer.writeArithmetic("add");
            } else if (localSymbol.equals("-")) {
                writer.writeArithmetic("sub");
            } else if (localSymbol.equals("*")) {
                // TODO: make sure the correctness of this line.  Need to push the arguments?
                writer.writeArithmetic("call Math.multiply 2");
            } else if (localSymbol.equals("/")) {
                writer.writeArithmetic("call Math.divide 2");
            } else if (localSymbol.equals("&")) {
                writer.writeArithmetic("and");
            } else if (localSymbol.equals("|")) {
                writer.writeArithmetic("or");
            } else if (localSymbol.equals("<")) {
                writer.writeArithmetic("lt");
            } else if (localSymbol.equals(">")) {
                writer.writeArithmetic("gt");
            } else if (localSymbol.equals("=")) {
                writer.writeArithmetic("eq");
            }
            // no advance here, because compileTerm needs to do one token look ahead
        }
    }

    public void compileTerm() {
        if (tokenizer.tokenType() == JackTokens.INT_CONST) {
            // push n
            writer.writePush("constant", tokenizer.intVal());
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokens. STRING_CONST) {
            // Need to create a string object here
            String strLiteral = tokenizer.stringVal();
            // push strLiteral.length()
            writer.writePush("constant", strLiteral.length());
            System.out.println("here: " +strLiteral + " " + Integer.toString(strLiteral.length()));
            // call String.new 1
            writer.writeCall("String.new", 1);
            // pop temp 0
            //writer.writePop("temp", 0);
            for (int i = 0; i < strLiteral.length(); i++) {
                //      push temp 0
                //writer.writePush("temp", 0);
                //      push int i
                //writer.writePush("constant", i);
                //      push char c
                writer.writePush("constant", (int) strLiteral.charAt(i));
                //      call String.setCharAt 3
                writer.writeCall("String.appendChar", 2);
                //      pop temp 1
                //writer.writePop("temp", 1);
            }
            //now on top of the stack should be the address of an intialized string
            //writer.writePush("temp", 0);

            tokenizer.advance();
        } else if (checkKeyword("true") || checkKeyword("false") || checkKeyword("null") ||
                   checkKeyword("this")) {
            if (checkKeyword("null") || checkKeyword("false")) {
                writer.writePush("constant", 0);
            } else if (checkKeyword("true")) {
                writer.writePush("constant", 1);
                writer.writeArithmetic("neg");
            } else if (checkKeyword("this")) {
                writer.writePush("pointer", 0);
            }
            tokenizer.advance();
        } else if (checkSymbol("-") || checkSymbol("~")) {
            tokenizer.advance();
            String localSymbol = tokenizer.symbol(); 
            compileTerm();

            // output op
            if (localSymbol.equals("-")) { 
                writer.writeArithmetic("neg");
            } else {
                writer.writeArithmetic("not");
            }
        } else if (checkIdentifier()) {
            String firstHalf = currentName;
            tokenizer.advance();
            if (checkSymbol("[")) {
                // push the array base address
                writer.writePush(table.kindOf(firstHalf), table.indexOf(firstHalf));
                compileArrayTerm();
                writer.writeArithmetic("add");
                writer.writePop("pointer", 1);
                writer.writePush("that", 0);
                tokenizer.advance();
            } else if (checkSymbol("(") || checkSymbol(".")) {
                compileSubRoutineCall(firstHalf);
                tokenizer.advance(); 
            } else {
                // if doesn't match [, (, or ., it is a normal identifier
                writer.writePush(table.kindOf(firstHalf), table.indexOf(firstHalf));
            }
        } else if (tokenizer.tokenType() == JackTokens.SYMBOL) {
            if (checkSymbol("(")) {
                tokenizer.advance();
                compileExpression();
                if (checkSymbol(")")) {
                    tokenizer.advance();
                } else {
                    System.out.println("no closing bracket for term");
                }
            }

        } else {
            System.out.printf("illegal varName: %s\n", tokenizer.token());
            return;
        }
    }

    public void compileArrayTerm() {
        tokenizer.advance();
        compileExpression();

        if (!checkSymbol("]")) {
            System.out.println("No closing ] for the array expression");
        }
    }

    public void compileSubRoutineCall(String firstHalf) {
        String classRegx = "^[A-Z].*";
        boolean isClass;

        if (firstHalf.matches(classRegx)) isClass = true;
        else                              isClass = false;
        
        String fullSubName = null;
        int numOfArgs = 0;

        if (tokenizer.symbol().equals("(")) {
            fullSubName = className + "." + firstHalf;
            tokenizer.advance();
            // this is a self method. Push this pointer here
            writer.writePush("pointer", 0);
            numOfArgs = compileExpressionList(isClass);

            if (!checkSymbol(")")) {
                System.out.println("No closing ) for the expressionlist");
                return;
            }
        } else {
            tokenizer.advance();
            if (checkIdentifier()) {
                if (isClass) { 
                    // class function, don't push this pointer
                    fullSubName = firstHalf + "." + currentName;
                } else {
                    // firstHalf must be a variable defined in the symbol table
                    fullSubName = table.typeOf(firstHalf) + "." + currentName;
                    // push b's address
                    writer.writePush(table.kindOf(firstHalf), table.indexOf(firstHalf));
                }
            } else {
                System.out.println("illegal identifier for subroutine call");
                return;
            }

            tokenizer.advance();
            if (!checkSymbol("(")) {
                System.out.println("Expecting a open bracket in subroutine call");
                return;
            }

            tokenizer.advance();
            numOfArgs = compileExpressionList(isClass);

            if (!checkSymbol(")")) {
                System.out.printf("%s %d: is not closing ) for the expressionlist\n", tokenizer.token(), 
                        tokenizer.tokenType());
                return;
            }
        }
        if (fullSubName != null) writer.writeCall(fullSubName, numOfArgs); 
    }

    public int compileExpressionList(boolean isClass) {
        // push this pointer is done in compileSubroutineCall
        int argCounter = 1;
        if (isClass) argCounter = 0;

        if (!tokenizer.symbol().equals(")")) {
            compileExpression();
            argCounter++;
            // push argument
            //writer.writePush("argument", argCounter++);


            // because compileExpression did 1 token look ahead, no advance here
            while (checkSymbol(",")) {
                tokenizer.advance();
                compileExpression();
                argCounter++;
                //writer.writePush("argument", argCounter++);
            }
        }

        return argCounter;
    }

    public static void main(String[] args) {
        String filename = args[0];
        CompilationEngine engine = new CompilationEngine(filename);
        engine.compileClass();
    }
}
