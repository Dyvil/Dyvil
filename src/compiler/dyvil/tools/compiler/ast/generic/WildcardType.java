package dyvil.tools.compiler.ast.generic;

import dyvil.lang.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class WildcardType implements IType
{
	public ICodePosition	position;
	protected IType			upperBound;
	protected IType			lowerBound;
	
	public WildcardType()
	{
	}
	
	public WildcardType(ICodePosition position)
	{
		this.position = position;
	}
	
	public void setUpperBound(IType upperBound)
	{
		this.upperBound = upperBound;
	}
	
	public IType getUpperBound()
	{
		return this.upperBound;
	}
	
	public void setLowerBound(IType lowerBound)
	{
		this.lowerBound = lowerBound;
	}
	
	public IType getLowerBound()
	{
		return this.lowerBound;
	}
	
	@Override
	public int typeTag()
	{
		return WILDCARD_TYPE;
	}
	
	@Override
	public Name getName()
	{
		return null;
	}
	
	@Override
	public IClass getTheClass()
	{
		return this.upperBound != null ? this.upperBound.getTheClass() : Types.OBJECT_CLASS;
	}
	
	@Override
	public IType getSuperType()
	{
		return this.upperBound == null ? Types.UNKNOWN : this.upperBound;
	}
	
	@Override
	public boolean equals(IType type)
	{
		if (this.upperBound != null)
		{
			if (!this.upperBound.isSuperTypeOf(type))
			{
				return false;
			}
		}
		if (this.lowerBound != null)
		{
			if (!type.isSuperTypeOf(this.lowerBound))
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		return this.equals(type);
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType resolve(MarkerList markers, IContext context)
	{
		if (this.upperBound != null)
		{
			this.upperBound = this.upperBound.resolve(markers, context);
		}
		if (this.lowerBound != null)
		{
			this.lowerBound = this.lowerBound.resolve(markers, context);
		}
		
		return this;
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		if (this.upperBound != null)
		{
			return this.upperBound.resolveType(typeVar);
		}
		return Types.ANY;
	}
	
	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		if (this.upperBound != null)
		{
			this.upperBound.inferTypes(concrete, typeContext);
		}
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return false;
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		if (this.lowerBound != null)
		{
			return this.lowerBound.getConcreteType(context);
		}
		
		if (this.upperBound != null)
		{
			WildcardType copy = new WildcardType(this.position);
			copy.upperBound = this.upperBound.getConcreteType(context);
			return copy;
		}
		return this;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (this.upperBound != null)
		{
			return this.upperBound.resolveField(name);
		}
		
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (this.upperBound != null)
		{
			this.upperBound.getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public byte getVisibility(IClassMember member)
	{
		return 0;
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}
	
	@Override
	public String getInternalName()
	{
		if (this.upperBound != null)
		{
			return this.upperBound.getInternalName();
		}
		return "java/lang/Object";
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append('L').append(this.getInternalName()).append(';');
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		if (this.lowerBound != null)
		{
			buffer.append('-');
			this.lowerBound.appendSignature(buffer);
		}
		else if (this.upperBound != null)
		{
			buffer.append('+');
			this.upperBound.appendSignature(buffer);
		}
		else
		{
			buffer.append('*');
		}
	}
	
	@Override
	public int getLoadOpcode()
	{
		return Opcodes.ALOAD;
	}
	
	@Override
	public int getArrayLoadOpcode()
	{
		return Opcodes.AALOAD;
	}
	
	@Override
	public int getStoreOpcode()
	{
		return Opcodes.ASTORE;
	}
	
	@Override
	public int getArrayStoreOpcode()
	{
		return Opcodes.AASTORE;
	}
	
	@Override
	public int getReturnOpcode()
	{
		return Opcodes.ARETURN;
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		if (this.lowerBound != null)
		{
			this.lowerBound.writeTypeExpression(writer);
		}
		else
		{
			writer.writeInsn(Opcodes.ACONST_NULL);
		}
		
		if (this.upperBound != null)
		{
			this.upperBound.writeTypeExpression(writer);
		}
		else
		{
			writer.writeInsn(Opcodes.ACONST_NULL);
		}
		
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/type/WildcardType", "apply",
				"(Ldyvil/lang/Type;[Ldyvil/lang/Type;)Ldyvil/reflect/type/WildcardType;", false);
	}
	
	@Override
	public WildcardType clone()
	{
		WildcardType clone = new WildcardType(this.position);
		clone.lowerBound = this.lowerBound;
		clone.upperBound = this.upperBound;
		return clone;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		this.toString("", sb);
		return sb.toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('_');
		if (this.lowerBound != null)
		{
			buffer.append(Formatting.Type.genericLowerBound);
			this.lowerBound.toString(prefix, buffer);
		}
		if (this.upperBound != null)
		{
			buffer.append(Formatting.Type.genericUpperBound);
			this.upperBound.toString(prefix, buffer);
		}
	}
}
