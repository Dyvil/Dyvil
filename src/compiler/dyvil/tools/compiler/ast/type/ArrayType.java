package dyvil.tools.compiler.ast.type;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.method.ConstructorMatchList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.AnnotationUtils;
import dyvil.tools.compiler.util.MarkerMessages;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ArrayType implements IObjectType, ITyped
{
	public static final int OBJECT_DISTANCE = 2;

	private IType   type;
	private boolean immutable;

	public ArrayType()
	{
	}
	
	public ArrayType(IType type)
	{
		this.type = type;
	}

	public ArrayType(IType type, boolean immutable)
	{
		this.immutable = immutable;
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

	public void setImmutable(boolean immutable)
	{
		this.immutable = immutable;
	}

	@Override
	public boolean isImmutable()
	{
		return this.immutable;
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
		return type.isArrayType() && this.type.isSameType(type.getElementType());
	}
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		if (!type.isArrayType())
		{
			return false;
		}

		IType elementType = type.getElementType();
		return this.type.isSuperTypeOf(elementType) && this.type.isPrimitive() == elementType.isPrimitive();
	}
	
	@Override
	public boolean classEquals(IType type)
	{
		if (!type.isArrayType())
		{
			return false;
		}

		IType elementType = type.getElementType();
		return this.type.classEquals(elementType) && this.type.isPrimitive() == elementType.isPrimitive();
	}
	
	@Override
	public boolean isSuperClassOf(IType type)
	{
		if (!type.isArrayType())
		{
			return false;
		}

		IType elementType = type.getElementType();
		return this.type.isSuperClassOf(elementType) && this.type.isPrimitive() == elementType.isPrimitive();
	}
	
	@Override
	public int getSuperTypeDistance(IType superType)
	{
		if (!superType.isArrayType())
		{
			return superType.getTheClass() == Types.OBJECT_CLASS ? OBJECT_DISTANCE : 0;
		}

		IType elementType = superType.getElementType();
		if (this.type.isPrimitive() || elementType.isPrimitive())
		{
			return this.type.isSameType(elementType) ? 1 : 0;
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
		IType elementType = subtype.getElementType();
		if (this.type.isPrimitive() || elementType.isPrimitive())
		{
			return this.type.isSameType(elementType) ? 1 : 0;
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
		IType elementType = subtype.getElementType();
		if (this.type.isPrimitive() || elementType.isPrimitive())
		{
			return this.type.isSameType(elementType) ? 1 : 0;
		}
		return this.type.getSubClassDistance(subtype.getElementType());
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
			markers.add(MarkerMessages.createMarker(this.type.getPosition(), "type.super.array"));
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
		if (!this.type.isPrimitive() && concrete.isPrimitive())
		{
			concrete = concrete.getObjectType();
		}
		if (concrete != null && concrete != this.type)
		{
			return new ArrayType(concrete, this.immutable);
		}
		return this;
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
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/types/ArrayType", "apply",
		                       "(Ldyvil/lang/Type;)Ldyvil/reflect/types/ArrayType;", false);
	}
	
	@Override
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
		if (step == steps && AnnotationUtils.IMMUTABLE.equals(annotation.getType().getInternalName()))
		{
			this.immutable = true;
			return;
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

		if (this.immutable)
		{
			visitor.visitTypeAnnotation(typeRef, TypePath.fromString(typePath), AnnotationUtils.IMMUTABE_EXTENDED,
			                            true);
		}
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		IType.writeType(this.type, out);
		out.writeBoolean(this.immutable);
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		this.type = IType.readType(in);
		this.immutable = in.readBoolean();
	}
	
	@Override
	public String toString()
	{
		return (this.immutable ? "[final " : "[") + this.type.toString() + "]";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('[');
		if (this.immutable)
		{
			buffer.append("final ");
		}
		this.type.toString(prefix, buffer);
		buffer.append(']');
	}
	
	@Override
	public IType clone()
	{
		return new ArrayType(this.type, this.immutable);
	}
}
