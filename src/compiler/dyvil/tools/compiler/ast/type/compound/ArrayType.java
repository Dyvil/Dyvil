package dyvil.tools.compiler.ast.type.compound;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Mutability;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.raw.IObjectType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ArrayType implements IObjectType, ITyped
{
	public static final int OBJECT_DISTANCE = 2;

	protected IType type;
	protected Mutability mutability = Mutability.UNDEFINED;

	public ArrayType()
	{
	}
	
	public ArrayType(IType type)
	{
		this.type = type;
	}

	public ArrayType(IType type, Mutability mutability)
	{
		this.mutability = mutability;
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
	public boolean isGenericType()
	{
		return false;
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
	public Mutability getMutability()
	{
		return this.mutability;
	}

	public void setMutability(Mutability mutability)
	{
		this.mutability = mutability;
	}

	@Override
	public IType getElementType()
	{
		return this.type;
	}
	
	@Override
	public IMethod getBoxMethod()
	{
		return null;
	}
	
	@Override
	public IMethod getUnboxMethod()
	{
		return null;
	}
	
	@Override
	public IType getSuperType()
	{
		return Types.OBJECT;
	}
	
	@Override
	public boolean isSameType(IType type)
	{
		return type.isArrayType() && this.mutability == type.getMutability() && this.type
				.isSameType(type.getElementType());
	}
	
	@Override
	public boolean classEquals(IType type)
	{
		if (!type.isArrayType())
		{
			return false;
		}

		final IType elementType = type.getElementType();
		return this.checkPrimitiveType(elementType) && this.type.classEquals(elementType);
	}

	@Override
	public boolean isSuperTypeOf(IType type)
	{
		if (!type.isArrayType())
		{
			return false;
		}
		if (!checkImmutable(this, type))
		{
			return false;
		}

		final IType elementType = type.getElementType();
		return this.checkPrimitiveType(elementType) && this.type.isSuperTypeOf(elementType);
	}

	@Override
	public boolean isSuperClassOf(IType type)
	{
		if (!type.isArrayType())
		{
			return false;
		}

		final IType elementType = type.getElementType();
		return this.checkPrimitiveType(elementType) && this.type.isSuperClassOf(elementType);
	}

	@Override
	public int getSuperTypeDistance(IType superType)
	{
		if (!superType.isArrayType())
		{
			return superType.getTheClass() == Types.OBJECT_CLASS ? OBJECT_DISTANCE : 0;
		}
		if (!checkImmutable(superType, this))
		{
			return 0;
		}

		IType elementType = superType.getElementType();
		if (!this.checkPrimitiveType(elementType))
		{
			return 0;
		}
		return this.type.getSuperTypeDistance(elementType);
	}

	@Override
	public float getSubTypeDistance(IType subtype)
	{
		if (!subtype.isArrayType())
		{
			return 0F;
		}
		if (!checkImmutable(this, subtype))
		{
			return 0F;
		}

		final IType elementType = subtype.getElementType();
		if (!this.checkPrimitiveType(elementType))
		{
			return 0F;
		}
		return this.type.getSubTypeDistance(elementType);
	}

	@Override
	public int getSubClassDistance(IType subtype)
	{
		if (!subtype.isArrayType())
		{
			return 0;
		}
		if (checkImmutable(this, subtype))
		{
			return 0;
		}

		final IType elementType = subtype.getElementType();
		if (!this.checkPrimitiveType(elementType))
		{
			return 0;
		}
		return this.type.getSubClassDistance(subtype.getElementType());
	}

	private static boolean checkImmutable(IType superType, IType subtype)
	{
		return superType.getMutability() == Mutability.UNDEFINED || superType.getMutability() == subtype
				.getMutability();
	}

	private boolean checkPrimitiveType(IType elementType)
	{
		return this.type.isPrimitive() == elementType.isPrimitive();
	}
	
	@Override
	public boolean isResolved()
	{
		return this.type.isResolved();
	}
	
	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		if (this.type == null)
		{
			this.type = Types.ANY;
		}
		this.type = this.type.resolveType(markers, context);
		return this;
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
	}
	
	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
		if (position == TypePosition.SUPER_TYPE)
		{
			markers.add(Markers.semantic(this.type.getPosition(), "type.super.array"));
		}
		
		this.type.checkType(markers, context, TypePosition.SUPER_TYPE_ARGUMENT);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
	}
	
	@Override
	public void foldConstants()
	{
		this.type.foldConstants();
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.type.cleanup(context, compilableList);
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return this.type.hasTypeVariables();
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		IType concrete = this.type.getConcreteType(context);

		final ITypeParameter typeParameter = this.type.getTypeVariable();
		if (typeParameter != null)
		{
			if (!this.type.isPrimitive() && concrete.isPrimitive())
			{
				concrete = concrete.getObjectType();
			}
			if (concrete != null && concrete != this.type)
			{
				return new ArrayType(concrete, this.mutability)
				{
					@Override
					public IType getReturnType()
					{
						return new ArrayType(typeParameter.getDefaultType(), this.mutability);
					}
				};
			}
			return this;
		}

		if (concrete != null && concrete != this.type)
		{
			return new ArrayType(concrete, this.mutability);
		}

		return this;
	}
	
	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		return this.type.resolveType(typeParameter);
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
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		this.type.getArrayClass().getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
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
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvilx/lang/model/type/ArrayType", "apply",
		                       "(Ldyvilx/lang/model/type/Type;)Ldyvilx/lang/model/type/ArrayType;", false);
	}
	
	@Override
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
		if (step == steps)
		{
			this.mutability = Mutability.readAnnotation(annotation);
		}

		if (step >= steps || typePath.getStep(step) != TypePath.ARRAY_ELEMENT)
		{
			return;
		}

		this.type = IType.withAnnotation(this.type, annotation, typePath, step + 1, steps);
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		this.type.writeAnnotations(visitor, typeRef, typePath.concat("["));
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		IType.writeType(this.type, out);
		this.mutability.write(out);
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		this.type = IType.readType(in);
		this.mutability = Mutability.read(in);
	}
	
	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder().append('[');
		this.mutability.appendKeyword(builder);
		builder.append(this.type.toString());
		return builder.append(']').toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('[');
		this.mutability.appendKeyword(buffer);
		this.type.toString(prefix, buffer);
		buffer.append(']');
	}
	
	@Override
	public IType clone()
	{
		return new ArrayType(this.type, this.mutability);
	}
}
