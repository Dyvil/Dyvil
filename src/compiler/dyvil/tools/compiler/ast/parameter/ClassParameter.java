package dyvil.tools.compiler.ast.parameter;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisExpr;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.modifiers.ModifierList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ClassParameter extends Field implements IParameter
{
	// Metadata
	protected int     index;
	protected int     localIndex;
	protected IType   internalType;
	protected boolean varargs;

	public ClassParameter(IClass enclosingClass)
	{
		super(enclosingClass);
	}

	public ClassParameter(IClass enclosingClass, Name name)
	{
		super(enclosingClass, name);
	}

	public ClassParameter(IClass enclosingClass, Name name, IType type)
	{
		super(enclosingClass, name, type);
	}

	public ClassParameter(IClass enclosingClass, ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		super(enclosingClass, position, name, type, modifiers == null ? new ModifierList() : modifiers, annotations);
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.CLASS_PARAMETER;
	}

	@Override
	public boolean isVariable()
	{
		return false;
	}

	@Override
	public boolean isAssigned()
	{
		return true;
	}

	@Override
	public boolean isReferenceCapturable()
	{
		return false;
	}

	@Override
	public IDataMember capture(IContext context)
	{
		return this;
	}

	@Override
	public IType getInternalType()
	{
		if (this.internalType != null)
		{
			return this.internalType;
		}

		return this.internalType = this.type.asParameterType();
	}

	@Override
	public void setVarargs(boolean varargs)
	{
		this.varargs = varargs;
	}

	@Override
	public boolean isVarargs()
	{
		return this.varargs;
	}

	@Override
	public int getIndex()
	{
		return this.index;
	}

	@Override
	public void setIndex(int index)
	{
		this.index = index;
	}

	@Override
	public int getLocalIndex()
	{
		return this.localIndex;
	}

	@Override
	public void setLocalIndex(int index)
	{
		this.localIndex = index;
	}

	@Override
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue receiver, IContext context)
	{
		if (receiver != null)
		{
			if (this.hasModifier(Modifiers.STATIC))
			{
				if (receiver.isClassAccess())
				{
					markers.add(Markers.semantic(position, "classparameter.access.static", this.name.unqualified));
					return null;
				}
			}
			else if (receiver.isClassAccess())
			{
				markers.add(Markers.semantic(position, "classparameter.access.instance", this.name.unqualified));
			}
		}
		else if (!this.hasModifier(Modifiers.STATIC))
		{
			markers.add(Markers.semantic(position, "classparameter.access.unqualified", this.name.unqualified));
			return new ThisExpr(position, this.enclosingClass.getThisType(), context, markers);
		}

		ModifierUtil.checkVisibility(this, position, markers, context);

		return receiver;
	}

	@Override
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue receiver, IValue newValue)
	{
		if (this.enclosingClass.hasModifier(Modifiers.ANNOTATION))
		{
			markers.add(Markers.semanticError(position, "classparameter.assign.annotation", this.name.unqualified));
		}
		return super.checkAssign(markers, context, position, receiver, newValue);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.property != null)
		{
			this.property.getModifiers().addIntModifier(Modifiers.PUBLIC);
		}

		super.resolveTypes(markers, context);
	}

	@Override
	protected boolean hasDefaultInit()
	{
		return true;
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if (this.enclosingClass.hasModifier(Modifiers.ANNOTATION))
		{
			return;
		}

		if (this.isVarargs())
		{
			this.modifiers.removeIntModifier(Modifiers.VARARGS);
		}

		super.write(writer);
	}

	@Override
	public void writeInit(MethodWriter writer, IValue value) throws BytecodeException
	{
		this.writeInit(writer);
	}

	@Override
	public void writeInit(MethodWriter writer) throws BytecodeException
	{
		if (this.varargs && !this.modifiers.hasIntModifier(Modifiers.VARARGS))
		{
			// Bugfix: VARARGS is the same bitflag as TRANSIENT, so Class Parameters cannot use the Modifier and have
			// to rely on a boolean field.

			this.modifiers.addIntModifier(Modifiers.VARARGS);
			AbstractParameter.writeInitImpl(this, writer);
			this.modifiers.removeIntModifier(Modifiers.VARARGS);
			return;
		}

		AbstractParameter.writeInitImpl(this, writer);
	}
}
