package dyvilx.tools.compiler.ast.structure;

import dyvil.collection.List;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayList;
import dyvil.collection.mutable.HashMap;
import dyvil.lang.Name;
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
import dyvilx.tools.compiler.backend.ObjectFormat;
import dyvilx.tools.compiler.backend.classes.ExternalClassVisitor;
import dyvilx.tools.compiler.library.Library;
import dyvilx.tools.compiler.sources.DyvilFileType;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.InputStream;
import java.util.stream.Stream;

public class Package implements Named, IDefaultContext, IClassConsumer
{
	// =============== Static Fields ===============

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

	// =============== Fields ===============

	protected final Package parent;

	protected Name   name;
	protected String fullName;
	protected String internalName;

	protected List<IClass>         classes     = new ArrayList<>();
	protected List<IHeaderUnit>    headers     = new ArrayList<>();
	protected Map<String, Package> subPackages = new HashMap<>();

	// =============== Constructors ===============

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

	// =============== Static Methods ===============

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

	// =============== Properties ===============

	// --------------- Name ---------------

	@Override
	public Name getName()
	{
		return this.name;
	}

	@Override
	public void setName(Name name)
	{
		this.name = name;
	}

	// --------------- Internal Name ---------------

	public String getInternalName()
	{
		return this.internalName;
	}

	public void setInternalName(String internalName)
	{
		this.internalName = internalName;
	}

	// --------------- Full Name ---------------

	public String getFullName()
	{
		return this.fullName;
	}

	public void setFullName(String fullName)
	{
		this.fullName = fullName;
	}

	// =============== Methods ===============

	@Override
	public DyvilCompiler getCompilationContext()
	{
		return rootPackage.compiler;
	}

	// TODO move to PackageDeclaration
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

	// --------------- Sub-Packages ---------------

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

	// --------------- Headers ---------------

	public void addHeader(IHeaderUnit unit)
	{
		this.headers.add(unit);
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

	public IHeaderUnit resolveHeader(String name)
	{
		return this.resolveHeader(Name.fromRaw(name));
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

	// --------------- Classes ---------------

	/**
	 * Lists internal names (including slash-separated package) of all external classes (i.e. classes from libraries) in
	 * this package. The classes are not loaded; the search is only file-based.
	 * <p/>
	 * The results are not guaranteed to match the exact case of the actual class names due to filesystem restrictions.
	 *
	 * @return the internal names of all classes in this package
	 */
	public Stream<String> listExternalClassNames()
	{
		final String internalName = this.getInternalName();
		final List<Library> libraries = rootPackage.compiler.config.libraries;
		return libraries.stream().flatMap(l -> l.listFileNames(internalName)).filter(p -> p.endsWith(".class"))
		                .map(p -> p.substring(0, p.length() - ".class".length()));
	}

	public Iterable<IClass> getClasses()
	{
		return this.classes;
	}

	@Override
	public void addClass(IClass theClass)
	{
		this.classes.add(theClass);
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
			if (h.getName() == name && (c = h.getClasses().get(name)) != null)
			{
				return c;
			}
		}

		String qualifiedName = name.qualified;
		// Check for inner / nested / anonymous classes
		final int cashIndex = qualifiedName.lastIndexOf('$');
		if (cashIndex < 0)
		{
			return this.loadClass(name);
		}

		final Name outerName = Name.fromRaw(qualifiedName.substring(0, cashIndex));
		final Name innerName = Name.fromRaw(qualifiedName.substring(cashIndex + 1));

		final IClass outerClass = this.resolveClass(outerName);
		final IClass innerClass;
		if (outerClass != null && (innerClass = outerClass.resolveClass(innerName)) != null)
		{
			return innerClass;
		}

		// might be a class that is not nested but is prefixed with the file name, which happens when a class has a
		// different name than its enclosing compilation unit
		return this.loadClass(name);
	}

	private IClass loadClass(Name name)
	{
		return loadClass(this.getInternalName() + name.qualified + DyvilFileType.CLASS_EXTENSION, name, this);
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

	// --------------- Formatting ---------------

	@Override
	public String toString()
	{
		return this.fullName;
	}
}
