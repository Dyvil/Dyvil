package dyvil.tools.compiler.ast.constructor;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.access.InitializerCall;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.VoidValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.modifiers.ModifierUtil;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
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
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CodeConstructor extends AbstractConstructor
{
	protected IValue          value;
	protected InitializerCall initializerCall;

	public CodeConstructor(IClass enclosingClass)
	{
		super(enclosingClass);
	}

	public CodeConstructor(IClass enclosingClass, ModifierSet modifiers)
	{
		super(enclosingClass, modifiers);
	}

	public CodeConstructor(ICodePosition position, ModifierSet modifiers, AnnotationList annotations)
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

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i] = this.exceptions[i].resolveType(markers, context);
		}

		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}

		context.pop();
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);

		context = context.push(this);

		this.parameters.resolve(markers, context);

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].resolve(markers, context);
		}

		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);

			final IValue typedValue = this.value.withType(Types.VOID, Types.VOID, markers, context);
			if (typedValue == null)
			{
				Marker marker = Markers.semantic(this.position, "constructor.return.type");
				marker.addInfo(Markers.getSemantic("return.type", this.value.getType()));
				markers.add(marker);
			}
			else
			{
				this.value = typedValue;
			}
		}

		this.resolveSuperConstructors(markers, context);

		context.pop();
	}

	private void resolveSuperConstructors(MarkerList markers, IContext context)
	{
		if (this.value.valueTag() == IValue.INITIALIZER_CALL)
		{
			this.initializerCall = (InitializerCall) this.value;
			this.value = null;
			return;
		}
		if (this.value.valueTag() == IValue.STATEMENT_LIST)
		{
			final StatementList statementList = (StatementList) this.value;
			if (statementList.valueCount() > 0)
			{
				final IValue firstValue = statementList.getValue(0);
				if (firstValue.valueTag() == IValue.INITIALIZER_CALL)
				{
					// We can't simply remove the value from the Statement List, so we replace it with a void statement
					statementList.setValue(0, new VoidValue(firstValue.getPosition()));

					this.initializerCall = (InitializerCall) firstValue;
					return;
				}
			}
		}

		// No Super Type -> don't try to resolve a Super Constructor
		final IType superType = this.enclosingClass.getSuperType();
		if (superType == null)
		{
			return;
		}

		// Implicit Super Constructor
		final IConstructor match = IContext.resolveConstructor(context, superType, EmptyArguments.INSTANCE);
		if (match == null)
		{
			markers.add(Markers.semantic(this.position, "constructor.super"));
			return;
		}

		this.initializerCall = new InitializerCall(this.position, true, EmptyArguments.INSTANCE, superType, match);
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		context = context.push(this);

		super.checkTypes(markers, context);

		this.parameters.checkTypes(markers, context);

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].checkType(markers, context, IType.TypePosition.RETURN_TYPE);
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

		for (int i = 0; i < this.exceptionCount; i++)
		{
			final IType exceptionType = this.exceptions[i];
			exceptionType.check(markers, context);

			if (!Types.isSuperType(Types.THROWABLE, exceptionType))
			{
				final Marker marker = Markers.semantic(exceptionType.getPosition(), "method.exception.type");
				marker.addInfo(Markers.getSemantic("exception.type", exceptionType));
				markers.add(marker);
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
		else if (this.initializerCall != null)
		{
			markers.add(Markers.semanticError(this.position, "constructor.abstract"));
		}

		if (this.isStatic())
		{
			markers.add(Markers.semantic(this.position, "constructor.static", this.name));
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

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].foldConstants();
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

		for (int i = 0; i < this.exceptionCount; i++)
		{
			this.exceptions[i].cleanup(compilableList, classCompilableList);
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
		final int modifiers = this.modifiers.toFlags() & ModifierUtil.JAVA_MODIFIER_MASK;
		final MethodWriter methodWriter = new MethodWriterImpl(writer, writer.visitMethod(modifiers, "<init>",
		                                                                                  this.getDescriptor(),
		                                                                                  this.getSignature(),
		                                                                                  this.getExceptions()));

		// Write Modifiers and Annotations
		ModifierUtil.writeModifiers(methodWriter, this.modifiers);

		if (this.annotations != null)
		{
			this.annotations.write(methodWriter);
		}

		if ((modifiers & Modifiers.DEPRECATED) != 0 && this.getAnnotation(Deprecation.DEPRECATED_CLASS) == null)
		{
			methodWriter.visitAnnotation(Deprecation.DYVIL_EXTENDED, true).visitEnd();
		}

		// Write Parameters
		methodWriter.setThisType(this.enclosingClass.getInternalName());
		this.parameters.writeInit(methodWriter);

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
