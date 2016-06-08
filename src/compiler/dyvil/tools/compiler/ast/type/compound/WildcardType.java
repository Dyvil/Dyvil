package dyvil.tools.compiler.ast.type.compound;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.raw.IRawType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class WildcardType implements IRawType, ITyped
{
	public    ICodePosition position;
	protected IType         bound;
	protected Variance      variance;

	public WildcardType()
	{
	}

	public WildcardType(Variance variance)
	{
		this.variance = variance;
	}

	public WildcardType(Variance variance, IType bound)
	{
		this.variance = variance;
		this.bound = bound;
	}

	public WildcardType(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public IType atPosition(ICodePosition position)
	{
		final WildcardType clone = this.clone();
		clone.position = position;
		return clone;
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
	public boolean isGenericType()
	{
		return false;
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
	public IType asReturnType()
	{
		if (this.bound == null || this.variance == Variance.CONTRAVARIANT)
		{
			return Types.ANY;
		}
		return this.bound;
	}

	@Override
	public IType asParameterType()
	{
		if (this.bound == null)
		{
			return this;
		}

		final IType bound = this.bound.asParameterType();
		if (this.variance == Variance.CONTRAVARIANT)
		{
			return bound;
		}

		return bound == this.bound ? this : new WildcardType(this.variance, bound);
	}

	@Override
	public int subTypeCheckLevel()
	{
		return SUBTYPE_WILDCARD;
	}

	@Override
	public boolean isSameType(IType type)
	{
		return this.bound == null || this.variance.checkCompatible(this.bound, type);
	}

	@Override
	public boolean isSuperClassOf(IType subType)
	{
		return this.isSameType(subType);
	}

	@Override
	public boolean isSuperTypeOf(IType subType)
	{
		return this.isSameType(subType);
	}

	@Override
	public boolean isSubClassOf(IType superType)
	{
		return this.isSameType(superType);
	}

	@Override
	public boolean isSubTypeOf(IType superType)
	{
		return this.isSameType(superType);
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		if (this.bound != null)
		{
			this.bound = this.bound.resolveType(markers, context);
		}

		return this;
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		if (this.bound != null)
		{
			this.bound.resolve(markers, context);
		}
	}

	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
		if (this.bound != null)
		{
			this.bound.checkType(markers, context, TypePosition.SUPER_TYPE_ARGUMENT);
		}

		if (position != TypePosition.GENERIC_ARGUMENT)
		{
			markers.add(Markers.semantic(this.position, "type.wildcard.invalid"));
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.bound != null)
		{
			this.bound.check(markers, context);
		}
	}

	@Override
	public void foldConstants()
	{
		if (this.bound != null)
		{
			this.bound.foldConstants();
		}
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.bound != null)
		{
			this.bound.cleanup(context, compilableList);
		}
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		if (this.bound != null)
		{
			return this.bound.resolveType(typeParameter);
		}
		return null;
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
		return this.bound != null && this.bound.hasTypeVariables();
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		if (this.bound == null)
		{
			return this;
		}

		final IType concreteBound = this.bound.getConcreteType(context);
		if (concreteBound == this.bound)
		{
			return this;
		}

		if (concreteBound.typeTag() == WILDCARD_TYPE)
		{
			return concreteBound;
		}

		final WildcardType copy = new WildcardType(this.position);
		copy.variance = this.variance;
		copy.bound = concreteBound;
		return copy;
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
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		if (this.bound != null && this.variance == Variance.COVARIANT)
		{
			this.bound.getMethodMatches(list, receiver, name, arguments);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		if (this.bound != null && this.variance == Variance.COVARIANT)
		{
			this.bound.getImplicitMatches(list, value, targetType);
		}
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, IArguments arguments)
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
		if (this.variance == Variance.CONTRAVARIANT || this.bound == null)
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
		writer.visitFieldInsn(Opcodes.GETSTATIC, "dyvil/reflect/Variance", this.variance.name(),
		                      "Ldyvil/reflect/Variance;");

		if (this.bound != null)
		{
			this.bound.writeTypeExpression(writer);
		}
		else
		{
			writer.visitInsn(Opcodes.ACONST_NULL);
		}

		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvilx/lang/model/type/WildcardType", "apply",
		                       "(Ldyvil/reflect/Variance;Ldyvilx/lang/model/type/Type;)Ldyvilx/lang/model/type/WildcardType;",
		                       false);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		Variance.write(this.variance, out);

		if (this.variance != Variance.INVARIANT)
		{
			IType.writeType(this.bound, out);
		}
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.variance = Variance.read(in);

		if (this.variance != Variance.INVARIANT)
		{
			this.bound = IType.readType(in);
		}
	}

	@Override
	public String toString()
	{
		if (this.bound == null)
		{
			return "_";
		}

		final StringBuilder builder = new StringBuilder();
		this.variance.appendPrefix(builder);
		builder.append(this.bound);
		return builder.toString();
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.bound != null)
		{
			this.variance.appendPrefix(buffer);
			this.bound.toString(prefix, buffer);
		}
		else
		{
			buffer.append('_');
		}
	}

	@Override
	public WildcardType clone()
	{
		WildcardType clone = new WildcardType(this.position);
		clone.variance = this.variance;
		clone.bound = this.bound;
		return clone;
	}
}
