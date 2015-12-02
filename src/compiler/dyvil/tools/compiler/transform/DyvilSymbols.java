package dyvil.tools.compiler.transform;

import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Symbols;
import dyvil.tools.parsing.lexer.Tokens;

public final class DyvilSymbols implements Symbols
{
	public static final int HASH           = Tokens.SYMBOL | 0x00060000;
	public static final int WILDCARD       = Tokens.SYMBOL | 0x00070000;
	public static final int ARROW_OPERATOR = Tokens.SYMBOL | 0x00080000;
	public static final int ELLIPSIS       = Tokens.SYMBOL | 0x00090000;
	public static final int AT             = Tokens.SYMBOL | 0x000B0000;
	
	public static final DyvilSymbols INSTANCE = new DyvilSymbols();
	
	@Override
	public int getSymbolType(String s)
	{
		switch (s)
		{
		case "_":
			return DyvilSymbols.WILDCARD;
		case "#":
			return DyvilSymbols.HASH;
		case "=>":
		case "\u21D2": // Rightwards double arrow
			return DyvilSymbols.ARROW_OPERATOR;
		case "...":
		case "\u2026": // Horizontal ellipsis
			return DyvilSymbols.ELLIPSIS;
		case "@":
			return DyvilSymbols.AT;
		}
		return BaseSymbols.INSTANCE.getSymbolType(s);
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
		case DyvilSymbols.WILDCARD:
			return "_";
		case DyvilSymbols.HASH:
			return "#";
		case DyvilSymbols.AT:
			return "@";
		case DyvilSymbols.ARROW_OPERATOR:
			return "=>";
		case DyvilSymbols.ELLIPSIS:
			return "...";
		}
		String s = BaseSymbols.INSTANCE.toString(type);
		return s != null ? s : DyvilKeywords.keywordToString(type);
	}
	
	@Override
	public int getLength(int type)
	{
		switch (type)
		{
		case DyvilSymbols.WILDCARD:
			return 1;
		case DyvilSymbols.HASH:
			return 1;
		case DyvilSymbols.AT:
			return 1;
		case DyvilSymbols.ARROW_OPERATOR:
			return 2;
		case DyvilSymbols.ELLIPSIS:
			return 3;
		}
		String s = DyvilKeywords.keywordToString(type);
		if (s == null)
		{
			return 1;
		}
		return s.length();
	}
}
