grammar Xpres;

// Remove this header if using the default IntelliJ project layout
@header {
package xpres.grammar;
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
: ID
| INT
;

ID:	('a'..'z')+ ;
INT:	('0'..'9')+ ;
WS:	[ \n\t\r]+ -> skip ;
