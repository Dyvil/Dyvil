package dyvilx.tools.compiler.ast.structure;

import dyvil.collection.Map;
import dyvil.collection.mutable.HashMap;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.header.PackageDeclaration;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.library.Library;
import dyvilx.tools.compiler.util.Markers;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;

public final class RootPackage extends Package
{
	private final Map<String, IClass> internalClass = new HashMap<>();

	public DyvilCompiler compiler;

	public RootPackage(DyvilCompiler compiler)
	{
		this.compiler = compiler;

		this.internalName = "";
		this.fullName = "";
		this.name = Name.fromRaw("");
	}

	@Override
	public void check(PackageDeclaration packageDecl, MarkerList markers)
	{
		if (packageDecl != null)
		{
			markers.add(Markers.semantic(packageDecl.getPosition(), "package.default"));
		}
	}

	public Package resolvePackageInternal(String internal)
	{
		for (Library library : this.compiler.config.libraries)
		{
			final Package pack = library.resolvePackage(internal);
			if (pack != null)
			{
				return pack;
			}
		}

		return null;
	}

	@Override
	public IClass resolveClass(String name)
	{
		return this.resolveInternalClass(ClassFormat.packageToInternal(name));
	}

	public IClass resolveInternalClass(String internal)
	{
		IClass iclass = this.internalClass.get(internal);
		if (iclass != null)
		{
			return iclass;
		}

		iclass = this.resolveInternalClass_(internal);
		this.internalClass.put(internal, iclass);

		return iclass;
	}

	private IClass resolveInternalClass_(String internal)
	{
		final int index = internal.lastIndexOf('/');
		if (index == -1)
		{
			return super.resolveClass(internal);
		}

		final String packageName = internal.substring(0, index);
		final String className = internal.substring(index + 1);

		for (Library lib : this.compiler.config.libraries)
		{
			final Package pack = lib.resolvePackage(packageName);
			if (pack != null)
			{
				final IClass iclass = pack.resolveClass(className);
				if (iclass != null)
				{
					return iclass;
				}
			}
		}
		return null;
	}

	@Override
	public String toString()
	{
		return "<default package>";
	}
}
