package dyvilx.tools.compiler.ast.field.capture;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

public abstract class CaptureDataMember implements IDataMember
{
	protected IVariable      variable;
	protected SourcePosition accessPosition;

	protected int localIndex;

	public CaptureDataMember()
	{
	}

	public CaptureDataMember(IVariable variable)
	{
		this.variable = variable;
	}

	public IVariable getVariable()
	{
		return this.variable;
	}

	public int getLocalIndex()
	{
		return this.localIndex;
	}

	public void setLocalIndex(int localIndex)
	{
		this.localIndex = localIndex;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.accessPosition;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
	}

	@Override
	public int getAccessLevel()
	{
		return this.variable.getAccessLevel();
	}

	@Override
	public Name getName()
	{
		return this.variable.getName();
	}

	@Override
	public void setName(Name name)
	{
	}

	@Override
	public IType getType()
	{
		return this.variable.getType();
	}

	@Override
	public void setType(IType type)
	{
	}

	public IType getInternalType()
	{
		return this.variable.getInternalType();
	}

	@Override
	public AttributeList getAttributes()
	{
		return this.variable.getAttributes();
	}

	@Override
	public void setAttributes(AttributeList attributes)
	{
	}

	@Override
	public Annotation getAnnotation(IClass type)
	{
		return this.variable.getAnnotation(type);
	}

	@Override
	public IValue getValue()
	{
		return this.variable.getValue();
	}

	@Override
	public void setValue(IValue value)
	{
	}

	@Override
	public IValue checkAccess(MarkerList markers, SourcePosition position, IValue receiver, IContext context)
	{
		if (this.accessPosition == null)
		{
			this.accessPosition = position;
		}

		return this.variable.checkAccess(markers, position, receiver, context);
	}

	@Override
	public IValue checkAssign(MarkerList markers, IContext context, SourcePosition position, IValue receiver,
		IValue newValue)
	{
		this.variable.setReferenceType();
		return this.variable.checkAssign(markers, context, position, receiver, newValue);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		// Check if the variable is neither final nor effectively final
		if (this.variable.isAssigned() && !this.variable.hasModifier(Modifiers.FINAL))
		{
			// Reference Capture is required
			this.variable.setReferenceType();
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
	}

	@Override
	public void foldConstants()
	{
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
	}

	@Override
	public String getInternalName()
	{
		return this.variable.getInternalName();
	}

	@Override
	public String getDescriptor()
	{
		return this.variable.getDescriptor();
	}

	@Override
	public String getSignature()
	{
		return this.variable.getSignature();
	}

	@Override
	public abstract void writeGet_Get(MethodWriter writer, int lineNumber) throws BytecodeException;

	@Override
	public void writeGet_Unwrap(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		this.variable.writeGet_Unwrap(writer, lineNumber);
	}

	@Override
	public boolean writeSet_PreValue(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.variable.isReferenceType())
		{
			this.writeGet_Get(writer, lineNumber);
			return true;
		}
		return false;
	}

	@Override
	public void writeSet_Wrap(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		this.variable.writeSet_Wrap(writer, lineNumber);
	}

	@Override
	public abstract void writeSet_Set(MethodWriter writer, int lineNumber) throws BytecodeException;

	@Override
	public String toString()
	{
		return this.variable.toString();
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.variable.toString(indent, buffer);
	}
}
