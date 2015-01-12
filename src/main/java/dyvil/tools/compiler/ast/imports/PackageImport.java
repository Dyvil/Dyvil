package dyvil.tools.compiler.ast.imports;

import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class PackageImport extends ASTNode implements IImport
{
	public IImport	parent;
	
	public PackageImport(ICodePosition position, IImport parent)
	{
		this.position = position;
		this.parent = parent;
	}

	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
	}
	
	@Override
	public boolean isValid()
	{
		return true;
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public IType getThisType()
	{
		return null;
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		return null;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return null;
	}
	
	@Override
	public FieldMatch resolveField(IContext context, String name)
	{
		return null;
	}
	
	@Override
	public MethodMatch resolveMethod(IContext context, String name, IType... argumentTypes)
	{
		return null;
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return 0;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('_');
	}
}
