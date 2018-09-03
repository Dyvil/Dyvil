package dyvilx.tools.compiler.ast.structure;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.collection.mutable.HashMap;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.consumer.IClassConsumer;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.external.ExternalClass;
import dyvilx.tools.compiler.ast.external.ExternalHeader;
import dyvilx.tools.compiler.ast.header.AbstractHeader;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.header.PackageDeclaration;
import dyvilx.tools.compiler.ast.member.Named;
import dyvilx.tools.compiler.backend.classes.ExternalClassVisitor;
import dyvilx.tools.compiler.backend.ObjectFormat;
import dyvilx.tools.compiler.library.Library;
import dyvilx.tools.compiler.sources.DyvilFileType;
import dyvilx.tools.compiler.util.Markers;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.InputStream;

public class Package implements Named, IDefaultContext, IClassConsumer
{
	public static RootPackage rootPackage;

	public static Package dyvil;
	public static Package dyvilAnnotation;
	public static Package dyvilArray;
	public static Package dyvilCollection;
	public static Package dyvilCollectionRange;
	public static Package dyvilFunction;
	public static Package dyvilLang;
	public static Package dyvilLangInternal;
	public static Package dyvilRef;
	public static Package dyvilRefSimple;
	public static Package dyvilTuple;
	public static Package dyvilUtil;
	public static Package dyvilReflectTypes;
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
	protected List<IHeaderUnit>    headers     = new ArrayList<>();
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
		dyvilLangInternal = dyvilLang.resolvePackage("internal");
		dyvilRef = dyvil.resolvePackage("ref");
		dyvilRefSimple = dyvilRef.resolvePackage("simple");
		dyvilTuple = dyvil.resolvePackage("tuple");
		dyvilUtil = dyvil.resolvePackage("util");

		dyvilReflectTypes = dyvil.resolvePackage("reflect").resolvePackage("types");

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

	public void addHeader(IHeaderUnit unit)
	{
		this.headers.add(unit);
	}

	public void addSubPackage(Package pack)
	{
		this.subPackages.put(pack.name.qualified, pack);
	}

	@Override
	public void addClass(IClass theClass)
	{
		this.classes.add(theClass);
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
			markers.add(Markers.semantic(SourcePosition.ORIGIN, "package.missing"));
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

	public IHeaderUnit resolveHeader(String name)
	{
		return this.resolveHeader(Name.fromRaw(name));
	}

	@Override
	public IHeaderUnit resolveHeader(Name name)
	{
		for (IHeaderUnit unit : this.headers)
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
		for (IClass c : this.classes)
		{
			if (c.getName() == name)
			{
				return c;
			}
		}

		for (IHeaderUnit h : this.headers)
		{
			final IClass c;
			// TODO Maybe remove the first check
			if (h.getName() == name && (c = h.getClass(name)) != null)
			{
				return c;
			}
		}

		String qualifiedName = name.qualified;
		// Check for inner / nested / anonymous classes
		final int cashIndex = qualifiedName.lastIndexOf('$');
		if (cashIndex < 0)
		{
			return this.loadClass(name, qualifiedName);
		}

		final Name outerName = Name.fromRaw(qualifiedName.substring(0, cashIndex));
		final Name innerName = Name.fromRaw(qualifiedName.substring(cashIndex + 1));

		final IClass outerClass = this.resolveClass(outerName);
		if (outerClass != null)
		{
			return outerClass.resolveClass(innerName);
		}

		return this.loadClass(name, qualifiedName);
	}

	private IClass loadClass(Name name, String qualifiedName)
	{
		final String fileName = this.getInternalName() + qualifiedName + DyvilFileType.CLASS_EXTENSION;
		return loadClass(fileName, name, this);
	}

	public static IClass loadClass(String fileName, Name name, IClassConsumer consumer)
	{
		final DyvilCompiler compiler = rootPackage.compiler;
		for (Library library : compiler.config.libraries)
		{
			final InputStream inputStream = library.getInputStream(fileName);
			if (inputStream != null)
			{
				final ExternalClass externalClass = new ExternalClass(name);
				consumer.addClass(externalClass);
				return ExternalClassVisitor.loadClass(compiler, externalClass, inputStream);
			}
		}
		return null;
	}

	private IHeaderUnit loadHeader(Name name)
	{
		String fileName = this.getInternalName() + name.qualified + DyvilFileType.OBJECT_EXTENSION;
		for (Library library : rootPackage.compiler.config.libraries)
		{
			IHeaderUnit header = this.loadHeader(fileName, name, library);
			if (header != null)
			{
				return header;
			}
		}

		return null;
	}

	private IHeaderUnit loadHeader(String fileName, Name name, Library library)
	{
		InputStream inputStream = library.getInputStream(fileName);
		if (inputStream != null)
		{
			final AbstractHeader header = new ExternalHeader(name, this);
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
