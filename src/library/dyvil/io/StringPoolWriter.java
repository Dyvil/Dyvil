package dyvil.io;

import java.io.*;

public class StringPoolWriter extends FilterOutputStream implements DataOutput
{
	private int constantPoolSize;
	private String[] constantPool = new String[16];
	
	private ByteArrayOutputStream dataBytes;
	private DataOutputStream      dataOutputStream;
	
	public StringPoolWriter(OutputStream target)
	{
		super(target);
		this.dataOutputStream = new DataOutputStream(this.dataBytes = new ByteArrayOutputStream());
	}
	
	@Override
	public void write(int b) throws IOException
	{
		this.dataOutputStream.write(b);
	}
	
	@Override
	public void write(byte[] b) throws IOException
	{
		this.dataOutputStream.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		this.dataOutputStream.write(b, off, len);
	}
	
	@Override
	public void writeBoolean(boolean v) throws IOException
	{
		this.dataOutputStream.writeBoolean(v);
	}
	
	@Override
	public void writeByte(int v) throws IOException
	{
		this.dataOutputStream.writeByte(v);
	}
	
	@Override
	public void writeShort(int v) throws IOException
	{
		this.dataOutputStream.writeShort(v);
	}
	
	@Override
	public void writeChar(int v) throws IOException
	{
		this.dataOutputStream.writeChar(v);
	}
	
	@Override
	public void writeInt(int v) throws IOException
	{
		this.dataOutputStream.writeInt(v);
	}
	
	@Override
	public void writeLong(long v) throws IOException
	{
		this.dataOutputStream.writeLong(v);
	}
	
	@Override
	public void writeFloat(float v) throws IOException
	{
		this.dataOutputStream.writeFloat(v);
	}
	
	@Override
	public void writeDouble(double v) throws IOException
	{
		this.dataOutputStream.writeDouble(v);
	}
	
	@Override
	public void writeBytes(String s) throws IOException
	{
		this.dataOutputStream.writeBytes(s);
	}
	
	@Override
	public void writeChars(String s) throws IOException
	{
		this.dataOutputStream.writeChars(s);
	}

	@Override
	public void writeUTF(String s) throws IOException
	{
		final int index = this.poolIndex(s);
		this.dataOutputStream.writeShort(index);
	}

	protected int poolIndex(String s)
	{
		int hash = s.hashCode();
		for (int i = 0; i < this.constantPoolSize; i++)
		{
			final String constPoolEntry = this.constantPool[i];
			if (constPoolEntry.hashCode() == hash && s.equals(constPoolEntry))
			{
				// Found the string in the constant pool
				return i;
			}
		}

		// Resize the constant pool

		final int index = this.constantPoolSize++;
		if (index >= this.constantPool.length)
		{
			String[] temp = new String[index << 1];
			System.arraycopy(this.constantPool, 0, temp, 0, this.constantPool.length);
			this.constantPool = temp;
		}

		this.constantPool[index] = s;
		return index;
	}

	@Override
	public void close() throws IOException
	{
		DataOutputStream constantPoolOutput = new DataOutputStream(this.out);

		// Write the constant pool
		constantPoolOutput.writeShort(this.constantPoolSize);
		for (int i = 0; i < this.constantPoolSize; i++)
		{
			constantPoolOutput.writeUTF(this.constantPool[i]);
		}

		this.dataBytes.writeTo(this.out);

		// No need to close dataOutputStream, dataBytes or constantPoolOutput

		super.close();
	}
}
