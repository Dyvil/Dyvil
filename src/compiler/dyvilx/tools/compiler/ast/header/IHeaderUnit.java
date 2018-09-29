package dyvilx.tools.compiler.ast.header;

import dyvil.lang.Name;
import dyvilx.tools.compiler.DyvilCompiler;
import dyvilx.tools.compiler.ast.classes.ClassList;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.consumer.IClassConsumer;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.operator.IOperator;
import dyvilx.tools.compiler.ast.expression.operator.IOperatorMap;
import dyvilx.tools.compiler.ast.imports.ImportDeclaration;
import dyvilx.tools.compiler.ast.member.ClassMember;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.alias.ITypeAlias;
import dyvilx.tools.compiler.ast.type.alias.ITypeAliasMap;
import dyvilx.tools.parsing.ASTNode;

public interface IHeaderUnit extends ASTNode, ObjectCompilable, IContext, IClassConsumer, ICompilableList, IOperatorMap, ITypeAliasMap
{
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

	// --------------- Classes ---------------

	ClassList getClasses();

	@Override
	default void addClass(IClass iclass)
	{
		iclass.setHeader(this);
		this.getClasses().add(iclass);
	}

	// --------------- Compilables ---------------

	@Override
	int compilableCount();

	@Override
	void addCompilable(ICompilable compilable);

	byte getVisibility(ClassMember member);

	// Compilation

	String getInternalName();

	String getFullName();
}
