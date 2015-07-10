package dyvil.tools.compiler.ast.generic.type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Util;

public abstract class GenericType implements IType, ITypeList
{
	protected IType[]	typeArguments;
	protected int		typeArgumentCount;
	
	public GenericType()
	{
		this.typeArguments = new IType[2];
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
	public void setType(int index, IType type)
	{
		this.typeArguments[index] = type;
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
	public IType getType(int index)
	{
		return this.typeArguments[index];
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
		writer.writeLDC(iclass == null ? this.getName().qualified : iclass.getFullName());
		
		writer.writeLDC(this.typeArgumentCount);
		writer.writeNewArray("dyvil/lang/Type", 1);
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			writer.writeInsn(Opcodes.DUP);
			writer.writeLDC(i);
			this.typeArguments[i].writeTypeExpression(writer);
			writer.writeInsn(Opcodes.AASTORE);
		}
		
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/type/GenericType", "apply",
				"(Ljava/lang/String;[Ldyvil/lang/Type;)Ldyvil/reflect/type/GenericType;", false);
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
	
	protected final void writeTypeArguments(DataOutputStream dos) throws IOException
	{
		dos.writeShort(this.typeArgumentCount);
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			IType.writeType(this.typeArguments[i], dos);
		}
	}
	
	protected final void readTypeArguments(DataInputStream dis) throws IOException
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
			buffer.append('[');
			Util.astToString(prefix, this.typeArguments, this.typeArgumentCount, Formatting.Type.genericSeperator, buffer);
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
	public abstract IType clone();
	
	@Override
	public boolean equals(Object obj)
	{
		return IType.equals(this, obj);
	}
	
	@Override
	public int hashCode()
	{
		return System.identityHashCode(this.getTheClass());
	}
}
