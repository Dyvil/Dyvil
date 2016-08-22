package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.backend.IObjectCompilable;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class ImportDeclaration implements IASTNode, IObjectCompilable
{
	protected ICodePosition position;
	protected IImport       theImport;

	public ImportDeclaration(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	public void setImport(IImport iimport)
	{
		this.theImport = iimport;
	}

	public IImport getImport()
	{
		return this.theImport;
	}

	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.theImport == null)
		{
			markers.add(Markers.semanticError(this.position, "import.invalid"));
			return;
		}

		this.theImport.resolveTypes(markers, context, Package.rootPackage, KindedImport.ANY);
	}

	public void resolve(MarkerList markers, IContext context)
	{
		if (this.theImport != null)
		{
			this.theImport.resolve(markers, context, Package.rootPackage, KindedImport.ANY);
		}
	}

	// Context

	public IImportContext getContext()
	{
		return this.theImport.asContext();
	}

	// Compilation

	@Override
	public void write(DataOutput output) throws IOException
	{
		IImport.writeImport(this.theImport, output);
	}

	@Override
	public void read(DataInput input) throws IOException
	{
		this.theImport = IImport.readImport(input);
	}

	// Formatting

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("import ");

		if (this.theImport != null)
		{
			this.theImport.toString(prefix, buffer);
		}
	}
}
