package dyvil.tools.compiler.ast.structure;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.imports.Import;
import dyvil.tools.compiler.ast.imports.PackageDecl;

public interface IDyvilUnit extends IContext
{
	// Package
	
	public void setPackage(Package pack);
	
	public Package getPackage();
	
	// Package Declaration
	
	public void setPackageDeclaration(PackageDecl pack);
	
	public PackageDecl getPackageDeclaration();
	
	// Imports
	
	public void addImport(Import i);
	
	public void addStaticImport(Import i);
	
	public boolean hasStaticImports();
	
	// Classes
	
	public void addClass(IClass iclass);
	
	// Compilation
	
	public String getInternalName(String subClass);
	
	public String getFullName(String subClass);
}
