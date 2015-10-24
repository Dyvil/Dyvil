package dyvil.tools.dpf;

import java.io.File;

import dyvil.io.FileUtils;
import dyvil.tools.dpf.ast.DPFFile;
import dyvil.tools.dpf.visitor.*;
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
	
	public static DPFFile parse(File file)
	{
		return parse(FileUtils.read(file));
	}
	
	public static DPFFile parse(String code)
	{
		MarkerList markers = new MarkerList();
		DPFFile file = new DPFFile();
		new DPFParser(markers, code).accept(file);
		return file;
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
			case BaseSymbols.COMMA:
			case BaseSymbols.SEMICOLON:
				return true;
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
			this.parseStringInterpolation(valueVisitor.visitStringInterpolation());
			return;
		case Tokens.IDENTIFIER:
		case Tokens.LETTER_IDENTIFIER:
		case Tokens.SYMBOL_IDENTIFIER:
		case Tokens.SPECIAL_IDENTIFIER:
		{
			IToken next = token.next();
			if (next.type() == BaseSymbols.DOT)
			{
				this.parseAccessSequence(valueVisitor);
				return;
			}
			if (token.nameValue().unqualified.equals("-"))
			{
				switch (next.type())
				{
				case Tokens.INT:
					this.tokens.next();
					valueVisitor.visitInt(-next.intValue());
					return;
				case Tokens.LONG:
					this.tokens.next();
					valueVisitor.visitLong(-next.longValue());
					return;
				case Tokens.FLOAT:
					this.tokens.next();
					valueVisitor.visitFloat(-next.floatValue());
					return;
				case Tokens.DOUBLE:
					this.tokens.next();
					valueVisitor.visitDouble(-next.doubleValue());
					return;
				}
			}
			valueVisitor.visitName(token.nameValue());
			return;
		}
		case BaseSymbols.OPEN_SQUARE_BRACKET:
			this.parseList(valueVisitor.visitList());
			return;
		case BaseSymbols.OPEN_CURLY_BRACKET:
			this.parseMap(valueVisitor.visitMap());
			return;
		}
		
		this.markers.add(new SyntaxError(token, "Invalid Value - Invalid " + token));
	}
	
	private void parseList(ListVisitor visitor)
	{
		if (this.tokens.lastReturned().type() == BaseSymbols.CLOSE_SQUARE_BRACKET)
		{
			this.tokens.next();
			visitor.visitEnd();
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
				visitor.visitEnd();
				return;
			}
			
			this.markers.add(new SyntaxError(token, "Invalid List - ',' expected"));
		}
	}
	
	private void parseMap(MapVisitor visitor)
	{
		if (this.tokens.lastReturned().type() == BaseSymbols.CLOSE_SQUARE_BRACKET)
		{
			this.tokens.next();
			visitor.visitEnd();
			return;
		}
		
		while (this.tokens.hasNext())
		{
			this.parseValue(visitor.visitKey());
			
			IToken token = this.tokens.next();
			if (token.type() != BaseSymbols.COLON)
			{
				this.markers.add(new SyntaxError(token, "Invalid Map - ':' expected"));
			}
			
			this.parseValue(visitor.visitValue());
			
			token = this.tokens.next();
			switch (token.type())
			{
			case BaseSymbols.COMMA:
				continue;
			case BaseSymbols.CLOSE_CURLY_BRACKET:
				visitor.visitEnd();
				return;
			}
			
			this.markers.add(new SyntaxError(token, "Invalid Map - ',' expected"));
		}
	}
	
	private void parseStringInterpolation(StringInterpolationVisitor visitor)
	{
		IToken token = this.tokens.lastReturned();
		visitor.visitStringPart(token.stringValue());
		
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
			
			this.markers.add(new SyntaxError(token, "Invalid String Interpolation - String expected"));
		}
	}
	
	private void parseAccessSequence(ValueVisitor visitor)
	{
		IToken token = this.tokens.lastReturned();
		while (token.hasNext() && token.next().type() == BaseSymbols.DOT)
		{
			token = token.next().next();
		}
		
		this.tokens.jump(token.next());
		while (token.prev().type() == BaseSymbols.DOT)
		{
			visitor = visitor.visitValueAccess(token.nameValue());
			
			token = token.prev().prev();
		}
		
		visitor.visitName(token.nameValue());
	}
}
