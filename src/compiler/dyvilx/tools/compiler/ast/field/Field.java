package dyvilx.tools.compiler.ast.field;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.FieldVisitor;
import dyvilx.tools.asm.Label;
import dyvilx.tools.asm.TypeReference;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.modifiers.ModifierUtil;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.CombiningContext;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.context.IDefaultContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.ThisExpr;
import dyvilx.tools.compiler.ast.expression.WriteableExpression;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.expression.access.FieldAssignment;
import dyvilx.tools.compiler.ast.expression.constant.VoidValue;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.member.Member;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.method.MethodWriterImpl;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.check.ModifierChecks;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.Deprecation;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;

public class Field extends Member implements IField, IDefaultContext
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

	public Field(IClass enclosingClass, Name name, IType type, AttributeList attributes)
	{
		super(name, type, attributes);
		this.enclosingClass = enclosingClass;
	}

	public Field(IClass enclosingClass, SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		super(position, name, type, attributes);
		this.enclosingClass = enclosingClass;
	}

	@Override
	public IClass getEnclosingClass()
	{
		return this.enclosingClass;
	}

	@Override
	public void setEnclosingClass(IClass enclosingClass)
	{
		this.enclosingClass = enclosingClass;
	}

	@Override
	public IValue getValue()
	{
		return this.value;
	}

	@Override
	public void setValue(IValue value)
	{
		this.value = value;
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

		return this.property = new Property(this.position, this.name, Types.UNKNOWN);
	}

	@Override
	public boolean isThisAvailable()
	{
		return !this.isStatic();
	}

	@Override
	public boolean skipAnnotation(String type, Annotation annotation)
	{
		switch (type)
		{
		case ModifierUtil.TRANSIENT_INTERNAL:
			this.attributes.addFlag(Modifiers.TRANSIENT);
			return true;
		case ModifierUtil.VOLATILE_INTERNAL:
			this.attributes.addFlag(Modifiers.VOLATILE);
			return true;
		case Deprecation.JAVA_INTERNAL:
		case Deprecation.DYVIL_INTERNAL:
			this.attributes.addFlag(Modifiers.DEPRECATED);
			return false;
		}
		return false;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.FIELD;
	}

	@Override
	public IValue checkAccess(MarkerList markers, SourcePosition position, IValue receiver, IContext context)
	{
		ModifierChecks.checkVisibility(this, position, markers, context);
		if (receiver == null)
		{
			if (!this.isStatic())
			{
				if (!context.isThisAvailable())
				{
					markers.add(Markers.semanticError(position, "field.access.instance", this.name));
				}
				else
				{
					receiver = new ThisExpr(position, this.enclosingClass.getThisType(), markers, context);

					if (!this.enclosingClass.isAnonymous())
					{
						markers.add(Markers.semantic(position, "field.access.unqualified", this.name.unqualified));
					}
				}
			}

			return receiver;
		}

		if (this.isStatic())
		{
			if (!receiver.isClassAccess())
			{
				markers.add(Markers.semanticError(position, "field.access.static", this.name));
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
			receiver = TypeChecker
				           .convertValue(receiver, this.enclosingClass.getThisType(), receiver.getType(), markers,
				                         context,
				                         TypeChecker.markerSupplier("field.access.receiver_type", this.name));
		}

		return receiver;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);

		if (this.value != null)
		{
			this.value.resolveTypes(markers, new CombiningContext(this, context));
		}

		if (this.property == null)
		{
			return;
		}

		copyModifiers(this.attributes, this.property.getAttributes());
		this.property.setEnclosingClass(this.enclosingClass);

		this.property.setType(this.type);
		this.property.resolveTypes(markers, context);
	}

	public static void copyModifiers(AttributeList from, AttributeList to)
	{
		final int exisiting = to.flags();
		final int newModifiers;
		if ((exisiting & Modifiers.ACCESS_MODIFIERS) != 0)
		{
			// only transfer static modifiers to the property
			newModifiers = from.flags() & Modifiers.STATIC;
		}
		else
		{
			// only transfer public, static and protected modifiers to the property
			newModifiers = from.flags() & (Modifiers.STATIC | Modifiers.PUBLIC | Modifiers.PROTECTED);
		}

		to.addFlag(newModifiers);
		to.addAll(from.annotations());
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);

		if (this.value != null)
		{
			final IContext context1 = new CombiningContext(this, context);

			this.value = this.value.resolve(markers, context1);

			boolean inferType = false;
			if (this.type == Types.UNKNOWN)
			{
				inferType = true;
				this.type = this.value.getType();
			}

			final TypeChecker.MarkerSupplier markerSupplier = TypeChecker.markerSupplier("field.type.incompatible",
			                                                                             "field.type", "value.type",
			                                                                             this.name);
			this.value = TypeChecker.convertValue(this.value, this.type, this.type, markers, context1, markerSupplier);

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
			final IParameter setterParameter = setter.getParameters().get(0);
			setterParameter.setType(this.type);

			if (setter.getValue() == null)
			{
				final SourcePosition setterPosition = setter.getPosition();
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
			this.value.checkTypes(markers, new CombiningContext(this, context));
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
			this.value.check(markers, new CombiningContext(this, context));
		}
		else if (!this.hasDefaultInit())
		{
			final Marker marker = Markers.semanticError(this.position, "field.type.no_default", this.name);
			marker.addInfo(Markers.getSemantic("field.type", this.type));
			markers.add(marker);
		}

		if (this.property != null)
		{
			this.property.check(markers, context);
		}

		if (Types.isVoid(this.type))
		{
			markers.add(Markers.semanticError(this.position, "field.type.void"));
		}
	}

	protected boolean hasDefaultInit()
	{
		return this.type.hasDefaultValue();
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
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		super.cleanup(compilableList, classCompilableList);

		if (this.value != null)
		{
			this.value = this.value.cleanup(compilableList, classCompilableList);
		}

		if (this.property != null)
		{
			this.property.cleanup(compilableList, classCompilableList);
		}
	}

	@Override
	public boolean hasConstantValue()
	{
		return this.value != null && this.hasModifier(Modifiers.CONST) && this.value.isConstant() //
		       && (this.type.isPrimitive() || this.type.getInternalName().equals("java/lang/String"));
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
	public String getDescriptor()
	{
		if (this.descriptor != null)
		{
			return this.descriptor;
		}

		return this.descriptor = this.type.getExtendedName();
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		final long flags = ModifierUtil.getFlags(this);
		final int modifiers = ModifierUtil.getJavaModifiers(flags);

		final String name = this.getInternalName();
		final String descriptor = this.getDescriptor();
		final String signature = this.getType().needsSignature() ? this.getSignature() : null;
		final Object value = this.getObjectValue();

		final FieldVisitor fieldVisitor = writer.visitField(modifiers, name, descriptor, signature, value);
		this.writeAnnotations(fieldVisitor, flags);
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
		final boolean isStatic = (flags & Modifiers.STATIC) != 0;

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

	private void writeAnnotations(FieldVisitor fieldVisitor, long flags)
	{
		ModifierUtil.writeModifiers(fieldVisitor, flags);

		final AttributeList annotations = this.getAttributes();
		if (annotations != null)
		{
			int count = annotations.size();
			for (int i = 0; i < count; i++)
			{
				annotations.get(i).write(fieldVisitor);
			}
		}

		IType.writeAnnotations(this.getType(), fieldVisitor, TypeReference.newTypeReference(TypeReference.FIELD), "");
	}

	@Nullable
	public Object getObjectValue()
	{
		return this.hasConstantValue() ? this.value.toObject() : null;
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

	private void writeReceiver(MethodWriter writer, WriteableExpression receiver)
	{
		if (receiver != null && !this.isStatic())
		{
			receiver.writeNullCheckedExpression(writer, this.getEnclosingClass().getReceiverType());
		}
	}

	@Override
	public void writeGet(@NonNull MethodWriter writer, WriteableExpression receiver, int lineNumber)
		throws BytecodeException
	{
		this.writeReceiver(writer, receiver);

		final IClass enclosingClass = this.getEnclosingClass();
		final String owner = enclosingClass.getInternalName();
		final String name = this.getInternalName();
		final String desc = this.getDescriptor();

		writer.visitLineNumber(lineNumber);
		switch (this.getAttributes().flags() & (Modifiers.STATIC | Modifiers.LAZY))
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
			                       enclosingClass.isInterface());
		}
	}

	@Override
	public void writeSet(@NonNull MethodWriter writer, WriteableExpression receiver, @NonNull WriteableExpression value,
		int lineNumber) throws BytecodeException
	{
		this.writeReceiver(writer, receiver);
		value.writeExpression(writer, this.getType());
		this.writePutInsn(writer, lineNumber);
	}

	@Override
	public void writeSetCopy(@NonNull MethodWriter writer, WriteableExpression receiver,
		@NonNull WriteableExpression value, int lineNumber) throws BytecodeException
	{
		this.writeReceiver(writer, receiver);
		value.writeExpression(writer, this.getType());
		writer.visitInsn(this.isStatic() ? Opcodes.AUTO_DUP : Opcodes.AUTO_DUP_X1);
		this.writePutInsn(writer, lineNumber);
	}

	private void writePutInsn(MethodWriter writer, int lineNumber)
	{
		final String owner = this.getEnclosingClass().getInternalName();
		final String name = this.getInternalName();
		final String desc = this.getDescriptor();

		if (this.isStatic())
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
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		this.attributesToString(indent, buffer);
		IDataMember.toString(indent, buffer, this, "field.type_ascription");

		this.valueToString(indent, buffer);

		if (this.property != null)
		{
			Property.formatBody(this.property, indent, buffer);
		}
	}

	protected void attributesToString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		super.toString(indent, buffer);
	}

	protected void valueToString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		if (this.value != null)
		{
			Formatting.appendSeparator(buffer, "field.assignment", '=');
			this.value.toString(indent, buffer);
		}
	}
}
