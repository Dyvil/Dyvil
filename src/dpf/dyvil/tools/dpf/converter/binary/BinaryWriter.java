package dyvil.tools.dpf.converter.binary;

import dyvil.tools.dpf.visitor.*;
import dyvil.tools.parsing.Name;

import java.io.DataOutput;
import java.io.IOException;

import static dyvil.tools.dpf.converter.binary.BinaryConstants.*;

public class BinaryWriter
		implements NodeVisitor, ValueVisitor, ListVisitor, MapVisitor, StringInterpolationVisitor, BuilderVisitor
{
	private final DataOutput dataOutput;

	public BinaryWriter(DataOutput dataOutput)
	{
		this.dataOutput = dataOutput;
	}

	private void writeTag(int tag)
	{
		try
		{
			this.writeTagThrowing(tag);
		}
		catch (IOException ex)
		{
			throwEx(ex);
		}
	}

	private void writeTagThrowing(int tag) throws IOException
	{
		this.dataOutput.writeByte((byte) tag);
	}

	private void writeName(Name name)
	{
		try
		{
			this.dataOutput.writeUTF(name == null ? "" : name.unqualified);
		}
		catch (IOException ex)
		{
			throwEx(ex);
		}
	}

	private void writeNameThrowing(Name name) throws IOException
	{
		this.dataOutput.writeUTF(name.unqualified);
	}

	private void writeTagAndName(int tag, Name name)
	{
		try
		{
			this.writeTagAndNameThrowing(tag, name);
		}
		catch (IOException ex)
		{
			throwEx(ex);
		}
	}

	private void writeTagAndNameThrowing(int tag, Name name) throws IOException
	{
		this.writeTagThrowing(tag);
		this.writeNameThrowing(name);
	}

	private static void throwEx(Exception ex)
	{
		throw new RuntimeException(ex);
	}

	// NodeVisitor

	@Override
	public NodeVisitor visitNode(Name name)
	{
		this.writeTagAndName(NODE, name);
		return this;
	}

	@Override
	public ValueVisitor visitProperty(Name name)
	{
		this.writeTagAndName(PROPERTY, name);
		return this;
	}

	@Override
	public NodeVisitor visitNodeAccess(Name name)
	{
		this.writeTagAndName(NODE_ACCESS, name);
		return this;
	}

	// ValueVisitor

	@Override
	public void visitInt(int value)
	{
		try
		{
			this.writeTagThrowing(INT);
			this.dataOutput.writeInt(value);
		}
		catch (IOException ex)
		{
			throwEx(ex);
		}
	}

	@Override
	public void visitLong(long value)
	{
		try
		{
			this.writeTagThrowing(LONG);
			this.dataOutput.writeLong(value);
		}
		catch (IOException ex)
		{
			throwEx(ex);
		}
	}

	@Override
	public void visitFloat(float value)
	{
		try
		{
			this.writeTagThrowing(FLOAT);
			this.dataOutput.writeFloat(value);
		}
		catch (IOException ex)
		{
			throwEx(ex);
		}
	}

	@Override
	public void visitDouble(double value)
	{
		try
		{
			this.writeTagThrowing(DOUBLE);
			this.dataOutput.writeDouble(value);
		}
		catch (IOException ex)
		{
			throwEx(ex);
		}
	}

	@Override
	public void visitString(String value)
	{
		try
		{
			this.writeTagThrowing(STRING);
			this.dataOutput.writeUTF(value);
		}
		catch (IOException ex)
		{
			throwEx(ex);
		}
	}

	@Override
	public StringInterpolationVisitor visitStringInterpolation()
	{
		this.writeTag(STRING_INTERPOLATION);
		return this;
	}

	@Override
	public void visitName(Name name)
	{
		this.writeTagAndName(NAME, name);
	}

	@Override
	public ValueVisitor visitValueAccess(Name name)
	{
		this.writeTagAndName(NAME_ACCESS, name);
		return this;
	}

	@Override
	public ListVisitor visitList()
	{
		this.writeTag(LIST);
		return this;
	}

	@Override
	public MapVisitor visitMap()
	{
		this.writeTag(MAP);
		return this;
	}

	@Override
	public BuilderVisitor visitBuilder(Name name)
	{
		this.writeTagAndName(BUILDER, name);
		return this;
	}

	// ListVisitor

	@Override
	public ValueVisitor visitElement()
	{
		return this;
	}

	// MapVisitor

	@Override
	public ValueVisitor visitKey()
	{
		return this;
	}

	// StringInterpolationVisitor

	@Override
	public void visitStringPart(String string)
	{
		try
		{
			this.dataOutput.writeUTF(string);
		}
		catch (IOException ex)
		{
			throwEx(ex);
		}
	}

	// BuilderVisitor

	@Override
	public ValueVisitor visitParameter(Name name)
	{
		this.writeName(name);
		return this;
	}

	@Override
	public NodeVisitor visitNode()
	{
		this.writeName(null);
		this.writeTag(END);
		return this;
	}

	// Shared

	@Override
	public ValueVisitor visitValue()
	{
		return this;
	}

	@Override
	public void visitEnd()
	{
		this.writeTag(END);
	}
}
