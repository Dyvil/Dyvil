package dyvil.tools.compiler.transform;

import dyvil.tools.parsing.lexer.Tokens;

public class DyvilKeywords
{
	public static final int ABSTRACT     = Tokens.KEYWORD | 0x00010000;
	public static final int AS           = Tokens.KEYWORD | 0x00020000;
	public static final int BREAK        = Tokens.KEYWORD | 0x00030000;
	public static final int CASE         = Tokens.KEYWORD | 0x00040000;
	public static final int CATCH        = Tokens.KEYWORD | 0x00050000;
	public static final int CLASS        = Tokens.KEYWORD | 0x00060000;
	public static final int CONST        = Tokens.KEYWORD | 0x00070000; // TODO use for expression templates
	public static final int CONTINUE     = Tokens.KEYWORD | 0x00080000;
	public static final int DO           = Tokens.KEYWORD | 0x00090000; // unused
	public static final int ELSE         = Tokens.KEYWORD | 0x000A0000;
	public static final int ENUM         = Tokens.KEYWORD | 0x000B0000;
	public static final int EXTENDS      = Tokens.KEYWORD | 0x000C0000;
	public static final int EXTENSION    = Tokens.KEYWORD | 0x000D0000;
	public static final int FALSE        = Tokens.KEYWORD | 0x000E0000;
	public static final int FINAL        = Tokens.KEYWORD | 0x000F0000;
	public static final int FINALLY      = Tokens.KEYWORD | 0x00100000;
	public static final int FOR          = Tokens.KEYWORD | 0x00110000;
	public static final int FUNC         = Tokens.KEYWORD | 0x00120000;
	public static final int GOTO         = Tokens.KEYWORD | 0x00130000;
	public static final int HEADER       = Tokens.KEYWORD | 0x00140000;
	public static final int IF           = Tokens.KEYWORD | 0x00150000;
	public static final int IMPLEMENTS   = Tokens.KEYWORD | 0x00160000;
	public static final int IMPLICIT     = Tokens.KEYWORD | 0x00170000;
	public static final int IMPORT       = Tokens.KEYWORD | 0x00180000;
	public static final int INCLUDE      = Tokens.KEYWORD | 0x00190000;
	public static final int INFIX        = Tokens.KEYWORD | 0x001A0000;
	public static final int INIT         = Tokens.KEYWORD | 0x001B0000;
	public static final int INLINE       = Tokens.KEYWORD | 0x001C0000;
	public static final int INTERFACE    = Tokens.KEYWORD | 0x001D0000;
	public static final int INTERNAL     = Tokens.KEYWORD | 0x001E0000;
	public static final int IS           = Tokens.KEYWORD | 0x001F0000;
	public static final int LABEL        = Tokens.KEYWORD | 0x00410000;
	public static final int LAZY         = Tokens.KEYWORD | 0x00200000;
	public static final int LET          = Tokens.KEYWORD | 0x00210000;
	public static final int MACRO        = Tokens.KEYWORD | 0x00220000; // unused
	public static final int MATCH        = Tokens.KEYWORD | 0x00230000;
	public static final int NEW          = Tokens.KEYWORD | 0x00240000;
	public static final int NIL          = Tokens.KEYWORD | 0x00250000;
	public static final int NULL         = Tokens.KEYWORD | 0x00260000;
	public static final int OBJECT       = Tokens.KEYWORD | 0x00270000;
	public static final int OVERRIDE     = Tokens.KEYWORD | 0x00280000;
	public static final int OPERATOR     = Tokens.KEYWORD | 0x00290000;
	public static final int PACKAGE      = Tokens.KEYWORD | 0x002A0000;
	public static final int POSTFIX      = Tokens.KEYWORD | 0x002B0000;
	public static final int PREFIX       = Tokens.KEYWORD | 0x002C0000;
	public static final int PRIVATE      = Tokens.KEYWORD | 0x002D0000;
	public static final int PROTECTED    = Tokens.KEYWORD | 0x002E0000;
	public static final int PUBLIC       = Tokens.KEYWORD | 0x002F0000;
	public static final int REPEAT       = Tokens.KEYWORD | 0x00400000;
	public static final int RETURN       = Tokens.KEYWORD | 0x00300000;
	public static final int SEALED       = Tokens.KEYWORD | 0x00310000; // unused
	public static final int STATIC       = Tokens.KEYWORD | 0x00320000;
	public static final int SUPER        = Tokens.KEYWORD | 0x00330000;
	public static final int SYNCHRONIZED = Tokens.KEYWORD | 0x00340000;
	public static final int THIS         = Tokens.KEYWORD | 0x00350000;
	public static final int THROW        = Tokens.KEYWORD | 0x00360000;
	public static final int THROWS       = Tokens.KEYWORD | 0x00370000;
	public static final int TRAIT        = Tokens.KEYWORD | 0x00380000;
	public static final int TRUE         = Tokens.KEYWORD | 0x00390000;
	public static final int TRY          = Tokens.KEYWORD | 0x003A0000;
	public static final int TYPE         = Tokens.KEYWORD | 0x003B0000;
	public static final int USING        = Tokens.KEYWORD | 0x003C0000;
	public static final int VAR          = Tokens.KEYWORD | 0x003D0000;
	public static final int WHERE        = Tokens.KEYWORD | 0x003E0000; // unused
	public static final int WHILE        = Tokens.KEYWORD | 0x003F0000;

