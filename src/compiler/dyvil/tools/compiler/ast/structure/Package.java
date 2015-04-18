package dyvil.tools.compiler.ast.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.CodePosition;
import dyvil.tools.compiler.library.Library;

public class Package implements INamed, IContext
{
	public static RootPackage	rootPackage	= new RootPackage();
	
	public static Package		dyvil;
	public static Package		dyvilLang;
	public static Package		dyvilAnnotation;
	public static Package		dyvilFunction;
	public static Package		dyvilTuple;
	public static Package		dyvilLangLiteral;
	public static Package		java;
	public static Package		javaLang;
	public static Package		javaLangAnnotation;
	
	public Package				parent;
	public Name					name;
	public String				fullName;
	public String				internalName;
	
	public List<IDyvilHeader>	units		= new ArrayList();
	public Map<String, Package>	subPackages	= new HashMap();
	
	protected Package()
	{
	}
	
	public Package(Package parent, Name name)
	{
		this.name = name;
		this.parent = parent;
		
		if (parent == null || parent.name == null)
		{
			this.fullName = name.qualified;
			this.internalName = ClassFormat.packageToInternal(name.qualified) + "/";
		}
		else
		{
			this.fullName = parent.fullName + "." + name.qualified;
			this.internalName = parent.internalName + name.qualified + "/";
		}
	}
	
	public static void init()
	{
		dyvil = Library.dyvilLibrary.resolvePackage("dyvil");
		dyvilLang = dyvil.resolvePackage("lang");
		dyvilAnnotation = dyvil.resolvePackage("annotation");
		dyvilFunction = dyvil.resolvePackage("function");
		dyvilTuple = dyvil.resolvePackage("tuple");
		dyvilLangLiteral = dyvilLang.resolvePackage("literal");
		java = Library.javaLibrary.resolvePackage("java");
		javaLang = java.resolvePackage("lang");
		javaLangAnnotation = javaLang.resolvePackage("annotation");
	}
	
	// Name
	
	@Override
	public void setName(Name name)
	{
		this.name = name;
	}
	
	@Override
	public Name getName()
	{
		return this.name;
	}
	
	// Units
	
	public void addCompilationUnit(IDyvilHeader unit)
	{
		this.units.add(unit);
	}
	
	public void addSubPackage(Package pack)
	{
		this.subPackages.put(pack.name.qualified, pack);
	}
	
	public Package createSubPackage(String name)
	{
		Package pack = this.subPackages.get(name);
		if (pack != null)
		{
			return pack;
		}
		
		pack = new Package(this, Name.getQualified(name));
		this.subPackages.put(name, pack);
		return pack;
	}
	
	public void check(PackageDecl packageDecl, CodeFile file, MarkerList markers)
	{
		if (packageDecl == null)
		{
			if (this.fullName != null)
			{
				markers.add(new CodePosition(0, 0, 1), "package.missing");
			}
			return;
		}
		
		if (this.fullName == null)
		{
			markers.add(packageDecl.getPosition(), "package.default");
			return;
		}
		
		if (!this.fullName.equals(packageDecl.thePackage))
		{
			markers.add(packageDecl.getPosition(), "package.invalid");
		}
	}
	
	@Override
	public boolean isStatic()
	{
		return true;
	}
	
	@Override
	public IClass getThisClass()
	{
		return null;
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return this.resolvePackage(name.qualified);
	}
	
	public Package resolvePackage(String name)
	{
		return this.subPackages.get(name);
	}
	
	public IDyvilHeader resolveHeader(String name)
	{
		for (IDyvilHeader unit : this.units)
		{
			if (unit.getName().equals(name))
			{
				return unit;
			}
		}
		return null;
	}
	
	public IClass resolveClass(String name)
	{
		return this.resolveClass(Name.getQualified(name));
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		for (IDyvilHeader c : this.units)
		{
			if (c.getName().equals(name.qualified))
			{
				return c.getClass(name);
			}
		}
		
		return null;
	}
	
	@Override
	public IField resolveField(Name name)
	{
		return null;
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return 0;
	}
	
	@Override
	public String toString()
	{
		return this.fullName;
	}
}
