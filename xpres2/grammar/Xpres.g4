grammar Xpres;

// Remove this header if using the default IntelliJ project layout
@header {
package xpres2.grammar;
}

file
: code
;

code
: statement ';' code
|
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
: addExpr
;

addExpr
: mulExpr
| addExpr '+' mulExpr
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
