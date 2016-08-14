package dyvil.tools.compiler.ast.type.generic;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.generic.ITypeParametric;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.raw.PackageType;
import dyvil.tools.compiler.ast.type.typevar.ResolvedTypeVarType;
import dyvil.tools.compiler.util.Markers;
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
		this.resolveTypeArguments(markers, context);

		if (this.parent == null)
		{
			return this.resolveTopLevel(markers, context);
		}

		this.parent = this.parent.resolveType(markers, context);
		if (!this.parent.isResolved())
		{
			return null;
		}
		return this.resolveWithParent(markers);
	}

	private IType resolveTopLevel(MarkerList markers, IContext context)
	{
		final IType primitive = Types.resolvePrimitive(this.name);
		if (primitive != null)
		{
			markers.add(Markers.semanticError(this.position, "type.generic.class.not_generic", primitive.getName()));
			return primitive.atPosition(this.position);
		}

		IType type = this.resolveTopLevelWith(markers, context);
		if (type != null)
		{
			return type;
		}

		type = this.resolveTopLevelWith(markers, Types.LANG_HEADER);
		if (type != null)
		{
			return type;
		}

		final Package thePackage = context.resolvePackage(this.name);
		if (thePackage != null)
		{
			markers.add(Markers.semanticError(this.position, "type.generic.package.not_generic", thePackage.getName()));
			return new PackageType(thePackage).atPosition(this.position);
		}

		markers.add(Markers.semanticError(this.position, "resolve.type", this.name));
		return this;
	}

	private IType resolveTopLevelWith(MarkerList markers, IContext context)
	{
		final IClass theClass = context.resolveClass(this.name);
		if (theClass != null)
		{
			final IType classType = theClass.getType();
			return this.checkCount(markers, theClass, "class", classType);
		}

		final ITypeParameter typeParameter = context.resolveTypeParameter(this.name);
		if (typeParameter != null)
		{
			markers.add(Markers.semanticError(this.position, "type.generic.type_parameter.not_generic",
			                                  typeParameter.getName()));
			return new ResolvedTypeVarType(typeParameter, this.position);
		}

		final ITypeAlias typeAlias = context.resolveTypeAlias(this.name, this.typeArgumentCount);
		if (typeAlias != null)
		{
			final IType type = typeAlias.getType();
			return this.checkCount(markers, typeAlias, "type_alias", type);
		}
		return null;
	}

	private IType resolveWithParent(MarkerList markers)
	{
		final IClass theClass = this.parent.resolveClass(this.name);
		if (theClass != null)
		{
			final IType classType = theClass.getType();
			return this.checkCount(markers, theClass, "class", classType);
		}

		final Package thePackage = this.parent.resolvePackage(this.name);
		if (thePackage != null)
		{
			markers.add(Markers.semanticError(this.position, "type.generic.package.not_generic", thePackage.getName()));
			return new PackageType(thePackage).atPosition(this.position);
		}

		markers.add(Markers.semanticError(this.position, "resolve.type.package", this.name, this.parent));
		return this;
	}

	private IType checkCount(MarkerList markers, ITypeParametric generic, String kind, IType type)
	{
		final int parameterCount = generic.typeParameterCount();

		if (parameterCount <= 0)
		{
			markers.add(Markers.semanticError(this.position, "type.generic." + kind + ".not_generic", type));
			return type.atPosition(this.position);
		}
		if (parameterCount != this.typeArgumentCount)
		{
			final Marker marker = Markers
				                      .semanticError(this.position, "type.generic." + kind + ".count_mismatch", type);
			marker.addInfo(Markers.getSemantic("type.generic.argument_count", this.typeArgumentCount));
			marker.addInfo(Markers.getSemantic("type.generic.parameter_count", parameterCount));
			markers.add(marker);
		}

		if (type == null)
		{
			return null;
		}
		return type.getConcreteType(typeParameter ->
		                            {
			                            final int index = typeParameter.getIndex();
			                            if (index >= this.typeArgumentCount)
			                            {
				                            return null;
			                            }
			                            return this.typeArguments[index];
		                            }).atPosition(this.position);
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
		this.name = Name.fromRaw(in.readUTF());
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
