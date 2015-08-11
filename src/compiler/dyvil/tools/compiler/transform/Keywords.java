package dyvil.tools.compiler.transform;

public interface Keywords
{
	int	ABSTRACT		= Tokens.KEYWORD | 0x00010000;
	int	ANNOTATION		= Tokens.KEYWORD | 0x00020000;
	int	AS				= Tokens.KEYWORD | 0x00030000;
	int	BREAK			= Tokens.KEYWORD | 0x00040000;
	int	CASE			= Tokens.KEYWORD | 0x00050000;
	int	CATCH			= Tokens.KEYWORD | 0x00060000;
	int	CLASS			= Tokens.KEYWORD | 0x00070000;
	int	CONST			= Tokens.KEYWORD | 0x00080000;
	int	CONTINUE		= Tokens.KEYWORD | 0x00090000;
	int	DO				= Tokens.KEYWORD | 0x000C0000;
	int	ELSE			= Tokens.KEYWORD | 0x000D0000;
	int	ENUM			= Tokens.KEYWORD | 0x000E0000;
	int	EXTENDS			= Tokens.KEYWORD | 0x000F0000;
	int	EXTENSION		= Tokens.KEYWORD | 0x003E0000;
	int	FALSE			= Tokens.KEYWORD | 0x00100000;
	int	FINAL			= Tokens.KEYWORD | 0x00110000;
	int	FINALLY			= Tokens.KEYWORD | 0x00120000;
	int	FOR				= Tokens.KEYWORD | 0x00130000;
	int	FUNCTIONAL		= Tokens.KEYWORD | 0x00140000;
	int	GOTO			= Tokens.KEYWORD | 0x00160000;
	int	IF				= Tokens.KEYWORD | 0x00170000;
	int	IMPLEMENTS		= Tokens.KEYWORD | 0x00180000;
	int	IMPLICIT		= Tokens.KEYWORD | 0x003B0000;
	int	IMPORT			= Tokens.KEYWORD | 0x00190000;
	int	INCLUDE			= Tokens.KEYWORD | 0x001A0000;
	int	INLINE			= Tokens.KEYWORD | 0x001B0000;
	int	INFIX			= Tokens.KEYWORD | 0x001C0000;
	int	INTERFACE		= Tokens.KEYWORD | 0x001D0000;
	int	INTERNAL		= Tokens.KEYWORD | 0x003F0000;
	int	IS				= Tokens.KEYWORD | 0x001E0000;
	int	LAZY			= Tokens.KEYWORD | 0x001F0000;
	int	MACRO			= Tokens.KEYWORD | 0x003A0000;
	int	MATCH			= Tokens.KEYWORD | 0x003C0000;
	int	NEW				= Tokens.KEYWORD | 0x00200000;
	int	NIL				= Tokens.KEYWORD | 0x00210000;
	int	NULL			= Tokens.KEYWORD | 0x00220000;
	int	OBJECT			= Tokens.KEYWORD | 0x00230000;
	int	OVERRIDE		= Tokens.KEYWORD | 0x00240000;
	int	OPERATOR		= Tokens.KEYWORD | 0x00250000;
	int	PACKAGE			= Tokens.KEYWORD | 0x00260000;
	int	POSTFIX			= Tokens.KEYWORD | 0x00270000;
	int	PREFIX			= Tokens.KEYWORD | 0x00390000;
	int	PRIVATE			= Tokens.KEYWORD | 0x00280000;
	int	PROTECTED		= Tokens.KEYWORD | 0x00290000;
	int	PUBLIC			= Tokens.KEYWORD | 0x002A0000;
	int	RETURN			= Tokens.KEYWORD | 0x002B0000;
	int	SEALED			= Tokens.KEYWORD | 0x002C0000;
	int	STATIC			= Tokens.KEYWORD | 0x002D0000;
	int	SUPER			= Tokens.KEYWORD | 0x002E0000;
	int	SYNCHRONIZED	= Tokens.KEYWORD | 0x002F0000;
	int	THIS			= Tokens.KEYWORD | 0x00300000;
	int	THROW			= Tokens.KEYWORD | 0x00310000;
	int	THROWS			= Tokens.KEYWORD | 0x00320000;
	int	TRUE			= Tokens.KEYWORD | 0x00330000;
	int	TRY				= Tokens.KEYWORD | 0x00340000;
	int	TYPE			= Tokens.KEYWORD | 0x00350000;
	int	USING			= Tokens.KEYWORD | 0x00360000;
	int	VAR				= Tokens.KEYWORD | 0x00370000;
	int	WHILE			= Tokens.KEYWORD | 0x00380000;
	
