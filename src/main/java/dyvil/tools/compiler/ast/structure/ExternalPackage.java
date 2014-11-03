package dyvil.tools.compiler.ast.structure;

import java.io.InputStream;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.bytecode.ClassReader;
import dyvil.tools.compiler.library.Library;

public class ExternalPackage extends Package
{
	public Library	library;
	
	public ExternalPackage(Package parent, String name, Library library)
	{
		super(parent, name);
		this.library = library;
		this.isExternal = true;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		IClass iclass = super.resolveClass(name);
		if (iclass == null)
		{
			InputStream is = this.library.getInputStream(this.internalName + name + ".class");
			if (is != null)
			{
				iclass = ClassReader.loadClass(is, false);
				this.classes.add(iclass);
			}
		}
		return iclass;
	}
}
