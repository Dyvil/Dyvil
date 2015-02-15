package dyvil.tools.compiler.ast.generic;

import java.util.List;
import java.util.Map;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.CaptureClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.Marker;
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
	
	public WildcardType(ICodePosition position, CaptureClass capture)
	{
		this.position = position;
		this.captureClass = capture;
		this.name = capture.var.getName();
		this.upperBound = capture.getSuperType();
		this.upperBounds = capture.getInterfaces();
		this.lowerBound = capture.getLowerBound();
	}
	
	@Override
	public IType getThisType()
	{
		return this;
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		return this.captureClass.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return this.captureClass.resolveClass(name);
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		return this.captureClass.resolveField(name);
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, List<IValue> arguments)
	{
		return this.captureClass.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, List<IValue> arguments)
	{
		this.captureClass.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public MethodMatch resolveConstructor(List<IValue> arguments)
	{
		return this.captureClass.resolveConstructor(arguments);
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, List<IValue> arguments)
	{
		this.captureClass.getConstructorMatches(list, arguments);
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return this.captureClass.getAccessibility(member);
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
		this.captureClass = theClass;
	}
	
	@Override
	public IClass getTheClass()
	{
		return this.captureClass;
	}
	
	@Override
	public boolean isGeneric()
	{
		return false;
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return false;
	}
	
	@Override
	public IType getConcreteType(Map<String, IType> typeVariables)
	{
		return this;
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
		return this.upperBound == null ? Type.NONE : this.upperBound;
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
		return this.upperBound == null ? "java/lang/Object" : this.upperBound.getInternalName();
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append('L').append(this.getInternalName()).append(';');
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append('T').append(this.name).append(';');
	}
	
	@Override
	public int getLoadOpcode()
	{
		return Opcodes.ALOAD;
	}
	
	@Override
	public int getArrayLoadOpcode()
	{
		return Opcodes.AALOAD;
	}
	
	@Override
	public int getStoreOpcode()
	{
		return Opcodes.ASTORE;
	}
	
	@Override
	public int getArrayStoreOpcode()
	{
		return Opcodes.AASTORE;
	}
	
	@Override
	public int getReturnOpcode()
	{
		return Opcodes.ARETURN;
	}
	
	@Override
	public IType clone()
	{
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.name != null)
		{
			buffer.append(this.name);
			return;
		}
		super.toString(prefix, buffer);
	}
}
