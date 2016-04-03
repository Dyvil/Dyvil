package dyvil.tools.compiler.ast.type.generic;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
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
	public IType asParameterType()
	{
		final GenericType copy = this.clone();
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			copy.typeArguments[i] = this.typeArguments[i].asParameterType();
		}
		return copy;
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
		GenericType copy = this.clone();
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			copy.typeArguments[i] = this.typeArguments[i].getConcreteType(context);
		}
		return copy;
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
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
		final TypePosition argumentPosition =
				position == TypePosition.SUPER_TYPE ? TypePosition.SUPER_TYPE_ARGUMENT : TypePosition.GENERIC_ARGUMENT;

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
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			this.typeArguments[i].cleanup(context, compilableList);
		}
	}
	
	@Override
	public String getSignature()
	{
		if (this.typeArgumentCount <= 0)
		{
			return null;
		}
		
		StringBuilder buf = new StringBuilder();
		this.appendSignature(buf);
		return buf.toString();
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append('L').append(this.getInternalName()).append(';');
	}
	
	@Override
	public void appendSignature(StringBuilder buf)
	{
		buf.append('L').append(this.getInternalName());
		if (this.typeArguments != null)
		{
			buf.append('<');
			for (int i = 0; i < this.typeArgumentCount; i++)
			{
				this.typeArguments[i].appendSignature(buf);
			}
			buf.append('>');
		}
		buf.append(';');
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		IClass iclass = this.getTheClass();
		writer.visitLdcInsn(iclass == null ? this.getName().qualified : iclass.getFullName());
		
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
		                       "(Ljava/lang/String;[Ldyvilx/lang/model/type/Type;)Ldyvilx/lang/model/type/GenericType;",
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
		this.typeArguments[index] = IType
				.withAnnotation(this.typeArguments[index], annotation, typePath, step + 1, steps);
	}
	
	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			this.typeArguments[i].writeAnnotations(visitor, typeRef, typePath + i + ';');
		}
	}
	
	protected final void appendFullTypes(StringBuilder builder)
	{
		if (this.typeArgumentCount > 0)
		{
			builder.append('[').append(this.typeArguments[0].toString());
			for (int i = 1; i < this.typeArgumentCount; i++)
			{
				builder.append(", ").append(this.typeArguments[i].toString());
			}
			builder.append(']');
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
			Formatting.appendSeparator(buffer, "generics.open_bracket", '[');
			Util.astToString(prefix, this.typeArguments, this.typeArgumentCount,
			                 Formatting.getSeparator("generics.separator", ','), buffer);

			if (Formatting.getBoolean("generics.close_bracket.space_before"))
			{
				buffer.append(' ');
			}
			buffer.append(']');
		}
	}
	
	protected final void copyTypeArguments(GenericType agt)
	{
		agt.typeArgumentCount = this.typeArgumentCount;
		agt.typeArguments = new IType[this.typeArgumentCount];
		System.arraycopy(this.typeArguments, 0, agt.typeArguments, 0, this.typeArgumentCount);
	}
	
	@Override
	public abstract GenericType clone();
}
