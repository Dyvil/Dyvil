package dyvil.tools.compiler.ast.type;

import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.constructor.ConstructorMatchList;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class TypeDelegate implements IType, ITyped
{
	protected IType type;

	protected abstract IType wrap(IType type);

	@Override
	public ICodePosition getPosition()
	{
		return this.type.getPosition();
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
	public ITypeParameter getTypeVariable()
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
		final IType objectType = this.type.getObjectType();
		return objectType != this.type ? this.wrap(objectType) : this;
	}

	@Override
	public String getTypePrefix()
	{
		return this.type.getTypePrefix();
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
	public boolean isExtension()
	{
		return this.type.isExtension();
	}

	@Override
	public void setExtension(boolean extension)
	{
		this.type.setExtension(extension);
	}

	@Override
	public Mutability getMutability()
	{
		final Mutability mutability = IType.super.getMutability();
		if (mutability != Mutability.UNDEFINED)
		{
			return mutability;
		}
		return this.type.getMutability();
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
	public IType resolveType(ITypeParameter typeParameter)
	{
		return this.type.resolveType(typeParameter);
	}

	@Override
	public boolean hasTypeVariables()
	{
		return this.type.hasTypeVariables();
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		final IType concreteType = this.type.getConcreteType(context);
		return concreteType != this.type ? this.wrap(concreteType) : this;
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
	public IAnnotation getAnnotation(IClass type)
	{
		return this.type.getAnnotation(type);
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
	public int getLocalSlots()
	{
		return this.type.getLocalSlots();
	}

	@Override
	public void writeCast(MethodWriter writer, IType target, int lineNumber) throws BytecodeException
	{
		this.type.writeCast(writer, target, lineNumber);
	}

	@Override
	public void writeClassExpression(MethodWriter writer) throws BytecodeException
	{
		this.type.writeClassExpression(writer);
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
		this.type.writeAnnotations(visitor, typeRef, typePath);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		IType.writeType(this.type, out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.type = IType.readType(in);
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public IType clone()
	{
		return this.wrap(this.type.clone());
	}
}
