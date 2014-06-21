package dyvil.tools.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.imports.IImport;

public class CompilationUnit
{
	private PackageDecl		packageDecl;
	private List<IImport>	imports	= new ArrayList();
	private ClassDecl		classDecl;
	
	public PackageDecl getPackageDecl()
	{
		return packageDecl;
	}
	
	public List<IImport> getImportDecls()
	{
		return this.imports;
	}
	
	public ClassDecl getClassDecl()
	{
		return this.classDecl;
	}
	
	public void setPackageDecl(PackageDecl packageDecl)
	{
		this.packageDecl = packageDecl;
	}
	
	public void addImport(IImport iimport)
	{
		this.imports.add(iimport);
	}
	
	public void setClassDecl(ClassDecl classDecl)
	{
		this.classDecl = classDecl;
	}
}
