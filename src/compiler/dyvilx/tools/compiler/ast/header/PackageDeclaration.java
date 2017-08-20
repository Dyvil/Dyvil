package dyvilx.tools.compiler.ast.header;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.parsing.ASTNode;

public class PackageDeclaration implements ASTNode
{
	protected SourcePosition position;
	protected String        thePackage;
	
	public PackageDeclaration(SourcePosition position)
	{
		this.position = position;
	}
	
	public PackageDeclaration(SourcePosition position, String thePackage)
	{
		this.position = position;
		this.thePackage = thePackage;
	}
	
	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void setPosition(SourcePosition position)
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
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("package ").append(this.thePackage);
	}
}
