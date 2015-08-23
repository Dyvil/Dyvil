package dyvil.tools.compiler.ast.generic.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import dyvil.collection.List;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class NamedGenericType extends GenericType
{
	protected ICodePosition	position;
	protected Name			name;
	
	public NamedGenericType(ICodePosition position, Name name)
	{
		this.position = position;
		this.name = name;
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
	public boolean isResolved()
	{
		return false;
	}
	
	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		// Package.rootPackage.resolveInternalClass(this.internalName);
		
		IClass iclass = IContext.resolveClass(context, this.name);
		
		if (iclass == null)
		{
			markers.add(this.position, "resolve.type", this.toString());
		}
		else
		{
			if (this.typeArgumentCount == 0)
			{
				return new ClassType(iclass);
			}
			
			int varCount = iclass.genericCount();
			if (varCount == 0)
			{
				if (this.typeArgumentCount != 0)
				{
					markers.add(this.position, "generic.not_generic", this.name.qualified);
				}
				return new ClassType(iclass);
			}
			if (varCount != this.typeArgumentCount)
			{
				markers.add(this.position, "generic.count");
				return new ClassType(iclass);
			}
		}
		
		/* TODO Position handling
		 * if (position == TypePosition.CLASS)
		{
			markers.add(this.position, "type.class.generic");
		}
		
		// If the position is a SUPER_TYPE position
		if (position == TypePosition.SUPER_TYPE || position == TypePosition.SUPER_TYPE_ARGUMENT)
		{
			position = TypePosition.SUPER_TYPE_ARGUMENT;
		}
		else
		{
			// Otherwise, resolve the type arguments with a GENERIC_ARGUMENT
			// position
			position = TypePosition.GENERIC_ARGUMENT;
		} */
		
		if (iclass == null)
		{
			for (int i = 0; i < this.typeArgumentCount; i++)
			{
				this.typeArguments[i] = this.typeArguments[i].resolveType(markers, context);
			}
			return this;
		}
		
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			IType t1 = this.typeArguments[i];
			IType t2 = t1.resolveType(markers, context);
			
			this.typeArguments[i] = t2;
			
			ITypeVariable var = iclass.getTypeVariable(i);
			if (!var.isSuperTypeOf(t2))
			{
				Marker marker = markers.create(t2.getPosition(), "generic.type", var.getName().qualified);
				marker.addInfo("Generic Type: " + t2);
				marker.addInfo("Type Variable: " + var);
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
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
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
