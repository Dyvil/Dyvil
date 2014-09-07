package dyvil.tools.compiler.ast.imports;

import java.util.HashSet;
import java.util.Set;

public class MultiImport extends PackageImport
{
	public Set<String> theClasses = new HashSet();
	
	public MultiImport(String basePackage)
	{
		super(basePackage);
	}
	
	public void addClass(String name)
	{
		this.theClasses.add(name);
	}
	
	public boolean containsClass(String name)
	{
		return this.theClasses.contains(name);
	}
	
	@Override
	public boolean imports(String path)
	{
		int index = path.lastIndexOf('.');
		if (index != -1)
		{
			String packageName = path.substring(0, index);
			if (!this.thePackage.equals(path))
			{
				return false;
			}
			String className = path.substring(index + 1);
			return this.theClasses.contains(className);
		}
		return false;
	}
	
	@Override
	public boolean isClassName(String name)
	{
		return this.containsClass(name);
	}
}
