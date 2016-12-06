package dyvil.tools.compiler.ast.parameter;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.AnnotatableVisitor;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.ClassOperator;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.backend.visitor.AnnotationValueReader;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.lang.annotation.ElementType;

public abstract class AbstractParameter extends Member implements IParameter
{
	public static final String DEFAULT_VALUE       = "Ldyvil/annotation/internal/DefaultValue;";
	public static final String DEFAULT_ARRAY_VALUE = "Ldyvil/annotation/internal/DefaultArrayValue;";

	protected IValue defaultValue;

	// Metadata
	protected ICallableMember method;
	protected boolean         assigned;
	protected int             index;
	protected int             localIndex;
	protected IType           internalType;

	public AbstractParameter()
	{
	}

	public AbstractParameter(Name name)
	{
		super(name);
	}

	public AbstractParameter(Name name, IType type)
	{
		super(name, type);
	}

	public AbstractParameter(ICodePosition position, Name name, IType type)
	{
		super(position, name, type);
	}

	public AbstractParameter(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		super(position, name, type, modifiers, annotations);
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.METHOD_PARAMETER;
	}

	@Override
	public boolean isField()
	{
		return false;
	}

	@Override
	public boolean isVariable()
	{
		return true;
	}

	@Override
	public boolean isAssigned()
	{
		return this.assigned;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.PARAMETER;
	}

	@Override
	public void setMethod(ICallableMember method)
	{
		this.method = method;
	}

	@Override
	public ICallableMember getMethod()
	{
		return this.method;
	}

	@Override
	public ModifierSet getModifiers()
	{
		if (this.modifiers == null)
		{
			this.modifiers = new FlagModifierSet();
		}

		return this.modifiers;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
		this.internalType = null;
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
	public void setValue(IValue value)
	{
		this.defaultValue = value;
	}

	@Override
	public IValue getValue()
	{
		return this.defaultValue;
	}

	@Override
	public void setIndex(int index)
	{
		this.index = index;
	}

	@Override
	public int getIndex()
	{
		return this.index;
	}

	@Override
	public void setLocalIndex(int index)
	{
		this.localIndex = index;
	}

	@Override
	public int getLocalIndex()
	{
		return this.localIndex;
	}

	@Override
	public void setVarargs(boolean varargs)
	{
		if (varargs)
		{
			this.getModifiers().addIntModifier(Modifiers.VARARGS);
		}
	}

	@Override
	public boolean isVarargs()
	{
		return this.hasModifier(Modifiers.VARARGS);
	}

	@Override
	public boolean addRawAnnotation(String type, IAnnotation annotation)
	{
		return true;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String internalType)
	{
		switch (internalType)
		{
		case "dyvil/annotation/internal/DefaultValue":
			return new AnnotationValueReader(this);
		case "dyvil/annotation/internal/DefaultArrayValue":
			return new AnnotationValueReader(value -> this.defaultValue = value.withType(this.type, this.type, null,
			                                                                             null));
		}

		return IParameter.super.visitAnnotation(internalType);
	}

	@Override
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue receiver, IContext context)
	{
		return receiver;
	}

