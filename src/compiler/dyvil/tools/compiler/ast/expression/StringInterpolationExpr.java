package dyvil.tools.compiler.ast.expression;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.StringValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.CaseClasses;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.lexer.LexerUtil;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class StringInterpolationExpr implements IValue
{
	public static final class Types
	{
		public static final IClass STRING_INTERPOLATION_CONVERTIBLE = Package.dyvilLangLiteral.resolveClass("StringInterpolationConvertible");
		
		private Types()
		{
			// no instances
		}
	}
	
	protected ICodePosition position;
	
	private IValue[]	values	= new IValue[1];
	private String[]	strings	= new String[2];
	private int			count;
	
	public StringInterpolationExpr(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
	{
		return STRING_INTERPOLATION;
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType getType()
	{
		return dyvil.tools.compiler.ast.type.Types.STRING;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type.isSuperTypeOf(dyvil.tools.compiler.ast.type.Types.STRING))
		{
			return this;
		}
		
		IClass iclass = type.getTheClass();
		IAnnotation annotation;
		if ((annotation = iclass.getAnnotation(dyvil.tools.compiler.ast.type.Types.STRING_CONVERTIBLE_CLASS)) != null)
		{
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}
		if ((annotation = iclass.getAnnotation(Types.STRING_INTERPOLATION_CONVERTIBLE)) != null)
		{
			StringValue string;
			int len = this.count / 2;
			if (len > 0)
			{
				StringBuilder builder = new StringBuilder();
				builder.append(this.strings[0]);
				for (int i = 1; i <= len; i++)
				{
					builder.append('\\').append(i);
					builder.append(this.strings[i]);
				}
				string = new StringValue(this.position, builder.toString());
			}
			else
			{
				string = new StringValue("");
			}
			
			ArgumentList list = new ArgumentList(1 + len);
			list.addValue(string);
			for (int i = 0; i < len; i++)
			{
				list.addValue(this.values[i]);
			}
			
			return new LiteralConversion(this, annotation, list).withType(type, typeContext, markers, context);
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type.isSuperTypeOf(dyvil.tools.compiler.ast.type.Types.STRING))
		{
			return true;
		}
		
		return this.isConvertible(type);
	}
	
	private boolean isConvertible(IType type)
	{
		IClass theClass = type.getTheClass();
		return theClass.getAnnotation(dyvil.tools.compiler.ast.type.Types.STRING_CONVERTIBLE_CLASS) != null
				|| theClass.getAnnotation(Types.STRING_INTERPOLATION_CONVERTIBLE) != null;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		float distance = type.getSubTypeDistance(dyvil.tools.compiler.ast.type.Types.STRING);
		if (distance != 0)
		{
			return distance;
		}
		return this.isConvertible(type) ? CONVERSION_MATCH : 0;
	}
	
	public void addString(String s)
	{
		int index = this.count++ / 2;
		if (index >= this.strings.length)
		{
			String[] temp = new String[index + 1];
			System.arraycopy(this.strings, 0, temp, 0, this.strings.length);
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
			IValue v = this.values[i];
			v.check(markers, context);
			
			if (v.getType() == dyvil.tools.compiler.ast.type.Types.VOID)
			{
				markers.add(I18n.createMarker(v.getPosition(), "string.interpolation.void"));
			}
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
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		int len = this.count / 2;
		for (int i = 0; i < len; i++)
		{
			this.values[i] = this.values[i].cleanup(context, compilableList);
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		int len = this.count / 2;
		String string = this.strings[0];
		
		int estSize = string.length();
		for (int i = 0; i < len; i++)
		{
			estSize += this.values[i].stringSize();
			estSize += this.strings[i + 1].length();
		}
		
		writer.writeTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
		writer.writeInsn(Opcodes.DUP);
		writer.writeLDC(estSize);
		writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(I)V", false);
		
		CaseClasses.writeStringAppend(writer, string);
		
		for (int i = 0; i < len; i++)
		{
			IValue value = this.values[i];
			value.writeExpression(writer);
			CaseClasses.writeStringAppend(writer, value.getType());
			
			string = this.strings[i + 1];
			CaseClasses.writeStringAppend(writer, string);
		}
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		int len = this.count / 2;
		String s = this.strings[0];
		buffer.append('"');
		LexerUtil.appendStringLiteralBody(s, buffer);
		
		for (int i = 0; i < len; i++)
		{
			buffer.append("\\(");
			this.values[i].toString(prefix, buffer);
			s = this.strings[i + 1];
			buffer.append(')');
			LexerUtil.appendStringLiteralBody(s, buffer);
		}
		buffer.append('"');
	}
}
