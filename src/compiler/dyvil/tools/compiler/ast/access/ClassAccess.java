package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class ClassAccess implements IValue
{
	protected IType type;

	// Metadata
	protected ICodePosition position;
	protected boolean       ignored;
	protected boolean       withTyped;

	public ClassAccess(IType type)
	{
		this.type = type;
	}

	public ClassAccess(ICodePosition position, IType type)
	{
		this.position = position;
		this.type = type;
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
	public int valueTag()
	{
		return CLASS_ACCESS;
	}

	@Override
	public boolean isClassAccess()
	{
		return true;
	}

	@Override
	public boolean isIgnoredClassAccess()
	{
		return this.ignored;
	}

	@Override
	public IValue asIgnoredClassAccess()
	{
		this.ignored = true;
		return this;
	}

	@Override
	public boolean isResolved()
	{
		return this.type.isResolved();
	}

	@Override
	public IType getType()
	{
		return this.type;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (Types.isSuperType(type, this.type))
		{
			this.withTyped = true;
			return this;
		}
		return null;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(null, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.type.checkType(markers, context, TypePosition.CLASS);

		if (!this.type.isResolved())
		{
			markers.add(Markers.semanticError(this.position, "resolve.type", this.type));
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);

		if (!this.withTyped)
		{
			// Don't need an additional error
			return;
		}

		if (!this.type.isResolved())
		{
			// Already reported this in CHECK_TYPES
			return;
		}

		final IClass iclass = this.type.getTheClass();
		if (iclass != null && iclass.hasModifier(Modifiers.OBJECT_CLASS))
		{
			// Object type, we can safely use it's instance field.
			return;
		}

		markers.add(Markers.semantic(this.position, "type.access.invalid", this.type.toString()));
	}

	@Override
	public IValue foldConstants()
	{
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (this.ignored)
		{
			return;
		}

		IClass iclass = this.type.getTheClass();
		if (iclass != null)
		{
			IDataMember field = iclass.getMetadata().getInstanceField();
			if (field != null)
			{
				field.writeGet(writer, null, this.getLineNumber());
				return;
			}
		}

		throw new BytecodeException("Object access was not compiled correctly");
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
	}
}
