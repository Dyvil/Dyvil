package dyvil.tools.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public class CompilationUnit
{
	private PackageDecl			packageDecl;
	private List<ImportDecl>	importDecls = new ArrayList();
	private ClassDecl			classDecl;
	
	public PackageDecl getPackageDecl()
	{
		return packageDecl;
	}
	
	public List<ImportDecl> getImportDecls()
	{
		return importDecls;
	}
	
	public ClassDecl getClassDecl()
	{
		return this.classDecl;
	}
	
	public void setPackageDecl(PackageDecl packageDecl)
	{
		this.packageDecl = packageDecl;
	}
	
	public void addImportDecl(ImportDecl importDecl)
	{
		this.importDecls.add(importDecl);
	}
	
	public void setClassDecl(ClassDecl classDecl)
	{
		this.classDecl = classDecl;
	}
}
