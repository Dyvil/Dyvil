package dyvilx.tools.compiler.ast.type.alias;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.generic.TypeParameterList;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.header.IHeaderUnit;
import dyvilx.tools.compiler.ast.header.ISourceHeader;
import dyvilx.tools.compiler.ast.member.Member;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.Candidate;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.IType.TypePosition;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.util.Markers;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

public class TypeAlias extends Member implements ITypeAlias, IDefaultContext
{
	protected @Nullable TypeParameterList typeParameters;

	// Metadata
	protected IHeaderUnit enclosingHeader;
	protected boolean     resolved;

	public TypeAlias()
	{
	}

	public TypeAlias(Name name)
	{
		this.name = name;
	}

	public TypeAlias(Name name, SourcePosition position)
	{
		this.name = name;
		this.position = position;
	}

	public TypeAlias(Name name, IType type)
	{
		this.name = name;
		this.type = type;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.TYPE;
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.TYPE_ALIAS;
	}

	@Override
	public IHeaderUnit getEnclosingHeader()
	{
		return this.enclosingHeader;
	}

	@Override
	public void setEnclosingHeader(IHeaderUnit header)
	{
		this.enclosingHeader = header;
	}

	@Override
	public IType getType()
	{
		this.ensureResolved();
		return this.type;
	}

	@Override
	public boolean isTypeParametric()
	{
		return this.typeParameters != null;
	}

	@Override
	public TypeParameterList getTypeParameters()
	{
		if (this.typeParameters != null)
		{
			return this.typeParameters;
		}
		return this.typeParameters = new TypeParameterList();
	}

	@Override
	public ITypeParameter resolveTypeParameter(Name name)
	{
		return this.typeParameters == null ? null : this.typeParameters.get(name);
	}

	@Override
	public boolean isVariadic()
	{
		return false;
	}

	// Resolution

	@Override
	public void checkMatch(MatchList<ITypeAlias> matches, IType receiver, Name name, TypeList arguments)
	{
		if (name != this.name && name != null)
		{
			return;
		}

		if (arguments == null)
		{
			matches.add(new Candidate<>(this));
			return;
		}

		final int size = arguments.size();
		if (size != this.typeArity())
		{
			matches.add(new Candidate<>(this, true));
			return;
		}

		final int[] matchValues = new int[size];
		final IType[] matchTypes = new IType[size];
		boolean invalid = false;

		for (int i = 0; i < size; i++)
		{
			final IType bound = this.typeParameters.get(i).getUpperBound();
			final IType argument = arguments.get(i);
			final int match = Types.getTypeMatch(bound, argument);
			if (match == IValue.MISMATCH)
			{
				invalid = true;
			}

			matchValues[i] = match;
			matchTypes[i] = bound;
		}

		matches.add(new Candidate<>(this, matchValues, matchTypes, 0, 0, invalid));
	}

	// Phases

	private void ensureResolved()
	{
		if (this.resolved)
		{
			return;
		}

		final MarkerList markers = this.enclosingHeader instanceof ISourceHeader ?
			                           ((ISourceHeader) this.enclosingHeader).getMarkers() :
			                           null;
		this.resolveTypes(markers, this.enclosingHeader.getContext());
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.annotations != null)
		{
			this.annotations.resolveTypes(markers, context, this);
		}

		context = context.push(this);

		if (this.type == null)
		{
			this.type = Types.UNKNOWN;
			markers.add(Markers.semanticError(this.position, "typealias.invalid"));
		}

		this.resolved = true;
		this.type = this.type.resolveType(markers, context);

		if (this.typeParameters != null)
		{
			this.typeParameters.resolveTypes(markers, context);
		}

		context.pop();
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		if (this.annotations != null)
		{
			this.annotations.resolve(markers, context);
		}

		context = context.push(this);

		this.type.resolve(markers, context);

		if (this.typeParameters != null)
		{
			this.typeParameters.resolve(markers, context);
		}

		context.pop();
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.annotations != null)
		{
			this.annotations.checkTypes(markers, context);
		}

		context = context.push(this);

		this.type.checkType(markers, context, TypePosition.GENERIC_ARGUMENT);

		if (this.typeParameters != null)
		{
			this.typeParameters.checkTypes(markers, context);
		}

		context.pop();
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.annotations != null)
		{
			this.annotations.check(markers, context, ElementType.TYPE);
		}

		context = context.push(this);

		this.type.check(markers, context);

		if (this.typeParameters != null)
		{
			this.typeParameters.check(markers, context);
		}

		context.pop();
	}

	@Override
	public void foldConstants()
	{
		super.foldConstants();

		if (this.typeParameters != null)
		{
			this.typeParameters.foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		super.cleanup(compilableList, classCompilableList);

		if (this.typeParameters != null)
		{
			this.typeParameters.cleanup(compilableList, classCompilableList);
		}
	}

	// Compilation

	@Override
	public String getInternalName()
	{
		return this.name.qualified;
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		this.name.write(out);
		TypeParameterList.write(this.typeParameters, out);
		IType.writeType(this.type, out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.name = Name.read(in);
		TypeParameterList.read(this, in);
		this.type = IType.readType(in);
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append("type ").append(this.name);

		if (this.typeParameters != null)
		{
			this.typeParameters.toString(indent, buffer);
		}

		Formatting.appendSeparator(buffer, "field.assignment", '=');
		this.type.toString(indent, buffer);
	}
}
