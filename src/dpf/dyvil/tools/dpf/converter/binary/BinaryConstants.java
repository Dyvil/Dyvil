package dyvil.tools.dpf.converter.binary;

public interface BinaryConstants
{
	int END = 0;

	// Node Elements

	int NODE        = 1;
	int NODE_ACCESS = 2;
	int PROPERTY    = 3;

	// Values

	int BOOLEAN = 16;

	int BYTE = 17;
	int SHORT = 18;
	int CHAR = 19;
	int INT = 20;

	int LONG   = 21;

	int FLOAT  = 22;
	int DOUBLE = 23;
	int STRING = 24;
	int STRING_INTERPOLATION = 25;
	int NAME = 28;
	int NAME_ACCESS = 29;

	int BUILDER = 31;

	// int TUPLE = 32;
	int LIST = 33;
	int MAP  = 34;
}
