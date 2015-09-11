package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class CompoundCall extends AbstractCall implements INamed
{
	public Name name;
	
	public IMethod updateMethod;
	
	public CompoundCall(ICodePosition position)
	{
		this.position = position;
	}
	
	public CompoundCall(ICodePosition position, IValue instance, Name name)
	{
		this.position = position;
		this.instance = instance;
		this.name = name;
	}
	
	@Override
	public int valueTag()
	{
		return METHOD_CALL;
	}
	
	@Override
	public void setName(Name name)
	{
		this.name = name;
	}
	
	@Override
	public Name getName()
	{
		return this.name;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.arguments = this.arguments.withLastValue(Name.update, value);
	}
	
	@Override
	public IValue getValue()
	{
		return this.arguments.getLastValue();
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.resolve(markers, context);
		}
		
		this.arguments.resolve(markers, context);
		
		IMethod method = ICall.resolveMethod(context, this.instance, this.name, this.arguments);
		if (method != null)
		{
			this.method = method;
			this.checkArguments(markers, context);
			return this;
		}
		
		ICall.addResolveMarker(markers, this.position, this.instance, this.name, this.arguments);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.checkTypes(markers, context);
			
			int valueTag = this.instance.valueTag();
			if (valueTag == APPLY_CALL)
			{
				ApplyMethodCall call = (ApplyMethodCall) this.instance;
				IArguments arguments1 = call.arguments.withLastValue(call);
				
				IMethod match = ICall.resolveMethod(context, call.instance, Name.update, arguments1);
				if (match != null)
				{
					this.updateMethod = match;
				}
				else
				{
					Marker marker = markers.create(this.position, "method.compound.update");
					marker.addInfo("Callee Type: " + call.instance.getType());
				}
			}
			else if (valueTag == SUBSCRIPT_GET)
			{
				SubscriptGetter setter = (SubscriptGetter) this.instance;
				IArguments arguments1 = setter.arguments.withLastValue(setter);
				
				IMethod match = ICall.resolveMethod(context, setter.instance, Name.subscript_$eq, arguments1);
				if (match != null)
				{
					this.updateMethod = match;
				}
				else
				{
					Marker marker = markers.create(this.position, "method.compound.subscript");
					marker.addInfo("Callee Type: " + setter.instance.getType());
				}
			}
			else if (valueTag == FIELD_ACCESS)
			{
				FieldAccess fa = (FieldAccess) this.instance;
				if (fa.field != null)
				{
					fa.field = fa.field.capture(context);
					this.arguments.setLastValue(fa.field.checkAssign(markers, context, fa.getPosition(), fa.instance, this.arguments.getLastValue()));
				}
			}
		}
		
		if (this.method != null)
		{
			this.method.checkArguments(markers, this.position, context, this.instance, this.arguments, this.getGenericData());
		}
		this.arguments.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.check(markers, context);
		}
		
		if (this.method != null)
		{
			IType type1 = this.instance.getType();
			IType type2 = this.getType();
			if (!type1.isSuperTypeOf(type2))
			{
				Marker marker = markers.create(this.position, "method.compound.type", this.name, this.instance.toString());
				marker.addInfo("Callee Type: " + type1);
				marker.addInfo("Method Type: " + type2);
			}
			
			this.method.checkCall(markers, this.position, context, this.instance, this.arguments, this.getGenericData());
		}
		
		this.arguments.check(markers, context);
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		int i = this.instance.valueTag();
		if (i == FIELD_ACCESS)
		{
			FieldAccess access = (FieldAccess) this.instance;
			IDataMember f = access.field;
			
			int lineNumber = this.instance.getLineNumber();
			if (this.writeIINC(writer, f))
			{
				f.writeGet(writer, null, lineNumber);
				return;
			}
			
			IValue instance = access.instance;
			if (instance != null)
			{
				instance.writeExpression(writer, f.getTheClass().getType());
				writer.writeInsn(Opcodes.AUTO_DUP);
			}
			
			f.writeGet(writer, null, lineNumber);
			this.method.writeCall(writer, null, this.arguments, null, lineNumber);
			writer.writeInsn(Opcodes.AUTO_DUP);
			f.writeSet(writer, null, null, lineNumber);
		}
		else if (i == APPLY_CALL || i == SUBSCRIPT_GET)
		{
			AbstractCall call = (AbstractCall) this.instance;
			
			call.instance.writeExpression(writer, call.method.getTheClass().getType());
			
			for (IValue v : call.arguments)
			{
				v.writeExpression(writer);
			}
			
			writer.writeInsn(Opcodes.DUP2);
			
			int line = this.instance.getLineNumber();
			call.method.writeCall(writer, null, EmptyArguments.INSTANCE, null, line);
			this.method.writeCall(writer, null, this.arguments, null, line);
			writer.writeInsn(Opcodes.DUP_X2);
			this.updateMethod.writeCall(writer, null, EmptyArguments.INSTANCE, null, line);
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		int i = this.instance.valueTag();
		if (i == FIELD_ACCESS)
		{
			FieldAccess access = (FieldAccess) this.instance;
			IDataMember f = access.field;
			
			if (this.writeIINC(writer, f))
			{
				return;
			}
			
			IValue instance = access.instance;
			if (instance != null)
			{
				instance.writeExpression(writer);
				writer.writeInsn(Opcodes.AUTO_DUP);
			}
			
			int lineNumber = this.instance.getLineNumber();
			f.writeGet(writer, null, lineNumber);
			this.method.writeCall(writer, null, this.arguments, null, lineNumber);
			f.writeSet(writer, null, null, lineNumber);
		}
		else if (i == APPLY_CALL || i == SUBSCRIPT_SET)
		{
			AbstractCall call = (ApplyMethodCall) this.instance;
			
			call.instance.writeExpression(writer);
			
			for (IValue v : call.arguments)
			{
				v.writeExpression(writer);
			}
			
			writer.writeInsn(Opcodes.DUP2);
			
			int lineNumber = this.instance.getLineNumber();
			call.method.writeCall(writer, null, EmptyArguments.INSTANCE, null, lineNumber);
			this.method.writeCall(writer, null, this.arguments, null, lineNumber);
			this.updateMethod.writeCall(writer, null, EmptyArguments.INSTANCE, null, lineNumber);
		}
	}
	
	private boolean writeIINC(MethodWriter writer, IDataMember f) throws BytecodeException
	{
		if (this.arguments.size() == 1 && f.getType() == Types.INT && f.isVariable())
		{
			if (((IVariable) f).isReferenceType())
			{
				return false;
			}
			
			boolean minus = false;
			if (this.name == Name.plus || (minus = this.name == Name.minus))
			{
				IValue value1 = this.arguments.getFirstValue();
				if (IValue.isNumeric(value1.valueTag()))
				{
					int count = value1.intValue();
					writer.writeIINC(((IVariable) f).getIndex(), minus ? -count : count);
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString(prefix, buffer);
			buffer.append(' ');
		}
		
		if (Formatting.Method.convertQualifiedNames)
		{
			buffer.append(this.name.qualified).append("$eq");
		}
		else
		{
			buffer.append(this.name.unqualified).append('=');
		}
		
		this.arguments.toString(prefix, buffer);
	}
}
