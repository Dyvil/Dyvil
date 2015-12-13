package dyvil.tools.compiler.ast.reference;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.MarkerMessages;
import dyvil.tools.parsing.marker.MarkerList;

public class ReferenceType extends ClassType
{
	private IType type;
	
	public ReferenceType(IClass iclass, IType type)
	{
		this.theClass = iclass;
		this.type = type;
	}
	
	@Override
	public IValue convertValue(IValue value, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		IValue value1 = value.withType(this.type, typeContext, markers, context);
		if (value1 == null)
		{
			return null;
		}
		
		IReference ref = value.toReference();
		if (ref != null)
		{
			return new ReferenceValue(value, ref);
		}
		
		markers.add(MarkerMessages.createMarker(value.getPosition(), "value.reference"));
		return value1;
	}
	
	public void writeGet(MethodWriter writer, int index) throws BytecodeException
	{
		writer.writeVarInsn(Opcodes.ALOAD, index);
		
		String internal = this.theClass.getInternalName();
		if (this.theClass == Types.OBJECT_REF_CLASS)
		{
			writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, internal, "get", "()Ljava/lang/Object;", true);
			
			if (this.type.getTheClass() != Types.OBJECT_CLASS)
			{
				writer.writeTypeInsn(Opcodes.CHECKCAST, this.type.getInternalName());
			}
			return;
		}
		
		StringBuilder sb = new StringBuilder("()");
		this.type.appendExtendedName(sb);
		writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, internal, "get", sb.toString(), true);
		return;
	}
	
	public static void writeGetRef(MethodWriter writer, IValue value, int index) throws BytecodeException
	{
		writer.writeVarInsn(Opcodes.ALOAD, index);
		
		if (value != null)
		{
			value.writeExpression(writer, null);
		}
		else
		{
			writer.writeInsn(Opcodes.AUTO_SWAP);
		}
	}
	
	public void writeSet(MethodWriter writer, int index, IValue value) throws BytecodeException
	{
		writeGetRef(writer, value, index);
		
		String internal = this.theClass.getInternalName();
		if (this.theClass == Types.OBJECT_REF_CLASS)
		{
			writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, internal, "set", "(Ljava/lang/Object;)V", true);
			return;
		}
		
		StringBuilder sb = new StringBuilder().append('(');
		this.type.appendExtendedName(sb);
		sb.append(")V");
		writer.writeInvokeInsn(Opcodes.INVOKEINTERFACE, internal, "set", sb.toString(), true);
	}
}
