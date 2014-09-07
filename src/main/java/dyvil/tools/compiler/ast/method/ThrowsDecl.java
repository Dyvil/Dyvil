package dyvil.tools.compiler.ast.method;


public class ThrowsDecl
{
	private String exception;
	
	public ThrowsDecl(String exception)
	{
		
	}
	
	public void setException(String exception)
	{
		this.exception = exception;
	}
	
	public String getException()
	{
		return this.exception;
	}
}
