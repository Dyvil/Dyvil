package dyvil.io;

import dyvil.annotation.internal.NonNull;

import java.io.*;

public class StringPoolReader extends FilterInputStream implements DataInput
{
	@NonNull
	private final DataInput input;
	private       String[]  constantPool;

	public StringPoolReader(InputStream input) throws IOException
	{
		super(input);

		if (input instanceof DataInput)
		{
			this.input = (DataInput) input;
		}
		else
		{
			this.input = new DataInputStream(input);
		}

		// Read constant pool entry count
		final int constPoolCount = this.input.readUnsignedShort();

		this.constantPool = new String[constPoolCount];

		// Read constant pool entries
		for (int i = 0; i < constPoolCount; i++)
		{
			this.constantPool[i] = this.input.readUTF();
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
		return this.input.readLine();
	}

	@NonNull
	@Override
	public String readUTF() throws IOException
	{
		return this.constantPool[this.input.readUnsignedShort()];
	}
}
