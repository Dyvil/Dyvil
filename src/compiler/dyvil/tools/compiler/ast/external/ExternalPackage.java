package dyvil.tools.compiler.ast.external;

import java.io.InputStream;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.ClassReader;
import dyvil.tools.compiler.backend.HeaderFile;
import dyvil.tools.compiler.library.Library;

public class ExternalPackage extends Package
{
	public Library			library;
	private List<IClass>	classes	= new ArrayList();
	
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
		
		String name1 = this.internalName + name;
		pack = this.library.resolvePackage(name1);
		if (pack != null)
		{
			this.addSubPackage(pack);
			return pack;
		}
		
		for (Library lib : DyvilCompiler.config.libraries)
		{
			if (lib == this.library)
			{
				continue;
			}
			
			pack = lib.resolvePackage(name1);
			if (pack != null)
			{
				this.addSubPackage(pack);
				return pack;
			}
		}
		
		return null;
	}
	
	@Override
	public synchronized IClass resolveClass(Name name)
	{
		for (IClass iclass : this.classes)
		{
			if (iclass.getName() == name)
			{
				return iclass;
			}
		}
		
		IClass iclass = super.resolveClass(name);
		if (iclass != null)
		{
			return iclass;
		}
		return this.loadClass(name.qualified);
	}
	
	@Override
	public synchronized IClass resolveClass(String name)
	{
		for (IClass iclass : this.classes)
		{
			if (name.equals(iclass.getName().qualified))
			{
				return iclass;
			}
		}
		
		return this.loadClass(name);
	}
	
	@Override
	public IDyvilHeader resolveHeader(String name)
	{
		IDyvilHeader header = super.resolveHeader(name);
		if (header != null)
		{
			return header;
		}
		
		return this.loadHeader(name);
	}
	
	private IClass loadClass(String name)
	{
		String fileName = this.internalName + name + ".class";
		IClass iclass = this.loadClass(fileName, name, this.library);
		if (iclass != null)
		{
			return iclass;
		}
		
		for (Library library : DyvilCompiler.config.libraries)
		{
			if (library == this.library)
			{
				continue;
			}
			
			iclass = this.loadClass(fileName, name, library);
			if (iclass != null)
			{
				return iclass;
			}
		}
		
		return null;
	}
	
	private IDyvilHeader loadHeader(String name)
	{
		String fileName = this.internalName + name + ".dyhbin";
		IDyvilHeader header = this.loadHeader(fileName, name, this.library);
		if (header != null)
		{
			return header;
		}
		for (Library library : DyvilCompiler.config.libraries)
		{
			if (library == this.library)
			{
				continue;
			}
			
			header = this.loadHeader(fileName, name, library);
			if (header != null)
			{
				return header;
			}
		}
		
		return null;
	}
	
	private IClass loadClass(String fileName, String name, Library library)
	{
		InputStream is = library.getInputStream(fileName);
		if (is != null)
		{
			ExternalClass bclass = new ExternalClass(Name.getQualified(name));
			this.classes.add(bclass);
			return ClassReader.loadClass(bclass, is, false);
		}
		return null;
	}
	
	private IDyvilHeader loadHeader(String fileName, String name, Library library)
	{
		InputStream is = library.getInputStream(fileName);
		if (is != null)
		{
			DyvilHeader header = HeaderFile.read(is);
			header.pack = this;
			this.headers.add(header);
			return header;
		}
		return null;
	}
}
