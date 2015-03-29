package dyvil.tools.compiler.ast.external;

import java.io.InputStream;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.ClassReader;
import dyvil.tools.compiler.library.Library;

public class ExternalPackage extends Package
{
	public Library	library;
	
	public ExternalPackage(Package parent, Name name, Library library)
	{
		super(parent, name);
		this.library = library;
	}
	
	@Override
	public Package createSubPackage(String name)
	{
		Package pack = this.subPackages.get(name);
		if (pack != null)
		{
			return pack;
		}
		
		pack = new ExternalPackage(this, Name.getQualified(name), this.library);
		this.subPackages.put(name, pack);
		return pack;
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		Package pack = super.resolvePackage(name);
		if (pack != null)
		{
			return pack;
		}
		
		String name1 = this.fullName + "." + name;
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
			synchronized (this)
			{
				InputStream is = this.library.getInputStream(this.internalName + name + ".class");
				if (is != null)
				{
					BytecodeClass bclass = new BytecodeClass(Name.getQualified(name));
					this.classes.add(bclass);
					iclass = ClassReader.loadClass(bclass, is, false);
				}
			}
		}
		return iclass;
	}
}
