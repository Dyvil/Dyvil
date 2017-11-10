package dyvilx.tools.gensrc.lexer;

import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Symbols;

public class GenSrcSymbols implements Symbols
{
	public static final GenSrcSymbols INSTANCE = new GenSrcSymbols();

	public static final int ARROW_LEFT = DyvilSymbols.ARROW_LEFT;

	public static final int CONST    = DyvilKeywords.CONST;
	public static final int ELSE     = DyvilKeywords.ELSE;
	public static final int FOR      = DyvilKeywords.FOR;
	public static final int FUNC     = DyvilKeywords.FUNC;
	public static final int IF       = DyvilKeywords.IF;
	public static final int IMPORT   = DyvilKeywords.IMPORT;
	public static final int LET      = DyvilKeywords.LET;
	public static final int TEMPLATE = DyvilKeywords.TEMPLATE;
	public static final int VAR      = DyvilKeywords.VAR;

	private GenSrcSymbols()
	{
	}

	@Override
	public int getKeywordType(String s)
	{
		switch (s)
		{
		// @formatter:off
		case "const": return CONST;
		case "else": return ELSE;
		case "for": return FOR;
		case "func": return FUNC;
		case "if": return IF;
		case "import": return IMPORT;
		case "let": return LET;
		case "template": return TEMPLATE;
		case "var": return VAR;
		// @formatter:on
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
		// @formatter:off
		case ARROW_LEFT: return "<-";
		// Keywords
		case CONST: return "const";
		case ELSE: return "else";
		case FOR: return "for";
		case FUNC: return "func";
		case IF: return "if";
		case IMPORT: return "import";
		case LET: return "let";
		case TEMPLATE: return "template";
		case VAR: return "var";
		// @formatter:on
		}
		return BaseSymbols.INSTANCE.toString(type);
	}

	@Override
	public int getLength(int type)
	{
		return this.toString(type).length();
	}
}