	@Override
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue receiver, IValue newValue)
	{
		this.assigned = true;
		return IParameter.super.checkAssign(markers, context, position, receiver, newValue);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);

		if (this.defaultValue != null)
		{
			this.defaultValue.resolveTypes(markers, context);
		}

		this.internalType = null;

		if (this.type != null)
		{
			if (this.type.isExtension())
			{
				this.getModifiers().addIntModifier(Modifiers.INFIX_FLAG);
			}
			else if (this.modifiers != null && this.modifiers.hasIntModifier(Modifiers.INFIX_FLAG))
			{
				this.type.setExtension(true);
			}
		}
	}

	@Override
	public void writeGet_Get(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		writer.visitVarInsn(this.type.getLoadOpcode(), this.localIndex);
	}

	@Override
	public void writeSet_Set(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		writer.visitVarInsn(this.type.getStoreOpcode(), this.localIndex);
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
	}

	public static void writeInitImpl(IParameter parameter, MethodWriter writer)
	{
		final ModifierSet modifiers = parameter.getModifiers();
		final AnnotationList annotations = parameter.getAnnotations();
		final IType type = parameter.getType();
		final IValue defaultValue = parameter.getValue();

		final int intModifiers = modifiers == null ?
			                         0 :
			                         modifiers.toFlags() & Modifiers.PARAMETER_MODIFIERS
				                         & ModifierUtil.JAVA_MODIFIER_MASK;

		final int index = parameter.getIndex();

		parameter.setLocalIndex(writer.localCount());
		writer.visitParameter(parameter.getLocalIndex(), parameter.getInternalName(), parameter.getInternalType(),
		                      intModifiers);

		// Annotations
		final AnnotatableVisitor visitor = (desc, visible) -> writer.visitParameterAnnotation(index, desc, visible);

		if (annotations != null)
		{
			annotations.write(visitor);
		}

		ModifierUtil.writeModifiers(visitor, modifiers);

		IType.writeAnnotations(type, writer, TypeReference.newFormalParameterReference(index), "");

		// Default Value
		if (defaultValue == null)
		{
			return;
		}

		if (type.isArrayType())
		{
			final AnnotationVisitor annotationVisitor = writer
				                                            .visitParameterAnnotation(index, DEFAULT_ARRAY_VALUE, false)
				                                            .visitArray("value");

			ArrayExpr arrayExpr = (ArrayExpr) defaultValue;
			int count = arrayExpr.valueCount();
			IType elementType = type.getElementType();

			for (int i = 0; i < count; i++)
			{
				writeDefaultAnnotation(annotationVisitor, elementType, arrayExpr.getValue(i));
			}

			annotationVisitor.visitEnd();

			return;
		}

		final AnnotationVisitor annotationVisitor = writer.visitParameterAnnotation(index, DEFAULT_VALUE, false);
		writeDefaultAnnotation(annotationVisitor, type, defaultValue);

		annotationVisitor.visitEnd();
	}

	private static void writeDefaultAnnotation(AnnotationVisitor visitor, IType type, IValue value)
	{
		if (type.isPrimitive())
		{
			switch (type.getTypecode())
			{
			case PrimitiveType.BOOLEAN_CODE:
				visitor.visit("booleanValue", value.booleanValue());
				return;
			case PrimitiveType.BYTE_CODE:
			case PrimitiveType.SHORT_CODE:
			case PrimitiveType.CHAR_CODE:
			case PrimitiveType.INT_CODE:
				visitor.visit("intValue", value.intValue());
				return;
			case PrimitiveType.LONG_CODE:
				visitor.visit("longValue", value.longValue());
				return;
			case PrimitiveType.FLOAT_CODE:
				visitor.visit("floatValue", value.floatValue());
				return;
			case PrimitiveType.DOUBLE_CODE:
				visitor.visit("doubleValue", value.doubleValue());
				return;
			}

			return;
		}

		IClass iClass = type.getTheClass();
		if (iClass == Types.STRING_CLASS)
		{
			visitor.visit("stringValue", value.stringValue());
			return;
		}
		if (iClass == ClassOperator.LazyFields.CLASS_CLASS)
		{
			visitor.visit("classValue", value.toObject());
		}
	}

	@Override
	public void writeInit(MethodWriter writer)
	{
		AbstractParameter.writeInitImpl(this, writer);
	}

	@Override
	public void writeInit(MethodWriter writer, IValue value) throws BytecodeException
	{
		this.writeInit(writer);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.annotations != null)
		{
			int count = this.annotations.annotationCount();
			for (int i = 0; i < count; i++)
			{
				this.annotations.getAnnotation(i).toString(prefix, buffer);
				buffer.append(' ');
			}
		}

		if (this.modifiers != null)
		{
			this.modifiers.toString(this.getKind(), buffer);
		}

		boolean typeAscription = false;
		if (this.type != null)
		{
			typeAscription = Formatting.typeAscription("parameter.type_ascription", this);

			if (!typeAscription)
			{
				this.appendType(prefix, buffer);
				buffer.append(' ');
			}
		}

		buffer.append(this.name);

		if (typeAscription)
		{
			Formatting.appendSeparator(buffer, "parameter.type_ascription", ':');
			this.appendType(prefix, buffer);
		}

		if (this.defaultValue != null)
		{
			Formatting.appendSeparator(buffer, "field.assignment", '=');
			this.defaultValue.toString(prefix, buffer);
		}
	}

	private void appendType(String prefix, StringBuilder buffer)
	{
		if (this.isVarargs())
		{
			this.type.getElementType().toString(prefix, buffer);
			buffer.append("...");
		}
		else
		{
			this.type.toString(prefix, buffer);
		}
	}
}
