

import java.util.*;
import java.io.PrintWriter;
import java.lang.reflect.Field;


abstract class AST implements sym {
  public static String indent = "  ";

  public abstract void print(PrintWriter pw, int indentLevel);

  protected void doIndent(PrintWriter pw, int indentLevel) {
    for (int i = 0; i < indentLevel; ++i)
      pw.print(indent);
  }

  protected void printNonTerm(PrintWriter pw, int indentLevel) {
    doIndent(pw, indentLevel);
    pw.println(getClass().getSimpleName());
  }

  protected void printTerm(PrintWriter pw, int indentLevel, int token) {
    doIndent(pw, indentLevel);
    pw.println(getTokenName(token));
  }

  protected void printTerm(PrintWriter pw, int indentLevel, int token, String val) {
    doIndent(pw, indentLevel);
    pw.println(getTokenName(token) + " (" + val + ")");
  }

  protected String getTokenName(int token) {
    try {
      if (terms == null)  // get class fields from sym once (at first)
        terms = sym.class.getFields();
      for (int i = 0; i < terms.length; ++i) {
        if (terms[i].getInt(null) == token) {
          return terms[i].getName();
        }
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    }

    return "UNKNOWN TOKEN";
  }

  protected static Field[] terms = null;
}

class Program extends AST {
    public Program(DeclList L) {
        myDeclList = L;
    }

    public void print(PrintWriter pw, int indentLevel) {
      printNonTerm(pw, indentLevel);
      myDeclList.print(pw, indentLevel + 1);
    }

    private DeclList myDeclList;
}

class DeclList extends AST {
    public DeclList(LinkedList<?> S) {
        myDecls = S;
    }

    public void print(PrintWriter pw, int indentLevel) {
        Iterator<?> it = myDecls.iterator();
        try {
            printNonTerm(pw, indentLevel);
            while (it.hasNext()) {
                ((Decl)it.next()).print(pw, indentLevel + 1);
            }
        } catch (Exception e) {
          e.printStackTrace(System.err);
        }
    }

    protected LinkedList<?> myDecls;
}

class VarDeclList extends AST {
  public VarDeclList(LinkedList<?> D) {
      myVarDecls = D;
  }

  public void print(PrintWriter pw, int indentLevel) {
      Iterator<?> it = myVarDecls.iterator();
      try {
          printNonTerm(pw, indentLevel);
          while (it.hasNext()) {
              ((VarDecl)it.next()).print(pw, indentLevel + 1);
          }
      } catch (Exception e) {
        e.printStackTrace(System.err);
      }
  }

  protected LinkedList<?> myVarDecls;
}

class FormalsList extends AST {
    public FormalsList(LinkedList<?> S) {
        myFormals = S;
    }

    public void print(PrintWriter pw, int indentLevel) {
        Iterator<?> it = myFormals.iterator();
        try {
            printNonTerm(pw, indentLevel);
            while (it.hasNext()) {
                ((FormalDecl)it.next()).print(pw, indentLevel + 1);

                if (it.hasNext())
                  printTerm(pw, indentLevel + 1, COMMA);
            }
        } catch (Exception e) {
          e.printStackTrace(System.err);
        }
    }

    private LinkedList<?> myFormals;
}

class FuncBody extends AST {
    public FuncBody(VarDeclList varDeclList, StmtList stmtList) {
        myVarDeclList = varDeclList;
        myStmtList = stmtList;
    }
    // added
    public void print(PrintWriter pw, int indentLevel) {
        printNonTerm(pw, indentLevel);
        printTerm(pw, indentLevel + 1, LCURLY);

        myVarDeclList.print(pw, indentLevel + 2);
        myStmtList.print(pw, indentLevel + 2);

        printTerm(pw, indentLevel + 1, RCURLY);
    }

    private VarDeclList myVarDeclList;
    private StmtList myStmtList;
}

class StmtList extends AST {
    public StmtList(LinkedList<?> S) {
        myStmts = S;
    }

    // added
    public void print(PrintWriter pw, int indentLevel) {
        Iterator<?> it = myStmts.iterator();
        try {
            printNonTerm(pw, indentLevel);
            while (it.hasNext()) {
                ((Stmt)it.next()).print(pw, indentLevel + 1);
            }
        } catch (Exception e) {
          e.printStackTrace(System.err);
        }
    }

    private LinkedList<?> myStmts;
}

abstract class Decl extends AST {

}

class VarDecl extends Decl {
    public VarDecl(Type type, ID id) {
        myType = type;
        myId = id;
        myIntLiteral = null;
    }

