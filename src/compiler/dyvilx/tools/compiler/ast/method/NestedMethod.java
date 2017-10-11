package dyvilx.tools.compiler.ast.method;

import dyvil.reflect.Modifiers;
import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.CaptureVariable;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.modifiers.ModifierSet;
import dyvilx.tools.compiler.ast.modifiers.ModifierUtil;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.MethodWriterImpl;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.CaptureHelper;
import dyvil.lang.Name;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class NestedMethod extends CodeMethod
{
	private CaptureHelper captureHelper = new CaptureHelper(CaptureVariable.FACTORY);

	public NestedMethod(SourcePosition position, Name name, IType type, ModifierSet modifiers, AttributeList annotations)
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
		if (this.typeParameters != null)
		{
			this.typeParameters.appendParameterDescriptors(buffer);
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

		this.parameters.write(methodWriter);

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
	protected void writeArguments(MethodWriter writer, IValue receiver, ArgumentList arguments) throws BytecodeException
	{
		super.writeArguments(writer, receiver, arguments);
		this.captureHelper.writeCaptures(writer);
	}
}
