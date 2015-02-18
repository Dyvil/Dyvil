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
	public int	arrayDimensions;
	
	public WildcardType()
	{
	}
	
	public WildcardType(ICodePosition position)
	{
		super(position);
	}
	
	public WildcardType(ICodePosition position, int arrayDimensions, CaptureClass capture)
	{
		this.position = position;
		this.arrayDimensions = arrayDimensions;
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
		if (this.arrayDimensions > 0)
		{
			return null;
		}
		
		return this.captureClass.resolveField(name);
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, List<IValue> arguments)
	{
		if (this.arrayDimensions > 0)
		{
			return Type.ARRAY_CLASS.resolveMethod(instance, name, arguments);
		}
		
		return this.captureClass.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, List<IValue> arguments)
	{
		if (this.arrayDimensions > 0)
		{
			Type.ARRAY_CLASS.getMethodMatches(list, instance, name, arguments);
			return;
		}
		
		this.captureClass.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public MethodMatch resolveConstructor(List<IValue> arguments)
	{
		if (this.arrayDimensions > 0)
		{
			return null;
		}
		
		return this.captureClass.resolveConstructor(arguments);
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, List<IValue> arguments)
	{
		if (this.arrayDimensions > 0)
		{
			return;
		}
		
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
		this.captureClass = (CaptureClass) theClass;
	}
	
	@Override
	public IClass getTheClass()
	{
		return this.captureClass;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.arrayDimensions != 0;
	}
	
	@Override
	public boolean isGeneric()
	{
		return true;
	}
	
	@Override
	public void addTypeVariables(IType type, Map<String, IType> typeVariables)
	{
		if (this.upperBound != null)
		{
			this.upperBound.addTypeVariables(type, typeVariables);
		}
		for (IType t : this.upperBounds)
		{
			t.addTypeVariables(type, typeVariables);
		}
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return this.name != null;
	}
	
	@Override
	public IType getConcreteType(Map<String, IType> typeVariables)
	{
		if (this.name != null)
		{
			IType t = typeVariables.get(this.name);
			if (t != null)
			{
				if (this.arrayDimensions > 0)
				{
					return t.getArrayType(this.arrayDimensions);
				}
				return t;
			}
		}
		return this;
	}
	
	@Override
	public void setArrayDimensions(int dimensions)
	{
		this.arrayDimensions = dimensions;
	}
	
	@Override
	public int getArrayDimensions()
	{
		return this.arrayDimensions;
	}
	
	@Override
	public IType getElementType()
	{
		WildcardType t = this.clone();
		t.arrayDimensions--;
		return t;
	}
	
	@Override
	public IType getArrayType()
	{
		WildcardType t = this.clone();
		t.arrayDimensions++;
		return t;
	}
	
	@Override
	public IType getArrayType(int dimensions)
	{
		WildcardType t = this.clone();
		t.arrayDimensions = dimensions;
		return t;
	}
	
	@Override
	public boolean isArrayType()
	{
		return this.arrayDimensions > 0;
	}
	
	@Override
	public IType getSuperType()
	{
		return this.upperBound == null ? Type.NONE : this.upperBound;
	}
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		if (this.arrayDimensions != type.getArrayDimensions())
		{
			return false;
		}
		return super.isSuperTypeOf(type);
	}
	
	@Override
	public IType resolve(List<Marker> markers, IContext context)
	{
		this.resolveTypes(markers, context);
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
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append('[');
		}
		buffer.append('L').append(this.getInternalName()).append(';');
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		if (this.name != null)
		{
			for (int i = 0; i < this.arrayDimensions; i++)
			{
				buffer.append('[');
			}
			buffer.append('T').append(this.name).append(';');
		}
		else if (this.lowerBound != null)
		{
			buffer.append('-');
			this.lowerBound.appendSignature(buffer);
		}
		else
		{
			buffer.append('+');
			if (this.upperBound != null)
			{
				this.upperBound.appendSignature(buffer);
			}
			else
			{
				this.upperBounds.get(0).appendSignature(buffer);
			}
		}
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
	public WildcardType clone()
	{
		return new WildcardType(this.position, this.arrayDimensions, this.captureClass);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append('[');
		}
		
		if (this.name != null)
		{
			buffer.append(this.name);
		}
		else
		{
			super.toString(prefix, buffer);
		}
		
		for (int i = 0; i < this.arrayDimensions; i++)
		{
			buffer.append(']');
		}
	}
}
