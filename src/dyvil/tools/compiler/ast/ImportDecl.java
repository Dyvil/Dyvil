package dyvil.tools.compiler.ast;


public class ImportDecl
{
	public static final int PHASE_INIT = 0;
	public static final int PHASE_PACKAGE = 1;
	public static final int PHASE_ASTERISK = 2;
	
	private String			theImport;
	
	private boolean			isStatic;
	private boolean			asterisk;
	
	public void setImport(String theImport)
	{
		this.theImport = theImport;
	}
	
	public void setStatic()
	{
		this.isStatic = true;
	}
	
	public void setAsterisk()
	{
		this.asterisk = true;
	}
	
	public String getImport()
	{
		return this.theImport;
	}
	
	public boolean isAsterisk()
	{
		return this.asterisk;
	}
	
	public boolean isStatic()
	{
		return this.isStatic;
	}
}
