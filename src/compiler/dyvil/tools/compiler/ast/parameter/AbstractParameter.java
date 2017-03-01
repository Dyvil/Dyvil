package dyvil.tools.compiler.ast.parameter;

import dyvil.annotation.internal.NonNull;
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
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.ArrayType;
import dyvil.tools.compiler.ast.type.compound.LambdaType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.backend.visitor.AnnotationValueReader;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.lang.annotation.ElementType;

public abstract class AbstractParameter extends Variable implements IParameter
{
	public static final String DEFAULT_VALUE       = "Ldyvil/annotation/internal/DefaultValue;";
	public static final String DEFAULT_ARRAY_VALUE = "Ldyvil/annotation/internal/DefaultArrayValue;";

	// Metadata
	protected ICallableMember method;
	protected int             index;
	private   IType           covariantType;

	public AbstractParameter()
	{
	}

	public AbstractParameter(Name name)
	{
		super(name, null);
	}

	public AbstractParameter(Name name, IType type)
	{
		super(name, type);
	}

	public AbstractParameter(ICallableMember callable, ICodePosition position, Name name, IType type)
	{
		super(position, name, type);
		this.method = callable;
	}

	public AbstractParameter(ICallableMember callable, ICodePosition position, Name name, IType type,
		                        ModifierSet modifiers, AnnotationList annotations)
	{
		super(position, name, type, modifiers, annotations);
		this.method = callable;
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.METHOD_PARAMETER;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.PARAMETER;
	}

	@Override
	public boolean isLocal()
	{
		return true;
	}

	@Override
	public ICallableMember getMethod()
	{
		return this.method;
	}

	@Override
	public void setMethod(ICallableMember method)
	{
		this.method = method;
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
		this.covariantType = null;
	}

	@Override
	public String getInternalName()
	{
		return this.name == null ? null : this.name.qualified;
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
	public boolean isVarargs()
	{
		return this.hasModifier(Modifiers.VARARGS);
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
			return new AnnotationValueReader(value -> this.value = value.withType(this.type, this.type, null, null));
		}

		return IParameter.super.visitAnnotation(internalType);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);

		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}

		this.covariantType = null;

		final LambdaType functionType;
		if (this.type != null && (functionType = this.type.extract(LambdaType.class)) != null)
		{
			if (functionType.isExtension())
			{
				this.getModifiers().addIntModifier(Modifiers.INFIX_FLAG);
			}
			else if (this.modifiers != null && this.modifiers.hasIntModifier(Modifiers.INFIX_FLAG))
			{
				functionType.setExtension(true);
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
		final AnnotationList annotations = parameter.getAnnotations();
		final IType type = parameter.getType();
		final IValue defaultValue = parameter.getValue();
		final long flags = ModifierUtil.getFlags(parameter);

		final int index = parameter.getIndex();

		parameter.setLocalIndex(writer.localCount());
		writer.visitParameter(parameter.getLocalIndex(), parameter.getInternalName(), parameter.getCovariantType(),
		                      ModifierUtil.getJavaModifiers(flags));

		// Annotations
		final AnnotatableVisitor visitor = (desc, visible) -> writer.visitParameterAnnotation(index, desc, visible);

		if (annotations != null)
		{
			annotations.write(visitor);
		}

		ModifierUtil.writeModifiers(visitor, parameter, flags);

		IType.writeAnnotations(type, writer, TypeReference.newFormalParameterReference(index), "");

		// Default Value
		if (defaultValue == null)
		{
			return;
		}

		final ArrayType arrayType = type.extract(ArrayType.class);
		if (arrayType != null)
		{
			final AnnotationVisitor annotationVisitor = writer
				                                            .visitParameterAnnotation(index, DEFAULT_ARRAY_VALUE, false)
				                                            .visitArray("value");

			ArrayExpr arrayExpr = (ArrayExpr) defaultValue;
			int count = arrayExpr.valueCount();
			IType elementType = arrayType.getElementType();

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
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		if (this.annotations != null)
		{
			int count = this.annotations.annotationCount();
			for (int i = 0; i < count; i++)
			{
				this.annotations.getAnnotation(i).toString(indent, buffer);
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
				this.appendType(indent, buffer);
				buffer.append(' ');
			}
		}

		if (this.name != null)
		{
			buffer.append(this.name);
		}
		else
		{
			buffer.append('_');
		}

		if (typeAscription)
		{
			Formatting.appendSeparator(buffer, "parameter.type_ascription", ':');
			this.appendType(indent, buffer);
		}

		if (this.value != null)
		{
			Formatting.appendSeparator(buffer, "field.assignment", '=');
			this.value.toString(indent, buffer);
		}
	}

	private void appendType(String prefix, StringBuilder buffer)
	{
		if (this.isVarargs())
		{
			this.type.extract(ArrayType.class).getElementType().toString(prefix, buffer);
			buffer.append("...");
		}
		else
		{
			this.type.toString(prefix, buffer);
		}
	}
}
