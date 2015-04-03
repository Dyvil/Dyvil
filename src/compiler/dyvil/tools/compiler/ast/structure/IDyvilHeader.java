package dyvil.tools.compiler.ast.structure;

import dyvil.tools.compiler.ast.classes.IClassList;
import dyvil.tools.compiler.ast.imports.HeaderComponent;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.Operator;

public interface IDyvilHeader extends IContext, IClassList
{
	public String getName();
	
	// Package
	
	public void setPackage(Package pack);
	
	public Package getPackage();
	
	// Package Declaration
	
	public void setPackageDeclaration(PackageDecl pack);
	
	public PackageDecl getPackageDeclaration();
	
	// Include
	
	public void addImport(HeaderComponent i);
	
	public void addStaticImport(HeaderComponent i);
	
	public boolean hasStaticImports();
	
	// Operators
	
	public void addOperator(Operator op);
	
	public Operator getOperator(Name name);
	
	// Compilation
	
	public String getInternalName(String subClass);
	
	public String getFullName(String subClass);
}
