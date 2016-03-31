package dyvil.tools.compiler.ast.field;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.FieldVisitor;
import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.access.FieldAssignment;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisExpr;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.lang.annotation.ElementType;

public class Field extends Member implements IField
{
	protected IValue value;

	protected IProperty property;

	// Metadata
	protected IClass enclosingClass;
	protected String descriptor;

	public Field(IClass enclosingClass)
	{
		this.enclosingClass = enclosingClass;
	}

	public Field(IClass enclosingClass, Name name)
	{
		super(name);
		this.enclosingClass = enclosingClass;
	}

	public Field(IClass enclosingClass, Name name, IType type)
	{
		super(name, type);
		this.enclosingClass = enclosingClass;
	}

	public Field(IClass enclosingClass, Name name, IType type, ModifierSet modifiers)
	{
		super(name, type, modifiers);
		this.enclosingClass = enclosingClass;
	}

	public Field(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		super(position, name, type, modifiers, annotations);
	}

	@Override
	public boolean isField()
	{
		return true;
	}

	@Override
	public void setEnclosingClass(IClass enclosingClass)
	{
		this.enclosingClass = enclosingClass;
	}

	@Override
	public IClass getEnclosingClass()
	{
		return this.enclosingClass;
	}

	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}

	@Override
	public IValue getValue()
	{
		return this.value;
	}

	@Override
	public IProperty getProperty()
	{
		return this.property;
	}

	@Override
	public void setProperty(IProperty property)
	{
		this.property = property;
	}

	@Override
	public IProperty createProperty()
	{
		return new Property(this.position, this.name, Types.UNKNOWN, new FlagModifierSet(), null);
	}

	@Override
	public String getDescriptor()
	{
		if (this.descriptor != null)
		{
			return this.descriptor;
		}

		return this.descriptor = this.type.getExtendedName();
	}

	@Override
	public String getSignature()
	{
		return this.type.getSignature();
	}

	@Override
	public boolean addRawAnnotation(String type, IAnnotation annotation)
	{
		switch (type)
		{
		case "dyvil/annotation/Transient":
			this.modifiers.addIntModifier(Modifiers.TRANSIENT);
			return false;
		case "dyvil/annotation/Volatile":
			this.modifiers.addIntModifier(Modifiers.VOLATILE);
			return false;
		case Deprecation.JAVA_INTERNAL:
		case Deprecation.DYVIL_INTERNAL:
			this.modifiers.addIntModifier(Modifiers.DEPRECATED);
			return true;
		}
		return true;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.FIELD;
	}

	@Override
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue receiver, IContext context)
	{
		if (receiver != null)
		{
			if (this.modifiers.hasIntModifier(Modifiers.STATIC))
			{
				if (receiver.valueTag() != IValue.CLASS_ACCESS)
				{
					markers.add(Markers.semantic(position, "field.access.static", this.name));
				}
				else if (receiver.getType().getTheClass() != this.enclosingClass)
				{
					markers.add(Markers.semantic(position, "field.access.static.type", this.name,
					                             this.enclosingClass.getFullName()));
				}
				receiver = null;
			}
			else if (receiver.valueTag() == IValue.CLASS_ACCESS)
			{
				if (!receiver.getType().getTheClass().isObject())
				{
					markers.add(Markers.semantic(position, "field.access.instance", this.name));
				}
			}
			else
			{
				IType type = this.enclosingClass.getClassType();
				receiver = TypeChecker.convertValue(receiver, type, type, markers, context, TypeChecker.markerSupplier(
					"field.access.receiver_type", this.name));
			}
		}
		else if (!this.modifiers.hasIntModifier(Modifiers.STATIC))
		{
			if (context.isStatic())
			{
				markers.add(Markers.semantic(position, "field.access.instance", this.name));
			}
			else
			{
				markers.add(Markers.semantic(position, "field.access.unqualified", this.name.unqualified));
				receiver = new ThisExpr(position, this.enclosingClass.getType(), context, markers);
			}
		}

		ModifierUtil.checkVisibility(this, position, markers, context);
		return receiver;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);

		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}

		if (this.property != null)
		{
			final int modifiers =
				this.modifiers.toFlags() & (Modifiers.STATIC | Modifiers.PUBLIC | Modifiers.PROTECTED);
			// only transfer public, static and protected modifiers to the property

			this.property.getModifiers().addIntModifier(modifiers);
			this.property.setEnclosingClass(this.enclosingClass);
			this.property.resolveTypes(markers, context);
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);

		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);

			boolean inferType = false;
			if (this.type == Types.UNKNOWN)
			{
				inferType = true;
				this.type = this.value.getType();
			}

			final TypeChecker.MarkerSupplier markerSupplier = TypeChecker.markerSupplier("field.type.incompatible",
			                                                                             "field.type", "value.type",
			                                                                             this.name);
			this.value = TypeChecker.convertValue(this.value, this.type, this.type, markers, context, markerSupplier);

			if (inferType)
			{
				this.type = this.value.getType();
				if (this.type == Types.UNKNOWN && this.value.isResolved())
				{
					markers.add(Markers.semantic(this.position, "field.type.infer", this.name.unqualified));
					this.type = Types.ANY;
				}
			}
		}
		else if (this.type == Types.UNKNOWN)
		{
			markers.add(Markers.semantic(this.position, "field.type.infer.novalue", this.name.unqualified));
			this.type = Types.ANY;
		}

		if (this.property == null)
		{
			return;
		}

		this.property.setType(this.type);

		final IMethod getter = this.property.getGetter();
		final IMethod setter = this.property.getSetter();

		final IValue receiver = this.hasModifier(Modifiers.STATIC) ?
			                        null :
			                        new ThisExpr(this.enclosingClass.getType(), VariableThis.DEFAULT);
		if (getter != null)
		{
			getter.setType(this.type);
			if (getter.getValue() == null)
			{
				// get: this.FIELD_NAME
				getter.setValue(new FieldAccess(getter.getPosition(), receiver, this));
			}
		}
		if (setter != null)
		{
			final IParameter setterParameter = setter.getParameter(0);
			setterParameter.setType(this.type);

			if (setter.getValue() == null)
			{
				// set: this.FIELD_NAME = newValue
				final ICodePosition setterPosition = setter.getPosition();
				setter.setValue(new FieldAssignment(setterPosition, receiver, this,
				                                    new FieldAccess(setterPosition, null, setterParameter)));
			}
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);

		if (this.value != null)
		{
			this.value.checkTypes(markers, context);
		}

		if (this.property != null)
		{
			this.property.checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);

		if (this.value != null)
		{
			this.value.check(markers, context);
		}

		if (this.property != null)
		{
			this.property.checkTypes(markers, context);
		}

		if (Types.isSameType(this.type, Types.VOID))
		{
			markers.add(Markers.semantic(this.position, "field.type.void"));
		}

		ModifierUtil.checkModifiers(markers, this, this.modifiers, Modifiers.FIELD_MODIFIERS);
	}

	@Override
	public void foldConstants()
	{
		super.foldConstants();

		if (this.value != null)
		{
			this.value = this.value.foldConstants();
		}

		if (this.property != null)
		{
			this.property.foldConstants();
		}
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		super.cleanup(context, compilableList);

		if (this.value != null)
		{
			this.value = this.value.cleanup(context, compilableList);
		}

		if (this.property != null)
		{
			this.property.cleanup(context, compilableList);
		}
	}

	private boolean hasFinalValue()
	{
		return this.hasModifier(Modifiers.FINAL) && this.value.isConstant();
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		final int modifiers = this.modifiers.toFlags() & ModifierUtil.JAVA_MODIFIER_MASK;

		final Object value;
		if (this.value != null && this.hasModifier(Modifiers.STATIC) && this.hasFinalValue())
		{
			value = this.value.toObject();
		}
		else
		{
			value = null;
		}

		final FieldVisitor fieldVisitor = writer.visitField(modifiers, this.name.qualified, this.getDescriptor(),
		                                                    this.getSignature(), value);

		IField.writeAnnotations(fieldVisitor, this.modifiers, this.annotations, this.type);
		fieldVisitor.visitEnd();

		if (this.property != null)
		{
			this.property.write(writer);
		}
	}

	@Override
	public void writeClassInit(MethodWriter writer) throws BytecodeException
	{
		if (this.modifiers.hasIntModifier(Modifiers.STATIC))
		{
			return;
		}

		if (this.value != null)
		{
			writer.visitVarInsn(Opcodes.ALOAD, 0);
			this.value.writeExpression(writer, this.type);
			writer.visitFieldInsn(Opcodes.PUTFIELD, this.enclosingClass.getInternalName(), this.name.qualified,
			                      this.getDescriptor());
		}

		if (this.property != null)
		{
			this.property.writeClassInit(writer);
		}
	}

	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
		if (!this.modifiers.hasIntModifier(Modifiers.STATIC))
		{
			return;
		}

		if (this.value != null && !this.hasFinalValue())
		{
			this.value.writeExpression(writer, this.type);
			writer.visitFieldInsn(Opcodes.PUTSTATIC, this.enclosingClass.getInternalName(), this.name.qualified,
			                      this.getDescriptor());
		}

		if (this.property != null)
		{
			this.property.writeStaticInit(writer);
		}
	}

	@Override
	public void writeGet_Get(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		String owner = this.enclosingClass.getInternalName();
		String name = this.name.qualified;
		String desc = this.type.getExtendedName();
		if (this.modifiers.hasIntModifier(Modifiers.STATIC))
		{
			writer.visitFieldInsn(Opcodes.GETSTATIC, owner, name, desc);
		}
		else
		{
			writer.visitLineNumber(lineNumber);
			writer.visitFieldInsn(Opcodes.GETFIELD, owner, name, desc);
		}
	}

	@Override
	public void writeSet_Set(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		String owner = this.enclosingClass.getInternalName();
		String name = this.name.qualified;
		String desc = this.type.getExtendedName();
		if (this.modifiers.hasIntModifier(Modifiers.STATIC))
		{
			writer.visitFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
		}
		else
		{
			writer.visitLineNumber(lineNumber);
			writer.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		super.toString(prefix, buffer);
		this.modifiers.toString(buffer);

		IDataMember.toString(prefix, buffer, this, "field.type_ascription");

		if (this.value != null)
		{
			Formatting.appendSeparator(buffer, "field.assignment", '=');
			this.value.toString(prefix, buffer);
		}
	}
}
