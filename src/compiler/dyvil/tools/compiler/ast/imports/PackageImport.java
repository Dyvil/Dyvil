package dyvil.tools.compiler.ast.imports;

import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class PackageImport extends ASTNode implements IImport
{
	public IImport	parent;
	
	private Package	thePackage;
	private IClass	theClass;
	
	public PackageImport(ICodePosition position, IImport parent)
	{
		this.position = position;
		this.parent = parent;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context, boolean isStatic)
	{
		if (isStatic)
		{
			if (!(context instanceof CodeClass))
			{
				markers.add(new SemanticError(this.position, "Invalid Wildcard Import"));
				return;
			}
			
			this.theClass = (CodeClass) context;
			return;
		}
		
		this.thePackage = (Package) context;
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
		return this.thePackage.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.thePackage.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		return this.theClass.resolveField(name);
	}
	
	@Override
	public MethodMatch resolveMethod(ITyped instance, String name, List<? extends ITyped> arguments)
	{
		return this.theClass.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, ITyped instance, String name, List<? extends ITyped> arguments)
	{
		this.theClass.getMethodMatches(list, instance, name, arguments);
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
