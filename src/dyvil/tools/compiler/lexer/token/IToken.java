package dyvil.tools.compiler.lexer.token;

import dyvil.tools.compiler.lexer.SyntaxError;

public interface IToken
{
	public byte	TYPE_IDENTIFIER		= 1;
	public byte	TYPE_SYMBOL			= 2;
	public byte	TYPE_BRACKET		= 3;
	
	public byte	TYPE_INT			= 4;
	public byte	TYPE_INT_HEX		= 5;
	public byte	TYPE_INT_BIN		= 6;
	
	public byte	TYPE_FLOAT			= 7;
	public byte	TYPE_FLOAT_HEX		= 8;
	
	public byte	TYPE_STRING			= 9;
	public byte	TYPE_CHAR			= 10;
	
	public byte	TYPE_LINE_COMMENT	= 11;
	public byte	TYPE_BLOCK_COMMENT	= 12;
	
	public String value() throws SyntaxError;
	
	public byte type() throws SyntaxError;
	
	public boolean equals(String value) throws SyntaxError;
	
	public boolean isType(byte type) throws SyntaxError;
	
	public int index() throws SyntaxError;
	
	public int start() throws SyntaxError;
	
	public int end() throws SyntaxError;
	
	public IToken prev() throws SyntaxError;
	
	public IToken next() throws SyntaxError;
	
	public boolean hasPrev();
	
	public boolean hasNext();
	
	public void setIndex(int index);
	
	public void setPrev(IToken prev);
	
	public void setNext(IToken next);
}
