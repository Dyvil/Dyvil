package dyvil.tools.compiler.ast.generic;

import java.util.List;
import java.util.Map;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class WildcardType extends TypeVariable implements IType
{
	public WildcardType()
	{
	}
	
	public WildcardType(ICodePosition position)
	{
		super(position);
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
	public FieldMatch resolveField(String name)
	{
		return null;
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, List<IValue> arguments)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, List<IValue> arguments)
	{
	}
	
	@Override
	public MethodMatch resolveConstructor(List<IValue> arguments)
	{
		return null;
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, List<IValue> arguments)
	{
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return 0;
	}
	
	@Override
	public void setFullName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getFullName()
	{
		return this.name;
	}
	
	@Override
	public void setClass(IClass theClass)
	{
	}
	
	@Override
	public IClass getTheClass()
	{
		return this.captureClass;
	}
	
	@Override
	public void setArrayDimensions(int dimensions)
	{
	}
	
	@Override
	public int getArrayDimensions()
	{
		return 0;
	}
	
	@Override
	public boolean isArrayType()
	{
		return false;
	}
	
	@Override
	public IType getSuperType()
	{
		return this.captureClass == null ? Type.NONE : this.captureClass.getSuperType();
	}
	
	@Override
	public IType resolve(List<Marker> markers, IContext context)
	{
		return this;
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public String getInternalName()
	{
		return null;
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
	}
	
	@Override
	public int getLoadOpcode()
	{
		return 0;
	}
	
	@Override
	public int getArrayLoadOpcode()
	{
		return 0;
	}
	
	@Override
	public int getStoreOpcode()
	{
		return 0;
	}
	
	@Override
	public int getArrayStoreOpcode()
	{
		return 0;
	}
	
	@Override
	public int getReturnOpcode()
	{
		return 0;
	}
	
	@Override
	public IType clone()
	{
		return null;
	}
	
}
