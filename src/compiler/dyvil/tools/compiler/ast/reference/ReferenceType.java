package dyvil.tools.compiler.ast.reference;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
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
import dyvil.tools.compiler.ast.type.IObjectType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ReferenceType implements IObjectType
{
	protected IClass theClass;
	protected IType  type;

	public ReferenceType(IType type)
	{
		this.type = type;
	}

	public ReferenceType(IClass iclass, IType type)
	{
		this.theClass = iclass;
		this.type = type;
	}

	@Override
	public int typeTag()
	{
		return REFERENCE;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.type.getPosition();
	}

	@Override
	public boolean isGenericType()
	{
		return true;
	}

	@Override
	public IType getElementType()
	{
		return this.type;
	}

	@Override
	public int getSuperTypeDistance(IType superType)
	{
		if (superType.getTheClass() == Types.OBJECT_CLASS)
		{
			return 2;
		}
		return this.isSameType(superType) ? 1 : 0;
	}

	@Override
	public boolean isSuperTypeOf(IType type)
	{
		return IObjectType.super.isSuperTypeOf(type) && this.isSameBaseType(type);
	}

	private boolean isSameBaseType(IType type)
	{
		if (this.theClass == Types.getObjectRefClass())
		{
			final IType otherType = type.resolveType(this.theClass.getTypeVariable(0));
			return otherType == null || this.type.isSameType(otherType);
		}
		return true;
	}

	@Override
	public boolean isSameType(IType type)
	{
		return this.theClass == type.getTheClass() && this.isSameBaseType(type);
	}

	@Override
	public Name getName()
	{
		return this.theClass.getName();
	}

	@Override
	public IClass getTheClass()
	{
		return this.theClass;
	}

	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		if (typeVar.getGeneric() == this.theClass)
		{
			return this.type;
		}
		return null;
	}

	@Override
	public boolean hasTypeVariables()
	{
		return this.type.hasTypeVariables();
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		IType concreteType = this.type.getConcreteType(context);
		if (!this.type.isPrimitive() && concreteType.isPrimitive())
		{
			concreteType = concreteType.getObjectType();
		}
		if (concreteType != null && concreteType != this.type)
		{
			return concreteType.getRefType();
		}
		return this;
	}

	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		if (!this.theClass.isGeneric())
		{
			return;
		}

		final ITypeVariable typeVariable = this.theClass.getTypeVariable(0);
		if (typeVariable != null)
		{
			final IType concreteRefType = concrete.resolveType(typeVariable);
			if (concreteRefType != null)
			{
				this.type.inferTypes(concreteRefType, typeContext);
			}
		}
	}

	@Override
	public boolean isResolved()
	{
		return this.theClass != null;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
		this.theClass = this.type.getRefClass();
		return this;
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
	}

	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
		this.type.checkType(markers, context, position);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
	}

	@Override
	public void foldConstants()
	{
		this.type.foldConstants();
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.type.cleanup(context, compilableList);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}

	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		this.theClass.getMethodMatches(list, instance, name, arguments);
	}

	@Override
	public void getConstructorMatches(ConstructorMatchList list, IArguments arguments)
	{
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}

	@Override
	public String getInternalName()
	{
		return this.theClass.getInternalName();
	}

	@Override
	public String getSignature()
	{
		if (this.theClass != Types.getObjectRefClass())
		{
			return null;
		}

		StringBuilder stringBuilder = new StringBuilder();
		this.appendSignature(stringBuilder);
		return stringBuilder.toString();
	}

	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append('L').append(this.getInternalName());
		if (this.theClass == Types.getObjectRefClass())
		{
			buffer.append('<');
			this.type.appendSignature(buffer);
			buffer.append('>');
		}
		buffer.append(';');
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		// TODO
	}

	@Override
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
		if (typePath.getStep(step) != TypePath.TYPE_ARGUMENT)
		{
			return;
		}

		if (typePath.getStepArgument(step) == 0)
		{
			this.type = IType.withAnnotation(this.type, annotation, typePath, step + 1, steps);
		}
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		this.type.writeAnnotations(visitor, typeRef, typePath + "0;");
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
	
	public void writeUnwrap(MethodWriter writer) throws BytecodeException
	{
		final String internal = this.theClass.getInternalName();
		if (this.theClass == Types.getObjectRefClass())
		{
			writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, internal, "get", "()Ljava/lang/Object;", true);
			
			if (this.type.getTheClass() != Types.OBJECT_CLASS)
			{
				writer.writeTypeInsn(Opcodes.CHECKCAST, this.type.getInternalName());
			}
			return;
		}
		
		final StringBuilder stringBuilder = new StringBuilder("()");
		this.type.appendExtendedName(stringBuilder);
		writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, internal, "get", stringBuilder.toString(), true);
	}
	
	public void writeWrap(MethodWriter writer) throws BytecodeException
	{
		final String internal = this.theClass.getInternalName();
		if (this.theClass == Types.getObjectRefClass())
		{
			writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, internal, "set", "(Ljava/lang/Object;)V", true);
			return;
		}
		
		final StringBuilder stringBuilder = new StringBuilder().append('(');
		this.type.appendExtendedName(stringBuilder);
		stringBuilder.append(")V");
		writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, internal, "set", stringBuilder.toString(), true);
	}

	@Override
	public String toString()
	{
		return this.type + "*";
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
		buffer.append('*');
	}

	@Override
	public IType clone()
	{
		return new ReferenceType(this.theClass, this.type.clone());
	}
}
