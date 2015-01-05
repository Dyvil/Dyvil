package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class PackageDecl extends ASTNode
{
	public String	thePackage;
	
	public PackageDecl(ICodePosition position)
	{
		this.position = position;
	}
	
	public PackageDecl(ICodePosition position, String thePackage)
	{
		this.position = position;
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
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("package ").append(this.thePackage);
	}
}
