package dyvil.tools.compiler.ast.type.alias;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.generic.TypeParameterList;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.header.IHeaderUnit;
import dyvil.tools.compiler.ast.header.ISourceHeader;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TypeAlias implements ITypeAlias, IDefaultContext
{
	protected Name  name;
	protected IType type;

	protected @Nullable TypeParameterList typeParameters;

	// Metadata
	protected IHeaderUnit   enclosingHeader;
	protected ICodePosition position;
	protected boolean       resolved;

	public TypeAlias()
	{
	}

	public TypeAlias(Name name)
	{
		this.name = name;
	}

	public TypeAlias(Name name, ICodePosition position)
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
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public Name getName()
	{
		return this.name;
	}

	@Override
	public void setName(Name name)
	{
		this.name = name;
	}

	@Override
	public IType getType()
	{
		this.ensureResolved();
		return this.type;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
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
		this.type.foldConstants();

		if (this.typeParameters != null)
		{
			this.typeParameters.foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.type.cleanup(compilableList, classCompilableList);

		if (this.typeParameters != null)
		{
			this.typeParameters.cleanup(compilableList, classCompilableList);
		}
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
		return IASTNode.toString(this);
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
