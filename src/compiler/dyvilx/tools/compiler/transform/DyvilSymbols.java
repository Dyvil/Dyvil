package dyvilx.tools.compiler.transform;

import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Symbols;
import dyvilx.tools.parsing.lexer.Tokens;

public final class DyvilSymbols implements Symbols
{
	public static final int HASH       = BaseSymbols.HASH;
	public static final int UNDERSCORE = BaseSymbols.UNDERSCORE;
	public static final int ELLIPSIS   = Tokens.SYMBOL | 0x00080000;
	public static final int AT         = Tokens.SYMBOL | 0x00090000;

	public static final int ARROW_LEFT         = Tokens.SYMBOL | 0x000A0000;
	public static final int ARROW_RIGHT        = Tokens.SYMBOL | 0x000B0000;
	public static final int DOUBLE_ARROW_RIGHT = Tokens.SYMBOL | 0x000C0000;

	public static final DyvilSymbols INSTANCE = new DyvilSymbols();

	@Override
	public int getSymbolType(String s)
	{
		switch (s)
		{
		case "_":
			return DyvilSymbols.UNDERSCORE;
		case "#":
			return DyvilSymbols.HASH;
		case "->":
		case "\u2192": // RIGHTWARDS ARROW
			return ARROW_RIGHT;
		case "<-":
		case "\u2190": // LEFTWARDS ARROW
			return ARROW_LEFT;
		case "=>":
		case "\u21D2": // RIGHTWARDS DOUBLE ARROW
			return DyvilSymbols.DOUBLE_ARROW_RIGHT;
		case "...":
		case "\u2026": // HORIZONTAL ELLIPSIS
			return DyvilSymbols.ELLIPSIS;
		case "@":
			return DyvilSymbols.AT;

		// Inlined from BaseSymbols.getSymbolType(String)
		case ":":
			return BaseSymbols.COLON;
		case "=":
			return BaseSymbols.EQUALS;
		}
		return 0;
	}

	@Override
	public int getKeywordType(String value)
	{
		return DyvilKeywords.getKeywordType(value);
	}

	@Override
	public String toString(int type)
	{
		switch (type)
		{
		case DyvilSymbols.UNDERSCORE:
			return "_";
		case DyvilSymbols.HASH:
			return "#";
		case DyvilSymbols.AT:
			return "@";
		case DyvilSymbols.ELLIPSIS:
		return "...";
		case DyvilSymbols.ARROW_LEFT:
			return "<-";
		case DyvilSymbols.ARROW_RIGHT:
			return "->";
		case DyvilSymbols.DOUBLE_ARROW_RIGHT:
			return "=>";
		}

		final String baseSymbol = BaseSymbols.INSTANCE.toString(type);
		return baseSymbol != null ? baseSymbol : DyvilKeywords.keywordToString(type);
	}

	@Override
	public int getLength(int type)
	{
		switch (type)
		{
		case UNDERSCORE:
		case HASH:
		case AT:
			return 1;
		case ARROW_LEFT:
		case ARROW_RIGHT:
		case DOUBLE_ARROW_RIGHT:
			return 2;
		case ELLIPSIS:
			return 3;
		}

		final String keyword = DyvilKeywords.keywordToString(type);
		return keyword == null ? 1 : keyword.length();
	}
}
