package dyvil.tools.dpf;

import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.DyvilLexer;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.token.IToken;

public class DPFParser
{
	private String			code;
	private TokenIterator	tokens;
	private MarkerList		markers;
	
	public DPFParser(MarkerList markers, String code)
	{
		this.code = code;
		this.markers = markers;
	}
	
	public void accept(NodeVisitor visitor)
	{
		if (this.tokens == null)
		{
			DyvilLexer lexer = new DyvilLexer(this.markers, BaseSymbols.INSTANCE);
			this.tokens = lexer.tokenize(this.code);
		}
		
		while (this.tokens.hasNext())
		{
			this.parseNode(visitor);
		}
	}
	
	private void parseNode(NodeVisitor visitor)
	{
		IToken token = this.tokens.next();
		switch (token.type()) {
		
		}
	}
}
