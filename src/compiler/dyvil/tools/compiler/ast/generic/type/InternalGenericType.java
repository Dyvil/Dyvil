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
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.LambdaType;
import dyvil.tools.compiler.ast.type.TupleType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class InternalGenericType extends GenericType
{
	protected String internalName;
	
	public InternalGenericType(String internal)
	{
		this.internalName = internal;
	}
	
	@Override
	public int typeTag()
	{
		return GENERIC_INTERNAL;
	}
	
	@Override
	public Name getName()
	{
		return Name.getQualified(this.internalName.substring(this.internalName.lastIndexOf('/') + 1));
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
		for (int i = 0; i < this.typeArgumentCount; i++)
		{
			this.typeArguments[i] = this.typeArguments[i].resolveType(markers, context);
		}
		
		if (this.internalName.startsWith("dyvil/tuple/Tuple"))
		{
			return new TupleType(this.typeArguments, this.typeArgumentCount);
		}
		if (this.internalName.startsWith("dyvil/function/Function"))
		{
			int i = this.typeArgumentCount - 1;
			IType returnType = this.typeArguments[i];
			this.typeArguments[i] = null;
			return new LambdaType(this.typeArguments, i, returnType);
		}
		
		IClass iclass = Package.rootPackage.resolveInternalClass(this.internalName);
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
	public GenericType clone()
	{
		InternalGenericType copy = new InternalGenericType(this.internalName);
		this.copyTypeArguments(copy);
		return copy;
	}
}
