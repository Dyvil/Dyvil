package dyvil.tools.compiler.ast.structure;

import dyvil.collection.Map;
import dyvil.collection.mutable.HashMap;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.header.PackageDeclaration;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.util.MarkerMessages;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

public final class RootPackage extends Package
{
	private final Map<String, IClass> internalClass = new HashMap<String, IClass>();
	
	public RootPackage()
	{
		this.setInternalName(this.fullName = ""); // Assignment intentional
		this.name = Name.getQualified("");
	}
	
	@Override
	public void check(PackageDeclaration packageDecl, MarkerList markers)
	{
		if (packageDecl != null)
		{
			markers.add(MarkerMessages.createMarker(packageDecl.getPosition(), "package.default"));
		}
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		Package pack = super.resolvePackage(name);
		if (pack != null)
		{
			return pack;
		}
		
		String internal = ClassFormat.internalToPackage(name);
		return this.resolvePackageInternal(internal);
	}
	
	public Package resolvePackageInternal(String internal)
	{
		Package pack;
		for (Library lib : DyvilCompiler.config.libraries)
		{
			pack = lib.resolvePackage(internal);
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
		int index = internal.lastIndexOf('/');
		if (index == -1)
		{
			return super.resolveClass(internal);
		}
		
		String packageName = internal.substring(0, index);
		String className = internal.substring(index + 1);
		Package pack;
		
		for (Library lib : DyvilCompiler.config.libraries)
		{
			pack = lib.resolvePackage(packageName);
			if (pack != null)
			{
				IClass iclass = pack.resolveClass(className);
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
