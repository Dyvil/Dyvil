package dyvil.tools.compiler.ast.type;

import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.method.ConstructorMatchList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;

public class AnnotatedType implements IType, ITyped
{
	private IType       type;
	private IAnnotation annotation;
	
	public AnnotatedType(IAnnotation annotation)
	{
		this.annotation = annotation;
	}
	
	public AnnotatedType(IType type, IAnnotation annotation)
	{
		this.type = type;
		this.annotation = annotation;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.type.getPosition();
	}
	
	@Override
	public int typeTag()
	{
		return ANNOTATED;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.type.isPrimitive();
	}
	
	@Override
	public int getTypecode()
	{
		return this.type.getTypecode();
	}
	
	@Override
	public boolean isGenericType()
	{
		return this.type.isGenericType();
	}
	
	@Override
	public ITypeVariable getTypeVariable()
	{
		return this.type.getTypeVariable();
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
	public Name getName()
	{
		return this.type.getName();
	}
	
	@Override
	public IClass getTheClass()
	{
		return this.type.getTheClass();
	}
	
	@Override
	public IType getObjectType()
	{
		if (this.type.isPrimitive())
		{
			return new AnnotatedType(this.type.getObjectType(), this.annotation);
		}
		return this;
	}

	@Override
	public IClass getRefClass()
	{
		return this.type.getRefClass();
	}

	@Override
	public IType getSimpleRefType()
	{
		return this.type.getSimpleRefType();
	}
	
	@Override
	public boolean isArrayType()
	{
		return this.type.isArrayType();
	}
	
	@Override
	public int getArrayDimensions()
	{
		return this.type.getArrayDimensions();
	}
	
	@Override
	public IType getElementType()
	{
		return this.type.getElementType();
	}
	
	@Override
	public IClass getArrayClass()
	{
		return this.type.getArrayClass();
	}
	
	@Override
	public IMethod getBoxMethod()
	{
		return this.type.getBoxMethod();
	}
	
	@Override
	public IMethod getUnboxMethod()
	{
		return this.type.getUnboxMethod();
	}
	
	@Override
	public boolean isSameType(IType type)
	{
		return this.type.isSameType(type);
	}
	
	@Override
	public boolean classEquals(IType type)
	{
		return this.type.classEquals(type);
	}
	
	@Override
	public IType getSuperType()
	{
		return this.type.getSuperType();
	}
	
	@Override
	public boolean isSuperTypeOf(IType type)
	{
		return this.type.isSuperTypeOf(type);
	}
	
	@Override
	public boolean isSuperClassOf(IType type)
	{
		return this.type.isSuperClassOf(type);
	}
	
	@Override
	public int getSubClassDistance(IType subtype)
	{
		return this.type.getSubClassDistance(subtype);
	}
	
	@Override
	public float getSubTypeDistance(IType subtype)
	{
		return this.type.getSubTypeDistance(subtype);
	}
	
	@Override
	public IType combine(IType type)
	{
		return this.type.combine(type);
	}
	
	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		return this.type.resolveType(typeVar);
	}
	
	@Override
	public boolean hasTypeVariables()
	{
		return this.type.hasTypeVariables();
	}
	
	@Override
	public IType getConcreteType(ITypeContext context)
	{
		IType t = this.type.getConcreteType(context);
		return t != this.type ? new AnnotatedType(this.type, this.annotation) : this;
	}
	
	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		this.type.inferTypes(concrete, typeContext);
	}
	
	@Override
	public boolean isResolved()
	{
		return this.type.isResolved();
	}
	
	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
		this.annotation.resolveTypes(markers, context);
		return this;
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
		this.annotation.resolve(markers, context);
	}
	
	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
		this.type.checkType(markers, context, position);
		this.annotation.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
		this.annotation.check(markers, context, ElementType.TYPE_USE);
	}
	
	@Override
	public void foldConstants()
	{
		this.type.foldConstants();
		this.annotation.foldConstants();
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.type.cleanup(context, compilableList);
		this.annotation.cleanup(context, compilableList);
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		return this.type.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		this.type.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
	{
		this.type.getConstructorMatches(list, arguments);
	}
	
	@Override
	public IMethod getFunctionalMethod()
	{
		return this.type.getFunctionalMethod();
	}
	
	@Override
	public String getInternalName()
	{
		return this.type.getInternalName();
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		this.type.appendExtendedName(buffer);
	}
	
	@Override
	public String getSignature()
	{
		return this.type.getSignature();
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		this.type.appendSignature(buffer);
	}
	
	@Override
	public int getLoadOpcode()
	{
		return this.type.getLoadOpcode();
	}
	
	@Override
	public int getArrayLoadOpcode()
	{
		return this.type.getArrayLoadOpcode();
	}
	
	@Override
	public int getStoreOpcode()
	{
		return this.type.getStoreOpcode();
	}
	
	@Override
	public int getArrayStoreOpcode()
	{
		return this.type.getArrayStoreOpcode();
	}
	
	@Override
	public int getReturnOpcode()
	{
		return this.type.getReturnOpcode();
	}
	
	@Override
	public Object getFrameType()
	{
		return this.type.getFrameType();
	}
	
	@Override
	public void writeCast(MethodWriter writer, IType target, int lineNumber) throws BytecodeException
	{
		this.type.writeCast(writer, target, lineNumber);
	}
	
	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		this.type.writeTypeExpression(writer);
	}
	
	@Override
	public void writeDefaultValue(MethodWriter writer) throws BytecodeException
	{
		this.type.writeDefaultValue(writer);
	}
	
	@Override
	public IConstantValue getDefaultValue()
	{
		return this.type.getDefaultValue();
	}
	
	@Override
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
		this.type = IType.withAnnotation(this.type, annotation, typePath, step, steps);
	}
	
	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		TypePath path = TypePath.fromString(typePath);
		IType type = this.type;
		
		this.annotation.write(visitor, typeRef, path);
		
		// Ensure that we don't create the TypePath object multiple times by
		// checking for multiple annotations on the same type
		while (type.typeTag() == ANNOTATED)
		{
			AnnotatedType t = (AnnotatedType) type;
			t.annotation.write(visitor, typeRef, path);
			type = t.type;
		}
		
		type.writeAnnotations(visitor, typeRef, typePath);
	}
	
	@Override
	public void write(DataOutput out) throws IOException
	{
		IType.writeType(this.type, out);
		this.annotation.write(out);
	}
	
	@Override
	public void read(DataInput in) throws IOException
	{
		this.type = IType.readType(in);
		this.annotation.read(in);
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.annotation.toString(prefix, buffer);
		buffer.append(' ');
		this.type.toString(prefix, buffer);
	}
	
	@Override
	public IType clone()
	{
		return new AnnotatedType(this.type.clone(), this.annotation);
	}
}
