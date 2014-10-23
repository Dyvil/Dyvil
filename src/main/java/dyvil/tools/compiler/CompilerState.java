package dyvil.tools.compiler;

import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.ast.structure.Package;

public enum CompilerState
{
	TOKENIZE, PARSE, RESOLVE, OPERATOR_PRECEDENCE, FOLD_CONSTANTS, CONVERT, OPTIMIZE, COMPILE;
	
	public Package	rootPackage;
	public CodeFile	file;
	
	public void addMarker(Marker marker)
	{
		this.file.markers.add(marker);
	}
}
