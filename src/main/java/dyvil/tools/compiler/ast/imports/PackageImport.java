package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;

public class PackageImport extends ASTObject implements IImport
{
	protected String thePackage;
	
	public PackageImport(String thePackage)
	{
		this.thePackage = thePackage;
	}
	
	public void setPackage(String thePackage)
	{
		this.thePackage = thePackage;
	}
	
	public String getPackage()
	{
		return this.thePackage;
	}
	
	@Override
	public boolean imports(String path)
	{
		int index = path.lastIndexOf('.');
		if (index != -1)
		{
			String s1 = path.substring(0, index);
			return this.thePackage.equals(path);
		}
		return false;
	}
	
	@Override
	public boolean isClassName(String name)
	{
		return false;
	}
	
	@Override
	public PackageImport applyState(CompilerState state)
	{
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append("import ").append(this.thePackage).append("_;");
	}
}
