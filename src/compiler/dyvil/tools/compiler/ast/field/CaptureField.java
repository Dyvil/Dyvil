package dyvil.tools.compiler.ast.field;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.ThisExpr;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.function.Function;

public final class CaptureField extends CaptureDataMember implements IField
{
	public IClass enclosingClass;
	public String internalName;

	public static Function<? super IVariable, ? extends CaptureDataMember> factory(IClass theClass)
	{
		return variable -> new CaptureField(theClass, variable);
	}

	public CaptureField(IClass iclass)
	{
		this.enclosingClass = iclass;
	}

	public CaptureField(IClass iclass, IVariable variable)
	{
		super(variable);
		this.enclosingClass = iclass;

		this.internalName = "this$" + variable.getInternalName();
	}

	@Override
	public void setEnclosingClass(IClass enclosingClass)
	{
		this.enclosingClass = enclosingClass;
	}

	@Override
	public IClass getEnclosingClass()
	{
		return this.enclosingClass;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.FIELD;
	}

	@Override
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue receiver, IContext context)
	{
		super.checkAccess(markers, position, receiver, context);

		if (receiver == null)
		{
			return new ThisExpr(this.getPosition(), this.enclosingClass.getType(), context, markers);
		}
		return receiver;
	}

	@Override
	public String getInternalName()
	{
		return this.internalName;
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		writer
			.visitField(Modifiers.MANDATED | Modifiers.SYNTHETIC, this.internalName, this.getDescriptor(), this.getSignature(),
			            null).visitEnd();
	}

	@Override
	public void writeGet_Get(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		// { int i = 0; new => int() { override int apply() = i++ } }

		String owner = this.enclosingClass.getInternalName();
		String name = this.internalName;
		String desc = this.getDescriptor();
		writer.visitFieldInsn(Opcodes.GETFIELD, owner, name, desc);
	}

	@Override
	public void writeSet_Set(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (!this.variable.isReferenceType())
		{
			String owner = this.enclosingClass.getInternalName();
			String name = this.internalName;
			String desc = this.variable.getInternalType().getExtendedName();
			writer.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
		}
	}

	@Override
	public void writeSignature(DataOutput out) throws IOException
	{
	}

	@Override
	public void readSignature(DataInput in) throws IOException
	{
	}
}
