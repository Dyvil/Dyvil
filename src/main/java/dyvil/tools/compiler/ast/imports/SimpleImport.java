package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.CompilerState;

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
	
	@Override
	public void applyState(CompilerState state)
	{}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append("import ").append(this.theImport).append(";");
	}
}
