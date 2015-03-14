package dyvil.tools.compiler.lexer.token;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public interface IToken extends ICodePosition
{
	public String value();
	
	public Object object();
	
	public int type();
	
	public boolean equals(String value);
	
	public default boolean isInferred()
	{
		return false;
	}
	
	public void setIndex(int index);
	
	public int index();
	
	public IToken prev() throws SyntaxError;
	
	public IToken next() throws SyntaxError;
	
	public IToken getPrev();
	
	public IToken getNext();
	
	public void setPrev(IToken prev);
	
	public void setNext(IToken next);
	
	public boolean hasPrev();
	
	public boolean hasNext();
}
