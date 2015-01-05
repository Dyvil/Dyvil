package dyvil.tools.compiler.ast.imports;

import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class PackageImport extends ASTNode implements IImport
{
	public Package		thePackage;
	
	protected String	packageName;
	
	public PackageImport(ICodePosition position, String packageName)
	{
		this.position = position;
		this.packageName = packageName;
	}
	
	public void setPackage(String thePackage)
	{
		this.packageName = thePackage;
	}
	
	public String getPackage()
	{
		return this.packageName;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		Package pack = Package.rootPackage.resolvePackage(this.packageName);
		if (pack == null)
		{
			markers.add(new SemanticError(this.position, "'" + this.packageName + "' could not be resolved to a package"));
		}
		
		this.thePackage = pack;
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public Type getThisType()
	{
		return null;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		if (this.thePackage == null)
		{
			return null;
		}
		return this.thePackage.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(IContext context, String name)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public MethodMatch resolveMethod(IContext returnType, String name, IType... argumentTypes)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("import ").append(this.packageName).append(Formatting.Import.packageImportEnd);
	}
}
