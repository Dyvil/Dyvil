package dyvil.tools.compiler.ast.parameter;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.FieldVisitor;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisExpr;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.lang.annotation.ElementType;

public final class ClassParameter extends Parameter implements IField
{
	protected IClass theClass;
	
	public ClassParameter()
	{
	}
	
	public ClassParameter(Name name)
	{
		super(name);
	}

	public ClassParameter(Name name, IType type)
	{
		super(name, type);
	}

	public ClassParameter(Name name, IType type, ModifierSet modifiers)
	{
		super(name, type, modifiers);
	}

	public ClassParameter(ICodePosition position, Name name, IType type, ModifierSet modifiers)
	{
		super(position, name, type, modifiers);
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.CLASS_PARAMETER;
	}

	@Override
	public boolean isField()
	{
		return true;
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
	public IDataMember capture(IContext context, IVariable variable)
	{
		return this;
	}
	
	@Override
	public void setTheClass(IClass iclass)
	{
		this.theClass = iclass;
	}
	
	@Override
	public IClass getTheClass()
	{
		return this.theClass;
	}
	
	@Override
	public boolean addRawAnnotation(String type, IAnnotation annotation)
	{
		return true;
	}
	
	@Override
	public ElementType getElementType()
	{
		return ElementType.FIELD;
	}
	
	@Override
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue instance, IContext context)
	{
		if (instance != null)
		{
			if (this.hasModifier(Modifiers.STATIC))
			{
				if (instance.valueTag() != IValue.CLASS_ACCESS)
				{
					markers.add(Markers.semantic(position, "classparameter.access.static", this.name.unqualified));
					return null;
				}
			}
			else if (instance.valueTag() == IValue.CLASS_ACCESS)
			{
				markers.add(Markers.semantic(position, "classparameter.access.instance", this.name.unqualified));
			}
		}
		else if (!this.hasModifier(Modifiers.STATIC))
		{
			markers.add(Markers.semantic(position, "classparameter.access.unqualified", this.name.unqualified));
			return new ThisExpr(position, this.theClass.getType(), context, markers);
		}
		
		return instance;
	}
	
	@Override
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue instance, IValue newValue)
	{
		if (this.theClass.hasModifier(Modifiers.ANNOTATION))
		{
			markers.add(Markers.semanticError(position, "classparameter.assign.annotation", this.name.unqualified));
		}
		else if (this.hasModifier(Modifiers.FINAL))
		{
			markers.add(Markers.semantic(position, "classparameter.assign.final", this.name.unqualified));
		}
		
		final IValue typed = newValue.withType(this.type, null, markers, context);
		if (typed == null)
		{
			final Marker marker = Markers
					.semantic(newValue.getPosition(), "classparameter.assign.type", this.name.unqualified);
			marker.addInfo(Markers.getSemantic("classparameter.type", this.type));
			marker.addInfo(Markers.getSemantic("value.type", newValue.getType()));
			markers.add(marker);
		}
		else
		{
			newValue = typed;
		}
		
		return newValue;
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		String desc = this.getDescription();
		FieldVisitor fv = writer
				.visitField(this.modifiers.toFlags() & ModifierUtil.JAVA_MODIFIER_MASK, this.name.qualified, desc,
				            this.getSignature(), null);
		
		IField.writeAnnotations(fv, this.modifiers, this.annotations, this.type);
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		this.localIndex = writer.localCount();
		writer.registerParameter(this.localIndex, this.name.qualified, this.type, 0);
		this.writeAnnotations(writer);
	}

	@Override
	public void writeGet_Get(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.theClass.hasModifier(Modifiers.ANNOTATION))
		{
			StringBuilder desc = new StringBuilder("()");
			this.type.appendExtendedName(desc);
			writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, this.theClass.getInternalName(), this.name.qualified,
			                       desc.toString(), true);
		}
		else
		{
			writer.writeFieldInsn(Opcodes.GETFIELD, this.theClass.getInternalName(), this.name.qualified,
			                      this.getDescription());
		}
	}
	
	@Override
	public void writeSet_Set(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		writer.writeFieldInsn(Opcodes.PUTFIELD, this.theClass.getInternalName(), this.name.qualified,
		                      this.getDescription());
	}
}
