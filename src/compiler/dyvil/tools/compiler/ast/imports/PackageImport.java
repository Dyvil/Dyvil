package dyvil.tools.compiler.ast.imports;

import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.MarkerList;
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
	public void resolveTypes(MarkerList markers, IContext context, boolean isStatic)
	{
		if (isStatic)
		{
			if (!(context instanceof IClass))
			{
				markers.add(this.position, "import.package.invalid");
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
	public Package resolvePackage(Name name)
	{
		return this.thePackage.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return this.thePackage.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(Name name)
	{
		return this.theClass.resolveField(name);
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, Name name, IArguments arguments)
	{
		return this.theClass.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.theClass.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public ConstructorMatch resolveConstructor(IArguments arguments)
	{
		return null;
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
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
