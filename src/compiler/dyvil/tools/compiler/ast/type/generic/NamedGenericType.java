package dyvil.tools.compiler.ast.type.generic;

import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.generic.ITypeParametric;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TypeList;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.raw.IUnresolvedType;
import dyvil.tools.compiler.ast.type.raw.PackageType;
import dyvil.tools.compiler.ast.type.typevar.ResolvedTypeVarType;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NamedGenericType extends GenericType implements IUnresolvedType
{
	protected IType         parent;
	protected SourcePosition position;
	protected Name          name;

	public NamedGenericType(SourcePosition position, Name name)
	{
		this.position = position;
		this.name = name;
	}

	public NamedGenericType(SourcePosition position, Name name, IType... arguments)
	{
		super(arguments);
		this.position = position;
		this.name = name;
	}

	public NamedGenericType(SourcePosition position, Name name, TypeList arguments)
	{
		super(arguments);
		this.position = position;
		this.name = name;
	}

	public NamedGenericType(SourcePosition position, IType parent, Name name)
	{
		this.position = position;
		this.name = name;
		this.parent = parent;
	}

	public NamedGenericType(SourcePosition position, IType parent, Name name, IType... arguments)
	{
		super(arguments);
		this.parent = parent;
		this.position = position;
		this.name = name;
	}

	public NamedGenericType(SourcePosition position, IType parent, Name name, TypeList arguments)
	{
		super(arguments);
		this.parent = parent;
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
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.arguments.resolveTypes(markers, context);

		if (this.parent == null)
		{
			return this.resolveTopLevel(markers, context);
		}

		this.parent = this.parent.resolveType(markers, context);
		if (!this.parent.isResolved())
		{
			return markers == null ? null : this;
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

		type = this.resolveTopLevelWith(markers, Types.BASE_CONTEXT);
		if (type != null)
		{
			return type;
		}

		markers.add(Markers.semanticError(this.position, "resolve.type", this.name));
		return this;
	}

	private IType resolveTopLevelWith(MarkerList markers, IContext context)
	{
		final MatchList<ITypeAlias> typeAliases = IContext.resolveTypeAlias(context, null, this.name, this.arguments);
		if (typeAliases.hasCandidate())
		{
			final ITypeAlias typeAlias = typeAliases.getBestMember();
			final IType aliasType = typeAlias.getType();
			if (!aliasType.isResolved())
			{
				markers.add(Markers.semanticError(this.position, "type.alias.unresolved", this.name));
				return aliasType.atPosition(this.position);
			}

			return this.checkCount(markers, typeAlias, "type_alias", aliasType);
		}

		final IClass theClass = context.resolveClass(this.name);
		if (theClass != null)
		{
			final IType classType = theClass.getThisType();
			return this.checkCount(markers, theClass, "class", classType);
		}

		final ITypeParameter typeParameter = context.resolveTypeParameter(this.name);
		if (typeParameter != null)
		{
			markers.add(Markers.semanticError(this.position, "type.generic.type_parameter.not_generic",
			                                  typeParameter.getName()));
			return new ResolvedTypeVarType(typeParameter, this.position);
		}


		final Package thePackage = context.resolvePackage(this.name);
		if (thePackage != null)
		{
			markers.add(Markers.semanticError(this.position, "type.generic.package.not_generic", thePackage.getName()));
			return new PackageType(thePackage).atPosition(this.position);
		}
		return null;
	}

	private IType resolveWithParent(MarkerList markers)
	{
		final IClass theClass = this.parent.resolveClass(this.name);
		if (theClass != null)
		{
			final IType classType = theClass.getThisType();
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
		final int genericArity = generic.typeArity();

		if (genericArity <= 0)
		{
			markers.add(Markers.semanticError(this.position, "type.generic." + kind + ".not_generic", type));
			return type.atPosition(this.position);
		}
		if (genericArity != this.arguments.size())
		{
			final Marker marker = Markers
				                      .semanticError(this.position, "type.generic." + kind + ".count_mismatch", type);
			marker.addInfo(Markers.getSemantic("type.generic.argument_count", this.arguments.size()));
			marker.addInfo(Markers.getSemantic("type.generic.parameter_count", genericArity));
			markers.add(marker);
		}

		return type.getConcreteType(typeParameter -> this.arguments.get(typeParameter.getIndex()))
		           .atPosition(this.position);
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
	protected GenericType withArguments(TypeList arguments)
	{
		return new NamedGenericType(this.position, this.parent, this.name, arguments);
	}
}
