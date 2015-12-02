package dyvil.tools.compiler.backend;

import java.io.*;

public class ObjectWriter extends OutputStream implements DataOutput
{
	protected static final int FILE_VERSION = 1;
	
	private int constantPoolSize;
	private String[] constantPool = new String[16];
	
	private ByteArrayOutputStream bufferData;
	private DataOutputStream      buffer;
	
	public ObjectWriter()
	{
		this.buffer = new DataOutputStream(this.bufferData = new ByteArrayOutputStream());
	}
	
	@Override
	public void write(int b) throws IOException
	{
		this.buffer.write(b);
	}
	
	@Override
	public void write(byte[] b) throws IOException
	{
		this.buffer.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		this.buffer.write(b, off, len);
	}
	
	@Override
	public void writeBoolean(boolean v) throws IOException
	{
		this.buffer.writeBoolean(v);
	}
	
	@Override
	public void writeByte(int v) throws IOException
	{
		this.buffer.writeByte(v);
	}
	
	@Override
	public void writeShort(int v) throws IOException
	{
		this.buffer.writeShort(v);
	}
	
	@Override
	public void writeChar(int v) throws IOException
	{
		this.buffer.writeChar(v);
	}
	
	@Override
	public void writeInt(int v) throws IOException
	{
		this.buffer.writeInt(v);
	}
	
	@Override
	public void writeLong(long v) throws IOException
	{
		this.buffer.writeLong(v);
	}
	
	@Override
	public void writeFloat(float v) throws IOException
	{
		this.buffer.writeFloat(v);
	}
	
	@Override
	public void writeDouble(double v) throws IOException
	{
		this.buffer.writeDouble(v);
	}
	
	@Override
	public void writeBytes(String s) throws IOException
	{
		this.buffer.writeBytes(s);
	}
	
	@Override
	public void writeChars(String s) throws IOException
	{
		this.buffer.writeChars(s);
	}
	
	@Override
	public void writeUTF(String s) throws IOException
	{
		int hash = s.hashCode();
		for (int i = 0; i < this.constantPoolSize; i++)
		{
			String constPoolEntry = this.constantPool[i];
			if (constPoolEntry.hashCode() == hash && s.equals(constPoolEntry))
			{
				// Found the string in the constant pool
				this.buffer.writeShort(i);
				return;
			}
		}
		
		int index = this.constantPoolSize++;
		if (index >= this.constantPool.length)
		{
			String[] temp = new String[index << 1];
			System.arraycopy(this.constantPool, 0, temp, 0, this.constantPool.length);
			this.constantPool = temp;
		}
		
		this.constantPool[index] = s;
		this.buffer.writeShort(index);
	}
	
	public void writeTo(OutputStream os) throws IOException
	{
		DataOutputStream dos = new DataOutputStream(os);
		
		// Write the .dyo File Version
		dos.writeShort(FILE_VERSION);
		
		// Write the constant pool
		dos.writeShort(this.constantPoolSize);
		for (int i = 0; i < this.constantPoolSize; i++)
		{
			dos.writeUTF(this.constantPool[i]);
		}
		
		this.bufferData.writeTo(os);
	}
	
	@Override
	public void close()
	{
		try
		{
			this.buffer.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
}
