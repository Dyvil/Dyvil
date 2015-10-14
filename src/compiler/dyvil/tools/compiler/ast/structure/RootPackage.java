package dyvil.tools.compiler.ast.structure;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.imports.PackageDeclaration;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.library.Library;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.marker.MarkerList;

public final class RootPackage extends Package
{
	public RootPackage()
	{
		this.setInternalName(this.fullName = "");
		this.name = Name.getQualified("");
	}
	
	@Override
	public void check(PackageDeclaration packageDecl, MarkerList markers)
	{
		if (packageDecl != null)
		{
			markers.add(I18n.createMarker(packageDecl.getPosition(), "package.default"));
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
