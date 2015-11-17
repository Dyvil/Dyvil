package dyvil.tools.compiler.ast.statement.loop;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.generic.type.ClassGenericType;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public class IterableForStatement extends ForEachStatement
{
	public static final ITypeVariable	ITERABLE_TYPE	= Types.ITERABLE.getTheClass().getTypeVariable(0);
	public static final IClass			ITERATOR_CLASS	= Package.javaUtil.resolveClass("Iterator");
	
	public static final Name $iterator = Name.getQualified("$iterator");
	
	protected Variable	iteratorVar;
	protected IMethod	boxMethod;
	
	public IterableForStatement(ICodePosition position, Variable variable)
	{
		this(position, variable, variable.getValue().getType());
	}
	
	public IterableForStatement(ICodePosition position, Variable variable, IType valueType)
	{
		this(position, variable, valueType, valueType.resolveTypeSafely(ITERABLE_TYPE));
	}
	
	public IterableForStatement(ICodePosition position, Variable variable, IType valueType, IType elementType)
	{
		super(position, variable);
		
		this.iteratorVar = new Variable($iterator, new ClassGenericType(ITERATOR_CLASS, new IType[] { elementType }, 1));
		
		IType varType = variable.getType();
		boolean primitive = varType.isPrimitive();
		if (primitive != elementType.isPrimitive())
		{
			if (primitive)
			{
				this.boxMethod = varType.getUnboxMethod();
			}
			else
			{
				this.boxMethod = elementType.getBoxMethod();
			}
		}
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		if (name == this.variable.getName())
		{
			return this.variable;
		}
		
		if (name == $iterator)
		{
			return this.iteratorVar;
		}
		
		return null;
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		dyvil.tools.asm.Label startLabel = this.startLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label updateLabel = this.updateLabel.target = new dyvil.tools.asm.Label();
		dyvil.tools.asm.Label endLabel = this.endLabel.target = new dyvil.tools.asm.Label();
		
		Variable var = this.variable;
		Variable iteratorVar = this.iteratorVar;
		IType varType = var.getType();
		int lineNumber = this.getLineNumber();
		
		dyvil.tools.asm.Label scopeLabel = new dyvil.tools.asm.Label();
		writer.writeLabel(scopeLabel);
		
		// Get the iterator
		var.getValue().writeExpression(writer);
		writer.writeLineNumber(lineNumber);
		writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, "java/lang/Iterable", "iterator", "()Ljava/util/Iterator;", true);
		
		// Local Variables
		int locals = writer.localCount();
		iteratorVar.setLocalIndex(locals);
		var.setLocalIndex(locals + 1);
		
		// Store Iterator
		writer.writeVarInsn(Opcodes.ASTORE, locals);
		
		// Jump to hasNext check
		writer.writeJumpInsn(Opcodes.GOTO, updateLabel);
		writer.writeTargetLabel(startLabel);
		
		// Invoke Iterator.next()
		writer.writeVarInsn(Opcodes.ALOAD, locals);
		writer.writeLineNumber(lineNumber);
		writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
		// Cast to the variable type
		// Auto(un)boxing
		if (this.boxMethod != null)
		{
			writer.writeTypeInsn(Opcodes.CHECKCAST, this.boxMethod.getTheClass().getInternalName());
			this.boxMethod.writeInvoke(writer, null, null, lineNumber);
		}
		else
		{
			Types.OBJECT.writeCast(writer, varType, lineNumber);
		}
		
		// Store the next element
		writer.writeVarInsn(varType.getStoreOpcode(), locals + 1);
		
		// Action
		if (this.action != null)
		{
			this.action.writeStatement(writer);
		}
		
		writer.writeLabel(updateLabel);
		// Load Iterator
		writer.writeVarInsn(Opcodes.ALOAD, locals);
		// Check hasNext
		writer.writeLineNumber(lineNumber);
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
