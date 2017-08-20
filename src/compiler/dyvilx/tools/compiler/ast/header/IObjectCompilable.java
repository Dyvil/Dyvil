package dyvilx.tools.compiler.ast.header;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public interface IObjectCompilable
{
	void write(DataOutput out) throws IOException;
	
	void read(DataInput in) throws IOException;
}
