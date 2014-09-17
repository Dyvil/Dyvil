package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class PackageDecl extends ASTObject
{
	private String thePackage;
	
	public PackageDecl()
	{
	}
	
	public PackageDecl(ICodePosition position, String thePackage)
	{
		super(position);
		this.thePackage = thePackage;
	}
	
	public void setPackage(String thePackage)
	{
		this.thePackage = thePackage;
	}
	
	public String getPackage()
	{
		return this.thePackage;
	}
	
	@Override
	public PackageDecl applyState(CompilerState state)
	{
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append("package ").append(this.thePackage).append(";");
	}
}
