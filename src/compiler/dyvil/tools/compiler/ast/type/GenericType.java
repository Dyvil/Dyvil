package dyvil.tools.compiler.ast.type;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public final class GenericType extends Type implements ITypeList
{
	public IType[]	generics	= new IType[2];
	public int		genericCount;
	
	public GenericType()
	{
		super();
	}
	
	public GenericType(Name name)
	{
		super(name);
	}
	
	public GenericType(ICodePosition position, Name name)
	{
		super(position, name);
	}
	
	public GenericType(IClass iclass)
	{
		super(iclass);
	}
	
	@Override
	public int typeTag()
	{
		return GENERIC_TYPE;
	}
	
	// ITypeList Overrides
	
	@Override
	public int typeCount()
	{
		return 0;
	}
	
	@Override
	public void setType(int index, IType type)
	{
		this.generics[index] = type;
	}
	
	@Override
	public void addType(IType type)
	{
		int index = this.genericCount++;
		if (this.genericCount > this.generics.length)
		{
			IType[] temp = new IType[this.genericCount];
			System.arraycopy(this.generics, 0, temp, 0, index);
			this.generics = temp;
		}
		this.generics[index] = type;
	}
	
	@Override
	public IType getType(int index)
	{
		return this.generics[index];
	}
	
	// IType Overrides
	
	@Override
	public boolean isGenericType()
	{
		return this.theClass == null || this.theClass.isGeneric();
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		if (typeVar.getGeneric() != this.theClass)
		{
			return this.theClass.resolveType(typeVar, this);
		}
		return this.generics[typeVar.getIndex()];
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar, IType concrete)
	{
		if (!concrete.isGenericType())
		{
			return null;
		}
		
		IType type;
		if (this.equals(concrete))
		{
			IType[] generics = ((GenericType) concrete).generics;
			for (int i = 0; i < this.genericCount; i++)
			{
				type = this.generics[i].resolveType(typeVar, generics[i]);
				if (type != null)
				{
					return type;
				}
			}
		}
		return null;
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		GenericType copy = this.clone();
		for (int i = 0; i < this.genericCount; i++)
		{
			copy.generics[i] = this.generics[i].getConcreteType(context);
		}
		return copy;
	}
	
	@Override
	public IType resolve(MarkerList markers, IContext context)
	{
		if (this.theClass != null)
		{
			return this;
		}
		
		IClass iclass;
		if (this.internalName != null)
		{
			iclass = Package.rootPackage.resolveInternalClass(this.internalName);
		}
		else
		{
			iclass = context.resolveClass(this.name);
		}
		
		if (iclass != null)
		{
			this.theClass = iclass;
			this.internalName = iclass.getInternalName();
			
			if (this.generics == null)
			{
				return this;
			}
			
			int varCount = this.theClass.genericCount();
			if (varCount == 0)
			{
				if (this.genericCount != 0 && markers != null)
				{
					markers.add(this.position, "generic.not_generic", this.name.qualified);
				}
				return this;
			}
			if (varCount != this.genericCount && markers != null)
			{
				markers.add(this.position, "generic.count");
				return this;
			}
			
			if (markers == null)
			{
				for (int i = 0; i < this.genericCount; i++)
				{
					this.generics[i] = this.generics[i].resolve(markers, context);
				}
				return this;
			}
			
			for (int i = 0; i < this.genericCount; i++)
			{
				IType t1 = this.generics[i];
				IType t2 = t1.resolve(markers, context);
				
				if (t2.isPrimitive())
				{
					t2 = t2.getReferenceType();
				}
				
				this.generics[i] = t2;
				
				ITypeVariable var = this.theClass.getTypeVariable(i);
				if (!var.isSuperTypeOf(t2))
				{
					Marker marker = markers.create(t2.getPosition(), "generic.type", var.getName().qualified);
					marker.addInfo("Generic Type: " + t2);
					marker.addInfo("Type Variable: " + var);
				}
			}
			return this;
		}
		if (markers != null)
		{
			markers.add(this.position, "resolve.type", this.toString());
		}
		return this;
	}
	
	@Override
	public String getSignature()
	{
		if (this.generics == null)
		{
			return null;
		}
		
		StringBuilder buf = new StringBuilder();
		this.appendSignature(buf);
		return buf.toString();
	}
	
	@Override
	public void appendSignature(StringBuilder buf)
	{
		buf.append('L').append(this.internalName);
		if (this.generics != null)
		{
			buf.append('<');
			for (int i = 0; i < this.genericCount; i++)
			{
				this.generics[i].appendSignature(buf);
			}
			buf.append('>');
		}
		buf.append(';');
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.writeLDC(this.theClass.getFullName());
		
		writer.writeLDC(this.genericCount);
		writer.writeNewArray("dyvil/lang/Type", 1);
		for (int i = 0; i < this.genericCount; i++)
		{
			writer.writeInsn(Opcodes.DUP);
			writer.writeLDC(i);
			this.generics[i].writeTypeExpression(writer);
			writer.writeInsn(Opcodes.AASTORE);
		}
		
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/type/GenericType", "apply",
				"(Ljava/lang/String;[Ldyvil/lang/Type;)Ldyvil/reflect/type/GenericType;", false);
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		if (this.genericCount > 0)
		{
			builder.append('[').append(this.generics[0].toString());
			for (int i = 1; i < this.genericCount; i++)
			{
				builder.append(", ").append(this.generics[i].toString());
			}
			builder.append(']');
		}
		return builder.toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name);
		if (this.genericCount > 0)
		{
			buffer.append('[');
			Util.astToString(prefix, this.generics, this.genericCount, Formatting.Type.genericSeperator, buffer);
			buffer.append(']');
		}
	}
	
	@Override
	public GenericType clone()
	{
		GenericType t = new GenericType();
		t.theClass = this.theClass;
		t.name = this.name;
		t.internalName = this.internalName;
		if (this.generics != null)
		{
			t.genericCount = this.genericCount;
			t.generics = new IType[this.genericCount];
			System.arraycopy(this.generics, 0, t.generics, 0, this.genericCount);
		}
		return t;
	}
}
