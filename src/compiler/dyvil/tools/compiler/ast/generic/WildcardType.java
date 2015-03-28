package dyvil.tools.compiler.ast.generic;

import java.util.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.CaptureClass;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.MarkerList;
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
		this.upperBounds = capture.upperBounds;
		this.upperBoundCount = capture.upperBoundCount;
		this.lowerBound = capture.lowerBound;
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
	public MethodMatch resolveMethod(IValue instance, String name, IArguments arguments)
	{
		if (this.arrayDimensions > 0)
		{
			return Type.ARRAY_CLASS.resolveMethod(instance, name, arguments);
		}
		
		return this.captureClass.resolveMethod(instance, name, arguments);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, IArguments arguments)
	{
		if (this.arrayDimensions > 0)
		{
			Type.ARRAY_CLASS.getMethodMatches(list, instance, name, arguments);
			return;
		}
		
		this.captureClass.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public ConstructorMatch resolveConstructor(IArguments arguments)
	{
		if (this.arrayDimensions > 0)
		{
			return null;
		}
		
		return this.captureClass.resolveConstructor(arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
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
	public IType resolveType(String name)
	{
		if (this.name != null)
		{
			if (this.name.equals(name))
			{
				return this;
			}
			return null;
		}
		
		IType type;
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			type = this.upperBounds[i].resolveType(name);
			if (type != null)
			{
				return type;
			}
		}
		return null;
	}
	
	@Override
	public IType resolveType(String name, IType concrete)
	{
		if (this.name != null)
		{
			if (this.name.equals(name) && this.isSuperTypeOf(concrete))
			{
				return concrete;
			}
			return null;
		}
		
		IType type;
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			type = this.upperBounds[i].resolveType(name, concrete);
			if (type != null)
			{
				return type;
			}
		}
		return null;
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return this.name != null;
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		if (this.name != null)
		{
			IType t = context.resolveType(this.name);
			if (t != null)
			{
				if (this.arrayDimensions > 0)
				{
					return t.getArrayType(this.arrayDimensions);
				}
				return t;
			}
			return this;
		}
		
		if (this.lowerBound != null)
		{
			return this.lowerBound.getConcreteType(context);
		}
		
		WildcardType type = new WildcardType(this.position);
		type.arrayDimensions = this.arrayDimensions;
		type.captureClass = this.captureClass;
		type.upperBounds = new IType[this.upperBoundCount];
		type.upperBoundCount = this.upperBoundCount;
		for (int i = 0; i < this.upperBoundCount; i++)
		{
			type.upperBounds[i] = this.upperBounds[i].getConcreteType(context);
		}
		return type;
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
		return this.upperBoundCount == 0 ? Type.NONE : this.upperBounds[0];
	}
	
	@Override
	public boolean equals(IType type)
	{
		if (this.arrayDimensions != type.getArrayDimensions())
		{
			return false;
		}
		if (this.upperBoundCount > 0)
		{
			for (int i = 0; i < this.upperBoundCount; i++)
			{
				if (!this.upperBounds[i].isSuperTypeOf(type))
				{
					return false;
				}
			}
		}
		if (this.lowerBound != null)
		{
			if (!type.isSuperTypeOf(this.lowerBound))
			{
				return false;
			}
		}
		return !type.isPrimitive();
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
	public IType resolve(MarkerList markers, IContext context)
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
		if (this.upperBoundCount > 0)
		{
			return this.upperBounds[0].getInternalName();
		}
		return "java/lang/Object";
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
			this.upperBounds[0].appendSignature(buffer);
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
