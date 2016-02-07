package dyvil.tools.compiler.ast.structure;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.collection.mutable.HashMap;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.external.ExternalHeader;
import dyvil.tools.compiler.ast.header.PackageDeclaration;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.ClassReader;
import dyvil.tools.compiler.backend.ObjectFormat;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.sources.DyvilFileType;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.CodePosition;

import java.io.InputStream;

public class Package implements INamed, IDefaultContext
{
	public static RootPackage rootPackage = new RootPackage();
	
	public static Package dyvil;
	public static Package dyvilAnnotation;
	public static Package dyvilArray;
	public static Package dyvilCollection;
	public static Package dyvilFunction;
	public static Package dyvilLang;
	public static Package dyvilLangLiteral;
	public static Package dyvilRef;
	public static Package dyvilRefSimple;
	public static Package dyvilTuple;
	public static Package dyvilUtil;
	public static Package dyvilx;
	public static Package dyvilxLang;
	public static Package dyvilxLangModel;
	public static Package dyvilxLangModelType;
	public static Package java;
	public static Package javaIO;
	public static Package javaLang;
	public static Package javaLangAnnotation;
	public static Package javaUtil;
	
	protected Package parent;
	protected Name    name;
	protected String  fullName;
	private   String  internalName;
	
	private   List<IClass>         classes     = new ArrayList<>();
	protected List<IDyvilHeader>   headers     = new ArrayList<>();
	protected Map<String, Package> subPackages = new HashMap<>();
	
	protected Package()
	{
	}
	
	public Package(Package parent, Name name)
	{
		this.name = name;
		this.parent = parent;
		
		if (parent == null || parent == rootPackage)
		{
			this.fullName = name.qualified;
			this.setInternalName(ClassFormat.packageToInternal(name.qualified) + "/");
		}
		else
		{
			this.fullName = parent.fullName + "." + name.qualified;
			this.setInternalName(parent.getInternalName() + name.qualified + "/");
		}
	}
	
	public static void init()
	{
		dyvil = rootPackage.resolvePackage("dyvil");
		dyvilAnnotation = dyvil.resolvePackage("annotation");
		dyvilArray = dyvil.resolvePackage("array");
		dyvilCollection = dyvil.resolvePackage("collection");
		dyvilFunction = dyvil.resolvePackage("function");
		dyvilLang = dyvil.resolvePackage("lang");
		dyvilLangLiteral = dyvilLang.resolvePackage("literal");
		dyvilRef = dyvil.resolvePackage("ref");
		dyvilRefSimple = dyvilRef.resolvePackage("simple");
		dyvilTuple = dyvil.resolvePackage("tuple");
		dyvilUtil = dyvil.resolvePackage("util");

		dyvilx = rootPackage.resolvePackage("dyvilx");
		dyvilxLang = dyvilx.resolvePackage("lang");
		dyvilxLangModel = dyvilxLang.resolvePackage("model");
		dyvilxLangModelType = dyvilxLangModel.resolvePackage("type");
		
		java = rootPackage.resolvePackage("java");
		javaIO = java.resolvePackage("io");
		javaLang = java.resolvePackage("lang");
		javaLangAnnotation = javaLang.resolvePackage("annotation");
		javaUtil = java.resolvePackage("util");
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
	
	public void setInternalName(String internalName)
	{
		this.internalName = internalName;
	}
	
	public String getInternalName()
	{
		return this.internalName;
	}
	
	public void addHeader(IDyvilHeader unit)
	{
		this.headers.add(unit);
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
	
	public void check(PackageDeclaration packageDecl, MarkerList markers)
	{
		if (packageDecl == null)
		{
			markers.add(Markers.semantic(CodePosition.ORIGIN, "package.missing"));
			return;
		}
		
		if (!this.fullName.equals(packageDecl.getPackage()))
		{
			markers.add(Markers.semantic(packageDecl.getPosition(), "package.invalid", this.fullName));
		}
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return this.resolvePackage(name.qualified);
	}
	
	public Package resolvePackage(String name)
	{
		Package pack = this.subPackages.get(name);
		if (pack != null)
		{
			return pack;
		}
		
		String internal = this.internalName + name;
		for (Library library : DyvilCompiler.config.libraries)
		{
			if (library.isSubPackage(internal))
			{
				return this.createSubPackage(name);
			}
		}
		
		return null;
	}
	
	public IDyvilHeader resolveHeader(String name)
	{
		return this.resolveHeader(Name.getQualified(name));
	}
	
	public IDyvilHeader resolveHeader(Name name)
	{
		for (IDyvilHeader unit : this.headers)
		{
			if (unit.getName() == name)
			{
				return unit;
			}
		}
		return this.loadHeader(name);
	}
	
	public IClass resolveClass(String name)
	{
		return this.resolveClass(Name.getQualified(name));
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		for (IDyvilHeader c : this.headers)
		{
			if (c.getName() == name)
			{
				return c.getClass(name);
			}
		}
		
		for (IClass c : this.classes)
		{
			if (c.getName() == name)
			{
				return c;
			}
		}
		
		String qualifiedName = name.qualified;
		// Check for inner / nested / anonymous classes
		int cashIndex = qualifiedName.indexOf('$');
		if (cashIndex >= 0)
		{
			Name firstName = Name.getQualified(qualifiedName.substring(0, cashIndex));
			Name lastName = Name.getQualified(qualifiedName.substring(cashIndex + 1));
			
			IClass c = this.resolveClass(firstName);
			if (c != null)
			{
				return c.resolveClass(lastName);
			}
		}
		
		return this.loadClass(name, qualifiedName);
	}
	
	private IClass loadClass(Name name, String qualifiedName)
	{
		String fileName = this.getInternalName() + qualifiedName + DyvilFileType.CLASS_EXTENSION;
		
		for (Library library : DyvilCompiler.config.libraries)
		{
			IClass iclass = this.loadClass(fileName, name, library);
			if (iclass != null)
			{
				return iclass;
			}
		}
		
		return null;
	}
	
	private IDyvilHeader loadHeader(Name name)
	{
		String fileName = this.getInternalName() + name.qualified + DyvilFileType.OBJECT_EXTENSION;
		for (Library library : DyvilCompiler.config.libraries)
		{
			IDyvilHeader header = this.loadHeader(fileName, name, library);
			if (header != null)
			{
				return header;
			}
		}
		
		return null;
	}
	
	public static IClass loadClass(String fileName, Name name)
	{
		for (Library library : DyvilCompiler.config.libraries)
		{
			InputStream is = library.getInputStream(fileName);
			if (is != null)
			{
				ExternalClass bclass = new ExternalClass(name);
				return ClassReader.loadClass(bclass, is, false);
			}
		}
		return null;
	}
	
	private IClass loadClass(String fileName, Name name, Library library)
	{
		InputStream is = library.getInputStream(fileName);
		if (is != null)
		{
			ExternalClass bclass = new ExternalClass(name);
			this.classes.add(bclass);
			return ClassReader.loadClass(bclass, is, false);
		}
		return null;
	}
	
	private IDyvilHeader loadHeader(String fileName, Name name, Library library)
	{
		InputStream is = library.getInputStream(fileName);
		if (is != null)
		{
			DyvilHeader header = new ExternalHeader(name);
			header.pack = this;
			this.headers.add(header);
			return ObjectFormat.read(is, header);
		}
		return null;
	}
	
	@Override
	public IType resolveType(Name name)
	{
		IClass iclass = this.resolveClass(name);
		if (iclass != null)
		{
			return new ClassType(iclass);
		}
		return null;
	}
	
	@Override
	public String toString()
	{
		return this.fullName;
	}
}
