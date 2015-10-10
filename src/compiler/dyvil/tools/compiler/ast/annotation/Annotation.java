package dyvil.tools.compiler.ast.annotation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.AnnotatableVisitor;
import dyvil.tools.asm.AnnotationVisitor;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.IClassMetadata;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.Array;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.MethodParameter;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public final class Annotation implements IAnnotation
{
	public static final class Types
	{
		public static final IClass	RETENTION_CLASS	= Package.javaLangAnnotation.resolveClass("Retention");
		public static final IClass	TARGET_CLASS	= Package.javaLangAnnotation.resolveClass("Target");
		
		public static final IClass		ANNOTATION_CLASS	= Package.javaLangAnnotation.resolveClass("Annotation");
		public static final ClassType	ANNOTATION			= new ClassType(ANNOTATION_CLASS);
		
		private Types()
		{
			// no instances
		}
	}
	
	public static final MethodParameter VALUE = new MethodParameter(Name.getQualified("value"));
	
	protected ICodePosition	position;
	protected IArguments	arguments	= EmptyArguments.INSTANCE;
	
	// Metadata
	protected IType type;
	
	public Annotation()
	{
	}
	
	public Annotation(IType type)
	{
		this.type = type;
	}
	
	public Annotation(ICodePosition position)
	{
		this.position = position;
	}
	
	public Annotation(ICodePosition position, IType type)
	{
		this.position = position;
		this.type = type;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IArguments getArguments()
	{
		return this.arguments;
	}
	
	@Override
	public void setArguments(IArguments arguments)
	{
		this.arguments = arguments;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
		
		this.arguments.resolveTypes(markers, context);
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.arguments.resolve(markers, context);
		
		IClass theClass = this.type.getTheClass();
		if (theClass == null)
		{
			return;
		}
		
		int count = theClass.parameterCount();
		for (int i = 0; i < count; i++)
		{
			IParameter param = theClass.getParameter(i);
			IType type = param.getType();
			IValue value = this.arguments.getValue(i, param);
			if (value == null)
			{
				if (param.getValue() == null)
				{
					markers.add(this.position, "annotation.parameter.missing", this.type, param.getName());
				}
				continue;
			}
			
			IValue value1 = value.withType(type, type, markers, context);
			if (value1 == null)
			{
				Util.createTypeError(markers, value, type, type, "annotation.parameter.type", param.getName());
				continue;
			}
			
			value1 = Util.constant(value1, markers);
			if (value1 != value)
			{
				this.arguments.setValue(i, param, value1);
			}
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.type.checkType(markers, context, TypePosition.CLASS);
		
		this.arguments.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context, ElementType target)
	{
		if (this.type == null)
		{
			return;
		}
		
		IClass theClass = this.type.getTheClass();
		
		if (!theClass.hasModifier(Modifiers.ANNOTATION))
		{
			markers.add(this.position, "annotation.type", this.type.getName());
			return;
		}
		
		if (target == null)
		{
			return;
		}
		
		IClassMetadata metadata = theClass.getMetadata();
		if (!metadata.isTarget(target))
		{
			Marker error = markers.create(this.position, "annotation.target", this.type.getName());
			error.addInfo("Element Target: " + target);
			error.addInfo("Allowed Targets: " + metadata.getTargets());
			markers.add(error);
		}
	}
	
	@Override
	public void foldConstants()
	{
		this.arguments.foldConstants();
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.arguments.cleanup(context, compilableList);
	}
	
	private RetentionPolicy getRetention()
	{
		return this.type.getTheClass().getMetadata().getRetention();
	}
	
	@Override
	public void write(AnnotatableVisitor writer)
	{
		RetentionPolicy retention = this.getRetention();
		if (retention != RetentionPolicy.SOURCE)
		{
			this.write(writer.visitAnnotation(ClassFormat.internalToExtended(this.type.getInternalName()), retention == RetentionPolicy.RUNTIME));
		}
	}
	
	@Override
	public void write(TypeAnnotatableVisitor writer, int typeRef, TypePath typePath)
	{
		RetentionPolicy retention = this.getRetention();
		if (retention != RetentionPolicy.SOURCE)
		{
			this.write(writer.visitTypeAnnotation(typeRef, typePath, ClassFormat.internalToExtended(this.type.getInternalName()),
					retention == RetentionPolicy.RUNTIME));
		}
	}
	
	@Override
	public void write(AnnotationVisitor visitor)
	{
		IClass iclass = this.type.getTheClass();
		int count = iclass.parameterCount();
		for (int i = 0; i < count; i++)
		{
			IParameter param = iclass.getParameter(i);
			visitValue(visitor, param.getName().qualified, this.arguments.getValue(i, param));
		}
		visitor.visitEnd();
	}
	
	public static void visitValue(AnnotationVisitor visitor, String key, IValue value)
	{
		int valueType = value.valueTag();
		if (valueType == IValue.ARRAY)
		{
			AnnotationVisitor arrayVisitor = visitor.visitArray(key);
			Array array = (Array) value;
			int count = array.valueCount();
			for (int i = 0; i < count; i++)
			{
				visitValue(arrayVisitor, null, array.getValue(i));
			}
			arrayVisitor.visitEnd();
		}
		else if (valueType == IValue.ENUM_ACCESS)
		{
			EnumValue enumValue = (EnumValue) value;
			visitor.visitEnum(key, enumValue.type.getExtendedName(), enumValue.name.qualified);
		}
		else if (valueType == IValue.ANNOTATION)
		{
			IAnnotation annotation = ((AnnotationValue) value).annotation;
			AnnotationVisitor av = visitor.visitAnnotation(key, annotation.getType().getExtendedName());
			annotation.write(av);
		}
		else if (value.isConstant())
		{
			visitor.visit(key, value.toObject());
		}
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		// TODO
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		// TODO
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('@');
		this.type.toString(prefix, buffer);
		this.arguments.toString(prefix, buffer);
	}
}
