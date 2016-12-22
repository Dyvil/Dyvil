package dyvil.tools.compiler.ast.reference;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.generic.ClassGenericType;
import dyvil.tools.compiler.ast.type.raw.IObjectType;
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
	public static final class LazyFields
	{
		public static IClass OBJECT_SIMPLE_REF_CLASS = Package.dyvilRefSimple.resolveClass("SimpleObjectRef");
		public static IClass OBJECT_REF_CLASS        = Package.dyvilRef.resolveClass("ObjectRef");

		public static IType getObjectSimpleRef(IType type)
		{
			ClassGenericType gt = new ClassGenericType(OBJECT_SIMPLE_REF_CLASS);
			gt.addType(type);
			return gt;
		}

		public static String getInternalRef(IType type, String prefix)
		{
			return "dyvil/ref/" + prefix + type.getTypePrefix() + "Ref";
		}

		public static String getReferenceFactoryName(IType type, String prefix)
		{
			return "new" + prefix + type.getTypePrefix() + "Ref";
		}
	}

	protected IClass theClass;
	protected IType  type;

	public ReferenceType()
	{
	}

	public ReferenceType(IType type)
	{
		this.type = type;
	}

	public ReferenceType(IClass iclass, IType type)
	{
		this.theClass = iclass;
		this.type = type;
	}

	public IType getType()
	{
		return this.type;
	}

	public void setType(IType type)
	{
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
	public boolean isSuperTypeOf(IType subType)
	{
		return IObjectType.super.isSuperTypeOf(subType) && this.isSameBaseType(subType);
	}

	private boolean isSameBaseType(IType type)
	{
		if (this.theClass == LazyFields.OBJECT_REF_CLASS)
		{
			final IType otherType = type.resolveType(this.theClass.getTypeParameter(0));
			return otherType == null || Types.isSameType(this.type, otherType);
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
	public IType asParameterType()
	{
		final IType type = this.type.asParameterType();
		return type == this.type ? this : type.getRefType();
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		if (typeParameter.getGeneric() == this.theClass)
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
		if (!this.theClass.isTypeParametric())
		{
			return;
		}

		final ITypeParameter typeVariable = this.theClass.getTypeParameter(0);
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
	public void checkType(MarkerList markers, IContext context, int position)
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
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.type.cleanup(compilableList, classCompilableList);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		this.theClass.getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.theClass.getImplicitMatches(list, value, targetType);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, IArguments arguments)
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
	public void appendDescriptor(StringBuilder buffer, int type)
	{
		if (type == NAME_FULL)
		{
			buffer.append('R');
			this.type.appendDescriptor(buffer, NAME_FULL);
			return;
		}

		buffer.append('L').append(this.getInternalName());
		if (type != NAME_DESCRIPTOR && this.theClass == LazyFields.OBJECT_REF_CLASS)
		{
			buffer.append('<');
			this.type.appendDescriptor(buffer, type);
			buffer.append('>');
		}
		buffer.append(';');
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		this.type.writeTypeExpression(writer);
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvilx/lang/model/type/ReferenceType", "apply",
		                       "(Ldyvilx/lang/model/type/Type;)Ldyvilx/lang/model/type/ReferenceType;", false);
	}

	@Override
	public IType withAnnotation(IAnnotation annotation)
	{
		if (AnnotationUtil.IMPLICITLY_UNWRAPPED_INTERNAL.equals(annotation.getType().getInternalName()))
		{
			return new ImplicitReferenceType(this.theClass, this.type);
		}
		return null;
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
		IType.writeAnnotations(this.type, visitor, typeRef, typePath + "0;");
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
		if (this.theClass == LazyFields.OBJECT_REF_CLASS)
		{
			writer.visitMethodInsn(Opcodes.INVOKEINTERFACE, internal, "get", "()Ljava/lang/Object;", true);

			if (this.type.getTheClass() != Types.OBJECT_CLASS)
			{
				writer.visitTypeInsn(Opcodes.CHECKCAST, this.type.getInternalName());
			}
			return;
		}

		final StringBuilder stringBuilder = new StringBuilder("()");
		this.type.appendExtendedName(stringBuilder);
		writer.visitMethodInsn(Opcodes.INVOKEINTERFACE, internal, "get", stringBuilder.toString(), true);
	}

	public void writeWrap(MethodWriter writer) throws BytecodeException
	{
		final String internal = this.theClass.getInternalName();
		if (this.theClass == LazyFields.OBJECT_REF_CLASS)
		{
			writer.visitMethodInsn(Opcodes.INVOKEINTERFACE, internal, "set", "(Ljava/lang/Object;)V", true);
			return;
		}

		final StringBuilder stringBuilder = new StringBuilder().append('(');
		this.type.appendExtendedName(stringBuilder);
		stringBuilder.append(")V");
		writer.visitMethodInsn(Opcodes.INVOKEINTERFACE, internal, "set", stringBuilder.toString(), true);
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
}
