package dyvil.tools.compiler.ast.type.generic;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.raw.ClassType;
import dyvil.tools.compiler.ast.type.raw.NamedType;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NamedGenericType extends GenericType
{
	protected IType         parent;
	protected ICodePosition position;
	protected Name          name;
	
	public NamedGenericType(ICodePosition position, Name name)
	{
		this.position = position;
		this.name = name;
	}

	public NamedGenericType(ICodePosition position, Name name, IType parent)
	{
		this.position = position;
		this.name = name;
		this.parent = parent;
	}
	
	@Override
	public int typeTag()
	{
		return GENERIC_NAMED;
	}
	
	@Override
	public Name getName()
	{
		return this.name;
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
	public boolean isSuperClassOf(IType subType)
	{
		return false;
	}

	@Override
	public boolean isSuperTypeOf(IType type)
	{
		return false;
	}

	@Override
	public boolean isResolved()
	{
		return false;
	}
	
	private void resolveTypeArguments(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			this.typeArguments[i] = this.typeArguments[i].resolveType(markers, context);
		}
	}
	
	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		// resolveType0 is used to avoid Type Variable -> Default Value replacement done by resolveType
		final IType resolved = new NamedType(this.position, this.name, this.parent).resolveType0(markers, context);

		this.resolveTypeArguments(markers, context);

		if (!resolved.isResolved())
		{
			return this;
		}

		final IClass iClass = resolved.getTheClass();
		final IType concrete;

		// Convert the non-generic class type to a generic one
		if (!resolved.isGenericType())
		{
			if (!iClass.isTypeParametric())
			{
				markers.add(
						Markers.semanticError(this.position, "type.generic.class_not_generic", iClass.getFullName()));
				return new ClassType(iClass);
			}

			concrete = new ResolvedGenericType(this.position, iClass, this.typeArguments, this.typeArgumentCount);
		}
		else
		{
			// Create a concrete type and save Type Variables in the above array
			concrete = resolved.getConcreteType(typeParameter -> {
				final int index = typeParameter.getIndex();
				if (index >= this.typeArgumentCount)
				{
					return null;
				}
				return this.typeArguments[index];
			});
		}

		return concrete;
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(MethodMatchList list, IValue receiver, Name name, IArguments arguments)
	{
	}

	@Override
	public void getImplicitMatches(MethodMatchList list, IValue value, IType targetType)
	{
	}

	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
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
		return this.name.qualified;
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(this.name.qualified);
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		this.name = Name.getQualified(in.readUTF());
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(this.name.toString());
		this.appendFullTypes(sb);
		return sb.toString();
	}
	
	@Override
	public GenericType clone()
	{
		NamedGenericType copy = new NamedGenericType(this.position, this.name);
		this.copyTypeArguments(copy);
		return copy;
	}
}
