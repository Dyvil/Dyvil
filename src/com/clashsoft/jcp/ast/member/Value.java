package com.clashsoft.jcp.ast.member;

public class Value
{
	public static final byte OBJECT = 0;
	public static final byte BOOLEAN = 1;
	public static final byte BYTE = 2;
	public static final byte SHORT = 3;
	public static final byte CHAR = 4;
	public static final byte INTEGER = 5;
	public static final byte LONG = 6;
	public static final byte FLOAT = 7;
	public static final byte DOUBLE = 8;
	
	private byte type;
	
	private Object object;
	
	private boolean booleanValue;
	private long intValue;
	private double floatValue;
	
	public void setObject(Object value)
	{
		this.type = OBJECT;
		this.object = value;
	}
	
	public void setBoolean(boolean b)
	{
		this.type = BOOLEAN;
		this.booleanValue = b;
	}
	
	public void setByte(byte b)
	{
		this.type = BYTE;
		this.intValue = b;
	}
	
	public void setShort(short s)
	{
		this.type = SHORT;
		this.intValue = s;
	}
	
	public void setChar(char c)
	{
		this.type = CHAR;
		this.intValue = c;
	}
	
	public void setInt(int i)
	{
		this.type = INTEGER;
		this.intValue = i;
	}
	
	public void setInt(long i)
	{
		this.type = INTEGER;
		this.intValue = i;
	}
	
	public void setLong(long l)
	{
		this.type = LONG;
		this.intValue = l;
	}
	
	public void setFloat(float f)
	{
		this.type = FLOAT;
		this.floatValue = f;
	}
	
	public void setFloat(double f)
	{
		this.type = FLOAT;
		this.floatValue = f;
	}
	
	public void setDouble(double d)
	{
		this.type = DOUBLE;
		this.floatValue = d;
	}
	
	public Object getObject()
	{
		return this.object;
	}
	
	
	public boolean getBoolean()
	{
		return this.booleanValue;
	}
	
	public int getInt()
	{
		return (int) this.intValue;
	}
	
	public float getFloat()
	{
		return (float) this.floatValue;
	}
}
