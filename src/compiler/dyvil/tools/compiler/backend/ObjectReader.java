package dyvil.tools.compiler.backend;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

public class ObjectReader implements DataInput, AutoCloseable
{
	private final DataInputStream input;
	private       String[]        constantPool;
	
	public ObjectReader(DataInputStream input) throws IOException
	{
		this.input = input;
		
		int version = input.readShort();
		if (version > ObjectWriter.FILE_VERSION)
		{
			throw new IllegalStateException("Unknown Dyvil Object File Version: " + version);
		}
		
		int constPoolSize = input.readShort();
		this.constantPool = new String[constPoolSize];
		for (int i = 0; i < constPoolSize; i++)
		{
			this.constantPool[i] = input.readUTF();
		}
	}
	
	@Override
	public void readFully(byte[] b) throws IOException
	{
		this.input.readFully(b);
	}
	
	@Override
	public void readFully(byte[] b, int off, int len) throws IOException
	{
		this.input.readFully(b, off, len);
	}
	
	@Override
	public int skipBytes(int n) throws IOException
	{
		return this.input.skipBytes(n);
	}
	
	@Override
	public boolean readBoolean() throws IOException
	{
		return this.input.readBoolean();
	}
	
	@Override
	public byte readByte() throws IOException
	{
		return this.input.readByte();
	}
	
	@Override
	public int readUnsignedByte() throws IOException
	{
		return this.input.readUnsignedByte();
	}
	
	@Override
	public short readShort() throws IOException
	{
		return this.input.readShort();
	}
	
	@Override
	public int readUnsignedShort() throws IOException
	{
		return this.input.readUnsignedShort();
	}
	
	@Override
	public char readChar() throws IOException
	{
		return this.input.readChar();
	}
	
	@Override
	public int readInt() throws IOException
	{
		return this.input.readInt();
	}
	
	@Override
	public long readLong() throws IOException
	{
		return this.input.readLong();
	}
	
	@Override
	public float readFloat() throws IOException
	{
		return this.input.readFloat();
	}
	
	@Override
	public double readDouble() throws IOException
	{
		return this.input.readDouble();
	}
	
	@Override
	public String readLine() throws IOException
	{
		return this.constantPool[this.input.readShort()];
	}
	
	@Override
	public String readUTF() throws IOException
	{
		return this.constantPool[this.input.readShort()];
	}
	
	@Override
	public void close() throws Exception
	{
		this.input.close();
	}
}