	public static int getKeywordType(String s)
	{
		// @formatter:off
		switch (s)
		{
		case "abstract": return ABSTRACT;
		case "as": return AS;
		case "break": return BREAK;
		case "case": return CASE;
		case "catch": return CATCH;
		case "class": return CLASS;
		case "const": return CONST;
		case "continue": return CONTINUE;
		case "do": return DO;
		case "else": return ELSE;
		case "enum": return ENUM;
		case "extends": return EXTENDS;
		case "extension": return EXTENSION;
		case "false": return FALSE;
		case "final": return FINAL;
		case "finally": return FINALLY;
		case "for": return FOR;
		case "func": return FUNC;
		case "goto": return GOTO;
		case "header": return HEADER;
		case "if": return IF;
		case "implements": return IMPLEMENTS;
		case "implicit": return IMPLICIT;
		case "import": return IMPORT;
		case "include": return INCLUDE;
		case "infix": return INFIX;
		case "init": return INIT;
		case "inline": return INLINE;
		case "interface": return INTERFACE;
		case "internal": return INTERNAL;
		case "is": return IS;
		case "label": return LABEL;
		case "lazy": return LAZY;
		case "let": return LET;
		case "macro": return MACRO;
		case "match": return MATCH;
		case "new": return NEW;
		case "nil": return NIL;
		case "null": return NULL;
		case "object": return OBJECT;
		case "operator": return OPERATOR;
		case "override": return OVERRIDE;
		case "package": return PACKAGE;
		case "postfix": return POSTFIX;
		case "prefix": return PREFIX;
		case "private": return PRIVATE;
		case "protected": return PROTECTED;
		case "public": return PUBLIC;
		case "repeat": return REPEAT;
		case "return": return RETURN;
		case "sealed": return SEALED;
		case "static": return STATIC;
		case "super": return SUPER;
		case "synchronized": return SYNCHRONIZED;
		case "this": return THIS;
		case "throw": return THROW;
		case "throws": return THROWS;
		case "trait": return TRAIT;
		case "true": return TRUE;
		case "try": return TRY;
		case "type": return TYPE;
		case "using": return USING;
		case "var": return VAR;
		case "where": return WHERE;
		case "while": return WHILE;
		}
		// @formatter:on
		return 0;
	}

	public static String keywordToString(int type)
	{
		// @formatter:off
		switch (type)
		{
		case ABSTRACT:	return "abstract";
		case AS: return "as";
		case BREAK: return "break";
		case CASE: return "case";
		case CATCH: return "catch";
		case CLASS: return "class";
		case CONST: return "const";
		case CONTINUE: return "continue";
		case DO: return "do";
		case ELSE: return "else";
		case ENUM: return "enum";
		case EXTENDS: return "extends";
		case EXTENSION: return "extension";
		case FALSE: return "false";
		case FINAL: return "final";
		case FINALLY: return "finally";
		case FOR: return "for";
		case FUNC: return "func";
		case GOTO: return "goto";
		case HEADER: return "header";
		case IF: return "if";
		case IMPLEMENTS: return "implements";
		case IMPLICIT: return "implicit";
		case IMPORT: return "import";
		case INCLUDE: return "include";
		case INIT: return "init";
		case INFIX: return "infix";
		case INLINE: return "inline";
		case INTERFACE: return "interface";
		case INTERNAL: return "internal";
		case IS: return "is";
		case LABEL: return "label";
		case LAZY: return "lazy";
		case LET :  return "let";
		case MACRO: return "macro";
		case MATCH: return "match";
		case NEW: return "new";
		case NIL: return "nil";
		case NULL: return "null";
		case OBJECT: return "object";
		case OPERATOR: return "operator";
		case OVERRIDE: return "override";
		case PACKAGE: return "package";
		case POSTFIX: return "postfix";
		case PREFIX: return "prefix";
		case PRIVATE: return "private";
		case PROTECTED: return "protected";
		case PUBLIC: return "public";
		case REPEAT: return "repeat";
		case RETURN: return "return";
		case SEALED: return "sealed";
		case STATIC: return "static";
		case SUPER: return "super";
		case SYNCHRONIZED: return "synchronized";
		case THIS: return "this";
		case THROW: return "throw";
		case THROWS: return "throws";
		case TRAIT: return "trait";
		case TRUE: return "true";
		case TRY: return "try";
		case TYPE: return "type";
		case USING: return "using";
		case VAR: return "var";
		case WHERE: return "where";
		case WHILE: return "while";
		}
		// @formatter:on
		return null;
	}
}
