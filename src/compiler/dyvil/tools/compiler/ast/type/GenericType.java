package dyvil.tools.compiler.ast.type;

import dyvil.lang.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public final class GenericType extends ASTNode implements IType, ITypeList
{
	protected Name		name;
	protected String	internalName;
	protected IClass	theClass;
	public IType[]		typeArguments	= new IType[2];
	public int			typeArgumentCount;
	
	public GenericType()
	{
	}
	
	public GenericType(Name name)
	{
		this.name = name;
	}
	
	public GenericType(ICodePosition position, Name name)
	{
		this.position = position;
		this.name = name;
	}
	
	public GenericType(String internalName, Name name)
	{
		this.internalName = internalName;
		this.name = name;
	}
	
	public GenericType(IClass iclass)
	{
		this.theClass = iclass;
		this.internalName = iclass.getInternalName();
		this.name = iclass.getName();
	}
	
	@Override
	public int typeTag()
	{
		return GENERIC;
	}
	
	// ITypeList Overrides
	
	@Override
	public boolean isGenericType()
	{
		return this.theClass == null || this.theClass.isGeneric();
	}
	
	@Override
	public Name getName()
	{
		return this.name;
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
	
	// IType Overrides
	
	@Override
	public IClass getTheClass()
	{
		return this.theClass;
	}
	
	@Override
	public boolean equals(IType type)
	{
		if (this == type)
		{
			return true;
		}
		
		if (!IType.super.equals(type))
		{
			return false;
		}
		
		return this.argumentsMatch(type);
	}
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		if (this == type)
		{
			return true;
		}
		
		if (!IType.super.isSuperTypeOf(type))
		{
			return false;
		}
		
		return this.argumentsMatch(type);
	}
	
	protected boolean argumentsMatch(IType type)
	{
		int count = this.theClass.genericCount();
		for (int i = 0; i < count; i++)
		{
			ITypeVariable typeVar = this.theClass.getTypeVariable(i);
			
			IType otherType = type.resolveType(typeVar);
			if (!this.typeArguments[i].equals(otherType))
			{
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		if (typeVar.getGeneric() != this.theClass)
		{
			if (this.theClass == null)
			{
				return Types.ANY;
			}
			return this.theClass.resolveType(typeVar, this);
		}
		return this.typeArguments[typeVar.getIndex()];
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
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			ITypeVariable typeVar = this.theClass.getTypeVariable(i);
			IType concreteType = concrete.resolveType(typeVar);
			this.typeArguments[i].inferTypes(concreteType, typeContext);
		}
	}
	
	@Override
	public boolean isResolved()
	{
		return this.theClass != null;
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
			iclass = IContext.resolveClass(context, this.name);
		}
		
		if (iclass != null)
		{
			this.theClass = iclass;
			this.internalName = iclass.getInternalName();
			
			if (this.typeArguments == null)
			{
				return this;
			}
			
			int varCount = this.theClass.genericCount();
			if (varCount == 0)
			{
				if (this.typeArgumentCount != 0 && markers != null)
				{
					markers.add(this.position, "generic.not_generic", this.name.qualified);
				}
				return this;
			}
			if (varCount != this.typeArgumentCount && markers != null)
			{
				markers.add(this.position, "generic.count");
				return this;
			}
			
			if (markers == null)
			{
				for (int i = 0; i < this.typeArgumentCount; i++)
				{
					this.typeArguments[i] = this.typeArguments[i].resolve(markers, context);
				}
				return this;
			}
			
			for (int i = 0; i < this.typeArgumentCount; i++)
			{
				IType t1 = this.typeArguments[i];
				IType t2 = t1.resolve(markers, context);
				
				if (t2.isPrimitive())
				{
					t2 = t2.getReferenceType();
				}
				
				this.typeArguments[i] = t2;
				
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
	public IDataMember resolveField(Name name)
	{
		if (this.theClass == null)
		{
			return null;
		}
		return this.theClass.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (this.theClass != null)
		{
			this.theClass.getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		if (this.theClass != null)
		{
			this.theClass.getConstructorMatches(list, arguments);
		}
	}
	
	@Override
	public byte getVisibility(IClassMember member)
	{
		return 0;
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return this.theClass == null ? null : this.theClass.getFunctionalMethod();
	}
	
	@Override
	public String getInternalName()
	{
		return this.internalName;
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
		buffer.append('L').append(this.internalName).append(';');
	}
	
	@Override
	public void appendSignature(StringBuilder buf)
	{
		buf.append('L').append(this.internalName);
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
		writer.writeLDC(this.theClass.getFullName());
		
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
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(this.theClass != null ? this.theClass.getFullName() : this.name.toString());
		if (this.typeArgumentCount > 0)
		{
			builder.append('[').append(this.typeArguments[0].toString());
			for (int i = 1; i < this.typeArgumentCount; i++)
			{
				builder.append(", ").append(this.typeArguments[i].toString());
			}
			builder.append(']');
		}
		return builder.toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name);
		if (this.typeArgumentCount > 0)
		{
			buffer.append('[');
			Util.astToString(prefix, this.typeArguments, this.typeArgumentCount, Formatting.Type.genericSeperator, buffer);
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
		if (this.typeArguments != null)
		{
			t.typeArgumentCount = this.typeArgumentCount;
			t.typeArguments = new IType[this.typeArgumentCount];
			System.arraycopy(this.typeArguments, 0, t.typeArguments, 0, this.typeArgumentCount);
		}
		return t;
	}
}
