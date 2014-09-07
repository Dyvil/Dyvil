package dyvil.tools.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.classes.AbstractClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.imports.IImport;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.config.Formatting;

public class CompilationUnit implements IContext
{
	private PackageDecl			packageDecl;
	private List<IImport>		imports	= new ArrayList();
	private List<AbstractClass>	classes	= new ArrayList();
	
	public PackageDecl getPackageDecl()
	{
		return packageDecl;
	}
	
	public List<IImport> getImportDecls()
	{
		return this.imports;
	}
	
	public List<AbstractClass> getClasses()
	{
		return this.classes;
	}
	
	public void setPackageDecl(PackageDecl packageDecl)
	{
		this.packageDecl = packageDecl;
	}
	
	public void addImport(IImport iimport)
	{
		this.imports.add(iimport);
	}
	
	public void addClass(AbstractClass type)
	{
		this.classes.add(type);
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		// FIXME
		return null;
	}
	
	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		
		this.packageDecl.toString("", buffer);
		if (Formatting.Package.newLine)
		{
			buffer.append('\n');
		}
		
		for (IImport iimport : this.imports)
		{
			iimport.toString("", buffer);
		}
		if (Formatting.Import.newLine)
		{
			buffer.append('\n');
		}
		
		for (IClass iclass : this.classes)
		{
			iclass.toString("", buffer);
			
			if (Formatting.Class.newLine)
			{
				buffer.append('\n');
			}
		}
		
		return buffer.toString();
	}
}
