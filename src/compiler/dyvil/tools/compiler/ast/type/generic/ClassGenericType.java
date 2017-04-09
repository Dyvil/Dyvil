package dyvil.tools.compiler.ast.type.generic;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.generic.TypeParameterList;
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TypeList;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ClassGenericType extends GenericType
{
	protected IClass theClass;

	public ClassGenericType()
	{
	}

	public ClassGenericType(IClass iclass)
	{
		this.theClass = iclass;
	}

	public ClassGenericType(IClass iclass, IType... arguments)
	{
		super(arguments);
		this.theClass = iclass;
	}

	public ClassGenericType(IClass iclass, TypeList arguments)
	{
		super(arguments);
		this.theClass = iclass;
	}

	@Override
	public int typeTag()
	{
		return GENERIC;
	}

	@Override
	public IType atPosition(ICodePosition position)
	{
		return new ResolvedGenericType(position, this.getTheClass(), this.arguments);
	}

	// TypeList Overrides

	@Override
	public boolean isGenericType()
	{
		return this.getTheClass().isTypeParametric();
	}

	@Override
	public Name getName()
	{
		return this.getTheClass().getName();
	}

	// IType Overrides

	@Override
	public IClass getTheClass()
	{
		return this.theClass;
	}

	@Override
	public boolean isSameType(IType type)
	{
		return this == type || super.isSameType(type) && this.argumentsMatch(type);
	}

	@Override
	public boolean isSuperTypeOf(IType subType)
	{
		return this == subType || super.isSuperTypeOf(subType) && (!subType.isGenericType() || this.argumentsMatch(
			subType));
	}

	protected boolean argumentsMatch(IType type)
	{
		final int count = Math.min(this.arguments.size(), this.getTheClass().typeArity());
		final TypeParameterList classTypeParams = this.getTheClass().getTypeParameters();
		for (int i = 0; i < count; i++)
		{
			final ITypeParameter typeVar = classTypeParams.get(i);
			final IType thisArgument = this.arguments.get(i);
			final IType thatArgument = Types.resolveTypeSafely(type, typeVar);

			if (!Variance.checkCompatible(typeVar.getVariance(), thisArgument, thatArgument))
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		if (typeParameter.getGeneric() != this.getTheClass())
		{
			return this.getTheClass().resolveType(typeParameter, this);
		}
		return this.arguments.get(typeParameter.getIndex()).asReturnType();
	}

	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		final TypeParameterList classTypeParams = this.getTheClass().getTypeParameters();
		for (int i = 0, size = this.arguments.size(); i < size; i++)
		{
			final ITypeParameter typeVar = classTypeParams.get(i);
			final IType concreteType = concrete.resolveType(typeVar);
			if (concreteType != null)
			{
				this.arguments.get(i).inferTypes(concreteType, typeContext);
			}
		}
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.arguments.resolveTypes(markers, context);
		return this;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.getTheClass().resolveField(name);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		this.getTheClass().getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.getTheClass().getImplicitMatches(list, value, targetType);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
	{
		this.getTheClass().getConstructorMatches(list, arguments);
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		return this.getTheClass().getFunctionalMethod();
	}

	@Override
	public String getInternalName()
	{
		return this.getTheClass().getInternalName();
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(this.getTheClass().getInternalName());
		this.arguments.write(out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		String internal = in.readUTF();
		this.theClass = Package.rootPackage.resolveInternalClass(internal);
		this.arguments.read(in);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(this.getTheClass().getFullName());
		this.appendFullTypes(sb);
		return sb.toString();
	}

	@Override
	protected GenericType withArguments(TypeList arguments)
	{
		return new ClassGenericType(this.getTheClass(), arguments);
	}
}
