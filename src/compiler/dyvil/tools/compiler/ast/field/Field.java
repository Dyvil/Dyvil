package dyvil.tools.compiler.ast.field;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.FieldVisitor;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.access.FieldAssignment;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.VoidValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisExpr;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
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
	protected String internalName;
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

	public Field(IClass enclosingClass, ICodePosition position, Name name, IType type, ModifierSet modifiers,
		            AnnotationList annotations)
	{
		super(position, name, type, modifiers, annotations);
		this.enclosingClass = enclosingClass;
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
		if (this.property != null)
		{
			return this.property;
		}

		return this.property = new Property(this.position, this.name, Types.UNKNOWN, new FlagModifierSet(), null);
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
				if (!receiver.isClassAccess())
				{
					markers.add(Markers.semantic(position, "field.access.static", this.name));
				}
				else if (receiver.getType().getTheClass() != this.enclosingClass)
				{
					markers.add(Markers.semantic(position, "field.access.static.type", this.name,
					                             this.enclosingClass.getFullName()));
				}
				receiver = receiver.asIgnoredClassAccess();
			}
			else if (receiver.isClassAccess())
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
				receiver = new ThisExpr(position, this.enclosingClass.getThisType(), context, markers);

				if (!this.enclosingClass.isAnonymous())
				{
					markers.add(Markers.semantic(position, "field.access.unqualified", this.name.unqualified));
				}
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
			                        new ThisExpr(this.enclosingClass.getThisType(), VariableThis.DEFAULT);
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
			final IParameter setterParameter = setter.getParameterList().get(0);
			setterParameter.setType(this.type);

			if (setter.getValue() == null)
			{
				final ICodePosition setterPosition = setter.getPosition();
				if (this.hasModifier(Modifiers.FINAL))
				{
					markers.add(Markers.semanticError(setterPosition, "field.property.setter.final", this.name));
					setter.setValue(new VoidValue(setterPosition)); // avoid abstract method error
				}
				else
				{
					// set: this.FIELD_NAME = newValue
					setter.setValue(new FieldAssignment(setterPosition, receiver, this,
					                                    new FieldAccess(setterPosition, null, setterParameter)));
				}
			}
		}

		this.property.resolve(markers, context);
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

		if (Types.isVoid(this.type))
		{
			markers.add(Markers.semantic(this.position, "field.type.void"));
		}
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

	private boolean hasConstantValue()
	{
		return this.hasModifier(Modifiers.FINAL) && this.value.isConstant();
	}

	@Override
	public String getInternalName()
	{
		if (this.internalName != null)
		{
			return this.internalName;
		}
		return this.internalName = this.name.qualified;
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		final int modifiers = this.modifiers.toFlags() & ModifierUtil.JAVA_MODIFIER_MASK;

		final Object value;
		if (this.value != null && this.hasModifier(Modifiers.STATIC) && this.hasConstantValue())
		{
			value = this.value.toObject();
		}
		else
		{
			value = null;
		}

		final String name = this.getInternalName();
		final String descriptor = this.getDescriptor();
		final String signature = this.type.needsSignature() ? this.getSignature() : null;

		final FieldVisitor fieldVisitor = writer.visitField(modifiers, name, descriptor, signature, value);

		IField.writeAnnotations(fieldVisitor, this.modifiers, this.annotations, this.type);
		fieldVisitor.visitEnd();

		if (this.property != null)
		{
			this.property.write(writer);
		}

		if (!this.hasModifier(Modifiers.LAZY))
		{
			return;
		}

		final String lazyName = name + "$lazy";
		final String ownerClass = this.enclosingClass.getInternalName();
		final boolean isStatic = (modifiers & Modifiers.STATIC) != 0;

		writer
			.visitField(isStatic ? Modifiers.PRIVATE | Modifiers.STATIC : Modifiers.PRIVATE, lazyName, "Z", null, null);

		final MethodWriter access = new MethodWriterImpl(writer, writer.visitMethod(modifiers, lazyName,
		                                                                            "()" + descriptor, null, null));
		access.visitCode();

		// Get the $lazy flag
		int getOpcode;
		if (!isStatic)
		{
			access.setLocalType(0, ownerClass);
			access.visitVarInsn(Opcodes.ALOAD, 0);
			access.visitFieldInsn(getOpcode = Opcodes.GETFIELD, ownerClass, lazyName, "Z");
		}
		else
		{
			access.visitFieldInsn(getOpcode = Opcodes.GETSTATIC, ownerClass, lazyName, "Z");
		}

		Label label = new Label();
		final int returnOpcode = this.type.getReturnOpcode();
		access.visitJumpInsn(Opcodes.IFEQ, label);                      // if (this.fieldName$lazy) {

		if (!isStatic)
		{
			access.visitVarInsn(Opcodes.ALOAD, 0);
		}

		access.visitFieldInsn(getOpcode, ownerClass, name, descriptor); //     this.fieldName ->
		access.visitInsn(returnOpcode);                                 //     return
		access.visitTargetLabel(label);                                 // }

		// this.fieldName$lazy = true
		// return this.fieldName = value

		if (!isStatic)
		{
			access.visitVarInsn(Opcodes.ALOAD, 0);
			access.visitInsn(Opcodes.DUP);
			access.visitLdcInsn(1);
			access.visitFieldInsn(Opcodes.PUTFIELD, ownerClass, lazyName, "Z");

			this.value.writeExpression(access, this.type);
			access.visitInsn(Opcodes.AUTO_DUP_X1);
			access.visitFieldInsn(Opcodes.PUTFIELD, ownerClass, name, descriptor);
		}
		else
		{
			access.visitLdcInsn(1);
			access.visitFieldInsn(Opcodes.PUTSTATIC, ownerClass, lazyName, "Z");

			this.value.writeExpression(access, this.type);
			access.visitInsn(Opcodes.AUTO_DUP);
			access.visitFieldInsn(Opcodes.PUTSTATIC, ownerClass, name, descriptor);
		}
		access.visitInsn(returnOpcode);
		access.visitEnd();
	}

	@Override
	public void writeClassInit(MethodWriter writer) throws BytecodeException
	{
		if (this.hasModifier(Modifiers.STATIC))
		{
			return;
		}

		if (this.value != null && !this.hasModifier(Modifiers.LAZY))
		{
			writer.visitVarInsn(Opcodes.ALOAD, 0);
			this.value.writeExpression(writer, this.type);
			writer.visitFieldInsn(Opcodes.PUTFIELD, this.enclosingClass.getInternalName(), this.getInternalName(),
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
		if (!this.hasModifier(Modifiers.STATIC))
		{
			return;
		}

		if (this.value != null && !this.hasConstantValue() && !this.hasModifier(Modifiers.LAZY))
		{
			this.value.writeExpression(writer, this.type);
			writer.visitFieldInsn(Opcodes.PUTSTATIC, this.enclosingClass.getInternalName(), this.getInternalName(),
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
		String name = this.getInternalName();
		String desc = this.getDescriptor();

		writer.visitLineNumber(lineNumber);
		switch (this.modifiers.toFlags() & (Modifiers.STATIC | Modifiers.LAZY))
		{
		case 0: // neither static nor lazy
			writer.visitFieldInsn(Opcodes.GETFIELD, owner, name, desc);
			return;
		case Modifiers.STATIC:
			writer.visitFieldInsn(Opcodes.GETSTATIC, owner, name, desc);
			return;
		case Modifiers.LAZY:
			writer.visitMethodInsn(this.hasModifier(Modifiers.PRIVATE) ? Opcodes.INVOKESPECIAL : Opcodes.INVOKEVIRTUAL,
			                       owner, name + "$lazy", "()" + desc, this.enclosingClass.isInterface());
			return;
		case Modifiers.STATIC | Modifiers.LAZY: // both static and lazy
			writer.visitMethodInsn(Opcodes.INVOKESTATIC, owner, name + "$lazy", "()" + desc,
			                       this.enclosingClass.isInterface());
		}
	}

	@Override
	public void writeSet_Set(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		String owner = this.enclosingClass.getInternalName();
		String name = this.getInternalName();
		String desc = this.getDescriptor();

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
		IDataMember.toString(prefix, buffer, this, "field.type_ascription");

		if (this.value != null)
		{
			Formatting.appendSeparator(buffer, "field.assignment", '=');
			this.value.toString(prefix, buffer);
		}

		if (this.property != null)
		{
			Property.formatBody(this.property, prefix, buffer);
		}
	}
}
