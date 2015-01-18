package dyvil.tools.compiler.lexer.token;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public interface IToken extends ICodePosition
{
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
