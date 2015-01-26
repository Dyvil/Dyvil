package dyvil.tools.compiler.ast.structure;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.CodePosition;
import dyvil.tools.compiler.library.Library;

public class Package implements IContext
{
	public static Package			rootPackage	= new RootPackage(null, null);
	
	public static Package			dyvilLang;
	public static Package			dyvilLangTuple;
	public static Package			dyvilLangAnnotation;
	public static Package			javaLang;
	public static Package			javaLangAnnotation;
	
	public Package					parent;
	public String					name;
	public String					fullName;
	public String					internalName;
	
	public boolean					isExternal;
	
	public List<CompilationUnit>	units		= new ArrayList();
	public List<IClass>				classes		= new ArrayList();
	public List<Package>			subPackages	= new ArrayList();
	
	public Package(Package parent, String name)
	{
		this.name = name;
		this.parent = parent;
		
		if (parent == null || parent.name == null)
		{
			if (name != null)
			{
				this.fullName = name;
				this.internalName = ClassFormat.packageToInternal(name) + "/";
			}
		}
		else
		{
			this.fullName = parent.fullName + "." + name;
			this.internalName = parent.internalName + name + "/";
		}
	}
	
	public static void init()
	{
		dyvilLang = Library.dyvilLibrary.resolvePackage("dyvil.lang");
		dyvilLangAnnotation = Library.dyvilLibrary.resolvePackage("dyvil.lang.annotation");
		dyvilLangTuple = Library.dyvilLibrary.resolvePackage("dyvil.lang.tuple");
		javaLang = Library.javaLibrary.resolvePackage("java.lang");
		javaLangAnnotation = Library.javaLibrary.resolvePackage("java.lang.annotation");
	}
	
	public void addCompilationUnit(CompilationUnit unit)
	{
		rootPackage.units.add(unit);
		this.units.add(unit);
	}
	
	public void addClass(IClass iclass)
	{
		this.classes.add(iclass);
	}
	
	public void addSubPackage(Package pack)
	{
		rootPackage.subPackages.add(pack);
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
	
	public void check(PackageDecl packageDecl, CodeFile file, List<Marker> markers)
	{
		if (packageDecl == null)
		{
			if (this.fullName != null)
			{
				markers.add(Markers.create(new CodePosition(file, 0, 1, 0, 1), "package.missing"));
			}
			return;
		}
		
		if (this.fullName == null)
		{
			markers.add(Markers.create(packageDecl.getPosition(), "package.default"));
			return;
		}
		
		if (!this.fullName.equals(packageDecl.thePackage))
		{
			markers.add(Markers.create(packageDecl.getPosition(), "package.invalid"));
		}
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
	
	@Override
	public Package resolvePackage(String name)
	{
		for (Package pack : this.subPackages)
		{
			if (pack.name.equals(name))
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
	public FieldMatch resolveField(String name)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, List<IValue> arguments)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, List<IValue> arguments)
	{
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString()
	{
		return this.fullName;
	}
}
