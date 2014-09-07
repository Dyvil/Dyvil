package dyvil.tools.compiler.ast.imports;

public class PackageDecl
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
	public String toString()
	{
		return "package " + this.thePackage + ";";
	}
}
