package dyvilx.tools.compiler.ast.structure;

import dyvil.collection.Map;
import dyvil.collection.mutable.HashMap;
import dyvil.lang.Name;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.header.PackageDeclaration;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public final class RootPackage extends Package
{
	private final Map<String, IClass> classCache = new HashMap<>();

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

	public Package resolveInternalPackage(String internal)
	{
		Package pack = this;
		int nextIndex;
		int startIndex = 0;

		while ((nextIndex = internal.indexOf('/', startIndex)) >= 0)
		{
			pack = pack.resolvePackage(internal.substring(startIndex, nextIndex));
			startIndex = nextIndex + 1;
		}

		return pack.resolvePackage(internal.substring(startIndex));
	}

	public IClass resolveInternalClass(String internal)
	{
		IClass result = this.classCache.get(internal);
		if (result != null)
		{
			return result;
		}

		Package pack = this;
		int nextIndex;
		int startIndex = 0;

		while ((nextIndex = internal.indexOf('/', startIndex)) >= 0)
		{
			pack = pack.resolvePackage(internal.substring(startIndex, nextIndex));
			startIndex = nextIndex + 1;
		}

		result = pack.resolveClass(internal.substring(startIndex));
		this.classCache.put(internal, result);

		return result;
	}

	@Override
	public String toString()
	{
		return "<default package>";
	}
}
