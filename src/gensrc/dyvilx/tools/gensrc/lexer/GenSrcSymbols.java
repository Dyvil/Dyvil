package dyvilx.tools.gensrc.lexer;

import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Symbols;
import dyvilx.tools.parsing.lexer.Tokens;

public class GenSrcSymbols implements Symbols
{
	public static final GenSrcSymbols INSTANCE = new GenSrcSymbols();

	public static final int ARROW_LEFT = Tokens.SYMBOL | 0x000A0000;

	public static final int IF       = Tokens.KEYWORD | 0x00010000;
	public static final int ELSE     = Tokens.KEYWORD | 0x00020000;
	public static final int FOR      = Tokens.KEYWORD | 0x00030000;
	public static final int DEFINE   = Tokens.KEYWORD | 0x00040000;
	public static final int UNDEFINE = Tokens.KEYWORD | 0x00050000;
	public static final int LOCAL    = Tokens.KEYWORD | 0x00060000;
	public static final int DELETE   = Tokens.KEYWORD | 0x00070000;
	public static final int NAME     = Tokens.KEYWORD | 0x00080000;
	public static final int INCLUDE  = Tokens.KEYWORD | 0x00090000;
	public static final int IMPORT   = Tokens.KEYWORD | 0x000A0000;

	private GenSrcSymbols()
	{
	}

	@Override
	public int getKeywordType(String s)
	{
		switch (s)
		{
		case "if":
			return IF;
		case "else":
			return ELSE;
		case "for":
			return FOR;
		case "define":
			return DEFINE;
		case "undefine":
			return UNDEFINE;
		case "local":
			return LOCAL;
		case "delete":
			return DELETE;
		case "name":
			return NAME;
		case "include":
			return INCLUDE;
		case "import":
			return IMPORT;
		}
		return 0;
	}

	@Override
	public int getSymbolType(String s)
	{
		switch (s)
		{
		case "<-":
			return ARROW_LEFT;
		}

		return BaseSymbols.INSTANCE.getSymbolType(s);
	}

	@Override
	public String toString(int type)
	{
		switch (type)
		{
		case ARROW_LEFT:
			return "<-";
		case IF:
			return "if";
		case ELSE:
			return "else";
		case FOR:
			return "for";
		case DEFINE:
			return "define";
		case UNDEFINE:
			return "undefine";
		case LOCAL:
			return "local";
		case DELETE:
			return "delete";
		case NAME:
			return "name";
		case INCLUDE:
			return "include";
		case IMPORT:
			return "import";
		}
		return BaseSymbols.INSTANCE.toString(type);
	}

	@Override
	public int getLength(int type)
	{
		return this.toString(type).length();
	}
}
