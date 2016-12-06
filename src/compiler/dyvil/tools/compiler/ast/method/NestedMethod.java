package dyvil.tools.compiler.ast.method;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.CaptureVariable;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
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
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		super.cleanup(compilableList, classCompilableList);

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

		this.parameters.appendDescriptor(buffer);
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
			                                                               .visitMethod(modifiers, this.getInternalName(),
			                                                                            this.getDescriptor(),
			                                                                            this.getSignature(),
			                                                                            this.getInternalExceptions()));

		this.writeAnnotations(methodWriter, modifiers);

		if (this.captureHelper.isThisCaptured())
		{
			methodWriter.setThisType(this.enclosingClass.getInternalName());
		}

		this.parameters.writeInit(methodWriter);

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

		this.parameters.writeLocals(methodWriter, start, end);
	}

	@Override
	protected void writeArguments(MethodWriter writer, IValue receiver, IArguments arguments) throws BytecodeException
	{
		super.writeArguments(writer, receiver, arguments);
		this.captureHelper.writeCaptures(writer);
	}
}
