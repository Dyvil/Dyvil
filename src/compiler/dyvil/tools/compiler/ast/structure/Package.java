package dyvil.tools.compiler.ast.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.CodePosition;
import dyvil.tools.compiler.library.Library;

public class Package implements INamed, IContext
{
	public static Package			rootPackage	= new RootPackage();
	
	public static Package			dyvil;
	public static Package			dyvilLang;
	public static Package			dyvilLangAnnotation;
	public static Package			dyvilLangFunction;
	public static Package			dyvilLangTuple;
	public static Package			java;
	public static Package			javaLang;
	public static Package			javaLangAnnotation;
	
	public Package					parent;
	public String					name;
	public String					fullName;
	public String					internalName;
	
	public List<DyvilFile>	units		= new ArrayList();
	public Map<String, IClass>		classes		= new HashMap();
	public Map<String, Package>		subPackages	= new HashMap();
	
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
		dyvil = Library.dyvilLibrary.resolvePackage("dyvil");
		dyvilLang = dyvil.resolvePackage("lang");
		dyvilLangAnnotation = dyvilLang.resolvePackage("annotation");
		dyvilLangFunction = dyvilLang.resolvePackage("function");
		dyvilLangTuple = dyvilLang.resolvePackage("tuple");
		java = Library.javaLibrary.resolvePackage("java");
		javaLang = java.resolvePackage("lang");
		javaLangAnnotation = javaLang.resolvePackage("annotation");
	}
	
	@Override
	public void setName(String name, String qualifiedName)
	{
		this.name = name;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void setQualifiedName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.name;
	}
	
	@Override
	public boolean isName(String name)
	{
		return this.name.equals(name);
	}
	
	public void addCompilationUnit(DyvilFile unit)
	{
		this.units.add(unit);
	}
	
	public void addClass(IClass iclass)
	{
		this.classes.put(iclass.getQualifiedName(), iclass);
	}
	
	public void addSubPackage(Package pack)
	{
		this.subPackages.put(pack.name, pack);
	}
	
	public Package createSubPackage(String name)
	{
		Package pack = this.subPackages.get(name);
		if (pack != null)
		{
			return pack;
		}
		
		pack = new Package(this, name);
		this.subPackages.put(name, pack);
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
		return this.subPackages.get(name);
	}
	
	public final Package resolvePackage2(String name)
	{
		return this.subPackages.get(name);
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.classes.get(name);
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, IArguments arguments)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, IArguments arguments)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public MethodMatch resolveConstructor(IArguments arguments)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, IArguments arguments)
	{
		throw new UnsupportedOperationException();
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
