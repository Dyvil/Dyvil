package dyvilx.tools.compiler.ast.type.raw;

import dyvil.lang.Name;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Type;
import dyvilx.tools.compiler.ast.attribute.annotation.Annotation;
import dyvilx.tools.compiler.ast.attribute.annotation.AnnotationUtil;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.method.MatchList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.PrimitiveType;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ClassType implements IRawType
{
	public IClass theClass;

	public ClassType()
	{
		super();
	}

	public ClassType(IClass iclass)
	{
		this.theClass = iclass;
	}

	@Override
	public int typeTag()
	{
		return CLASS;
	}

	@Override
	public IType atPosition(SourcePosition position)
	{
		return new ResolvedClassType(this.theClass, position);
	}

	// Names

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

	// Super Type

	@Override
	public boolean isSameType(IType type)
	{
		return this.theClass == type.getTheClass() && this.isPrimitive() == type.isPrimitive();
	}

	@Override
	public boolean isSameClass(IType type)
	{
		return this.theClass == type.getTheClass() && !type.isPrimitive();
	}

	// Resolve

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public IType withAnnotation(Annotation annotation)
	{
		if (AnnotationUtil.PRIMITIVE_INTERNAL.equals(annotation.getTypeDescriptor()))
		{
			return PrimitiveType.getPrimitiveType(this);
		}
		return null;
	}

	// IContext

	@Override
	public IDataMember resolveField(Name name)
	{
		return this.theClass.resolveField(name);
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		this.theClass.getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.theClass.getImplicitMatches(list, value, targetType);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
	{
		if (this.theClass != null)
		{
			this.theClass.getConstructorMatches(list, arguments);
		}
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		return this.theClass.getFunctionalMethod();
	}

	// Compilation

	@Override
	public String getInternalName()
	{
		return this.theClass.getInternalName();
	}

	@Override
	public void appendDescriptor(StringBuilder buffer, int type)
	{
		buffer.append('L').append(this.theClass.getInternalName()).append(';');
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.visitLdcInsn(Type.getObjectType(this.theClass.getInternalName()));
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/types/Type", "apply",
		                       "(Ljava/lang/Class;)Ldyvil/reflect/types/Type;", true);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		out.writeUTF(this.theClass.getInternalName());
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		String internal = in.readUTF();
		this.theClass = Package.rootPackage.resolveGlobalExternalClass(internal);
	}

	// Misc

	@Override
	public String toString()
	{
		return this.theClass.getFullName();
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.theClass.getName());
	}
}
