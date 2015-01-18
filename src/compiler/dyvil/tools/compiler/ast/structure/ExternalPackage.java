package dyvil.tools.compiler.ast.structure;

import java.io.InputStream;

import dyvil.tools.compiler.ast.classes.BytecodeClass;
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
	public Package resolvePackage(String name)
	{
		Package pack = super.resolvePackage(name);
		if (pack != null)
		{
			return pack;
		}
		
		String name1 = this.name + "." + name;
		pack = this.library.resolvePackage(name1);
		if (pack != null)
		{
			this.addSubPackage(pack);
			return pack;
		}
		
		return null;
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
				BytecodeClass bclass = new BytecodeClass();
				this.classes.add(bclass);
				iclass = ClassReader.loadClass(bclass, is, false);
			}
		}
		return iclass;
	}
}