    public VarDecl(Type type, ID id, IntLiteral intLiteral) {
      myType = type;
      myId = id;
      myIntLiteral = intLiteral;
    }

    public void print(PrintWriter pw, int indentLevel) {
      printNonTerm(pw, indentLevel);
      myType.print(pw, indentLevel + 1);
      myId.print(pw, indentLevel + 1);

      if (myIntLiteral != null) {
          printTerm(pw, indentLevel + 1, LSQBRACKET);
          myIntLiteral.print(pw, indentLevel + 1);
          printTerm(pw, indentLevel + 1, RSQBRACKET);
      }

      printTerm(pw, indentLevel + 1, SEMICOLON);
    }

    private Type      myType;
    private ID        myId;
    private IntLiteral myIntLiteral;
}

class FuncDef extends Decl {
    public FuncDef(Type type, int numPtr, ID id, FormalsList formalList,
            FuncBody body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    public void print(PrintWriter pw, int indentLevel) {
        printNonTerm(pw, indentLevel);

        myType.print(pw, indentLevel + 1);

        myId.print(pw, indentLevel + 1);
        printTerm(pw, indentLevel + 1, LPAREN);
        myFormalsList.print(pw, indentLevel + 1);
        printTerm(pw, indentLevel + 1, RPAREN);
        myBody.print(pw, indentLevel + 1);
    }

    private Type        myType;
    private ID          myId;
    private FormalsList myFormalsList;
    private FuncBody    myBody;
}

class FuncDecl extends Decl {
    public FuncDecl(Type type, ID id, FormalsList formalsList) {
        myType = type;
        myId = id;
        myFormalsList = formalsList;
    }

    public void print(PrintWriter pw, int indentLevel) {
        printNonTerm(pw, indentLevel);

        myType.print(pw, indentLevel);
        myId.print(pw, indentLevel);
        printTerm(pw, indentLevel, LPAREN);
        myFormalsList.print(pw, indentLevel + 1);
        printTerm(pw, indentLevel, RPAREN);
        printTerm(pw, indentLevel, SEMICOLON);
    }

    private Type myType;
    private ID myId;
    private FormalsList myFormalsList;
}

class FormalDecl extends Decl {
    public FormalDecl(Type type, ID id) {
        myType = type;
        myId = id;
    }

    public void print(PrintWriter pw, int indentLevel) {
        printNonTerm(pw, indentLevel);

        myType.print(pw, indentLevel + 1);
        myId.print(pw, indentLevel + 1);
    }

    private Type myType;
    private ID   myId;
}

// added
class StructDecl extends Decl {
    public StructDecl(ID id, VarDeclList varDeclList) {
        myId = id;
        myvarDeclList = varDeclList;
    }

    public void print(PrintWriter pw, int indentLevel) {
        printNonTerm(pw, indentLevel);
        printTerm(pw, indentLevel, STRUCT);
        myId.print(pw, indentLevel);
        printTerm(pw, indentLevel, LCURLY);
        myvarDeclList.print(pw, indentLevel + 1);
        printTerm(pw, indentLevel, RCURLY);
        printTerm(pw, indentLevel, SEMICOLON);
    }

    private ID myId;
    private VarDeclList myvarDeclList;
}

// **********************************************************************
// Type
// **********************************************************************
abstract class Type extends AST {

    abstract public String name();

    public void print(PrintWriter pw, int indentLevel) {
      doIndent(pw, indentLevel);
      pw.println("Type (" + name() + ")");
    }
}

class IntType extends Type {
    public IntType() {
    }

    public String name() {
        return "INT";
    }
}

// added
class BoolType extends Type {
    public BoolType() {
    }

    public String name() {
        return "BOOL";
    }
}

// added
class DoubleType extends Type {
    public DoubleType() {
    }

    public String name() {
        return "DOUBLE";
    }
}

class VoidType extends Type {
    public VoidType() {
    }

    public String name() {
        return "VOID";
    }
}

// **********************************************************************
// Expr
// **********************************************************************

abstract class Expr extends AST {

}

class IntLiteral extends Expr {
    public IntLiteral(int intVal) {
        myIntVal = intVal;
    }

    public void print(PrintWriter pw, int indent) {
      printTerm(pw, indent, INTLITERAL, Integer.toString(myIntVal));
    }

    private int myIntVal;
}

class ID extends Expr {
    public ID(String strVal) {
        myStrVal = strVal;
    }

