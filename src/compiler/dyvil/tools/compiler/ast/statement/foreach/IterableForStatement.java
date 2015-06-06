package dyvil.tools.compiler.ast.statement.foreach;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public class IterableForStatement extends ForEachStatement
{
	public static final ITypeVariable	ITERABLE_TYPE	= Types.ITERABLE.getTheClass().getTypeVariable(0);
	
	public static final Name			$iterator		= Name.getQualified("$iterator");
	
	protected Variable					iteratorVar;
	
	public IterableForStatement(Variable variable, IValue action)
	{
		this(variable, action, variable.value.getType());
	}
	
	public IterableForStatement(Variable variable, IValue action, IType valueType)
	{
		super(variable, action);
		
		Variable var = new Variable();
		var.type = valueType;
		var.name = $iterator;
		this.iteratorVar = var;
	}
	
	@Override
	public IField resolveField(Name name)
	{
		if (name == this.variable.name)
		{
			return this.variable;
		}
		
		if (name == $iterator)
		{
			return this.iteratorVar;
		}
		
		return this.context.resolveField(name);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		org.objectweb.asm.Label startLabel = this.startLabel.target = new org.objectweb.asm.Label();
		org.objectweb.asm.Label updateLabel = this.updateLabel.target = new org.objectweb.asm.Label();
		org.objectweb.asm.Label endLabel = this.endLabel.target = new org.objectweb.asm.Label();
		
		Variable var = this.variable;
		Variable iteratorVar = this.iteratorVar;
		
		org.objectweb.asm.Label scopeLabel = new org.objectweb.asm.Label();
		writer.writeLabel(scopeLabel);
		
		// Get the iterator
		var.value.writeExpression(writer);
		writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, "java/lang/Iterable", "iterator", "()Ljava/util/Iterator;", true);
		
		// Local Variables
		int locals = writer.localCount();
		var.index = locals + 1;
		
		// Store Iterator
		writer.writeVarInsn(Opcodes.ASTORE, iteratorVar.index = locals);
		
		// Jump to hasNext check
		writer.writeJumpInsn(Opcodes.GOTO, updateLabel);
		writer.writeTargetLabel(startLabel);
		
		// Invoke Iterator.next()
		writer.writeVarInsn(Opcodes.ALOAD, iteratorVar.index);
		writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
		// Cast to the variable type
		if (!var.type.equals(Types.OBJECT))
		{
			writer.writeTypeInsn(Opcodes.CHECKCAST, var.type.getInternalName());
		}
		// Store the next element
		writer.writeVarInsn(Opcodes.ASTORE, var.index);
		
		// Action
		if (this.action != null)
		{
			this.action.writeStatement(writer);
		}
		
		writer.writeLabel(updateLabel);
		// Load Iterator
		writer.writeVarInsn(Opcodes.ALOAD, iteratorVar.index);
		// Check hasNext
		writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
		// Go back to start if Iterator.hasNext() returned true
		writer.writeJumpInsn(Opcodes.IFNE, startLabel);
		
		// Local Variables
		writer.resetLocals(locals);
		writer.writeLabel(endLabel);
		
		var.writeLocal(writer, scopeLabel, endLabel);
		iteratorVar.writeLocal(writer, scopeLabel, endLabel);
	}
}
