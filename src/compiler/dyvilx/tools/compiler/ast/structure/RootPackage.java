package dyvilx.tools.compiler.ast.structure;

import dyvil.collection.Map;
import dyvil.collection.mutable.HashMap;
import dyvil.lang.Name;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.external.ExternalClass;

public final class RootPackage extends Package
{
	// =============== Fields ===============

	public DyvilCompiler compiler;

	// --------------- Cache ---------------

	private final Map<String, ExternalClass> globalExternalClassCache = new HashMap<>();

	// =============== Constructors ===============

	public RootPackage(DyvilCompiler compiler)
	{
		this.compiler = compiler;

		this.internalName = "";
		this.fullName = "";
		this.name = Name.fromRaw("");
	}

	// =============== Methods ===============

	// --------------- External Resolution ---------------

	public Package resolveGlobalPackage(String descriptor)
	{
		Package pack = this;
		int nextIndex;
		int startIndex = 0;

		while ((nextIndex = descriptor.indexOf('/', startIndex)) >= 0)
		{
			pack = pack.resolvePackage(descriptor.substring(startIndex, nextIndex));
			startIndex = nextIndex + 1;
		}

		return pack.resolvePackage(descriptor.substring(startIndex));
	}

	public IClass resolveGlobalClass(String descriptor)
	{
		final ExternalClass cached = this.globalExternalClassCache.get(descriptor);
		if (cached != null)
		{
			return cached;
		}

		Package pack = this;
		int nextIndex;
		int startIndex = 0;

		while ((nextIndex = descriptor.indexOf('/', startIndex)) >= 0)
		{
			pack = pack.resolvePackage(descriptor.substring(startIndex, nextIndex));
			startIndex = nextIndex + 1;
		}

		final IClass result = pack.resolveClass(descriptor.substring(startIndex));
		if (result instanceof ExternalClass)
		{
			this.globalExternalClassCache.put(descriptor, (ExternalClass) result);
		}

		return result;
	}

	// --------------- Formatting ---------------

	@Override
	public String toString()
	{
		return "<default package>";
	}
}
