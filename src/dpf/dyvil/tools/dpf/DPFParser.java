package dyvil.tools.dpf;

import dyvil.tools.dpf.visitor.NodeVisitor;
import dyvil.tools.dpf.visitor.ValueVisitor;
import dyvil.tools.parsing.TokenIterator;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.DyvilLexer;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.marker.SyntaxError;
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
		
		this.parseNodeElements(visitor);
	}
	
	private void parseNodeElements(NodeVisitor visitor)
	{
		while (this.tokens.hasNext())
		{
			this.parseNodeElement(visitor);
		}
	}
	
	private void parseNodeElement(NodeVisitor visitor)
	{
		IToken token = this.tokens.next();
		switch (token.type())
		{
		case Tokens.LETTER_IDENTIFIER:
			switch (token.next().type())
			{
			case BaseSymbols.OPEN_CURLY_BRACKET:
				this.tokens.next();
				this.parseNodeElements(visitor.visitNode(token.nameValue()));
				return;
			case BaseSymbols.EQUALS:
				this.tokens.next();
				this.parseValue(visitor.visitProperty(token.nameValue()));
				return;
			}
		case BaseSymbols.CLOSE_CURLY_BRACKET:
			return;
		}
		
		this.markers.add(new SyntaxError(token, "Invalid Node Element - Invalid " + token));
	}
	
	private void parseValue(ValueVisitor valueVisitor)
	{
		IToken token = this.tokens.next();
		switch (token.type())
		{
		case Tokens.INT:
			valueVisitor.visitInt(token.intValue());
			return;
		case Tokens.LONG:
			valueVisitor.visitLong(token.longValue());
			return;
		case Tokens.FLOAT:
			valueVisitor.visitFloat(token.floatValue());
			return;
		case Tokens.DOUBLE:
			valueVisitor.visitDouble(token.doubleValue());
			return;
		case Tokens.STRING:
			valueVisitor.visitString(token.stringValue());
			return;
		case Tokens.IDENTIFIER:
		case Tokens.SYMBOL_IDENTIFIER:
		case Tokens.SPECIAL_IDENTIFIER:
			valueVisitor.visitName(token.nameValue());
			return;
		}
		
		this.markers.add(new SyntaxError(token, "Invalid Value - Invalid " + token));
	}
}
