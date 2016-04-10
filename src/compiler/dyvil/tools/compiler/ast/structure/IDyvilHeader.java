package dyvil.tools.compiler.ast.structure;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassList;
import dyvil.tools.compiler.ast.context.IStaticContext;
import dyvil.tools.compiler.ast.header.HeaderDeclaration;
import dyvil.tools.compiler.ast.header.ImportDeclaration;
import dyvil.tools.compiler.ast.header.IncludeDeclaration;
import dyvil.tools.compiler.ast.header.PackageDeclaration;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.operator.IOperator;
import dyvil.tools.compiler.ast.operator.IOperatorMap;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.alias.ITypeAliasMap;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.compiler.backend.IObjectCompilable;
import dyvil.tools.parsing.Name;

public interface IDyvilHeader extends IObjectCompilable, IStaticContext, IClassList, IOperatorMap, ITypeAliasMap
{
	default boolean isHeader()
	{
		return true;
	}

	@Override
	DyvilCompiler getCompilationContext();

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
	
	int importCount();
	
	void addImport(ImportDeclaration component);
	
	ImportDeclaration getImport(int index);
	
	// Using
	
	boolean hasMemberImports();
	
	int usingCount();
	
	void addUsing(ImportDeclaration component);
	
	ImportDeclaration getUsing(int index);
	
	// Include
	
	int includeCount();
	
	void addInclude(IncludeDeclaration component);
	
	IncludeDeclaration getInclude(int index);
	
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
