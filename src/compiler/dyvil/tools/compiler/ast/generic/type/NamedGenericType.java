package dyvil.tools.compiler.ast.generic.type;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.ConstructorMatchList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.*;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.MarkerMessages;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
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
	public boolean isSuperClassOf(IType type)
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
		if (this.parent == null)
		{
			if (this.name == Names.Tuple)
			{
				this.resolveTypeArguments(markers, context);
				return new TupleType(this.typeArguments, this.typeArgumentCount);
			}
			if (this.name == Names.Function)
			{
				if (this.typeArgumentCount > 0)
				{
					this.resolveTypeArguments(markers, context);
					return new LambdaType(this.typeArguments, this.typeArgumentCount - 1,
					                      this.typeArguments[this.typeArgumentCount - 1]);
				}
			}
		}

		// resolveType0 is used to avoid Type Variable -> Default Value replacement done be replaceType
		final IType resolved = new NamedType(this.position, this.name, this.parent).resolveType0(markers, context);

		if (!resolved.isResolved())
		{
			this.resolveTypeArguments(markers, context);
			return this;
		}

		this.resolveTypeArguments(markers, context);

		final IClass iClass = resolved.getTheClass();
		final ITypeParameter[] typeVariables;
		final IType concrete;

		// Convert the non-generic class type to a generic one
		if (!resolved.isGenericType())
		{
			typeVariables = iClass.getTypeParameters();

			concrete = new ClassGenericType(iClass, this.typeArguments, this.typeArgumentCount);
		}
		else
		{
			typeVariables = new ITypeParameter[this.typeArgumentCount];

			// Create a concrete type and save Type Variables in the above array
			concrete = resolved.getConcreteType(typeParameter -> {
				int index = typeParameter.getIndex();

				if (index >= this.typeArgumentCount)
				{
					return null;
				}
				typeVariables[index] = typeParameter;
				return this.typeArguments[index];
			});
		}

		// Check if the Type Variable Bounds accept the supplied Type Arguments
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			final ITypeParameter typeVariable = typeVariables[i];
			final IType type = this.typeArguments[i];
			if (typeVariable != null && !typeVariable.isAssignableFrom(type))
			{
				final Marker marker = MarkerMessages.createMarker(type.getPosition(), "generic.type.incompatible",
				                                            typeVariable.getName().qualified);
				marker.addInfo(MarkerMessages.getMarker("generic.type", type));
				marker.addInfo(MarkerMessages.getMarker("typevariable", typeVariable));
				markers.add(marker);
			}
		}

		return concrete;
	}
	
	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
		/*
		 * TODO Position handling
		 * if (position == TypePosition.CLASS) {
		 * markers.add(MarkerMessages.createMarker(this.position, "type.class.generic"));
		 * } // If the position is a SUPER_TYPE position if (position ==
		 * TypePosition.SUPER_TYPE || position ==
		 * TypePosition.SUPER_TYPE_ARGUMENT) { position =
		 * TypePosition.SUPER_TYPE_ARGUMENT; } else { // Otherwise, resolve the
		 * type arguments with a GENERIC_ARGUMENT // position position =
		 * TypePosition.GENERIC_ARGUMENT; }
		 */

		super.checkType(markers, context, position);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
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
