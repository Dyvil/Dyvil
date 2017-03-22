package dyvil.tools.compiler.ast.type.generic;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.reference.ReferenceType;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TypeList;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.LambdaType;
import dyvil.tools.compiler.ast.type.compound.MapType;
import dyvil.tools.compiler.ast.type.compound.TupleType;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class InternalGenericType extends GenericType
{
	protected String internalName;

	public InternalGenericType(String internal)
	{
		this.internalName = internal;
	}

	public InternalGenericType(String internalName, TypeList arguments)
	{
		super(arguments);
		this.internalName = internalName;
	}

	@Override
	public int typeTag()
	{
		return GENERIC_INTERNAL;
	}

	@Override
	public Name getName()
	{
		return Name.fromRaw(this.internalName.substring(this.internalName.lastIndexOf('/') + 1));
	}

	@Override
	public IClass getTheClass()
	{
		return Types.OBJECT_CLASS;
	}

	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		return null;
	}

	@Override
	public boolean isResolved()
	{
		return false;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.arguments.resolveTypes(markers, context);

		if (this.internalName.startsWith("dyvil/tuple/Tuple$Of"))
		{
			return new TupleType(this.arguments);
		}
		if (this.internalName.startsWith("dyvil/function/Function$Of"))
		{
			return new LambdaType(this.arguments);
		}

		switch (this.internalName)
		{
		case "dyvil/ref/ObjectRef":
			return new ReferenceType(ReferenceType.LazyFields.OBJECT_REF_CLASS, this.arguments.get(0));
		case "dyvil/collection/Map":
			return MapType.base(this.arguments.get(0), this.arguments.get(1));
		case "dyvil/collection/MutableMap":
			return MapType.mutable(this.arguments.get(0), this.arguments.get(1));
		case "dyvil/collection/ImmutableMap":
			return MapType.immutable(this.arguments.get(0), this.arguments.get(1));
		}

		final IClass iclass = Package.rootPackage.resolveInternalClass(this.internalName);
		return new ClassGenericType(iclass, this.arguments);
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
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
		return this.internalName;
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(this.internalName);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.internalName = in.readUTF();
	}

	@Override
	protected GenericType withArguments(TypeList arguments)
	{
		return new InternalGenericType(this.internalName, arguments);
	}
}
