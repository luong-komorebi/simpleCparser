

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

class StructDeclList extends DeclList {
    public StructDeclList(LinkedList<?> S) {
        super(S);
    }

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
    public FuncBody(DeclList declList, StmtList stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }
    // added
    public void print(PrintWriter pw, int indentLevel) {
        printNonTerm(pw, indentLevel);
        printTerm(pw, indentLevel + 1, LCURLY);

        myDeclList.print(pw, indentLevel + 2);
        myStmtList.print(pw, indentLevel + 2);

        printTerm(pw, indentLevel + 1, RCURLY);
    }

    private DeclList myDeclList;
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
    // added
    //public abstract void print(PrintWriter pw, int indentLevel);
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

    protected Expr myExp1;
    protected Expr myExp2;
}

class PlusExpr extends BinaryExpr {
    public PlusExpr(Expr exp1, Expr exp2) {
        super(exp1, exp2);
    }

    public void print(PrintWriter pw, int indentLevel) {

    }
}

class MinusExpr extends BinaryExpr {
    public MinusExpr(Expr exp1, Expr exp2) {
        super(exp1, exp2);
    }

    public void print(PrintWriter pw, int indentLevel) {

    }
}

// **********************************************************************
// Stmt
// **********************************************************************

abstract class Stmt extends AST {
	Stmt() {} //empty constructor so that some method doesnt invoke error missing method
}

class AssignStmt extends Stmt {
    public AssignStmt(Expr lhs, Expr exp) {
        myLhs = lhs;
        myExp = exp;
    }

    public void print(PrintWriter pw, int indentLevel) {
		printNonTerm(pw, indentLevel);
		myLhs.print(pw, indent + 1);
	    printTerm(p, indent, ASSIGN);
	    myExp.print(p, indent);
	    printTerm(p, indent, SEMICOLON);
    }

    private Expr myLhs;
    private Expr myExp;
}

class IfStmt extends Stmt {
  
    public IfStmt(Expr exp, VarDeclList dlist, StmtList slist)
    {
      myVarDeclList = dlist;
      myExp = exp;
      myStmtList = slist;
    }
  
    public void print(PrintWriter p, int indent) {
      printNonTerm(p, indent);
      printTerm(p, indent + 1, IF);
      printTerm(p, indent + 1, LPAREN);
      myExp.print(p, indent + 1);
      printTerm(p, indent + 1, RPAREN);
      printTerm(p, indent + 1, LCURLY);
      myVarDeclList.print(p, indent + 1);
      myStmtList.print(p, indent + 1);
      printTerm(p, indent + 1, RCURLY);
    }
	
    private Expr myExp;
    private DeclList myVarDeclList;
    private StmtList myStmtList;
}

class IfElseStmt extends Stmt { 
	
  public IfElseStmt(Expr exp, VarDeclList l1, StmtList stm1, VarDeclList l2, StmtList stm2) {
    myExp = exp;
    myVarDeclList1 = l1;
	myVarDeclList2 = l2;
    myStmtList1 = stm1;
    myStmtList2 = stm2;
  }
  
  public void print(PrintWriter p, int indent) {
    printNonTerm(p, indent)
    printTerm(p, indent + 1, IF);
    printTerm(p, indent + 1, LPAREN);
    myExp.print(p, indent + 1);
    printTerm(p, indent + 1, RPAREN);
    printTerm(p, indent + 1, LCURLY);
    myVarDeclList1.print(p, indent + 1);
    myStmtList1.print(p, indent + 1);
    printTerm(p, indent + 1, RCURLY);
    printTerm(p, indent + 1, ELSE);
	printTerm(p, indent + 1, LCURLY);
    myVarDeclList2.print(p, indent + 1);
    myStmtList2.print(p, indent + 1);
    printTerm(p, indent + 1, RCURLY);
  }
  
  private Expr myExp;
  private VarDeclList myVarDeclList1;
  private VarDeclList myVarDeclList2;
  private StmtList myStmtList1;
  private StmtList myStmtList2;
}

class ForStmt extends Stmt { 
	
  public ForStmt(Stmt init, Expr cond, Stmt incr, VarDeclList declist, StmtList stmlist) {
    myInit = init;
    myCond = cond;
    myIncr = incr;
    myVarDeclList = declist;
    myStmtList = stmlist;
  }
  
  public void print(PrintWriter p, int indent) {
    printNonTerm(p, indent);
    printTerm(p, indent+1, FOR);
    printTerm(p, indent+1, LPAREN);
    myInit.print(p, indent+1);
    printTerm(p, indent+1, SEMICOLON);
    myCond.print(p, indent+1);
    printTerm(p, indent+1, SEMICOLON);
    myIncr.print(p, indent+1);
    printTerm(p, indent+1, RPAREN);
    printTerm(p, indent+1, LCURLY);
    myVarDeclList.print(p, indent+1);
    myStmtList.print(p, indent+1);
    printTerm(p, indent+1, RCURLY);
  }
  
  private Stmt myInit;
  private Expr myCond;
  private Stmt myIncr;
  private VarDeclList myVarDeclList;
  private StmtList myStmtList;
}

class WhileStmt extends Stmt {
	
  public WhileStmt(Expr exp, VarDeclList list, StmtList slist) {
    myExp = exp;
    myVarDeclList = list;
    myStmtList = slist;
  }
  
  public void print(PrintWriter p, int indent) {
    printNonTerm(p, indent);
    printTerm(p, indent+1, WHILE);
    printTerm(p, indent+1, LPAREN);
    myExp.print(p, indent+1);
    printTerm(p, indent+1, RPAREN);
    printTerm(p, indent+1, LCURLY);
    myVarDeclList.print(p, indent+1);
    myStmtList.print(p, indent+1);
    printTerm(p, indent+1, RCURLY);
  }
  
  private Expr myExp;
  private VarDeclList myVarDeclList;
  private StmtList myStmtList;
}

class ReturnStmt extends Stmt { 
  public ReturnStmt(Expr exp) {
    myExp = exp;
  }
  
  public void print(PrintWriter p, int indent) {
    printNonTerm(p, indent);
    if (myExp == null) {
      printTerm(p, indent+1, RETURN);
    } else {
      printTerm(p, indent+1, RETURN);
      myExp.print(p, indent+1);
      printTerm(p, indent+1, SEMICOLON);
    }
  }
  
  private Expr myExp;
}



