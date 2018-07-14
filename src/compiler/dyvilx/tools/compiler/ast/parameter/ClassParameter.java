package dyvilx.tools.compiler.ast.parameter;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.ThisExpr;
import dyvilx.tools.compiler.ast.field.Field;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.ICallableMember;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.check.ModifierChecks;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class ClassParameter extends Field implements IParameter
{
	// Metadata
	protected int   index;
	protected int   localIndex;
	protected IType covariantType;

	protected ICallableMember constructor;

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

	public ClassParameter(IClass enclosingClass, SourcePosition position, Name name, IType type,
		                     AttributeList attributes)
	{
		super(enclosingClass, position, name, type, attributes);
	}

	@Override
	public Name getLabel()
	{
		return this.name;
	}

	@Override
	public void setLabel(Name name)
	{
	}

	@Override
	public String getQualifiedLabel()
	{
		return this.getInternalName();
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.CLASS_PARAMETER;
	}

	@Override
	public boolean isLocal()
	{
		return false;
	}

	@Override
	public ICallableMember getMethod()
	{
		return this.constructor;
	}

	@Override
	public void setMethod(ICallableMember method)
	{
		this.constructor = method;
	}

	@Override
	public IType getInternalType()
	{
		return this.type;
	}

	@Override
	public IDataMember capture(IContext context)
	{
		return this;
	}

	@Override
	public IType getCovariantType()
	{
		if (this.covariantType != null)
		{
			return this.covariantType;
		}

		return this.covariantType = this.type.asParameterType();
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
	public IValue checkAccess(MarkerList markers, SourcePosition position, IValue receiver, IContext context)
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
			return new ThisExpr(position, this.enclosingClass.getThisType(), markers, context);
		}

		ModifierChecks.checkVisibility(this, position, markers, context);

		return receiver;
	}

	@Override
	public IValue checkAssign(MarkerList markers, IContext context, SourcePosition position, IValue receiver,
		                         IValue newValue)
	{
		if (this.enclosingClass.isAnnotation())
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
			if (this.isOverride())
			{
				markers.add(Markers.semanticError(this.position, "classparameter.override.property", this.name));
			}
			else
			{
				this.property.getAttributes().addFlag(Modifiers.PUBLIC);
			}
		}

		super.resolveTypes(markers, context);
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);

		if (this.value != null)
		{
			this.attributes.addFlag(Modifiers.DEFAULT);
		}

		if (this.isOverride())
		{
			IDataMember superField = this.enclosingClass.getSuperType().resolveField(this.name);
			if (superField == null)
			{
				markers.add(Markers.semanticError(this.position, "classparameter.override.not_found", this.name));
			}
			else if (superField.hasModifier(Modifiers.STATIC))
			{
				markers.add(Markers.semanticError(this.position, "classparameter.override.static", this.name,
				                                  superField.getEnclosingClass().getFullName()));
			}
		}
	}

	@Override
	protected boolean hasDefaultInit()
	{
		return true;
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if (this.enclosingClass.isAnnotation() || this.hasModifier(Modifiers.OVERRIDE))
		{
			return;
		}

		super.write(writer);

		if (this.hasModifier(Modifiers.DEFAULT))
		{
			this.writeDefaultValue(writer);
		}
	}

	@Override
	public void writeParameter(MethodWriter writer)
	{
		IParameter.super.writeParameter(writer);
	}
}
