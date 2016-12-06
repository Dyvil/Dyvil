package dyvil.tools.compiler.ast.type.generic;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Type;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.raw.IObjectType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class GenericType implements IObjectType, ITypeList
{
	protected IType[] typeArguments;
	protected int     typeArgumentCount;

	public GenericType()
	{
		this.typeArguments = new IType[2];
	}

	public GenericType(int typeArgumentCount)
	{
		this.typeArguments = new IType[typeArgumentCount];
	}

	public GenericType(IType[] typeArguments, int typeArgumentCount)
	{
		this.typeArguments = typeArguments;
		this.typeArgumentCount = typeArgumentCount;
	}

	@Override
	public int typeCount()
	{
		return this.typeArgumentCount;
	}

	@Override
	public boolean isGenericType()
	{
		return true;
	}

	public int typeArgumentCount()
	{
		return this.typeArgumentCount;
	}

	public IType[] getTypeArguments()
	{
		return this.typeArguments;
	}

	@Override
	public IType getType(int index)
	{
		return this.typeArguments[index];
	}

	@Override
	public void addType(IType type)
	{
		int index = this.typeArgumentCount++;
		if (this.typeArgumentCount > this.typeArguments.length)
		{
			IType[] temp = new IType[this.typeArgumentCount];
			System.arraycopy(this.typeArguments, 0, temp, 0, index);
			this.typeArguments = temp;
		}
		this.typeArguments[index] = type;
	}

	@Override
	public void setType(int index, IType type)
	{
		this.typeArguments[index] = type;
	}

	@Override
	public boolean hasTypeVariables()
	{
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			if (this.typeArguments[i].hasTypeVariables())
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		final IType[] types = getConcreteTypes(this.typeArguments, this.typeArgumentCount, context);
		if (types == this.typeArguments)
		{
			// Nothing changed, no need to create a new instance
			return this;
		}

		final GenericType copy = this.copyName();
		copy.typeArguments = types;
		copy.typeArgumentCount = this.typeArgumentCount;
		return copy;
	}

	public static IType[] getConcreteTypes(IType[] types, int count, ITypeContext context)
	{
		IType[] newTypes = null;
		boolean changed = false;

		for (int i = 0; i < count; i++)
		{
			final IType original = types[i];
			final IType concrete = original.getConcreteType(context);
			if (changed)
			{
				// As soon as changed is true, the array is initialized and we have to copy all elements
				newTypes[i] = concrete;
			}
			else if (concrete != original)
			{
				// If there is a single mismatch, create the array and copy previous elements
				changed = true;
				newTypes = new IType[count];
				newTypes[i] = concrete;
				System.arraycopy(types, 0, newTypes, 0, i);
			}
		}

		return changed ? newTypes : types;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			this.typeArguments[i].resolve(markers, context);
		}
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
		final int argumentPosition = TypePosition.genericArgument(position);

		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			this.typeArguments[i].checkType(markers, context, argumentPosition);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			this.typeArguments[i].check(markers, context);
		}
	}

	@Override
	public void foldConstants()
	{
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			this.typeArguments[i].foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			this.typeArguments[i].cleanup(compilableList, classCompilableList);
		}
	}

	@Override
	public void appendDescriptor(StringBuilder buffer, int type)
	{
		buffer.append('L').append(this.getInternalName());
		if (type != NAME_DESCRIPTOR && this.typeArgumentCount > 0)
		{
			final int parType = type == NAME_FULL ? NAME_FULL : NAME_SIGNATURE_GENERIC_ARG;

			buffer.append('<');
			for (int i = 0; i < this.typeArgumentCount; i++)
			{
				this.typeArguments[i].appendDescriptor(buffer, parType);
			}
			buffer.append('>');
		}
		buffer.append(';');
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.visitLdcInsn(Type.getObjectType(this.getTheClass().getInternalName()));

		writer.visitLdcInsn(this.typeArgumentCount);
		writer.visitTypeInsn(Opcodes.ANEWARRAY, "dyvilx/lang/model/type/Type");
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			writer.visitInsn(Opcodes.DUP);
			writer.visitLdcInsn(i);
			this.typeArguments[i].writeTypeExpression(writer);
			writer.visitInsn(Opcodes.AASTORE);
		}

		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvilx/lang/model/type/GenericType", "apply",
		                       "(Ljava/lang/Class;[Ldyvilx/lang/model/type/Type;)Ldyvilx/lang/model/type/GenericType;",
		                       false);
	}

	@Override
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
		if (typePath.getStep(step) != TypePath.TYPE_ARGUMENT)
		{
			return;
		}

		int index = typePath.getStepArgument(step);
		this.typeArguments[index] = IType.withAnnotation(this.typeArguments[index], annotation, typePath, step + 1,
		                                                 steps);
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			IType.writeAnnotations(this.typeArguments[i], visitor, typeRef, typePath + i + ';');
		}
	}

	protected final void appendFullTypes(StringBuilder builder)
	{
		if (this.typeArgumentCount > 0)
		{
			builder.append('<').append(this.typeArguments[0].toString());
			for (int i = 1; i < this.typeArgumentCount; i++)
			{
				builder.append(", ").append(this.typeArguments[i].toString());
			}
			builder.append('>');
		}
	}

	protected final void writeTypeArguments(DataOutput dos) throws IOException
	{
		dos.writeShort(this.typeArgumentCount);
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			IType.writeType(this.typeArguments[i], dos);
		}
	}

	protected final void readTypeArguments(DataInput dis) throws IOException
	{
		int len = this.typeArgumentCount = dis.readShort();
		if (len > this.typeArguments.length)
		{
			this.typeArguments = new IType[len];
		}
		for (int i = 0; i < len; i++)
		{
			this.typeArguments[i] = IType.readType(dis);
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.getName());

		if (this.typeArgumentCount > 0)
		{
			Formatting.appendSeparator(buffer, "generics.open_bracket", '<');
			Util.astToString(prefix, this.typeArguments, this.typeArgumentCount,
			                 Formatting.getSeparator("generics.separator", ','), buffer);

			if (Formatting.getBoolean("generics.close_bracket.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append('>');
		}
	}

	@Override
	public GenericType clone()
	{
		GenericType type = this.copyName();
		this.copyArgumentsTo(type);
		return type;
	}

	protected abstract GenericType copyName();

	private void copyArgumentsTo(GenericType target)
	{
		target.typeArgumentCount = this.typeArgumentCount;
		target.typeArguments = new IType[this.typeArgumentCount];
		System.arraycopy(this.typeArguments, 0, target.typeArguments, 0, this.typeArgumentCount);
	}
}
