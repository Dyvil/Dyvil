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
	public static RootPackage rootPackage;

	public static Package dyvil;
	public static Package dyvilAnnotation;
	public static Package dyvilArray;
	public static Package dyvilCollection;
	public static Package dyvilCollectionRange;
	public static Package dyvilFunction;
	public static Package dyvilLang;
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

	protected final Package parent;

	protected Name   name;
	protected String fullName;
	protected String internalName;

	protected List<IClass>         classes     = new ArrayList<>();
	protected List<IDyvilHeader>   headers     = new ArrayList<>();
	protected Map<String, Package> subPackages = new HashMap<>();

	protected Package()
	{
		this.parent = rootPackage;
	}

	public Package(Package parent, Name name)
	{
		this.name = name;
		this.parent = parent;

		if (parent == null || parent == rootPackage)
		{
			this.fullName = name.qualified;
			this.internalName = name.qualified + '/';
		}
		else
		{
			this.fullName = parent.fullName + '.' + name.qualified;
			this.internalName = parent.getInternalName() + name.qualified + '/';
		}
	}

	public static void initRoot(DyvilCompiler compiler)
	{
		if (rootPackage != null && rootPackage.compiler == compiler)
		{
			return;
		}

		rootPackage = new RootPackage(compiler);
	}

	public static void init()
	{
		dyvil = rootPackage.resolvePackage("dyvil");
		dyvilAnnotation = dyvil.resolvePackage("annotation");
		dyvilArray = dyvil.resolvePackage("array");
		dyvilCollection = dyvil.resolvePackage("collection");
		dyvilCollectionRange = dyvilCollection.resolvePackage("range");
		dyvilFunction = dyvil.resolvePackage("function");
		dyvilLang = dyvil.resolvePackage("lang");
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

	public String getInternalName()
	{
		return this.internalName;
	}

	public void setInternalName(String internalName)
	{
		this.internalName = internalName;
	}

	public String getFullName()
	{
		return this.fullName;
	}

	public void setFullName(String fullName)
	{
		this.fullName = fullName;
	}

	// Units

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

		pack = new Package(this, Name.fromRaw(name));
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
	public DyvilCompiler getCompilationContext()
	{
		return rootPackage.compiler;
	}

	@Override
	public Package resolvePackage(Name name)
	{
		return this.resolvePackage(name.qualified);
	}

	public Package resolvePackage(String name)
	{
		final Package pack = this.subPackages.get(name);
		if (pack != null)
		{
			return pack;
		}

		String internal = this.internalName + name;
		for (Library library : rootPackage.compiler.config.libraries)
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
		return this.resolveHeader(Name.fromRaw(name));
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
		return this.resolveClass(Name.fromRaw(name));
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
			Name firstName = Name.fromRaw(qualifiedName.substring(0, cashIndex));
			Name lastName = Name.fromRaw(qualifiedName.substring(cashIndex + 1));

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
		final String fileName = this.getInternalName() + qualifiedName + DyvilFileType.CLASS_EXTENSION;

		for (Library library : rootPackage.compiler.config.libraries)
		{
			final IClass iclass = this.loadClass(fileName, name, library);
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
		for (Library library : rootPackage.compiler.config.libraries)
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
		for (Library library : rootPackage.compiler.config.libraries)
		{
			final InputStream inputStream = library.getInputStream(fileName);
			if (inputStream != null)
			{
				final ExternalClass externalClass = new ExternalClass(name);
				return ClassReader.loadClass(rootPackage.compiler, externalClass, inputStream);
			}
		}
		return null;
	}

	private IClass loadClass(String fileName, Name name, Library library)
	{
		final InputStream inputStream = library.getInputStream(fileName);
		if (inputStream != null)
		{
			final ExternalClass externalClass = new ExternalClass(name);
			this.classes.add(externalClass);
			return ClassReader.loadClass(rootPackage.compiler, externalClass, inputStream);
		}
		return null;
	}

	private IDyvilHeader loadHeader(String fileName, Name name, Library library)
	{
		InputStream inputStream = library.getInputStream(fileName);
		if (inputStream != null)
		{
			final DyvilHeader header = new ExternalHeader(rootPackage.compiler, name);
			header.pack = this;
			this.headers.add(header);
			return ObjectFormat.read(rootPackage.compiler, inputStream, header);
		}
		return null;
	}

	@Override
	public String toString()
	{
		return this.fullName;
	}
}
