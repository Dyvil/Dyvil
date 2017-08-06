package dyvilx.tools.compiler.ast.header;

import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.classes.IClassList;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.imports.ImportDeclaration;
import dyvilx.tools.compiler.ast.member.IClassMember;
import dyvilx.tools.compiler.ast.expression.operator.IOperator;
import dyvilx.tools.compiler.ast.expression.operator.IOperatorMap;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.alias.ITypeAlias;
import dyvilx.tools.compiler.ast.type.alias.ITypeAliasMap;
import dyvil.lang.Name;
import dyvilx.tools.parsing.ASTNode;

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
