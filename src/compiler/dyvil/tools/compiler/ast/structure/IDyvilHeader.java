package dyvil.tools.compiler.ast.structure;

import java.util.Comparator;
import java.util.Map;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassList;
import dyvil.tools.compiler.ast.imports.ImportDeclaration;
import dyvil.tools.compiler.ast.imports.IncludeDeclaration;
import dyvil.tools.compiler.ast.imports.PackageDeclaration;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.IOperatorMap;
import dyvil.tools.compiler.ast.operator.Operator;

public interface IDyvilHeader extends IContext, IClassList, IOperatorMap
{
	Comparator<? super ICompilationUnit>	HEADERS_FIRST	= (c1, c2) -> {
																final boolean h1 = c1.isHeader();
																final boolean h2 = c2.isHeader();
																if (h1 == h2)
																{
																	return 0;
																}
																if (h1)
																{
																	return -1;
																}
																if (h2)
																{
																	return 1;
																}
																return 0;
															};
	
	public default boolean isHeader()
	{
		return true;
	}
	
	public String getName();
	
	// Package
	
	public void setPackage(Package pack);
	
	public Package getPackage();
	
	// Package Declaration
	
	public void setPackageDeclaration(PackageDeclaration pack);
	
	public PackageDeclaration getPackageDeclaration();
	
	// Import
	
	public int importCount();
	
	public void addImport(ImportDeclaration component);
	
	public ImportDeclaration getImport(int index);
	
	// Using
	
	public boolean hasStaticImports();
	
	public int staticImportCount();
	
	public void addStaticImport(ImportDeclaration component);
	
	public ImportDeclaration getStaticImport(int index);
	
	// Include
	
	public int includeCount();
	
	public void addInclude(IncludeDeclaration component);
	
	public IncludeDeclaration getInclude(int index);
	
	// Operators
	
	public Map<Name, Operator> getOperators();
	
	@Override
	public Operator getOperator(Name name);
	
	@Override
	public void addOperator(Operator op);
	
	// Classes
	
	@Override
	public int classCount();
	
	@Override
	public void addClass(IClass iclass);
	
	@Override
	public IClass getClass(int index);
	
	@Override
	public IClass getClass(Name name);
	
	public int innerClassCount();
	
	public void addInnerClass(IClassCompilable iclass);
	
	public IClassCompilable getInnerClass(int index);
	
	// Compilation
	
	public String getInternalName(String subClass);
	
	public String getFullName(String subClass);
}
