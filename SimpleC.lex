import java.io.*;
import java_cup.runtime.*;

%%

%public
%class Scanner
%implements sym

%line
%column

%cup
%cupdebug

%{
  StringBuffer string = new StringBuffer();

  private Symbol symbol(int type) {
       return new MySymbol(type, yyline+1, yycolumn+1, yytext());
    }

  private Symbol symbol(int type, Object value) {
    return new MySymbol(type, yyline+1, yycolumn+1, value);
  }

  public String getTokName(int token) {
      return getTokenName(token);
  }
%}

/* main character classes */

LineTerminator = \r|\n|\r\n
WhiteSpace = {LineTerminator} | [ \v\t\f]

Identifier = [_a-zA-Z][_a-zA-Z0-9]*

Sign = [+-]?
/* => May conflict with Minus Unary Law
*/

IntSuffices = [uU]? (l|L|ll|LL)?
IntDec = 0 | [1-9][0-9]*
IntHex = 0[xX][a-fA-F0-9]+
IntOct = 0[0-7]+
IntLiteral = ({IntDec} | {IntHex} | {IntOct}) {IntSuffices}

RealLiteral = (([0-9]+ \.? [0-9]*) | (\. [0-9]+)) ([eE]{Sign}[0-9]+)? [fFlL]?

CommentLine = "//" [^\r\n]*
CommentBlock = "/*" ~ "*/"

StringLiteral = L? \"(\\. | [^\"\\])*\"

CharLiteral = L? '(\\[^x\r\n] | [^\'\\\r\n] | \\x[a-fA-F0-9]+)+'

/* PreProcessor = #[ \t]* (include|ifdef|endif|ifndef|elif|else|pragma|error|warning)
- Should be a grammar rule in parser
*/

%%

<YYINITIAL> {

  /* keywords */
  "int"       { return symbol(INT); }
  "bool"      { return symbol(BOOL); }
  "return"    { return symbol(RETURN); }
  "void"      { return symbol(VOID); }
  "if"        { return symbol(IF); }
  "else"      { return symbol(ELSE); }
  "true"      { return symbol(TRUE); }
  "false"     { return symbol(FALSE); }
  "TRUE"      { return symbol(TRUE); }
  "FALSE"     { return symbol(FALSE); }
	"auto"			{return symbol(AUTO); }
	"break"			{ return symbol(BREAK); }
  "case"			{ return symbol(CASE); }
  "char"			{ return symbol(CHAR); }
  "const"			{ return symbol(CONST); }
  "continue"		{ return symbol(CONTINUE); }
  "default"		{ return symbol(DEFAULT); }
  "do"			{ return symbol(DO); }
  "double"		{ return symbol(DOUBLE); }
  "enum"			{ return symbol(ENUM); }
  "extern"		{ return symbol(EXTERN); }
  "float"			{ return symbol(FLOAT); }
  "for"			{ return symbol(FOR); }
  "goto"			{ return symbol(GOTO); }
  "long"			{ return symbol(LONG); }
  "register"		{ return symbol(REGISTER); }
  "short"			{ return symbol(SHORT); }
  "signed"		{ return symbol(SIGNED); }
  "sizeof"		{ return symbol(SIZEOF); }
  "static"		{ return symbol(STATIC); }
  "struct"		{ return symbol(STRUCT); }
  "switch"		{ return symbol(SWITCH); }
  "typedef"		{ return symbol(TYPEDEF); }
  "union"			{ return symbol(UNION); }
  "unsigned"		{ return symbol(UNSIGNED); }
  "volatile"		{ return symbol(VOLATILE); }
  "while"			{ return symbol(WHILE); }
  "NULL"      { return symbol(NULL); }

  /* punctuators */
  "("       { return symbol(LPAREN); }
  ")"       { return symbol(RPAREN); }
  "{"       { return symbol(LCURLY); }
  "}"       { return symbol(RCURLY); }
  "["       { return symbol(LSQBRACKET); }
  "]"       { return symbol(RSQBRACKET); }
  ";"       { return symbol(SEMICOLON); }
  ","       { return symbol(COMMA); }
  "<"       { return symbol(LESS); }
  ">"       { return symbol(GREATER); }
  "+"       { return symbol(PLUS); }
  "-"       { return symbol(MINUS); }
  "/"       { return symbol(DIVIDE); }
  "*"       { return symbol(TIMES); }
  "="       { return symbol(ASSIGN); }
  "=="      { return symbol(EQUALS); }
  "..."     { return symbol(ELLIPSIS); }
  ">>="     { return symbol(RIGHT_ASSIGN); }
  "<<="     { return symbol(LEFT_ASSIGN); }
  "+="      { return symbol(ADD_ASSIGN); }
  "-="      { return symbol(SUB_ASSIGN); }
  "*="      { return symbol(MUL_ASSIGN); }
  "/="      { return symbol(DIV_ASSIGN); }
  "%="      { return symbol(MOD_ASSIGN); }
  "&="      { return symbol(AND_ASSIGN); }
  "^="      { return symbol(XOR_ASSIGN); }
  "|="      { return symbol(OR_ASSIGN); }
  ">>"      { return symbol(RIGHT_OP); }
  "<<"      { return symbol(LEFT_OP); }
  "++"      { return symbol(INC_OP); }
/*  "--"      { return symbol(DEC_OP); } */
  "->"      { return symbol(PTR_OP); }
  "&&"      { return symbol(AND_OP); }
  "||"      { return symbol(OR_OP); }
  "<="      { return symbol(LE_OP); }
  ">="      { return symbol(GE_OP); }
  "!="      { return symbol(NE_OP); }
  ":"     { return symbol(COLON); }
  "."     { return symbol(FULLSTOP); }
  "&"     { return symbol(AMPERSAND); }
  "!"     { return symbol(BANG); }
  "~"     { return symbol(TILDE); }
  "%"     { return symbol(PERCENT); }
  "^"     { return symbol(CARET); }
  "|"     { return symbol(VERTICALBAR); }
  "?"     { return symbol(QUESTIONMAR); }
  "#"	  { return symbol(HASH); }

  {IntLiteral}               { return symbol(INTLITERAL, yytext()); }

  {RealLiteral} { return symbol(REALLITERAL, yytext()); }

  {StringLiteral} { return symbol(STRINGLITERAL, yytext()); }

  {CharLiteral} { return symbol(CHARLITERAL, yytext()); }

  {CommentLine}	{  }
  {CommentBlock}	{  }

/*  {PreProcessor}	{ return symbol(PREPROCESSOR, yytext()); } */

  {WhiteSpace}                   { /* ignore */ }

  /* identifiers */
  {Identifier}                   { return symbol(ID, yytext()); }
}

.                              { Errors.fatal(yyline+1, yycolumn+1, "Illegal character \"" + yytext()+ "\"");
       System.exit(-1); }
<<EOF>>                          { return symbol(EOF); }
