package dyvilx.tools.compiler.ast.imports;

import dyvil.lang.Formattable;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.consumer.IImportConsumer;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IObjectCompilable;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ImportDeclaration implements ASTNode, IObjectCompilable, IImportConsumer
{
	protected SourcePosition position;
	protected IImport        theImport;

	public ImportDeclaration(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	public IImport getImport()
	{
		return this.theImport;
	}

	@Override
	public void setImport(IImport iimport)
	{
		this.theImport = iimport;
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
		return Formattable.toString(this);
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
