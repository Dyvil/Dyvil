package dyvilx.tools.compiler.ast.type.compound;

import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.annotation.IAnnotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.Mutability;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.raw.IObjectType;
import dyvilx.tools.compiler.ast.type.typevar.TypeVarType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.util.Markers;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ArrayType implements IObjectType
{
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
	public Mutability getMutability()
	{
		return this.mutability;
	}

	public void setMutability(Mutability mutability)
	{
		this.mutability = mutability;
	}

	public IType getElementType()
	{
		return this.type;
	}

	public void setElementType(IType type)
	{
		this.type = type;
	}

	@Override
	public int typeTag()
	{
		return ARRAY;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.type == null ? null : this.type.getPosition();
	}

	@Override
	public Name getName()
	{
		return this.type.getName();
	}

	@Override
	public boolean isGenericType()
	{
		return this.type.isGenericType();
	}

	@Override
	public IClass getTheClass()
	{
		return this.type.getArrayClass();
	}

	@Override
	public IType asParameterType()
	{
		final IType elementType = this.type.asParameterType();
		return elementType == this.type ? this : new ArrayType(elementType, this.mutability);
	}

	@Override
	public boolean isSameType(IType type)
	{
		final ArrayType arrayType = type.extract(ArrayType.class);
		if (arrayType == null || this.mutability != type.getMutability())
		{
			return false;
		}

		final IType elementType = arrayType.getElementType();
		return this.checkPrimitiveType(elementType) && Types.isSameType(this.type, elementType);
	}

	@Override
	public boolean isSameClass(IType type)
	{
		final ArrayType arrayType = type.extract(ArrayType.class);
		if (arrayType == null)
		{
			return false;
		}

		final IType elementType = arrayType.getElementType();
		return this.checkPrimitiveType(elementType) && this.type.isSameClass(elementType);
	}

	@Override
	public boolean isSuperTypeOf(IType subType)
	{
		final ArrayType arrayType = subType.extract(ArrayType.class);
		if (arrayType == null || !checkImmutable(this, subType))
		{
			return false;
		}

		final IType elementType = arrayType.getElementType();
		return this.checkPrimitiveType(elementType) && Types.isSuperType(this.type, elementType);
	}

	@Override
	public boolean isSuperClassOf(IType subType)
	{
		final ArrayType arrayType = subType.extract(ArrayType.class);
		if (arrayType == null)
		{
			return false;
		}

		final IType elementType = arrayType.getElementType();
		return this.checkPrimitiveType(elementType) && Types.isSuperClass(this.type, elementType);
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

		if (Types.isVoid(this.type))
		{
			markers.add(Markers.semanticError(this.getPosition(), "type.array.void"));
		}
		return this;
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
		if (position == TypePosition.SUPER_TYPE)
		{
			markers.add(Markers.semantic(this.getPosition(), "type.array.super"));
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
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.type.cleanup(compilableList, classCompilableList);
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

		final TypeVarType typeVar = this.type.extract(TypeVarType.class);
		if (typeVar != null && concrete.isPrimitive() && !typeVar.getTypeVariable().isAny())
		{
			concrete = concrete.getObjectType();
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
		final ArrayType arrayType = concrete.extract(ArrayType.class);
		if (arrayType != null)
		{
			this.type.inferTypes(arrayType.getElementType(), typeContext);
		}
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		this.type.getArrayClass().getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.type.getArrayClass().getImplicitMatches(list, value, targetType);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
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
	public void appendDescriptor(StringBuilder buffer, int type)
	{
		buffer.append('[');
		this.type.appendDescriptor(buffer, type == NAME_SIGNATURE_GENERIC_ARG ? NAME_SIGNATURE : type);
	}

	@Override
	public void writeClassExpression(MethodWriter writer, boolean wrapPrimitives) throws BytecodeException
	{
		if (!this.type.hasTypeVariables())
		{
			IObjectType.super.writeClassExpression(writer, wrapPrimitives);
			return;
		}

		this.type.writeClassExpression(writer, wrapPrimitives);

		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/lang/Types",
		                       wrapPrimitives ? "objectArrayType" : "arrayType",
		                       "(Ljava/lang/Class;)Ljava/lang/Class;", false);
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		this.type.writeTypeExpression(writer);
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/types/ArrayType", "apply",
		                       "(Ldyvil/reflect/types/Type;)Ldyvil/reflect/types/ArrayType;", false);
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
		if (this.mutability != Mutability.UNDEFINED)
		{
			this.mutability.writeAnnotation(visitor, typeRef, typePath);
		}
		IType.writeAnnotations(this.type, visitor, typeRef, typePath.concat("["));
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
}
