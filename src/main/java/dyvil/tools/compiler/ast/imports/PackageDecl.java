package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.ast.api.IASTObject;

public class PackageDecl implements IASTObject
{
	private String thePackage;
	
	public PackageDecl(String thePackage)
	{
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
		buffer.append(prefix).append("package ").append(this.thePackage).append(";\n");
	}
}
