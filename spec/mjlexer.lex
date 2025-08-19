package rs.ac.bg.etf.pp1;
import java_cup.runtime.Symbol;
%%

%{
	// ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type) {
		return new Symbol(type, yyline+1, yycolumn);
	}
	
	// ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type, Object value) {
		return new Symbol(type, yyline+1, yycolumn, value);
	}

%}

%cup
%line
%column

%xstate COMMENT
%xstate CHAR

%eofval{
	return new_symbol(sym.EOF);
%eofval}

%%

" " { }
"\b" { }
"\t" { }
"\r" { }
"\n" { }
"\f" { }

"program" { return new_symbol(sym.PROG, yytext()); }
"break" { return new_symbol(sym.BREAK, yytext()); }
"class" { return new_symbol(sym.CLASS, yytext()); }
"else" { return new_symbol(sym.ELSE, yytext()); }
"const" { return new_symbol(sym.CONST, yytext()); }
"if" { return new_symbol(sym.IF, yytext()); }
"new" { return new_symbol(sym.NEW, yytext()); }
"print" { return new_symbol(sym.PRINT, yytext()); }
"read" { return new_symbol(sym.READ, yytext()); }
"return" { return new_symbol(sym.RETURN, yytext()); }
"void" { return new_symbol(sym.VOID, yytext()); }
"extends" { return new_symbol(sym.EXTENDS, yytext()); }
"continue" { return new_symbol(sym.CONTINUE, yytext()); }
"intersect" { return new_symbol(sym.INTERSECT, yytext()); }
"union" { return new_symbol(sym.UNION, yytext()); }
"do" { return new_symbol(sym.DO, yytext()); }
"while" { return new_symbol(sym.WHILE, yytext()); }
"map" { return new_symbol(sym.MAP, yytext()); }
"interface" { return new_symbol(sym.INTERFACE, yytext()); }

"//" { yybegin(COMMENT); }
<COMMENT> . {yybegin(COMMENT);}
<COMMENT> "\r" { yybegin(YYINITIAL); }
<COMMENT> "\n" { yybegin(YYINITIAL); }

"#" { return new_symbol(sym.HASH, yytext()); }
"+++" { return new_symbol(sym.DUAL_INC, yytext()); }
"++" { return new_symbol(sym.INC, yytext()); }
"--" { return new_symbol(sym.DEC, yytext()); }
"+" { return new_symbol(sym.PLUS, yytext()); }
"-" { return new_symbol(sym.MINUS, yytext()); }
"*" { return new_symbol(sym.MULTIPLY, yytext()); }
"/" { return new_symbol(sym.DIVIDE, yytext()); }
"%" { return new_symbol(sym.PERCENT, yytext()); }
"==" { return new_symbol(sym.IS_E, yytext()); }
"!=" { return new_symbol(sym.IS_NE, yytext()); }
">=" { return new_symbol(sym.IS_GTE, yytext()); }
">" { return new_symbol(sym.IS_GT, yytext()); }
"<=" { return new_symbol(sym.IS_LTE, yytext()); }
"<" { return new_symbol(sym.IS_LT, yytext()); }
"&&" { return new_symbol(sym.LOGICAL_AND, yytext()); }
"||" { return new_symbol(sym.LOGICAL_OR, yytext()); }
"=" { return new_symbol(sym.EQUAL, yytext()); }
";" { return new_symbol(sym.SEMI, yytext()); }
":" { return new_symbol(sym.COLUMN, yytext()); }
"," { return new_symbol(sym.COMMA, yytext()); }
"." { return new_symbol(sym.DOT, yytext()); }
"(" { return new_symbol(sym.L_BRACKET_CURVED, yytext()); }
")" { return new_symbol(sym.R_BRACKET_CURVED, yytext()); }
"[" { return new_symbol(sym.L_BRACKET_SQUARED, yytext()); }
"]" { return new_symbol(sym.R_BRACKET_SQUARED, yytext()); }
"{" { return new_symbol(sym.L_BRACKET_CURLY, yytext()); }
"}" { return new_symbol(sym.R_BRACKET_CURLY, yytext()); }


"'"."'" { return new_symbol(sym.CHAR, yytext().charAt(1)); }
[0-9]+ { return new_symbol(sym.NUMBER, Integer.parseInt(yytext())); }
"true" { return new_symbol (sym.BOOL, Boolean.valueOf("true")); }
"false" { return new_symbol (sym.BOOL, Boolean.valueOf("false")); }

([a-zA-Z])[a-zA-Z0-9_]* { return new_symbol (sym.IDENT, yytext()); }



. { System.err.println("Leksicka greska (" + yytext() + ") u liniji " + (yyline+1)); }
