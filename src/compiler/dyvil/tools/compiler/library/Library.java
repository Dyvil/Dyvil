package dyvil.tools.compiler.library;

import java.io.File;
import java.io.InputStream;
import java.nio.file.LinkOption;
import java.util.HashMap;
import java.util.Map;

import dyvil.tools.compiler.ast.external.ExternalPackage;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.Package;

public abstract class Library
{
	public static final File					javaLibraryLocation;
	public static final File					dyvilLibraryLocation;
	
	public static final Library					dyvilLibrary;
	public static final Library					dyvilBinLibrary;
	public static final Library					javaLibrary;
	
	static
	{
		String s = System.getProperty("sun.boot.class.path");
		int index = s.indexOf("rt.jar");
		if (index != -1)
		{
			int index1 = s.lastIndexOf(':', index);
			String s1 = s.substring(index1 + 1, index + 6);
			javaLibraryLocation = new File(s1);
		}
		else
		{
			javaLibraryLocation = null;
		}
		
		File bin = new File("bin");
		if (bin.exists())
		{
			dyvilLibraryLocation = bin;
		}
		else
		{
			s = System.getenv("DYVIL_HOME");
			if (s == null || s.isEmpty())
			{
				throw new Error("No installed Dyvil Runtime Library found!");
			}
			dyvilLibraryLocation = new File(s);
		}
		
		dyvilLibrary = load(dyvilLibraryLocation);
		javaLibrary = load(javaLibraryLocation);
		
		bin = new File("dbin");
		if (bin.exists())
		{
			dyvilBinLibrary = load(bin);
		}
		else
		{
			dyvilBinLibrary = null;
		}
	}
	
	protected static final Map<String, String>	env					= new HashMap<>();
	
	static
	{
		env.put("create", "true");
	}
	
	protected static final String[]				emptyStrings		= {};
	protected static final LinkOption[]			emptyLinkOptions	= {};
	
	public File									file;
	public Map<String, Package>					packages			= new HashMap();
	
	protected Library(File file)
	{
		this.file = file;
	}
	
	public static Library load(File file)
	{
		if (file.isDirectory())
		{
			return new FileLibrary(file);
		}
		else if (file.getPath().endsWith(".jar"))
		{
			return new JarLibrary(file);
		}
		return null;
	}
	
	public abstract void loadLibrary();
	
	public abstract void unloadLibrary();
	
	public abstract boolean isSubPackage(String internal);
	
	public Package resolvePackage(String internal)
	{
		Package pack = this.packages.get(internal);
		if (pack != null)
		{
			return pack;
		}
		
		int index = internal.indexOf('/');
		if (index >= 0)
		{
			if (this.isSubPackage(internal))
			{
				String s = internal.substring(0, index);
				pack = this.resolvePackage2(s);
				
				if (pack == null)
				{
					return null;
				}
				
				do
				{
					int index1 = internal.indexOf('/', index + 1);
					int index2 = index1 >= 0 ? index1 : internal.length();
					s = internal.substring(index + 1, index2);
					pack = pack.createSubPackage(s);
					if (pack == null)
					{
						return null;
					}
					index = index1;
				}
				while (index >= 0);
				return pack;
			}
			
			return null;
		}
		if (this.isSubPackage(internal))
		{
			pack = new ExternalPackage(Package.rootPackage, Name.getQualified(internal), this);
			Package.rootPackage.addSubPackage(pack);
			this.packages.put(internal, pack);
			return pack;
		}
		return null;
	}
	
	public Package resolvePackage2(String name)
	{
		Package pack = this.packages.get(name);
		if (pack != null)
		{
			return pack;
		}
		if (this.isSubPackage(name))
		{
			pack = new ExternalPackage(Package.rootPackage, Name.getQualified(name), this);
			Package.rootPackage.addSubPackage(pack);
			this.packages.put(name, pack);
			return pack;
		}
		return null;
	}
	
	public abstract InputStream getInputStream(String fileName);
	
	@Override
	public String toString()
	{
		return this.file.getAbsolutePath();
	}
}
