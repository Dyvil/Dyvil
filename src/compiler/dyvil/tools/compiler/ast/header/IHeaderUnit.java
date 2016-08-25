package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.imports.ImportDeclaration;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.operator.IOperator;
import dyvil.tools.compiler.ast.operator.IOperatorMap;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.alias.ITypeAliasMap;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.compiler.backend.IObjectCompilable;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;

public interface IHeaderUnit extends IASTNode, IObjectCompilable, IContext, IClassList, IOperatorMap, ITypeAliasMap
{
	boolean isHeader();

	@Override
	DyvilCompiler getCompilationContext();

	IContext getContext();

	void setName(Name name);
	
	Name getName();
	
	// Package
	
	void setPackage(Package pack);
	
	Package getPackage();
	
	// Package Declaration
	
	void setPackageDeclaration(PackageDeclaration pack);
	
	PackageDeclaration getPackageDeclaration();
	
	// Header Declaration
	
	void setHeaderDeclaration(HeaderDeclaration declaration);
	
	HeaderDeclaration getHeaderDeclaration();
	
	// Import

	boolean hasMemberImports();

	int importCount();

	void addImport(ImportDeclaration component);

	ImportDeclaration getImport(int index);
	
	// Operators
	
	int operatorCount();

	@Override
	IOperator resolveOperator(Name name, int type);
	
	IOperator getOperator(int index);

	void setOperator(int index, IOperator operator);

	@Override
	void addOperator(IOperator op);

	// Type Aliases

	int typeAliasCount();
	
	@Override
	ITypeAlias resolveTypeAlias(Name name, int arity);

	ITypeAlias getTypeAlias(int index);

	void setTypeAlias(int index, ITypeAlias typeAlias);

	@Override
	void addTypeAlias(ITypeAlias typeAlias);

	// Classes
	
	@Override
	int classCount();
	
	@Override
	void addClass(IClass iclass);
	
	@Override
	IClass getClass(int index);
	
	@Override
	IClass getClass(Name name);
	
	int innerClassCount();
	
	void addInnerClass(IClassCompilable iclass);
	
	IClassCompilable getInnerClass(int index);
	
	byte getVisibility(IClassMember member);
	
	// Compilation
	
	String getInternalName();
	
	String getInternalName(Name subClass);
	
	String getFullName();
	
	String getFullName(Name subClass);
}