    public String getName() {
        return myStrVal;
    }

    public void print(PrintWriter pw, int indentLevel) {
      printTerm(pw, indentLevel, ID, getName());
    }

    private String myStrVal;
}

abstract class BinaryExpr extends Expr {
    public BinaryExpr(Expr exp1, Expr exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    public void print(PrintWriter pw, int indentLevel, int token) {
        printNonTerm(pw, indentLevel);
        myExp1.print(pw, indentLevel + 1);
        printTerm(pw, indentLevel + 1, token);
        myExp2.print(pw, indentLevel + 1);
    }

    protected Expr myExp1;
    protected Expr myExp2;
}

class PlusExpr extends BinaryExpr {
    public PlusExpr(Expr exp1, Expr exp2) {
        super(exp1, exp2);
    }

    public void print(PrintWriter pw, int indentLevel) {
        print(pw, indentLevel, PLUS);
    }
}

class MinusExpr extends BinaryExpr {
    public MinusExpr(Expr exp1, Expr exp2) {
        super(exp1, exp2);
    }

    public void print(PrintWriter pw, int indentLevel) {
        print(pw, indentLevel, MINUS);
    }
}

class TimesExpr extends BinaryExpr {
    public TimesExpr(Expr exp1, Expr exp2) {
        super(exp1, exp2);
    }

    public void print(PrintWriter pw, int indentLevel) {
        print(pw, indentLevel, TIMES);
    }
}

class DivideExpr extends BinaryExpr {
    public DivideExpr(Expr exp1, Expr exp2) {
        super(exp1, exp2);
    }

    public void print(PrintWriter pw, int indentLevel) {
        print(pw, indentLevel, DIVIDE);
    }
}

class PercentExpr extends BinaryExpr {
    public PercentExpr(Expr exp1, Expr exp2) {
        super(exp1, exp2);
    }

    public void print(PrintWriter pw, int indentLevel) {
        print(pw, indentLevel, PERCENT);
    }
}

class AndExpr extends BinaryExpr {
    public AndExpr(Expr exp1, Expr exp2) {
        super(exp1, exp2);
    }

    public void print(PrintWriter pw, int indentLevel) {
        print(pw, indentLevel, AMPERSAND);
    }
}

class OrExpr extends BinaryExpr {
    public OrExpr(Expr exp1, Expr exp2) {
        super(exp1, exp2);
    }

    public void print(PrintWriter pw, int indentLevel) {
        print(pw, indentLevel, VERTICALBAR);
    }
}

class EqualsExpr extends BinaryExpr {
    public EqualsExpr(Expr exp1, Expr exp2) {
        super(exp1, exp2);
    }

    public void print(PrintWriter pw, int indentLevel) {
        print(pw, indentLevel, EQUALS);
    }
}

class NotEqualsExpr extends BinaryExpr {
    public NotEqualsExpr(Expr exp1, Expr exp2) {
        super(exp1, exp2);
    }

    public void print(PrintWriter pw, int indentLevel) {
        print(pw, indentLevel, NE_OP);
    }
}

class LessExpr extends BinaryExpr {
    public LessExpr(Expr exp1, Expr exp2) {
        super(exp1, exp2);
    }

    public void print(PrintWriter pw, int indentLevel) {
        print(pw, indentLevel, LESS);
    }
}

class GreaterExpr extends BinaryExpr {
    public GreaterExpr(Expr exp1, Expr exp2) {
        super(exp1, exp2);
    }

    public void print(PrintWriter pw, int indentLevel) {
        print(pw, indentLevel, GREATER);
    }
}

class LessEqExpr extends BinaryExpr {
    public LessEqExpr(Expr exp1, Expr exp2) {
        super(exp1, exp2);
    }

    public void print(PrintWriter pw, int indentLevel) {
        print(pw, indentLevel, LE_OP);
    }
}

class GreaterEqExpr extends BinaryExpr {
    public GreaterEqExpr(Expr exp1, Expr exp2) {
        super(exp1, exp2);
    }

    public void print(PrintWriter pw, int indentLevel) {
        print(pw, indentLevel, GE_OP);
    }
}


// **********************************************************************
// Stmt
// **********************************************************************

abstract class Stmt extends AST {

}

class AssignStmt extends Stmt {
    public AssignStmt(Expr lhs, Expr exp) {
        myLhs = lhs;
        myExp = exp;
    }

    public void print(PrintWriter pw, int indentLevel) {

    }

    private Expr myLhs;
    private Expr myExp;
}
