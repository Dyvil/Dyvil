package dyvil.tools.compiler.ast.expression;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.constant.StringValue;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.transform.CaseClasses;

public final class FormatStringExpression extends ASTNode implements IValue
{
	private IValue[]	values	= new IValue[1];
	private String[]	strings	= new String[2];
	private int			count;
	
	public FormatStringExpression(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getValueType()
	{
		return STRING; // FIXME
	}
	
	@Override
	public IType getType()
	{
		return Types.STRING;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.isSuperTypeOf(Types.STRING);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type.equals(Types.STRING))
		{
			return 3;
		}
		if (type.isSuperTypeOf(Types.STRING))
		{
			return 2;
		}
		return 0;
	}
	
	public void addString(String s)
	{
		int index = this.count++ / 2;
		if (index >= this.strings.length)
		{
			String[] temp = new String[index + 1];
			System.arraycopy(strings, 0, temp, 0, strings.length);
			this.strings = temp;
		}
		this.strings[index] = s;
	}
	
	public void addValue(IValue value)
	{
		int index = this.count++ / 2;
		if (index >= this.values.length)
		{
			IValue[] temp = new IValue[index + 1];
			System.arraycopy(this.values, 0, temp, 0, this.values.length);
			this.values = temp;
		}
		this.values[index] = value;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		int len = this.count / 2;
		for (int i = 0; i < len; i++)
		{
			this.values[i].resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		int len = this.count / 2;
		for (int i = 0; i < len; i++)
		{
			this.values[i] = this.values[i].resolve(markers, context);
		}
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		int len = this.count / 2;
		for (int i = 0; i < len; i++)
		{
			this.values[i].checkTypes(markers, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		int len = this.count / 2;
		for (int i = 0; i < len; i++)
		{
			this.values[i].check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		int len = this.count / 2;
		for (int i = 0; i < len; i++)
		{
			this.values[i] = this.values[i].foldConstants();
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		int len = this.count / 2;
		String s = this.strings[0];
		
		writer.writeTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
		writer.writeInsn(Opcodes.DUP);
		writer.writeLDC(s);
		writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
		
		for (int i = 0; i < len; i++)
		{
			IValue value = this.values[i];
			value.writeExpression(writer);
			CaseClasses.writeToString(writer, value.getType());
			
			writer.writeLDC(this.strings[i + 1]);
			writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
		}
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		int len = this.count / 2;
		String s = this.strings[0];
		buffer.append("@\"");
		StringValue.append(s, s.length(), buffer);
		
		for (int i = 0; i < len; i++)
		{
			buffer.append("${");
			this.values[i].toString(prefix, buffer);
			s = this.strings[i + 1];
			buffer.append('}');
			StringValue.append(s, s.length(), buffer);
		}
		buffer.append('"');
	}
}
