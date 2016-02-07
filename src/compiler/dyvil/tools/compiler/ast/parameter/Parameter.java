package dyvil.tools.compiler.ast.parameter;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.AnnotatableVisitor;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.TypeReference;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.ArrayExpr;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.modifiers.FlagModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.operator.ClassOperator;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.visitor.AnnotationValueReader;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public abstract class Parameter extends Member implements IParameter
{
	protected int     index;
	protected int     localIndex;
	protected boolean varargs;
	
	protected IValue defaultValue;
	
	public Parameter()
	{
	}
	
	public Parameter(Name name)
	{
		super(name);
	}
	
	public Parameter(Name name, IType type)
	{
		super(name, type);
	}

	public Parameter(Name name, IType type, ModifierSet modifiers)
	{
		super(name, type, modifiers);
	}

	public Parameter(ICodePosition position, Name name, IType type, ModifierSet modifiers)
	{
		super(position, name, type, modifiers);
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
		this.varargs = varargs;
	}
	
	@Override
	public boolean isVarargs()
	{
		return this.varargs;
	}
	
	@Override
	public String getDescription()
	{
		return this.getInternalType().getExtendedName();
	}
	
	@Override
	public String getSignature()
	{
		return this.getInternalType().getSignature();
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
		case "dyvil/annotation/_internal/DefaultValue":
			return new AnnotationValueReader(this);
		case "dyvil/annotation/_internal/DefaultArrayValue":
			return new AnnotationValueReader(
					value -> this.defaultValue = value.withType(this.type, this.type, null, null));
		}

		return IParameter.super.visitAnnotation(internalType);
	}

	protected void writeDefaultAnnotation(MethodWriter writer)
	{
		if (this.type.isArrayType())
		{
			AnnotationVisitor visitor = writer
					.visitParameterAnnotation(this.index, "Ldyvil/annotation/_internal/DefaultArrayValue;", false);
			visitor = visitor.visitArray("value");

			ArrayExpr arrayExpr = (ArrayExpr) this.defaultValue;
			int count = arrayExpr.valueCount();
			IType elementType = this.type.getElementType();

			for (int i = 0; i < count; i++)
			{
				writeDefaultAnnotation(visitor, elementType, arrayExpr.getValue(i));
			}

			visitor.visitEnd();

			return;
		}

		AnnotationVisitor visitor = writer
				.visitParameterAnnotation(this.index, "Ldyvil/annotation/_internal/DefaultValue;", false);
		writeDefaultAnnotation(visitor, this.type, this.defaultValue);
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
		if (iClass == ClassOperator.Types.CLASS_CLASS)
		{
			visitor.visit("classValue", value.toObject());
		}
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);
		
		if (this.defaultValue != null)
		{
			this.defaultValue.resolveTypes(markers, context);
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);

		if (this.defaultValue != null)
		{
			this.defaultValue = this.defaultValue.resolve(markers, context);

			final IValue typed = this.defaultValue.withType(this.type, null, markers, context);
			if (typed == null)
			{
				final Marker marker = Markers
						.semantic(this.defaultValue.getPosition(), this.getKind().getName() + ".type.incompatible",
						          this.name.unqualified);
				marker.addInfo(Markers.getSemantic(this.getKind().getName() + ".type", this.type));
				marker.addInfo(Markers.getSemantic("value.type", this.defaultValue.getType()));
				markers.add(marker);
			}
			else
			{
				this.defaultValue = typed;
			}

			this.defaultValue = Util.constant(this.defaultValue, markers);
			return;
		}
		if (this.type == Types.UNKNOWN)
		{
			markers.add(Markers.semantic(this.position, this.getKind().getName() + ".type.nodefault",
			                             this.name.unqualified));
			this.type = Types.ANY;
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);
		
		if (this.defaultValue != null)
		{
			this.defaultValue.checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);

		ModifierUtil
				.checkModifiers(markers, this, this.modifiers, Modifiers.PARAMETER_MODIFIERS);

		if (this.defaultValue != null)
		{
			this.defaultValue.check(markers, context);
		}

		if (this.type == Types.VOID)
		{
			markers.add(Markers.semantic(this.position, this.getKind().getName() + ".type.void"));
		}
	}

	@Override
	public void foldConstants()
	{
		super.foldConstants();
		
		if (this.defaultValue != null)
		{
			this.defaultValue = this.defaultValue.foldConstants();
		}
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		super.cleanup(context, compilableList);
		
		if (this.defaultValue != null)
		{
			this.defaultValue = this.defaultValue.cleanup(context, compilableList);
		}
	}
	
	protected void writeAnnotations(MethodWriter writer)
	{
		final AnnotatableVisitor visitor = (desc, visible) -> writer
				.visitParameterAnnotation(Parameter.this.index, desc, visible);

		if (this.annotations != null)
		{
			final int count = this.annotations.annotationCount();
			for (int i = 0; i < count; i++)
			{
				this.annotations.getAnnotation(i).write(visitor);
			}
		}

		ModifierUtil.writeModifiers(visitor, this.modifiers);
		
		this.type.writeAnnotations(writer, TypeReference.newFormalParameterReference(this.index), "");

		if (this.defaultValue != null)
		{
			this.writeDefaultAnnotation(writer);
		}
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
			this.modifiers.toString(buffer);
		}

		if (this.varargs)
		{
			this.type.getElementType().toString(prefix, buffer);
			buffer.append("... ");
		}
		else
		{
			this.type.toString(prefix, buffer);
			buffer.append(' ');
		}
		buffer.append(this.name);
		
		if (this.defaultValue != null)
		{
			Formatting.appendSeparator(buffer, "field.assignment", '=');
			this.defaultValue.toString(prefix, buffer);
		}
	}
}
