package dyvil.tools.dpf;

import dyvil.tools.dpf.visitor.ListVisitor;
import dyvil.tools.dpf.visitor.NodeVisitor;
import dyvil.tools.dpf.visitor.StringInterpolationVisitor;
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
		while (this.tokens.hasNext() && this.parseNodeElement(visitor))
		{
		}
	}
	
	private boolean parseNodeElement(NodeVisitor visitor)
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
				return true;
			case BaseSymbols.DOT:
				this.tokens.next();
				this.parseNodeElement(visitor.visitNodeAccess(token.nameValue()));
				return true;
			case BaseSymbols.COLON:
			case BaseSymbols.EQUALS:
				this.tokens.next();
				this.parseValue(visitor.visitProperty(token.nameValue()));
				return true;
			}
		case BaseSymbols.CLOSE_CURLY_BRACKET:
			visitor.visitEnd();
			return false;
		}
		
		this.markers.add(new SyntaxError(token, "Invalid Node Element - Invalid " + token));
		return true;
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
		case Tokens.SINGLE_QUOTED_STRING:
		case Tokens.STRING:
			valueVisitor.visitString(token.stringValue());
			return;
		case Tokens.STRING_START:
			this.tokens.jump(token);
			this.parseStringInterpolation(valueVisitor.visitStringInterpolation());
			return;
		case Tokens.IDENTIFIER:
		case Tokens.LETTER_IDENTIFIER:
		case Tokens.SYMBOL_IDENTIFIER:
		case Tokens.SPECIAL_IDENTIFIER:
			valueVisitor.visitName(token.nameValue());
			return;
		case BaseSymbols.OPEN_SQUARE_BRACKET:
			this.parseList(valueVisitor.visitList());
			return;
		}
		
		this.markers.add(new SyntaxError(token, "Invalid Value - Invalid " + token));
	}
	
	private void parseList(ListVisitor visitor)
	{
		if (this.tokens.current().type() == BaseSymbols.CLOSE_SQUARE_BRACKET)
		{
			this.tokens.next();
			return;
		}
		
		while (this.tokens.hasNext())
		{
			this.parseValue(visitor.visitElement());
			
			IToken token = this.tokens.next();
			switch (token.type())
			{
			case BaseSymbols.COMMA:
				continue;
			case BaseSymbols.CLOSE_SQUARE_BRACKET:
				return;
			}
			
			this.markers.add(new SyntaxError(token, "Invalid List - ',' expected"));
		}
	}
	
	private void parseStringInterpolation(StringInterpolationVisitor visitor)
	{
		IToken token = this.tokens.current();
		visitor.visitStringPart(token.stringValue());
		this.tokens.next();
		
		while (this.tokens.hasNext())
		{
			this.parseValue(visitor.visitValue());
			
			token = this.tokens.next();
			switch (token.type())
			{
			case Tokens.STRING_PART:
				visitor.visitStringPart(token.stringValue());
				continue;
			case Tokens.STRING_END:
				visitor.visitStringPart(token.stringValue());
				visitor.visitEnd();
				return;
			}
		}
	}
}
