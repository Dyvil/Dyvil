package dyvil.tools.compiler.ast.constructor;

import dyvil.annotation.internal.Nullable;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.access.InitializerCall;
import dyvil.tools.compiler.ast.expression.constant.VoidValue;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.ParameterList;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Deprecation;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CodeConstructor extends AbstractConstructor
{
	protected @Nullable IValue          value;
	protected @Nullable InitializerCall initializerCall;

	public CodeConstructor(IClass enclosingClass)
	{
		super(enclosingClass);
	}

	public CodeConstructor(IClass enclosingClass, ModifierSet modifiers)
	{
		super(enclosingClass, modifiers);
	}

	public CodeConstructor(SourcePosition position, ModifierSet modifiers, AnnotationList annotations)
	{
		super(position, modifiers, annotations);
	}

	@Override
	public InitializerCall getInitializer()
	{
		return this.initializerCall;
	}

	@Override
	public void setInitializer(InitializerCall initializer)
	{
		this.initializerCall = initializer;
	}

	@Override
	public IValue getValue()
	{
		return this.value;
	}

	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.resolveTypes(markers, context);

		this.parameters.resolveTypes(markers, context);
		if (this.parameters.isLastVariadic())
		{
			this.modifiers.addIntModifier(Modifiers.ACC_VARARGS);
		}

		if (this.initializerCall != null)
		{
			this.initializerCall.resolveTypes(markers, context);
		}

		if (this.exceptions != null)
		{
			this.exceptions.resolveTypes(markers, context);
		}

		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}

		this.resolveInitializer(markers);

		context.pop();
	}

	private void resolveInitializer(MarkerList markers)
	{
		if (this.value == null)
		{
			return;
		}
		if (this.value.valueTag() == IValue.INITIALIZER_CALL)
		{
			this.trySetInit(this.value, markers);
			this.value = null;
			return;
		}
		if (this.value.valueTag() != IValue.STATEMENT_LIST)
		{
			return;
		}

		final StatementList statementList = (StatementList) this.value;
		if (statementList.size() <= 0)
		{
			return;
		}

		final IValue firstValue = statementList.get(0);
		if (firstValue.valueTag() != IValue.INITIALIZER_CALL)
		{
			return;
		}

		// We can't simply remove the value from the Statement List, so we replace it with a void statement
		statementList.set(0, new VoidValue(firstValue.getPosition()));

		this.trySetInit(firstValue, markers);
	}

	private void trySetInit(IValue value, MarkerList markers)
	{
		if (this.initializerCall != null)
		{
			markers.add(Markers.semanticError(value.getPosition(), "initializer.call.duplicate"));
			return;
		}

		this.initializerCall = (InitializerCall) value;
		markers.add(Markers.syntaxWarning(value.getPosition(), "initializer.call.deprecated", value));
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);

		context = context.push(this);

		this.parameters.resolve(markers, context);

		if (this.exceptions != null)
		{
			this.exceptions.resolve(markers, context);
		}

		this.resolveInitCall(markers, context);

		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);

			final IValue typedValue = this.value.withType(Types.VOID, Types.VOID, markers, context);
			if (typedValue == null)
			{
				Marker marker = Markers.semanticError(this.position, "constructor.return.type");
				marker.addInfo(Markers.getSemantic("return.type", this.value.getType()));
				markers.add(marker);
			}
			else
			{
				this.value = typedValue;
			}
		}

		context.pop();
	}

	private void resolveInitCall(MarkerList markers, IContext context)
	{
		if (this.initializerCall != null)
		{
			this.initializerCall.resolve(markers, context);
			return;
		}

		// No Super Type -> don't try to resolve a Super Constructor
		final IType superType = this.enclosingClass.getSuperType();
		if (superType == null)
		{
			return;
		}

		// Implicit Super Constructor
		final IConstructor match = IContext.resolveConstructor(context, superType, ArgumentList.EMPTY);
		if (match == null)
		{
			markers.add(Markers.semanticError(this.position, "constructor.super"));
			return;
		}

		this.initializerCall = new InitializerCall(this.position, true, ArgumentList.EMPTY, superType, match);
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.checkTypes(markers, context);

		this.parameters.checkTypes(markers, context);

		if (this.exceptions != null)
		{
			this.exceptions.checkTypes(markers, context, IType.TypePosition.RETURN_TYPE);
		}

		if (this.initializerCall != null)
		{
			this.initializerCall.checkTypes(markers, context);
		}

		if (this.value != null)
		{
			this.value.checkTypes(markers, context);
		}

		context.pop();
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.check(markers, context);

		this.parameters.check(markers, context);

		if (this.exceptions != null)
		{
			for (int i = 0; i < this.exceptions.size(); i++)
			{
				final IType exceptionType = this.exceptions.get(i);
				exceptionType.check(markers, context);

				if (!Types.isSuperType(Types.THROWABLE, exceptionType))
				{
					final Marker marker = Markers.semanticError(exceptionType.getPosition(), "method.exception.type");
					marker.addInfo(Markers.getSemantic("exception.type", exceptionType));
					markers.add(marker);
				}
			}
		}

		if (this.initializerCall != null)
		{
			this.initializerCall.checkNoError(markers, context);
		}

		if (this.value != null)
		{
			this.value.check(markers, this);
		}
		else if (this.initializerCall == null)
		{
			markers.add(Markers.semanticError(this.position, "constructor.abstract"));
		}

		if (this.isStatic())
		{
			markers.add(Markers.semanticError(this.position, "constructor.static", this.name));
		}

		context.pop();
	}

	@Override
	public void foldConstants()
	{
		if (this.annotations != null)
		{
			this.annotations.foldConstants();
		}

		this.parameters.foldConstants();

		if (this.exceptions != null)
		{
			this.exceptions.foldConstants();
		}

		if (this.initializerCall != null)
		{
			this.initializerCall.foldConstants();
		}
		if (this.value != null)
		{
			this.value = this.value.foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		super.cleanup(compilableList, classCompilableList);

		this.parameters.cleanup(compilableList, classCompilableList);

		if (this.exceptions != null)
		{
			this.exceptions.cleanup(compilableList, classCompilableList);
		}

		if (this.initializerCall != null)
		{
			this.initializerCall.cleanup(compilableList, classCompilableList);
		}
		if (this.value != null)
		{
			this.value = this.value.cleanup(compilableList, classCompilableList);
		}
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		final long flags = ModifierUtil.getFlags(this);
		final MethodWriter methodWriter = new MethodWriterImpl(writer, writer.visitMethod(
			ModifierUtil.getJavaModifiers(flags), "<init>", this.getDescriptor(), this.getSignature(),
			this.getInternalExceptions()));

		// Write Modifiers and Annotations
		ModifierUtil.writeModifiers(methodWriter, this, flags);

		if (this.annotations != null)
		{
			this.annotations.write(methodWriter);
		}

		if (this.hasModifier(Modifiers.DEPRECATED) && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			methodWriter.visitAnnotation(Deprecation.DYVIL_EXTENDED, true).visitEnd();
		}

		// Write Parameters
		methodWriter.setThisType(this.enclosingClass.getInternalName());
		this.parameters.write(methodWriter);

		// Write Code
		final Label start = new Label();
		final Label end = new Label();

		methodWriter.visitCode();
		methodWriter.visitLabel(start);

		if (this.initializerCall != null)
		{
			this.initializerCall.writeExpression(methodWriter, Types.VOID);
		}

		if (this.initializerCall == null || this.initializerCall.isSuper())
		{
			this.enclosingClass.writeClassInit(methodWriter);
		}

		if (this.value != null)
		{
			this.value.writeExpression(methodWriter, Types.VOID);
		}

		methodWriter.visitLabel(end);
		methodWriter.visitEnd(Types.VOID);

		// Write Local Variable Data
		methodWriter.visitLocalVariable("this", 'L' + this.enclosingClass.getInternalName() + ';', null, start, end, 0);

		this.parameters.writeLocals(methodWriter, start, end);
	}

	@Override
	public void writeSignature(DataOutput out) throws IOException
	{
		this.parameters.writeSignature(out);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		this.writeAnnotations(out);

		this.parameters.write(out);
	}

	@Override
	public void readSignature(DataInput in) throws IOException
	{
		this.parameters.readSignature(in);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.readAnnotations(in);
		this.parameters = ParameterList.read(in);
	}
}
