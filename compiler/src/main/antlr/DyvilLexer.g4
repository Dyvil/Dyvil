lexer grammar DyvilLexer;

// --------------- Symbols ---------------

// DyvilSymbols
ELLIPSIS          : '...';
AT                : '@';
ARROW_LEFT        : '<-';
ARROW_RIGHT       : '->';
DOUBLE_ARROW_RIGHT: '=>';

// BaseSymbols

LPAREN  : '(';
RPAREN  : ')';
LBRACKET: '[';
RBRACKET: ']';
LBRACE  : '{';
RBRACE  : '}';

DOT       : '.';
COLON     : ':';
SEMICOLON : ';';
COMMA     : ',';
EQUALS    : '=';
HASH      : '#';
UNDERSCORE: '_';

// --------------- Keywords ---------------

ABSTRACT    : 'abstract';
AS          : 'as';
BREAK       : 'break';
CASE        : 'case';
CATCH       : 'catch';
CLASS       : 'class';
CONST       : 'const';
CONTINUE    : 'continue';
DO          : 'do';
ELSE        : 'else';
ENUM        : 'enum';
EXPLICIT    : 'explicit';
EXTENDS     : 'extends';
EXTENSION   : 'extension';
FALSE       : 'false';
FINAL       : 'final';
FINALLY     : 'finally';
FOR         : 'for';
FUNC        : 'func';
GOTO        : 'goto';
HEADER      : 'header';
IF          : 'if';
IMPLEMENTS  : 'implements';
IMPLICIT    : 'implicit';
IMPORT      : 'import';
INFIX       : 'infix';
INIT        : 'init';
INLINE      : 'inline';
INTERFACE   : 'interface';
INTERNAL    : 'internal';
IS          : 'is';
LABEL       : 'label';
LAZY        : 'lazy';
LET         : 'let';
MACRO       : 'macro';
MATCH       : 'match';
NEW         : 'new';
NIL         : 'nil';
NULL        : 'null';
OBJECT      : 'object';
OVERRIDE    : 'override';
OPERATOR    : 'operator';
PACKAGE     : 'package';
POSTFIX     : 'postfix';
PREFIX      : 'prefix';
PRIVATE     : 'private';
PROTECTED   : 'protected';
PUBLIC      : 'public';
REPEAT      : 'repeat';
RETURN      : 'return';
STATIC      : 'static';
STRUCT      : 'struct';
SUPER       : 'super';
SYNCHRONIZED: 'synchronized';
TEMPLATE    : 'template';
THIS        : 'this';
THROW       : 'throw';
THROWS      : 'throws';
TRAIT       : 'trait';
TRUE        : 'true';
TRY         : 'try';
TYPE        : 'type';
USING       : 'using';
VAR         : 'var';
WHERE       : 'where';
WHILE       : 'while';

// --------------- Others ---------------

WS: [ \t] -> skip;
NL: '\r'? '\n';
