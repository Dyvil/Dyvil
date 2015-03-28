package dyvil.tools.compiler.transform;

public interface Keywords
{
	int	ABSTRACT		= Tokens.KEYWORD | 0x00010000;
	int	ANNOTATION		= Tokens.KEYWORD | 0x00020000;
	int	BOOLEAN			= Tokens.KEYWORD | 0x00030000;
	int	BREAK			= Tokens.KEYWORD | 0x00040000;
	int	BYTE			= Tokens.KEYWORD | 0x00050000;
	int	CASE			= Tokens.KEYWORD | 0x00060000;
	int	CATCH			= Tokens.KEYWORD | 0x00070000;
	int	CLASS			= Tokens.KEYWORD | 0x00080000;
	int	CONST			= Tokens.KEYWORD | 0x00090000;
	int	CONTINUE		= Tokens.KEYWORD | 0x000A0000;
	int	DERIVED			= Tokens.KEYWORD | 0x000B0000;
	int	DEPRECATED		= Tokens.KEYWORD | 0x000C0000;
	int	DO				= Tokens.KEYWORD | 0x000D0000;
	int	ELSE			= Tokens.KEYWORD | 0x000E0000;
	int	ENUM			= Tokens.KEYWORD | 0x000F0000;
	int	EXTENDS			= Tokens.KEYWORD | 0x00100000;
	int	FALSE			= Tokens.KEYWORD | 0x00110000;
	int	FINAL			= Tokens.KEYWORD | 0x00120000;
	int	FINALLY			= Tokens.KEYWORD | 0x00130000;
	int	FOR				= Tokens.KEYWORD | 0x00140000;
	int	FUNCTIONAL		= Tokens.KEYWORD | 0x00150000;
	int	GET				= Tokens.KEYWORD | 0x00160000;
	int	GOTO			= Tokens.KEYWORD | 0x00170000;
	int	IF				= Tokens.KEYWORD | 0x00180000;
	int	IMPLEMENTS		= Tokens.KEYWORD | 0x00190000;
	int	IMPORT			= Tokens.KEYWORD | 0x001A0000;
	int	INLINE			= Tokens.KEYWORD | 0x001B0000;
	int	INFIX			= Tokens.KEYWORD | 0x001C0000;
	int	INTERFACE		= Tokens.KEYWORD | 0x001D0000;
	int	LAZY			= Tokens.KEYWORD | 0x001E0000;
	int	NATIVE			= Tokens.KEYWORD | 0x001F0000;
	int	NEW				= Tokens.KEYWORD | 0x00200000;
	int	NULL			= Tokens.KEYWORD | 0x00210000;
	int	OBJECT			= Tokens.KEYWORD | 0x00220000;
	int	OVERRIDE		= Tokens.KEYWORD | 0x00230000;
	int	PACKAGE			= Tokens.KEYWORD | 0x00240000;
	int	PRIVATE			= Tokens.KEYWORD | 0x00250000;
	int	PROTECTED		= Tokens.KEYWORD | 0x00260000;
	int	PUBLIC			= Tokens.KEYWORD | 0x00270000;
	int	RETURN			= Tokens.KEYWORD | 0x00280000;
	int	SEALED			= Tokens.KEYWORD | 0x00290000;
	int	SET				= Tokens.KEYWORD | 0x002A0000;
	int	STATIC			= Tokens.KEYWORD | 0x002B0000;
	int	STRICTFP		= Tokens.KEYWORD | 0x002C0000;
	int	SUPER			= Tokens.KEYWORD | 0x002D0000;
	int	SYNCHRONIZED	= Tokens.KEYWORD | 0x002E0000;
	int	THIS			= Tokens.KEYWORD | 0x002F0000;
	int	THROW			= Tokens.KEYWORD | 0x00300000;
	int	THROWS			= Tokens.KEYWORD | 0x00310000;
	int	TRANSIENT		= Tokens.KEYWORD | 0x00320000;
	int	TRUE			= Tokens.KEYWORD | 0x00330000;
	int	TRY				= Tokens.KEYWORD | 0x00340000;
	int	USING			= Tokens.KEYWORD | 0x00350000;
	int	VAR				= Tokens.KEYWORD | 0x00360000;
	int	VOLATILE		= Tokens.KEYWORD | 0x00370000;
	int	WHILE			= Tokens.KEYWORD | 0x00380000;
	
	public static int getKeywordType(String s)
	{
		switch (s)
		{
		case "abstract":
			return ABSTRACT;
		case "annotation":
			return ANNOTATION;
		case "boolean":
			return BOOLEAN;
		case "break":
			return BREAK;
		case "byte":
			return BYTE;
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
		case "derived":
			return DERIVED;
		case "deprecated":
			return DEPRECATED;
		case "do":
			return DO;
		case "else":
			return ELSE;
		case "enum":
			return ENUM;
		case "extends":
			return EXTENDS;
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
		case "get":
			return GET;
		case "goto":
			return GOTO;
		case "if":
			return IF;
		case "implements":
			return IMPLEMENTS;
		case "import":
			return IMPORT;
		case "inline":
			return INLINE;
		case "infix":
			return INFIX;
		case "interface":
			return INTERFACE;
		case "lazy":
			return LAZY;
		case "native":
			return NATIVE;
		case "new":
			return NEW;
		case "null":
			return NULL;
		case "object":
			return OBJECT;
		case "override":
			return OVERRIDE;
		case "package":
			return PACKAGE;
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
		case "set":
			return SET;
		case "static":
			return STATIC;
		case "strictfp":
			return STRICTFP;
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
		case "transient":
			return TRANSIENT;
		case "true":
			return TRUE;
		case "try":
			return TRY;
		case "using":
			return USING;
		case "var":
			return VAR;
		case "volatile":
			return VOLATILE;
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
		case BOOLEAN:
			return "boolean";
		case BREAK:
			return "break";
		case BYTE:
			return "byte";
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
		case DERIVED:
			return "derived";
		case DEPRECATED:
			return "deprecated";
		case DO:
			return "do";
		case ELSE:
			return "else";
		case ENUM:
			return "enum";
		case EXTENDS:
			return "extends";
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
		case GET:
			return "get";
		case GOTO:
			return "goto";
		case IF:
			return "if";
		case IMPLEMENTS:
			return "implements";
		case IMPORT:
			return "import";
		case INLINE:
			return "inline";
		case INFIX:
			return "infix";
		case INTERFACE:
			return "interface";
		case LAZY:
			return "lazy";
		case NATIVE:
			return "native";
		case NEW:
			return "new";
		case NULL:
			return "null";
		case OBJECT:
			return "object";
		case OVERRIDE:
			return "override";
		case PACKAGE:
			return "package";
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
		case SET:
			return "set";
		case STATIC:
			return "static";
		case STRICTFP:
			return "strictfp";
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
		case TRANSIENT:
			return "transient";
		case TRUE:
			return "true";
		case TRY:
			return "try";
		case USING:
			return "using";
		case VAR:
			return "var";
		case VOLATILE:
			return "volatile";
		case WHILE:
			return "while";
		}
		return "";
	}
}
