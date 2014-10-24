package dyvil.tools.compiler.ast.structure;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.type.Type;

public class Package implements IContext
{
	public static Package	rootPackage	= new Package(null, null);
	
	public Package			parent;
	public String			name;
	public String			fullName;
	
	public List<IClass>		classes		= new ArrayList();
	public List<Package>	subPackages	= new ArrayList();
	
	public Package(Package parent, String name)
	{
		this.name = name;
		this.parent = parent;
		if (parent == null || parent.fullName == null)
		{
			this.fullName = name;
		}
		else
		{
			this.fullName = parent.fullName + "." + name;
		}
	}
	
	public void addClass(IClass iclass)
	{
		this.classes.add(iclass);
	}
	
	public void addSubPackage(Package pack)
	{
		if (this != rootPackage)
		{
			rootPackage.subPackages.add(pack);
		}
		this.subPackages.add(pack);
	}
	
	public Package createSubPackage(String name)
	{
		for (Package pack : this.subPackages)
		{
			if (pack.name.equals(name))
			{
				return pack;
			}
		}
		
		Package pack = new Package(this, name);
		this.addSubPackage(pack);
		return pack;
	}
	
	public int check(PackageDecl packageDecl)
	{
		if (packageDecl == null)
		{
			if (this.fullName == null)
			{
				return 0;
			}
			// Missing Package Decl.
			return 1;
		}
		
		if (this.fullName == null)
		{
			// Existing Package Decl. in default package
			return 3;
		}
		else if (this.fullName.equals(packageDecl.thePackage))
		{
			return 0;
		}
		// Invalid Package Decl.
		return 2;
	}
	
	@Override
	public boolean isStatic()
	{
		return true;
	}
	
	@Override
	public Type getThisType()
	{
		return null;
	}
	
	public Package resolvePackage(String name)
	{
		for (Package pack : rootPackage.subPackages)
		{
			if (name.equals(pack.fullName))
			{
				return pack;
			}
		}
		
		return null;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		for (IClass iclass : this.classes)
		{
			if (name.equals(iclass.getName()))
			{
				return iclass;
			}
		}
		return null;
	}
	
	@Override
	public IField resolveField(String name)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IMethod resolveMethodName(String name)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IMethod resolveMethod(String name, Type... args)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString()
	{
		return this.fullName;
	}
}
