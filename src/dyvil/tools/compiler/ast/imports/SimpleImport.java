package dyvil.tools.compiler.ast.imports;

public class SimpleImport implements IImport
{
	private String			theImport;
	
	public SimpleImport(String theImport)
	{
		this.theImport = theImport;
	}
	
	public void setImport(String theImport)
	{
		this.theImport = theImport;
	}
	
	public String getImport()
	{
		return this.theImport;
	}
	
	@Override
	public boolean imports(String className)
	{
		return this.theImport.equals(className);
	}
	
	@Override
	public boolean isClassName(String name)
	{
		return this.theImport.endsWith(name);
	}
}
