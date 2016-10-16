package dyvil.tools.compiler.ast.field;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public abstract class CaptureDataMember implements IDataMember
{
	protected IVariable     variable;
	protected ICodePosition accessPosition;

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
	public ICodePosition getPosition()
	{
		return this.accessPosition;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
	}

	@Override
	public int getAccessLevel()
	{
		return this.variable.getAccessLevel();
	}

	@Override
	public void setName(Name name)
	{
	}

	@Override
	public Name getName()
	{
		return this.variable.getName();
	}

	@Override
	public void setType(IType type)
	{
	}

	@Override
	public IType getType()
	{
		return this.variable.getType();
	}

	public IType getInternalType()
	{
		return this.variable.getInternalType();
	}

	@Override
	public void setModifiers(ModifierSet modifiers)
	{
	}

	@Override
	public ModifierSet getModifiers()
	{
		return this.variable.getModifiers();
	}

	@Override
	public boolean hasModifier(int mod)
	{
		return this.variable.hasModifier(mod);
	}

	@Override
	public AnnotationList getAnnotations()
	{
		return this.variable.getAnnotations();
	}

	@Override
	public void setAnnotations(AnnotationList annotations)
	{
	}

	@Override
	public IAnnotation getAnnotation(IClass type)
	{
		return this.variable.getAnnotation(type);
	}

	@Override
	public void addAnnotation(IAnnotation annotation)
	{
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
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue receiver, IContext context)
	{
		if (this.accessPosition == null)
		{
			this.accessPosition = position;
		}

		return this.variable.checkAccess(markers, position, receiver, context);
	}

	@Override
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue receiver, IValue newValue)
	{
		if (!this.variable.isReferenceCapturable())
		{
			markers.add(Markers.semantic(position, "variable.assign.capture", this.variable.getName()));
		}
		else
		{
			this.variable.setReferenceType();
		}

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
			if (!this.variable.isReferenceCapturable())
			{
				markers.add(
						Markers.semanticError(this.accessPosition, "variable.access.capture", this.variable.getName()));
			}
			else
			{
				// Reference Capture is required
				this.variable.setReferenceType();
			}
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
	public void toString(String prefix, StringBuilder buffer)
	{
		this.variable.toString(prefix, buffer);
	}
}
