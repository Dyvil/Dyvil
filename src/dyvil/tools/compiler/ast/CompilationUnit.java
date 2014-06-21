package dyvil.tools.compiler.ast;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.imports.SimpleImport;

public class CompilationUnit
{
	private PackageDecl			packageDecl;
	private List<SimpleImport>	simpleImports = new ArrayList();
	private ClassDecl			classDecl;
	
	public PackageDecl getPackageDecl()
	{
		return packageDecl;
	}
	
	public List<SimpleImport> getImportDecls()
	{
		return simpleImports;
	}
	
	public ClassDecl getClassDecl()
	{
		return this.classDecl;
	}
	
	public void setPackageDecl(PackageDecl packageDecl)
	{
		this.packageDecl = packageDecl;
	}
	
	public void addImportDecl(SimpleImport simpleImport)
	{
		this.simpleImports.add(simpleImport);
	}
	
	public void setClassDecl(ClassDecl classDecl)
	{
		this.classDecl = classDecl;
	}
}
