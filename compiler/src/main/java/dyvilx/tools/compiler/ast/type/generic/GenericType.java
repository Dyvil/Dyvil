package dyvilx.tools.compiler.ast.type.generic;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Opcodes;
import dyvilx.tools.asm.Type;
import dyvilx.tools.asm.TypeAnnotatableVisitor;
import dyvilx.tools.asm.TypePath;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.raw.IObjectType;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

public abstract class GenericType implements IObjectType
{
	protected @NonNull TypeList arguments;

	public GenericType()
	{
		this.arguments = new TypeList();
	}

	public GenericType(int capacity)
	{
		this.arguments = new TypeList(capacity);
	}

	public GenericType(IType... arguments)
	{
		this.arguments = new TypeList(arguments);
	}

	public GenericType(TypeList arguments)
	{
		this.arguments = arguments;
	}

	@Override
	public boolean isGenericType()
	{
		return true;
	}

	public TypeList getArguments()
	{
		return this.arguments;
	}

	@Override
	public boolean hasTypeVariables()
	{
		for (int i = 0, size = this.arguments.size(); i < size; i++)
		{
			if (this.arguments.get(i).hasTypeVariables())
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		final TypeList types = getConcreteTypes(this.arguments, context);
		if (types == this.arguments)
		{
			// Nothing changed, no need to create a new instance
			return this;
		}

		return this.withArguments(types);
	}

	public static TypeList getConcreteTypes(TypeList types, ITypeContext context)
	{
		TypeList newTypes = null;

		for (int i = 0, count = types.size(); i < count; i++)
		{
			final IType original = types.get(i);
			final IType concrete = original.getConcreteType(context);
			if (newTypes != null)
			{
				newTypes.set(i, concrete);
			}
			else if (concrete != original)
			{
				// If there is a single mismatch, create the array and copy previous elements
				newTypes = types.copy();
				newTypes.set(i, concrete);
			}
		}

		return newTypes != null ? newTypes : types;
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.arguments.resolve(markers, context);
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
		final int argumentPosition = TypePosition.genericArgument(position);
		this.arguments.checkTypes(markers, context, argumentPosition);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.arguments.check(markers, context);
	}

	@Override
	public void foldConstants()
	{
		this.arguments.foldConstants();
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.arguments.cleanup(compilableList, classCompilableList);
	}

	@Override
	public void appendDescriptor(StringBuilder buffer, int type)
	{
		buffer.append('L').append(this.getInternalName());
		if (type != NAME_DESCRIPTOR && this.arguments.size() > 0)
		{
			final int parType = type == NAME_FULL ? NAME_FULL : NAME_SIGNATURE_GENERIC_ARG;

			buffer.append('<');
			this.arguments.appendDescriptors(buffer, parType);
			buffer.append('>');
		}
		buffer.append(';');
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.visitLdcInsn(Type.getObjectType(this.getTheClass().getInternalName()));

		final int size = this.arguments.size();
		writer.visitLdcInsn(size);
		writer.visitTypeInsn(Opcodes.ANEWARRAY, "dyvil/reflect/types/Type");
		for (int i = 0; i < size; i++)
		{
			writer.visitInsn(Opcodes.DUP);
			writer.visitLdcInsn(i);
			this.arguments.get(i).writeTypeExpression(writer);
			writer.visitInsn(Opcodes.AASTORE);
		}

		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/types/GenericType", "apply",
		                       "(Ljava/lang/Class;[Ldyvil/reflect/types/Type;)Ldyvil/reflect/types/GenericType;",
		                       false);
	}

	@Override
	public void addAnnotation(Annotation annotation, TypePath typePath, int step, int steps)
	{
		if (typePath.getStep(step) != TypePath.TYPE_ARGUMENT)
		{
			return;
		}

		final int index = typePath.getStepArgument(step);
		this.arguments
			.set(index, IType.withAnnotation(this.arguments.get(index), annotation, typePath, step + 1, steps));
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		for (int i = 0, size = this.arguments.size(); i < size; i++)
		{
			IType.writeAnnotations(this.arguments.get(i), visitor, typeRef, typePath + i + ";");
		}
	}

	protected final void appendFullTypes(StringBuilder builder)
	{
		final int size = this.arguments.size();
		if (size > 0)
		{
			builder.append('<');
			builder.append(this.arguments.get(0).toString());
			for (int i = 1; i < size; i++)
			{
				builder.append(", ").append(this.arguments.get(i).toString());
			}
			builder.append('>');
		}
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append(this.getName());

		if (this.arguments.size() > 0)
		{
			this.arguments.toString(indent, buffer);
		}
	}

	protected abstract GenericType withArguments(TypeList arguments);
}
