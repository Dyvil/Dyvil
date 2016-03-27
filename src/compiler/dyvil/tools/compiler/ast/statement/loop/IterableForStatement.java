package dyvil.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.position.ICodePosition;

public class IterableForStatement extends ForEachStatement
{
	public static final class LazyFields
	{
		public static       IClass         ITERABLE_CLASS = Package.javaLang.resolveClass("Iterable");
		public static final IType          ITERABLE       = ITERABLE_CLASS.getClassType();
		public static final ITypeParameter ITERABLE_TYPE  = ITERABLE_CLASS.getTypeParameter(0);

		public static final IClass         ITERATOR_CLASS = Package.javaUtil.resolveClass("Iterator");
		public static final IType          ITERATOR       = ITERATOR_CLASS.getClassType();
		public static final ITypeParameter ITERATOR_TYPE  = ITERATOR_CLASS.getTypeParameter(0);
	}

	protected IMethod boxMethod;
	protected boolean iterator;

	public IterableForStatement(ICodePosition position, IVariable variable, boolean iterator)
	{
		super(position, variable);
		this.iterator = iterator;
	}

	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		dyvil.tools.asm.Label startLabel = this.startLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label updateLabel = this.updateLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = this.endLabel.target = new dyvil.tools.asm.Label();

		final IVariable var = this.variable;
		final IType varType = var.getType();
		final int lineNumber = this.getLineNumber();

		// Scope
		dyvil.tools.asm.Label scopeLabel = new dyvil.tools.asm.Label();
		writer.visitLabel(scopeLabel);
		int localCount = writer.localCount();

		// Get the iterator
		var.getValue().writeExpression(writer, null);

		if (!this.iterator)
		{
			writer.visitLineNumber(lineNumber);
			writer.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/lang/Iterable", "iterator", "()Ljava/util/Iterator;",
			                       true);
		}

		// Local Variables
		int iteratorVarIndex = writer.localCount();
		var.setLocalIndex(iteratorVarIndex + 1);

		// Store Iterator
		writer.visitVarInsn(Opcodes.ASTORE, iteratorVarIndex);

		// Jump to hasNext check
		writer.visitJumpInsn(Opcodes.GOTO, updateLabel);
		writer.visitTargetLabel(startLabel);

		// Invoke Iterator.next()
		writer.visitVarInsn(Opcodes.ALOAD, iteratorVarIndex);
		writer.visitLineNumber(lineNumber);
		writer.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
		// Auocasting
		Types.OBJECT.writeCast(writer, varType, lineNumber);

		// Store the next element
		writer.visitVarInsn(varType.getStoreOpcode(), iteratorVarIndex + 1);

		// Action
		if (this.action != null)
		{
			this.action.writeExpression(writer, Types.VOID);
		}

		writer.visitLabel(updateLabel);
		// Load Iterator
		writer.visitVarInsn(Opcodes.ALOAD, iteratorVarIndex);
		// Check hasNext
		writer.visitLineNumber(lineNumber);
		writer.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
		// Go back to start if Iterator.hasNext() returned true
		writer.visitJumpInsn(Opcodes.IFNE, startLabel);

		// Local Variables
		writer.resetLocals(localCount);
		writer.visitLabel(endLabel);

		var.writeLocal(writer, scopeLabel, endLabel);
	}
}
