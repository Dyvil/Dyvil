package dyvil.tools.compiler.ast.type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import dyvil.lang.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class ArrayType implements IType, ITyped
{
	private IType	type;
	
	public ArrayType()
	{
	}
	
	public ArrayType(IType type)
	{
		this.type = type;
	}
	
	public static IType getArrayType(IType type, int dims)
	{
		switch (dims)
		{
		case 0:
			return type;
		case 1:
			return new ArrayType(type);
		case 2:
			return new ArrayType(new ArrayType(type));
		default:
			for (; dims > 0; dims--)
			{
				type = new ArrayType(type);
			}
			return type;
		}
	}
	
	@Override
	public int typeTag()
	{
		return ARRAY;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public Name getName()
	{
		return this.type.getName();
	}
	
	@Override
	public IClass getTheClass()
	{
		return this.type.getArrayClass();
	}
	
	@Override
	public boolean isArrayType()
	{
		return true;
	}
	
	@Override
	public int getArrayDimensions()
	{
		return 1 + this.type.getArrayDimensions();
	}
	
	@Override
	public IType getElementType()
	{
		return this.type;
	}
	
	@Override
	public IType getSuperType()
	{
		return Types.OBJECT;
	}
	
	@Override
	public boolean equals(IType type)
	{
		if (!type.isArrayType())
		{
			return false;
		}
		return this.type.equals(type.getElementType());
	}
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		if (!type.isArrayType())
		{
			return false;
		}
		return this.type.isSuperTypeOf(type.getElementType());
	}
	
	@Override
	public boolean isResolved()
	{
		return this.type.isResolved();
	}
	
	@Override
	public IType resolve(MarkerList markers, IContext context, TypePosition position)
	{
		if (position == TypePosition.SUPER_TYPE)
		{
			markers.add(this.type.getPosition(), "type.super.array");
			return this.type.resolve(markers, context, TypePosition.SUPER_TYPE);
		}
		
		if (this.type == null)
		{
			this.type = Types.ANY;
		}
		this.type = this.type.resolve(markers, context, TypePosition.TYPE);
		return this;
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return this.type.hasTypeVariables();
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		return new ArrayType(this.type.getConcreteType(context));
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		return this.type.resolveType(typeVar);
	}
	
	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		if (concrete.isArrayType())
		{
			this.type.inferTypes(concrete.getElementType(), typeContext);
		}
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.type.getArrayClass().getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}
	
	@Override
	public String getInternalName()
	{
		return this.getExtendedName();
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append('[');
		this.type.appendExtendedName(buffer);
	}
	
	@Override
	public String getSignature()
	{
		String s = this.type.getSignature();
		if (s != null)
		{
			return '[' + s;
		}
		return null;
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append('[');
		this.type.appendSignature(buffer);
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		this.type.writeTypeExpression(writer);
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/type/ArrayType", "apply", "(Ldyvil/lang/Type;)Ldyvil/reflect/type/ArrayType;", false);
	}
	
	@Override
	public void write(DataOutputStream dos) throws IOException
	{
		IType.writeType(this.type, dos);
	}
	
	@Override
	public void read(DataInputStream dis) throws IOException
	{
		this.type = IType.readType(dis);
	}
	
	@Override
	public String toString()
	{
		return "[" + this.type.toString() + "]";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('[');
		this.type.toString(prefix, buffer);
		buffer.append(']');
	}

	@Override
	public IType clone()
	{
		return new ArrayType(this.type);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return this.equals((IType) obj);
	}
	
	@Override
	public int hashCode()
	{
		return 127 * this.type.hashCode();
	}
}
