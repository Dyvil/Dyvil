package dyvil.tools.compiler.ast.generic.type;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.method.ConstructorMatchList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.*;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.I18n;
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
	public IType resolveType(ITypeVariable typeVar)
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

		IType resolved = new NamedType(this.position, this.name, this.parent).resolveType(markers, context);
		
		IClass iclass = resolved.getTheClass();
		if (iclass == null)
		{
			this.resolveTypeArguments(markers, context);
			return this;
		}

		if (this.typeArgumentCount == 0)
		{
			return new ClassType(iclass);
		}

		int varCount = iclass.genericCount();
		if (varCount == 0)
		{
			if (this.typeArgumentCount != 0)
			{
				markers.add(I18n.createMarker(this.position, "generic.not_generic", this.name.qualified));
			}
			return new ClassType(iclass);
		}
		if (varCount != this.typeArgumentCount)
		{
			markers.add(I18n.createMarker(this.position, "generic.count"));
			return new ClassType(iclass);
		}
		
		/*
		 * TODO Position handling
		 * if (position == TypePosition.CLASS) {
		 * markers.add(I18n.createMarker(this.position, "type.class.generic"));
		 * } // If the position is a SUPER_TYPE position if (position ==
		 * TypePosition.SUPER_TYPE || position ==
		 * TypePosition.SUPER_TYPE_ARGUMENT) { position =
		 * TypePosition.SUPER_TYPE_ARGUMENT; } else { // Otherwise, resolve the
		 * type arguments with a GENERIC_ARGUMENT // position position =
		 * TypePosition.GENERIC_ARGUMENT; }
		 */
		
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			IType resolvedType = this.typeArguments[i].resolveType(markers, context);
			
			this.typeArguments[i] = resolvedType;
			
			ITypeVariable typeVariable = iclass.getTypeVariable(i);
			if (!typeVariable.isAssignableFrom(resolvedType))
			{
				Marker marker = I18n.createMarker(resolvedType.getPosition(), "generic.type.incompatible",
				                                  typeVariable.getName().qualified);
				marker.addInfo(I18n.getString("generic.type", resolvedType));
				marker.addInfo(I18n.getString("typevariable", typeVariable));
				markers.add(marker);
			}
		}
		return new ClassGenericType(iclass, this.typeArguments, this.typeArgumentCount);
	}
	
	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
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
