package dyvilx.tools.compiler.ast.generic;

import dyvil.annotation.internal.NonNull;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.IType.TypePosition;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.phase.Resolvable;
import dyvilx.tools.parsing.marker.MarkerList;

public final class GenericData implements Resolvable, ITypeContext
{
	protected @NonNull ITypeParametricMember member;

	protected @NonNull TypeList generics;

	protected int lockedCount;

	protected ITypeContext fallbackTypeContext;

	public GenericData()
	{
		this.generics = new TypeList();
	}

	public GenericData(ITypeParametricMember member)
	{
		this(member, member.typeArity());
	}

	public GenericData(ITypeParametricMember member, int capacity)
	{
		this.member = member;
		this.generics = new TypeList(capacity);
	}

	public GenericData(ITypeParametricMember member, IType... generics)
	{
		this.member = member;
		this.generics = new TypeList(generics);
	}

	public ITypeParametricMember getMember()
	{
		return this.member;
	}

	public void setMember(ITypeParametricMember member)
	{
		this.member = member;
	}

	public ITypeContext getFallbackTypeContext()
	{
		return this.fallbackTypeContext;
	}

	public void setFallbackTypeContext(ITypeContext fallbackTypeContext)
	{
		this.fallbackTypeContext = fallbackTypeContext;
	}

	public TypeList getTypes()
	{
		return this.generics;
	}

	public void lockAvailable()
	{
		this.lock(this.generics.size());
	}

	public void lock(int lockedCount)
	{
		if (lockedCount > this.lockedCount)
		{
			this.lockedCount = lockedCount;
		}
	}

	private boolean isMethodTypeVariable(ITypeParameter typeVar)
	{
		if (typeVar.getGeneric() == this.member)
		{
			return true;
		}

		final int index = typeVar.getIndex();
		return index < this.member.typeArity() && this.member.getTypeParameters().get(index) == typeVar;
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		if (this.isMethodTypeVariable(typeParameter))
		{
			final int index = typeParameter.getIndex();
			if (index >= this.lockedCount)
			{
				return null;
			}
			return this.generics.get(index);
		}
		if (this.fallbackTypeContext != null && typeParameter.getGeneric() == this.member.getEnclosingClass())
		{
			return Types.resolveTypeSafely(this.fallbackTypeContext, typeParameter);
		}

		return null;
	}

	@Override
	public boolean isReadonly()
	{
		return false;
	}

	@Override
	public void addMapping(ITypeParameter typeParameter, IType type)
	{
		if (!this.isMethodTypeVariable(typeParameter))
		{
			return;
		}

		final int index = typeParameter.getIndex();

		final IType current = this.generics.get(index);
		if (current == null)
		{
			this.generics.set(index, type);
			return;
		}
		if (index < this.lockedCount)
		{
			return;
		}
		this.generics.set(index, Types.combine(current, type));
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.generics.resolveTypes(markers, context);

		this.lockedCount = this.generics.size();
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.generics.resolve(markers, context);
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.generics.checkTypes(markers, context, TypePosition.GENERIC_ARGUMENT | TypePosition.REIFY_FLAG);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.generics.check(markers, context);
	}

	@Override
	public void foldConstants()
	{
		this.generics.foldConstants();
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.generics.cleanup(compilableList, classCompilableList);
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		this.toString("", builder);
		return builder.toString();
	}

	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		if (this.generics.size() > 0)
		{
			this.generics.toString(indent, buffer);
		}
	}
}