	public static int getKeywordType(String s)
	{
		switch (s)
		{
		case "abstract":
			return ABSTRACT;
		case "annotation":
			return ANNOTATION;
		case "as":
			return AS;
		case "break":
			return BREAK;
		case "case":
			return CASE;
		case "catch":
			return CATCH;
		case "class":
			return CLASS;
		case "const":
			return CONST;
		case "continue":
			return CONTINUE;
		case "do":
			return DO;
		case "else":
			return ELSE;
		case "enum":
			return ENUM;
		case "extends":
			return EXTENDS;
		case "extension":
			return EXTENSION;
		case "false":
			return FALSE;
		case "final":
			return FINAL;
		case "finally":
			return FINALLY;
		case "for":
			return FOR;
		case "functional":
			return FUNCTIONAL;
		case "goto":
			return GOTO;
		case "if":
			return IF;
		case "implements":
			return IMPLEMENTS;
		case "implicit":
			return IMPLICIT;
		case "import":
			return IMPORT;
		case "include":
			return INCLUDE;
		case "inline":
			return INLINE;
		case "infix":
			return INFIX;
		case "interface":
			return INTERFACE;
		case "internal":
			return INTERNAL;
		case "is":
			return IS;
		case "lazy":
			return LAZY;
		case "macro":
			return MACRO;
		case "match":
			return MATCH;
		case "new":
			return NEW;
		case "nil":
			return NIL;
		case "null":
			return NULL;
		case "object":
			return OBJECT;
		case "operator":
			return OPERATOR;
		case "override":
			return OVERRIDE;
		case "package":
			return PACKAGE;
		case "postfix":
			return POSTFIX;
		case "prefix":
			return PREFIX;
		case "private":
			return PRIVATE;
		case "protected":
			return PROTECTED;
		case "public":
			return PUBLIC;
		case "return":
			return RETURN;
		case "sealed":
			return SEALED;
		case "static":
			return STATIC;
		case "super":
			return SUPER;
		case "synchronized":
			return SYNCHRONIZED;
		case "this":
			return THIS;
		case "throw":
			return THROW;
		case "throws":
			return THROWS;
		case "true":
			return TRUE;
		case "try":
			return TRY;
		case "type":
			return TYPE;
		case "using":
			return USING;
		case "var":
			return VAR;
		case "while":
			return WHILE;
		}
		return 0;
	}
	
	public static String keywordToString(int type)
	{
		switch (type)
		{
		case ABSTRACT:
			return "abstract";
		case ANNOTATION:
			return "annotation";
		case AS:
			return "as";
		case BREAK:
			return "break";
		case CASE:
			return "case";
		case CATCH:
			return "catch";
		case CLASS:
			return "class";
		case CONST:
			return "const";
		case CONTINUE:
			return "continue";
		case DO:
			return "do";
		case ELSE:
			return "else";
		case ENUM:
			return "enum";
		case EXTENDS:
			return "extends";
		case EXTENSION:
			return "extension";
		case FALSE:
			return "false";
		case FINAL:
			return "final";
		case FINALLY:
			return "finally";
		case FOR:
			return "for";
		case FUNCTIONAL:
			return "functional";
		case GOTO:
			return "goto";
		case IF:
			return "if";
		case IMPLEMENTS:
			return "implements";
		case IMPLICIT:
			return "implicit";
		case IMPORT:
			return "import";
		case INCLUDE:
			return "include";
		case INLINE:
			return "inline";
		case INFIX:
			return "infix";
		case INTERFACE:
			return "interface";
		case INTERNAL:
			return "internal";
		case IS:
			return "is";
		case LAZY:
			return "lazy";
		case MACRO:
			return "macro";
		case MATCH:
			return "match";
		case NEW:
			return "new";
		case NIL:
			return "nil";
		case NULL:
			return "null";
		case OBJECT:
			return "object";
		case OPERATOR:
			return "operator";
		case OVERRIDE:
			return "override";
		case PACKAGE:
			return "package";
		case POSTFIX:
			return "postfix";
		case PREFIX:
			return "prefix";
		case PRIVATE:
			return "private";
		case PROTECTED:
			return "protected";
		case PUBLIC:
			return "public";
		case RETURN:
			return "return";
		case SEALED:
			return "sealed";
		case STATIC:
			return "static";
		case SUPER:
			return "super";
		case SYNCHRONIZED:
			return "synchronized";
		case THIS:
			return "this";
		case THROW:
			return "throw";
		case THROWS:
			return "throws";
		case TRUE:
			return "true";
		case TRY:
			return "try";
		case TYPE:
			return "type";
		case USING:
			return "using";
		case VAR:
			return "var";
		case WHILE:
			return "while";
		}
		return "";
	}
}
