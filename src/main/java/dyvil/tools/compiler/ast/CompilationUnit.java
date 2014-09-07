package dyvil.tools.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.classes.AbstractClass;
import dyvil.tools.compiler.ast.imports.IImport;
import dyvil.tools.compiler.ast.imports.PackageDecl;

public class CompilationUnit
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
}
