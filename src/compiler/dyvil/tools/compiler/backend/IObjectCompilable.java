package dyvil.tools.compiler.backend;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface IObjectCompilable
{
	public void write(DataOutput out) throws IOException;
	
	public void read(DataInput in) throws IOException;
}
