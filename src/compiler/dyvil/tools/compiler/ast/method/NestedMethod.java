package dyvil.tools.compiler.ast.method;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.CaptureVariable;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.CaptureHelper;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class NestedMethod extends CodeMethod
{
	private CaptureHelper captureHelper = new CaptureHelper(CaptureVariable.FACTORY);

	public NestedMethod(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		super(position, name, type, modifiers, annotations);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (context.isStatic())
		{
			this.modifiers.addIntModifier(Modifiers.STATIC);
		}

		super.resolveTypes(markers, context);
	}

	@Override
	public IDataMember capture(IVariable variable)
	{
		if (this.isMember(variable))
		{
			return variable;
		}

		return this.captureHelper.capture(variable);
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		super.cleanup(context, compilableList);

		if (!this.captureHelper.isThisCaptured())
		{
			this.modifiers.addIntModifier(Modifiers.STATIC);
		}
	}

	@Override
	public String getDescriptor()
	{
		if (this.descriptor != null)
		{
			return this.descriptor;
		}

		final StringBuilder buffer = new StringBuilder();
		buffer.append('(');

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].getInternalType().appendExtendedName(buffer);
		}
		for (int i = 0; i < this.typeParameterCount; i++)
		{
			this.typeParameters[i].appendParameterDescriptor(buffer);
		}

		this.captureHelper.appendCaptureTypes(buffer);

		buffer.append(')');
		this.type.appendExtendedName(buffer);

		return this.descriptor = buffer.toString();
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		final int modifiers = this.modifiers.toFlags() & ModifierUtil.JAVA_MODIFIER_MASK;

		final MethodWriter methodWriter = new MethodWriterImpl(writer, writer
			                                                               .visitMethod(modifiers, this.name.qualified,
			                                                                            this.getDescriptor(),
			                                                                            this.getSignature(),
			                                                                            this.getInternalExceptions()));

		this.writeAnnotations(methodWriter, modifiers);

		if (this.captureHelper.isThisCaptured())
		{
			methodWriter.setThisType(this.enclosingClass.getInternalName());
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].writeInit(methodWriter);
		}

		this.captureHelper.writeCaptureParameters(methodWriter, methodWriter.localCount());

		Label start = new Label();
		Label end = new Label();

		if (this.value != null)
		{
			methodWriter.visitCode();
			methodWriter.visitLabel(start);
			this.value.writeExpression(methodWriter, this.type);
			methodWriter.visitLabel(end);
			methodWriter.visitEnd(this.type);
		}

		for (int i = 0; i < this.parameterCount; i++)
		{
			this.parameters[i].writeLocal(methodWriter, start, end);
		}
	}

	@Override
	protected void writeArguments(MethodWriter writer, IValue instance, IArguments arguments) throws BytecodeException
	{
		super.writeArguments(writer, instance, arguments);
		this.captureHelper.writeCaptures(writer);
	}
}
