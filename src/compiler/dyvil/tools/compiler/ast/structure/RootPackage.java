package dyvil.tools.compiler.ast.structure;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.library.Library;

public final class RootPackage extends Package
{
	public RootPackage()
	{
		super(null, null);
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		Package pack = super.resolvePackage(name);
		if (pack != null)
		{
			return pack;
		}
		
		for (Library lib : DyvilCompiler.config.libraries)
		{
			pack = lib.resolvePackage(name);
			if (pack != null)
			{
				return pack;
			}
		}
		
		return null;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		int index = name.lastIndexOf('.');
		if (index == -1)
		{
			return super.resolveClass(name);
		}
		else
		{
			String packageName = name.substring(0, index);
			String className = name.substring(index + 1);
			Package pack = this.resolvePackage(packageName);
			if (pack != null)
			{
				return pack.resolveClass(className);
			}
		}
		
		return null;
	}
	
	@Override
	public String toString()
	{
		return "<default package>";
	}
}
