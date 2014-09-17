package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class SimpleImport extends ASTObject implements IImport
{
	private String			theImport;
	
	public SimpleImport(ICodePosition position)
	{
		this.position = position;
	}
	
	public SimpleImport(ICodePosition position, String theImport)
	{
		this.position = position;
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
	public SimpleImport applyState(CompilerState state)
	{
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append("import ").append(this.theImport).append(";");
	}
}
