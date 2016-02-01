grammar Xpres;

@header {
package xpres2.grammar;
}

code
: statement ';' code
| EOF                           // implicitly defined terminal
;

statement
: decl
| assign
| print
;

decl
: 'var' ID
;

assign
: ID '=' expr
;

print
: 'print' expr
;

expr
: mulExpr
| expr '+' mulExpr
;

mulExpr
: atomExpr
| mulExpr '*' atomExpr
;

atomExpr
: ID
| INT
| '(' expr ')'
;

ID:	('a'..'z')+ ;
INT:	('0'..'9')+ ;
WS:	[ \n\t\r]+ -> skip ;
