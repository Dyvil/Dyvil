package dyvil.tools.compiler.lexer.token;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public interface IToken extends ICodePosition
{
	// MODIFIERS
	public int	MOD_DEC				= 0x00000000;
	public int	MOD_BIN				= 0x00010000;
	public int	MOD_OCT				= 0x00020000;
	public int	MOD_HEX				= MOD_BIN | MOD_OCT;
	
	public int	MOD_LINE			= 0x00010000;
	public int	MOD_BLOCK			= 0x00020000;
	
	public int	MOD_OPEN			= 0x00010000;
	public int	MOD_CLOSE			= 0x00020000;
	
	public int	MOD_LETTER			= 0x00010000;
	public int	MOD_SYMBOL			= 0x00020000;
	public int	MOD_DOTS			= 0x00040000;
	
	// TYPES
	public int	TYPE_IDENTIFIER		= 0x00000001;
	public int	TYPE_KEYWORD		= 0x00000002;
	public int	TYPE_SYMBOL			= 0x00000004;
	public int	TYPE_BRACKET		= 0x00000008;
	
	public int	TYPE_OPEN_BRACKET	= TYPE_BRACKET | MOD_OPEN;
	public int	TYPE_CLOSE_BRACKET	= TYPE_BRACKET | MOD_CLOSE;
	
	public int	TYPE_INT			= 0x00000010;
	public int	TYPE_LONG			= 0x00000020;
	public int	TYPE_FLOAT			= 0x00000040;
	public int	TYPE_DOUBLE			= 0x00000080;
	
	public int	TYPE_STRING			= 0x00000100;
	public int	TYPE_STRING_2		= TYPE_STRING | MOD_HEX;
	public int	TYPE_CHAR			= 0x00000200;
	
	public int	TYPE_COMMENT		= 0x00001000;
	public int	TYPE_LINE_COMMENT	= TYPE_COMMENT | MOD_LINE;
	public int	TYPE_BLOCK_COMMENT	= TYPE_COMMENT | MOD_BLOCK;
	
	public int	KEYWORD_WC			= TYPE_KEYWORD | 0x00010000;
	public int	KEYWORD_AT			= TYPE_KEYWORD | 0x00020000;
	public int	KEYWORD_NULL		= TYPE_KEYWORD | 0x00030000;
	public int	KEYWORD_TRUE		= TYPE_KEYWORD | 0x00040000;
	public int	KEYWORD_FALSE		= TYPE_KEYWORD | 0x00050000;
	public int	KEYWORD_THIS		= TYPE_KEYWORD | 0x00060000;
	public int	KEYWORD_SUPER		= TYPE_KEYWORD | 0x00070000;
	public int	KEYWORD_NEW			= TYPE_KEYWORD | 0x00080000;
	public int	KEYWORD_RETURN		= TYPE_KEYWORD | 0x00090000;
	public int	KEYWORD_IF			= TYPE_KEYWORD | 0x000A0000;
	public int	KEYWORD_ELSE		= TYPE_KEYWORD | 0x000B0000;
	public int	KEYWORD_WHILE		= TYPE_KEYWORD | 0x000C0000;
	public int	KEYWORD_DO			= TYPE_KEYWORD | 0x000D0000;
	public int	KEYWORD_FOR			= TYPE_KEYWORD | 0x000E0000;
	public int	KEYWORD_SWITCH		= TYPE_KEYWORD | 0x000F0000;
	public int	KEYWORD_CASE		= TYPE_KEYWORD | 0x00100000;
	
	public String value() throws SyntaxError;
	
	public Object object() throws SyntaxError;
	
	public int type() throws SyntaxError;
	
	public boolean equals(String value) throws SyntaxError;
	
	public boolean isType(int type) throws SyntaxError;
	
	public boolean isAnyType(int types) throws SyntaxError;
	
	public int index() throws SyntaxError;
	
	public IToken prev() throws SyntaxError;
	
	public IToken next() throws SyntaxError;
	
	public boolean match(Object object) throws SyntaxError;
	
	public boolean match(Object... objects) throws SyntaxError;
	
	public boolean hasPrev();
	
	public boolean hasNext();
	
	public void setIndex(int index);
	
	public void setPrev(IToken prev);
	
	public void setNext(IToken next);
}
