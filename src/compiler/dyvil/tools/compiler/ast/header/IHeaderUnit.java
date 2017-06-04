package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.imports.ImportDeclaration;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.expression.operator.IOperator;
import dyvil.tools.compiler.ast.expression.operator.IOperatorMap;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TypeList;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.alias.ITypeAliasMap;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ASTNode;

public interface IHeaderUnit extends ASTNode, IObjectCompilable, IContext, IClassList, ICompilableList, IOperatorMap, ITypeAliasMap
{
	boolean isHeader();

	@Override
	DyvilCompiler getCompilationContext();

	IContext getContext();

	Name getName();

	void setName(Name name);

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
	IOperator resolveOperator(Name name, byte type);

	@Override
	void addOperator(IOperator operator);

	// Type Aliases

	int typeAliasCount();
	
	@Override
	void resolveTypeAlias(MatchList<ITypeAlias> matches, IType receiver, Name name, TypeList arguments);

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
	
	@Override
	int compilableCount();
	
	@Override
	void addCompilable(ICompilable compilable);
	
	byte getVisibility(IClassMember member);
	
	// Compilation
	
	String getInternalName();
	
	String getInternalName(Name subClass);
	
	String getFullName();
	
	String getFullName(Name subClass);
}
