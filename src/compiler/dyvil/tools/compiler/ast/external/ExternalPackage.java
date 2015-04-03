package dyvil.tools.compiler.ast.external;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.ClassReader;
import dyvil.tools.compiler.library.Library;

public class ExternalPackage extends Package
{
	public Library			library;
	private List<IClass>	classes	= new ArrayList();
	
	public ExternalPackage(Package parent, Name name, Library library)
	{
		super(parent, name);
		this.library = library;
	}
	
	@Override
	public Package createSubPackage(String name)
	{
		Package pack = this.subPackages.get(name);
		if (pack != null)
		{
			return pack;
		}
		
		pack = new ExternalPackage(this, Name.getQualified(name), this.library);
		this.subPackages.put(name, pack);
		return pack;
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		Package pack = super.resolvePackage(name);
		if (pack != null)
		{
			return pack;
		}
		
		String name1 = this.fullName + "." + name;
		pack = this.library.resolvePackage(name1);
		if (pack != null)
		{
			this.addSubPackage(pack);
			return pack;
		}
		
		return null;
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		for (IClass iclass : this.classes)
		{
			if (iclass.getName() == name)
			{
				return iclass;
			}
		}
		IClass iclass = super.resolveClass(name);
		if (iclass != null)
		{
			return iclass;
		}
		return this.loadClass(name.qualified);
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		for (IClass iclass : this.classes)
		{
			if (name.equals(iclass.getName().qualified))
			{
				return iclass;
			}
		}
		
		return this.loadClass(name);
	}
	
	private IClass loadClass(String name)
	{
		synchronized (this)
		{
			InputStream is = this.library.getInputStream(this.internalName + name + ".class");
			if (is != null)
			{
				ExternalClass bclass = new ExternalClass(Name.getQualified(name));
				this.classes.add(bclass);
				return ClassReader.loadClass(bclass, is, false);
			}
		}
		return null;
	}
}
