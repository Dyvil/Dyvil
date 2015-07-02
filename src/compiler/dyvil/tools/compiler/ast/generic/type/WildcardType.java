package dyvil.tools.compiler.ast.generic.type;

import dyvil.lang.List;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class WildcardType implements IType, ITyped
{
	public ICodePosition	position;
	protected IType			bound;
	protected Variance		variance;
	
	public WildcardType(Variance variance)
	{
		this.variance = variance;
	}
	
	public WildcardType(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public void setType(IType upperBound)
	{
		this.bound = upperBound;
	}
	
	@Override
	public IType getType()
	{
		return this.bound;
	}
	
	public void setVariance(Variance variance)
	{
		this.variance = variance;
	}
	
	public Variance getVariance()
	{
		return this.variance;
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
		return this.bound != null ? this.bound.getTheClass() : Types.OBJECT_CLASS;
	}
	
	@Override
	public IType getReturnType()
	{
		if (this.bound == null || this.variance == Variance.CONTRAVARIANT)
		{
			return Types.ANY;
		}
		return this.bound;
	}
	
	@Override
	public IType getSuperType()
	{
		return this.bound == null ? Types.UNKNOWN : this.bound;
	}
	
	@Override
	public boolean equals(IType type)
	{
		if (this.bound != null)
		{
			return this.variance.checkCompatible(this.bound, type);
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
	public IType resolve(MarkerList markers, IContext context, TypePosition position)
	{
		if (this.bound != null)
		{
			this.bound = this.bound.resolve(markers, context, TypePosition.SUPER_TYPE_ARGUMENT);
		}
		
		if (position != TypePosition.GENERIC_ARGUMENT)
		{
			markers.add(this.position, "type.invalid.wildcard");
			return this.bound == null ? Types.ANY : this.bound;
		}
		
		return this;
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		if (this.bound != null && this.variance == Variance.COVARIANT)
		{
			return this.bound.resolveType(typeVar);
		}
		return Types.ANY;
	}
	
	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		if (this.bound != null)
		{
			this.bound.inferTypes(concrete, typeContext);
		}
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return this.bound == null ? false : this.bound.hasTypeVariables();
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		if (this.bound != null)
		{
			WildcardType copy = new WildcardType(this.position);
			copy.variance = this.variance;
			copy.bound = this.bound.getConcreteType(context);
			return copy;
		}
		return this;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (this.bound != null && this.variance == Variance.COVARIANT)
		{
			return this.bound.resolveField(name);
		}
		
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		if (this.bound != null && this.variance == Variance.COVARIANT)
		{
			this.bound.getMethodMatches(list, instance, name, arguments);
		}
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
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
		if (this.variance == Variance.CONTRAVARIANT)
		{
			return "java/lang/Object";
		}
		return this.bound.getInternalName();
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append('L').append(this.getInternalName()).append(';');
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		if (this.bound != null)
		{
			this.variance.appendPrefix(buffer);
			this.bound.appendSignature(buffer);
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
		writer.writeFieldInsn(Opcodes.GETSTATIC, "dyvil/reflect/Variance", this.variance.name(), "Ldyvil/reflect/Variance;");
		
		if (this.bound != null)
		{
			this.bound.writeTypeExpression(writer);
		}
		else
		{
			writer.writeInsn(Opcodes.ACONST_NULL);
		}
		
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/type/WildcardType", "apply",
				"(Ldyvil/reflect/Variance;Ldyvil/lang/Type;)Ldyvil/reflect/type/WildcardType;", false);
	}
	
	@Override
	public WildcardType clone()
	{
		WildcardType clone = new WildcardType(this.position);
		clone.variance = this.variance;
		clone.bound = this.bound;
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
		if (this.bound != null)
		{
			this.variance.appendInfix(buffer);
			this.bound.toString(prefix, buffer);
		}
	}
}
