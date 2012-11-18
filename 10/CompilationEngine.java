public class CompilationEngine {
    private JackTokenizer tokenizer;
    private Out output;
    private String spacing;

    public CompilationEngine(String filename) {
        tokenizer = new JackTokenizer(filename);
        String outputName = "YX" + filename.split("\\.")[0] + ".xml";
        output = new Out(outputName);
        spacing = "";
	}

    public void compileClass() {
        tokenizer.advance();
        if (tokenizer.keyword() == JackTokens.CLASS) {
            // write class
            output.println(spacing + "<class>");
            increaseSpacing();

            writeTag(tokenizer.token(), "keyword");

            // write class name
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
                writeTag(tokenizer.identifier(), "identifier");
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
                System.out.printf("%s %d %d: is not closing } for class\n", tokenizer.token(), tokenizer.tokenType(), tokenizer.keyword());
                return;
            }

            if (tokenizer.hasMoreTokens()) {
                System.out.println("addtional tokens after closing }");
            }

            // write close tag of class
            decreaseSpacing();
            output.println(spacing + "</class>");
        } else {
            System.out.println("does not start with class");
            return;
        }
    }

    public void compileClassVarDec() {
        // we already know the current token is legit, so directy write it out.
        output.println(spacing + "<classVarDec>");
        increaseSpacing();

        writeTag(tokenizer.token(), "keyword");

        // match type
        tokenizer.advance();
        if (!checkAndWriteType()) {
            System.out.println("illegal type for class var dec");
            return;
        }

        // match varName
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
        } else {
            System.out.println("illegal classVar identifier");
            return;
        }

        // match potential ", varName" part
        tokenizer.advance();
        while (tokenizer.symbol().equals(",")) {
            writeTag(",", "symbol");
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
                writeTag(tokenizer.identifier(), "identifier");
            } else {
                System.out.println("illegal classVar identifier");
                return;
            }
            tokenizer.advance();
        }

        // match ;
        if (tokenizer.symbol().equals(";")) {
            writeTag(";", "symbol");
        } else {
            System.out.println("no ending ;");
            return;
        }

        decreaseSpacing();
        output.println(spacing + "</classVarDec>");
    }

    private boolean checkAndWriteType() {
        if (tokenizer.keyword() == JackTokens.INT ||
            tokenizer.keyword() == JackTokens.CHAR ||
            tokenizer.keyword() == JackTokens.BOOLEAN) {
            writeTag(tokenizer.token(), "keyword");
            return true;
        } else if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
            return true;
        } else {
            return false;
        }
    }

    public void compileSubRoutine() {
        // write subroutineDec tag
        output.println(spacing + "<subroutineDec>");

        // New level
        increaseSpacing();

        // already know that the current token start with constructor, function or method
        writeTag(tokenizer.token(), "keyword");

        // match return type
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokens.KEYWORD &&
            tokenizer.token().equals("void")) {
            writeTag("void", "keyword");
        } else if (!checkAndWriteType()) {
            System.out.println("Illegal type name for subroutine");
            return;
        }

        // match subroutine identifier
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
        } else {
            System.out.println("illegal subroutine name");
            return;
        }

        // match parameter list
        tokenizer.advance();
        if (tokenizer.symbol().equals("(")) {
            writeTag("(", "symbol");
            compileParameterList();
        } else {
            System.out.println("no () after function name");
            return;
        }

        // match the closing ) for the paramater list
        if (tokenizer.symbol().equals(")")) {
            writeTag(")", "symbol");
        } else {
            System.out.println("no () after function name");
            return;
        }

        // match subroutine body
        tokenizer.advance();
        if (tokenizer.symbol().equals("{")) {
            compileSubroutineBody();
        } else {
            System.out.println("no { after function parameters");
            return;
        }

        // the closing } is matched in compileSubroutineBody()

        // decrease spacing
        decreaseSpacing();

        // write close subrountine tag
        output.println(spacing + "</subroutineDec>");
    }

    public void compileParameterList() {
        output.println(spacing + "<parameterList>");
        increaseSpacing();

        // write type
        tokenizer.advance();
        if (checkAndWriteType()) {
            // match varName
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
                writeTag(tokenizer.identifier(), "identifier");
            } else {
                System.out.println("illegal identifier in parameter list");
                return;
            }

            // match other arguments
            tokenizer.advance();
            while (tokenizer.symbol().equals(",")) {
                writeTag(",", "symbol");
                tokenizer.advance();
                if (!checkAndWriteType()) {
                    System.out.println("illegal type name");
                    return;
                }
                tokenizer.advance();
                if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
                    writeTag(tokenizer.identifier(), "identifier");
                } else {
                    System.out.println("illegal identifier name");
                    return;
                }
                tokenizer.advance();
            }
        }

        decreaseSpacing();
        output.println(spacing + "</parameterList>");
    }

    public void compileSubroutineBody() {
        output.println(spacing + "<subroutineBody>");
        increaseSpacing();

        writeTag("{", "symbol");

        tokenizer.advance();
        while ( tokenizer.tokenType() == JackTokens.KEYWORD &&
                tokenizer.token().equals("var")) {
            compileVarDec();
            tokenizer.advance();
        }

        compileStatements();

        // match }
        if (!checkSymbol("}")) {
            System.out.println("no } found to close subroutine call");
            System.out.printf("current token is : %s\n", tokenizer.token());
        }

        decreaseSpacing();
        output.println(spacing + "</subroutineBody>");
    }

    public void compileVarDec() {
        output.println(spacing + "<varDec>");
        increaseSpacing();

        // write var
        writeTag("var", "keyword");

        // check type
        tokenizer.advance();
        if (!checkAndWriteType()) {
            System.out.println("illegal type for var");
            return;
        }

        // check varName
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
        } else {
            System.out.println("illegal identifier for var");
            return;
        }

        tokenizer.advance();
        while (tokenizer.symbol().equals(",")) {
            writeTag(",", "symbol");

            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
                writeTag(tokenizer.identifier(), "identifier");
            } else {
                System.out.println("illegal identifier for var");
                return;
            }

            tokenizer.advance();
        }

        if (tokenizer.symbol().equals(";")) {
            writeTag(";", "symbol");
        } else {
            System.out.println("varDec doesn't end with ;");
            return;
        }

        decreaseSpacing();
        output.println(spacing + "</varDec>");
    }

    public void compileStatements() {
        output.println(spacing + "<statements>");
        increaseSpacing();
        
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

        decreaseSpacing();
        output.println(spacing + "</statements>");
    }

    private boolean checkIdentifier() {
        if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
            return true;
        } else {
            return false;
        }
    }

    private boolean checkSymbol(String s) {
        if (s.equals("<")) { s = "&lt;"; }
        else if (s.equals(">")) { s = "&gt;"; }
        else if (s.equals("&")) { s = "&amp;"; }

        if (tokenizer.symbol().equals(s)) {
            writeTag(s, "symbol");
            return true;
        } else {
            return false;
        }
    }

    private boolean checkKeyword(String k) {
        if (tokenizer.tokenType() == JackTokens.KEYWORD &&
            tokenizer.token().equals(k)) {
            writeTag(k, "keyword");
            return true;
        } else {
            return false;
        }
    }

    public void compileLet() {
        output.println(spacing + "<letStatement>");
        increaseSpacing();

        writeTag("let", "keyword");

        tokenizer.advance();
        if (!checkIdentifier()) {
            System.out.println("Illegal identifier");
            return;
        }

        tokenizer.advance();
        if (checkSymbol("[")) {
            tokenizer.advance();
            compileExpression();

            if(!checkSymbol("]")) {
                System.out.printf("No closing ], current: %s\n", tokenizer.token());
                return;
            }
            // if has [], advance and next should be =
            tokenizer.advance();
        }

        if (!checkSymbol("=")) {
            System.out.println("No = found");
            return;
        }

        tokenizer.advance();
        compileExpression();

        // No need to advance because compileExpression does one token look ahead
        if (!checkSymbol(";")) {
            System.out.println("No ; found at the end of statement");
            return;
        }

        decreaseSpacing();
        output.println(spacing + "</letStatement>");
    }

    public void compileIf() {
        output.println(spacing + "<ifStatement>");
        increaseSpacing();

        writeTag("if", "keyword");

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

        tokenizer.advance();
        if (!checkSymbol("{")) {
            System.out.println("No { for if statement");
            return;
        }

        tokenizer.advance();
        compileStatements();

        if (!checkSymbol("}")) {
            System.out.println("No } for if statement");
            System.out.printf("the current symbol is %s\n", tokenizer.token());
            return;
        }

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

        decreaseSpacing();
        output.println(spacing + "</ifStatement>");
    }

    public void compileWhile() {
        output.println(spacing + "<whileStatement>");
        increaseSpacing();

        writeTag("while", "keyword");

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

        tokenizer.advance();
        if (!checkSymbol("{")) {
            System.out.println("No { in while statement");
            return;
        }

        tokenizer.advance();
        compileStatements();

        //tokenizer.advance();
        if (!checkSymbol("}")) {
            System.out.println("No } in while statement");
            return;
        }

        decreaseSpacing();
        output.println(spacing + "</whileStatement>");
    }

    public void compileDo() {
        output.println(spacing + "<doStatement>");
        increaseSpacing();

        writeTag("do", "keyword");

        tokenizer.advance();
        // Before call compileSubRoutineCall, first check if the current
        // token is valid identifier. Then advance again and check if the it is . or (
        if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");

            tokenizer.advance();
            if (checkSymbol(".") || checkSymbol("(")) {
                compileSubRoutineCall();
            } else {
                System.out.println("Not valid subroutine call");
                return;
            }
        } else {
            System.out.println("Not a valid identifier for do statement");
            return;
        }

        tokenizer.advance();
        if (!checkSymbol(";")) {
            System.out.println("No closing ;");
            return;
        }

        decreaseSpacing();
        output.println(spacing + "</doStatement>");
    }

    public void compileReturn() {
        output.println(spacing + "<returnStatement>");
        increaseSpacing();

        writeTag("return", "keyword");

        tokenizer.advance();
        // if the following is not ; then try to parse argument
        if (!checkSymbol(";")) {
            compileExpression();

            // after the expresison, it should end with ;
            if (!checkSymbol(";")) {
                System.out.println("return statement not ending with ;");
            }
        }

        decreaseSpacing();
        output.println(spacing + "</returnStatement>");
    }

    public void compileExpression() {
        output.println(spacing + "<expression>");
        increaseSpacing();

        compileTerm();

        // compileTerm needs to do one token look ahead, so no advance here.
        while (checkSymbol("+") || checkSymbol("-") || checkSymbol("*") || checkSymbol("/") ||
               checkSymbol("&") || checkSymbol("|") || checkSymbol("<") || checkSymbol(">") ||
               checkSymbol("=")) {
            tokenizer.advance();
            compileTerm();
            // no advance here, because compileTerm needs to do one token look ahead
        }

        decreaseSpacing();
        output.println(spacing + "</expression>");
    }

    public void compileTerm() {
        output.println(spacing + "<term>");
        increaseSpacing();

        if (tokenizer.tokenType() == JackTokens.INT_CONST) {
            writeTag(Integer.toString(tokenizer.intVal()), "integerConstant");
            tokenizer.advance();
        } else if (tokenizer.tokenType() == JackTokens. STRING_CONST) {
           writeTag(tokenizer.stringVal(), "stringConstant");
           tokenizer.advance();
        } else if (checkKeyword("true") || checkKeyword("false") || checkKeyword("null") ||
           checkKeyword("this")) {
            tokenizer.advance();
        } else if (checkSymbol("-") || checkSymbol("~")) {
            tokenizer.advance();
            compileTerm();
        } else if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
            writeTag(tokenizer.identifier(), "identifier");
            tokenizer.advance();
            if (checkSymbol("[")) {
                compileArrayTerm();
                tokenizer.advance();
            } else if (checkSymbol("(") || checkSymbol(".")) {
                compileSubRoutineCall();
                tokenizer.advance();
            }
            // if doesn't match [, (, or ., it is a normal identifier
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

        decreaseSpacing();
        output.println(spacing + "</term>");
    }

    public void compileArrayTerm() {
        tokenizer.advance();
        compileExpression();

        if (!checkSymbol("]")) {
            System.out.println("No closing ] for the array expression");
        }
    }

    public void compileSubRoutineCall() {
        if (tokenizer.symbol().equals("(")) {
            tokenizer.advance();
            compileExpressionList();

            if (!checkSymbol(")")) {
                System.out.println("No closing ) for the expressionlist");
                return;
            }
        } else {
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokens.IDENTIFIER) {
                writeTag(tokenizer.identifier(), "identifier");
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
            compileExpressionList();

            if (!checkSymbol(")")) {
                System.out.println("No closing ) for the expressionlist");
                return;
            }
        }
    }

    public void compileExpressionList() {
        output.println(spacing + "<expressionList>");
        increaseSpacing();

        if (!tokenizer.symbol().equals(")")) {
            compileExpression();

            // because compileExpression did 1 token look ahead, no advance here
            while (checkSymbol(",")) {
                tokenizer.advance();
                compileExpression();
            }
        }

        decreaseSpacing();
        output.println(spacing + "</expressionList>");
    }

    private void writeTag(String word, String type) {
        output.println(spacing + "<" + type + "> " + word + " </" + type + ">");
//        System.out.println("<" + type + "> " + word + " </" + type + ">");
    }

    private void increaseSpacing() {
        spacing += "\t";
    }

    private void decreaseSpacing() {
        spacing = spacing.substring(1);
    }

    public static void main(String[] args) {
        String filename = args[0];
        CompilationEngine engine = new CompilationEngine(filename);
        engine.compileClass();
    }
}
