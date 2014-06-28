package dyvil.tools.compiler.lexer.token;

import dyvil.tools.compiler.lexer.SyntaxException;

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
	
	public String value() throws SyntaxException;
	
	public byte type() throws SyntaxException;
	
	public boolean equals(String value) throws SyntaxException;
	
	public boolean isType(byte type) throws SyntaxException;
	
	public int index() throws SyntaxException;
	
	public int start() throws SyntaxException;
	
	public int end() throws SyntaxException;
	
	public IToken prev() throws SyntaxException;
	
	public IToken next() throws SyntaxException;
	
	public boolean hasPrev();
	
	public boolean hasNext();
	
	public void setIndex(int index);
	
	public void setPrev(IToken prev);
	
	public void setNext(IToken next);
}
