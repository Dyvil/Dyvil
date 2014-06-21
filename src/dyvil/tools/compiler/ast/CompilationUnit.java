package dyvil.tools.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.classes.AbstractClass;
import dyvil.tools.compiler.ast.imports.IImport;

public class CompilationUnit
{
	private PackageDecl		packageDecl;
	private List<IImport>	imports	= new ArrayList();
	private AbstractClass		abstractClass;
	
	public PackageDecl getPackageDecl()
	{
		return packageDecl;
	}
	
	public List<IImport> getImportDecls()
	{
		return this.imports;
	}
	
	public AbstractClass getClassDecl()
	{
		return this.abstractClass;
	}
	
	public void setPackageDecl(PackageDecl packageDecl)
	{
		this.packageDecl = packageDecl;
	}
	
	public void addImport(IImport iimport)
	{
		this.imports.add(iimport);
	}
	
	public void setClassDecl(AbstractClass abstractClass)
	{
		this.abstractClass = abstractClass;
	}
}
