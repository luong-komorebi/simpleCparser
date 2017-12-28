

import java.util.*;
import java.io.PrintWriter;
import java.lang.reflect.Field;

abstract class AST implements sym {
  public static String indent = "    ";

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
    public DeclList(LinkedList<?> D) {
        myDecls = D;
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
    public FormalsList(LinkedList<?> F) {
        myFormals = F;
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

    public void print(PrintWriter pw, int indentLevel) {
        printNonTerm(pw, indentLevel);
        printTerm(pw, indentLevel + 1, LCURLY);

        myVarDeclList.print(pw, indentLevel + 1);
        myStmtList.print(pw, indentLevel + 1);

        printTerm(pw, indentLevel + 1, RCURLY);
    }

    private VarDeclList myVarDeclList;
    private StmtList myStmtList;
}

class StmtList extends AST {
    public StmtList(LinkedList<?> S) {
        myStmts = S;
    }

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

class ActualList extends AST {
  public ActualList(LinkedList<?> E) {
    myExprs = E;
  }

  public void print(PrintWriter pw, int indentLevel) {
      Iterator<?> it = myExprs.iterator();
      try {
          printNonTerm(pw, indentLevel);
          while (it.hasNext()) {
              ((Expr)it.next()).print(pw, indentLevel + 1);

              if (it.hasNext())
                printTerm(pw, indentLevel + 1, COMMA);
          }
      } catch (Exception e) {
        e.printStackTrace(System.err);
      }
  }

  private LinkedList<?> myExprs;
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

        myType.print(pw, indentLevel + 1);
        myId.print(pw, indentLevel + 1);
        printTerm(pw, indentLevel + 1, LPAREN);
        myFormalsList.print(pw, indentLevel + 1);
        printTerm(pw, indentLevel + 1, RPAREN);
        printTerm(pw, indentLevel + 1, SEMICOLON);
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

class StructDecl extends Decl {
    public StructDecl(ID id, VarDeclList varDeclList) {
        myId = id;
        myVarDeclList = varDeclList;
    }

    public void print(PrintWriter pw, int indentLevel) {
        printNonTerm(pw, indentLevel);

        printTerm(pw, indentLevel + 1, STRUCT);
        myId.print(pw, indentLevel + 1);
        printTerm(pw, indentLevel + 1, LCURLY);
        myVarDeclList.print(pw, indentLevel + 1);
        printTerm(pw, indentLevel + 1, RCURLY);
        printTerm(pw, indentLevel + 1, SEMICOLON);
    }

    private ID myId;
    private VarDeclList myVarDeclList;
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

class BoolType extends Type {
    public BoolType() {
    }

    public String name() {
        return "BOOL";
    }
}

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

class CharType extends Type {
    public CharType() {
    }

    public String name() {
        return "CHAR";
    }
}

class StructType extends Type {
  public StructType(ID id) {
    myStructName = id;
  }

  public String name() {
    return "STRUCT " + myStructName.getName();
  }

  private ID myStructName;
}

// **********************************************************************
// Expr
// **********************************************************************

abstract class Expr extends AST {

}

abstract class Term extends Expr {

}

class IntLiteral extends Term {
    public IntLiteral(String intVal) {
        myIntVal = intVal;

        try {
          convertedInt = new Integer(myIntVal);
        } catch (Exception e) {
          convertedInt = null;
        }
    }

    public void print(PrintWriter pw, int indentLevel) {
      printTerm(pw, indentLevel, INTLITERAL, myIntVal);
    }

    private String myIntVal;
    private Integer convertedInt;
}

class RealLiteral extends Term {
    public RealLiteral(String realVal) {
        myRealVal = realVal;

        try {
          convertedReal = new Double(myRealVal);
        } catch (Exception e) {
          convertedReal = null;
        }
    }

    public void print(PrintWriter pw, int indentLevel) {
      printTerm(pw, indentLevel, REALLITERAL, myRealVal);
    }

    private String myRealVal;
    private Double convertedReal;
}

class StringLiteral extends Term {
    public StringLiteral(String stringVal) {
        myStringVal = stringVal;
    }

    public void print(PrintWriter pw, int indentLevel) {
      printTerm(pw, indentLevel, STRINGLITERAL, myStringVal);
    }

    private String myStringVal;
}

class CharLiteral extends Term {
    public CharLiteral(String charVal) {
        myCharVal = charVal;
    }

    public void print(PrintWriter pw, int indentLevel) {
      printTerm(pw, indentLevel, CHARLITERAL, myCharVal);
    }

    private String myCharVal;
}

class True extends Term {
  public True() {
  }

  public void print(PrintWriter pw, int indentLevel) {
    printTerm(pw, indentLevel, TRUE);
  }
}

class False extends Term {
  public False() {
  }

  public void print(PrintWriter pw, int indentLevel) {
    printTerm(pw, indentLevel, FALSE);
  }
}

class Null extends Term {
  public Null() {
  }

  public void print(PrintWriter pw, int indentLevel) {
    printTerm(pw, indentLevel, NULL);
  }
}

class Sizeof extends Term {
  public Sizeof(ID id) {
    myId = id;
  }

  public void print(PrintWriter pw, int indentLevel) {
    printTerm(pw, indentLevel, SIZEOF);
    printTerm(pw, indentLevel + 1, LPAREN);
    myId.print(pw, indentLevel + 1);
    printTerm(pw, indentLevel + 1, RPAREN);
  }

  private ID myId;
}

class BoundedExpr extends Term {
  public BoundedExpr(Expr e) {
    myExpr = e;
  }

  public void print(PrintWriter pw, int indentLevel) {
    printTerm(pw, indentLevel, LPAREN);
    myExpr.print(pw, indentLevel);
    printTerm(pw, indentLevel, RPAREN);
  }

  private Expr myExpr;
}

class CallExpr extends Term {
  public CallExpr(ID id, ActualList A) {
    myFuncName = id;
    myActualList = A;
  }

  public void print(PrintWriter pw, int indentLevel) {
    printNonTerm(pw, indentLevel);
    myFuncName.print(pw, indentLevel + 1);
    printTerm(pw, indentLevel + 1, LPAREN);
    myActualList.print(pw, indentLevel + 1);
    printTerm(pw, indentLevel + 1, RPAREN);
  }

  private ID myFuncName;
  private ActualList myActualList;
}

abstract class Loc extends Term {

}

class ID extends Loc {
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

class ArrayExpr extends Loc {
  public ArrayExpr(Loc loc, Expr expr) {
    myLoc = loc;
    myExpr = expr;
  }

  public void print(PrintWriter pw, int indentLevel) {
    printNonTerm(pw, indentLevel);
    myLoc.print(pw, indentLevel + 1);
    printTerm(pw, indentLevel + 1, LSQBRACKET);
    myExpr.print(pw, indentLevel + 1);
    printTerm(pw, indentLevel + 1, RSQBRACKET);
  }

  private Loc myLoc;
  private Expr myExpr;
}

class AccessLoc extends Loc {
  public AccessLoc(Loc loc, ID id) {
    myLoc = loc;
    myId = id;
  }

  public void print(PrintWriter pw, int indentLevel) {
    myLoc.print(pw, indentLevel);
    printTerm(pw, indentLevel, FULLSTOP);
    myId.print(pw, indentLevel);
  }

  private Loc myLoc;
  private ID myId;
}

abstract class UnaryExpr extends Expr {
  public UnaryExpr(Expr e) {
    myExp = e;
  }

  protected Expr myExp;
}

class MinusUnaryExpr extends UnaryExpr {
  public MinusUnaryExpr(Expr e) {
    super(e);
  }

  public void print(PrintWriter pw, int indentLevel) {
    printNonTerm(pw, indentLevel);
    printTerm(pw, indentLevel + 1, MINUS);
    myExp.print(pw, indentLevel + 1);
  }
}

class AddrOfExpr extends UnaryExpr {
  public AddrOfExpr(Expr e) {
    super(e);
  }

  public void print(PrintWriter pw, int indentLevel) {
    printNonTerm(pw, indentLevel);
    printTerm(pw, indentLevel + 1, AMPERSAND);
    myExp.print(pw, indentLevel + 1);
  }
}

class NotExpr extends UnaryExpr {
  public NotExpr(Expr e) {
    super(e);
  }

  public void print(PrintWriter pw, int indentLevel) {
    printNonTerm(pw, indentLevel);
    printTerm(pw, indentLevel + 1, BANG);
    myExp.print(pw, indentLevel + 1);
  }
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
        hasSemi = true;
    }

    public void print(PrintWriter pw, int indentLevel) {
        printNonTerm(pw, indentLevel);
        myLhs.print(pw, indentLevel + 1);
        printTerm(pw, indentLevel + 1, ASSIGN);
        myExp.print(pw, indentLevel + 1);
        if (hasSemi)
          printTerm(pw, indentLevel, SEMICOLON);
    }

    public void toggleSemi() {
      hasSemi = !hasSemi;
    }

    private Expr myLhs;
    private Expr myExp;
    private boolean hasSemi;
}

class CallStmt extends Stmt {
  public CallStmt(CallExpr callExpr) {
    myCallExpr = callExpr;
  }

  public void print(PrintWriter pw, int indentLevel) {
      printNonTerm(pw, indentLevel);
      myCallExpr.print(pw, indentLevel + 1);
      printTerm(pw, indentLevel + 1, SEMICOLON);
  }

  private CallExpr myCallExpr;
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
    private VarDeclList myVarDeclList;
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
    printNonTerm(p, indent);
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

  public ForStmt(AssignStmt init, Expr cond, AssignStmt incr, VarDeclList declist, StmtList stmlist) {
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
    if (myInit != null)
      myInit.print(p, indent+1);
    else
      printTerm(p, indent+1, SEMICOLON);
    myCond.print(p, indent+1);
    printTerm(p, indent+1, SEMICOLON);
    if (myIncr != null) {
      myIncr.toggleSemi();
      myIncr.print(p, indent+1);
    }
    printTerm(p, indent+1, RPAREN);
    printTerm(p, indent+1, LCURLY);
    myVarDeclList.print(p, indent+1);
    myStmtList.print(p, indent+1);
    printTerm(p, indent+1, RCURLY);
  }

  private AssignStmt myInit;
  private Expr myCond;
  private AssignStmt myIncr;
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

  public ReturnStmt() {
    myExp = null;
  }

  public void print(PrintWriter p, int indent) {
    printNonTerm(p, indent);
    if (myExp == null) {
      printTerm(p, indent+1, RETURN);
    } else {
      printTerm(p, indent+1, RETURN);
      myExp.print(p, indent+1);
    }
    printTerm(p, indent+1, SEMICOLON);
  }

  private Expr myExp;
}
