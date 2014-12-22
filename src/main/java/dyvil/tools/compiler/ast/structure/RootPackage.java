package dyvil.tools.compiler.ast.structure;

import dyvil.tools.compiler.ast.classes.IClass;

final class RootPackage extends Package
{
	RootPackage(Package parent, String name)
	{
		super(parent, name);
	}
	
	@Override
	public void addCompilationUnit(CompilationUnit unit)
	{
		this.units.add(unit);
	}
	
	@Override
	public void addSubPackage(Package pack)
	{
		this.subPackages.add(pack);
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
}