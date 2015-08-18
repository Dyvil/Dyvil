package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class PackageDeclaration implements IASTNode
{
	protected ICodePosition	position;
	protected String		thePackage;
	
	public PackageDeclaration(ICodePosition position)
	{
		this.position = position;
	}
	
	public PackageDeclaration(ICodePosition position, String thePackage)
	{
		this.position = position;
		this.thePackage = thePackage;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
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
