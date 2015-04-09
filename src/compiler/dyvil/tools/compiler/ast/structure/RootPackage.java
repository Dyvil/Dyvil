package dyvil.tools.compiler.ast.structure;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.library.Library;

public final class RootPackage extends Package
{
	public RootPackage()
	{
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		Package pack = super.resolvePackage(name);
		if (pack != null)
		{
			return pack;
		}
		
		String internal = ClassFormat.internalToPackage(name);
		for (Library lib : DyvilCompiler.config.libraries)
		{
			pack = lib.resolvePackage(internal);
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
		return this.resolveInternalClass(ClassFormat.packageToInternal(name));
	}
	
	public IClass resolveInternalClass(String internal)
	{
		int index = internal.lastIndexOf('/');
		if (index == -1)
		{
			return super.resolveClass(internal);
		}
		String packageName = internal.substring(0, index);
		String className = internal.substring(index + 1);
		Package pack;
		for (Library lib : DyvilCompiler.config.libraries)
		{
			pack = lib.resolvePackage(packageName);
			if (pack != null)
			{
				IClass iclass = pack.resolveClass(className);
				if (iclass != null)
				{
					return iclass;
				}
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
