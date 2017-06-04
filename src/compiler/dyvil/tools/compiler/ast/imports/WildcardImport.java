package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IDefaultContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class WildcardImport extends Import
{
	private IImportContext context;

	public WildcardImport()
	{
		super(null);
	}

	public WildcardImport(SourcePosition position)
	{
		super(position);
	}

	@Override
	public int importTag()
	{
		return WILDCARD;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context, IImportContext parentContext, int mask)
	{
		if (this.parent != null)
		{
			this.parent.resolveTypes(markers, context, parentContext, KindedImport.PARENT);
			parentContext = this.parent.asParentContext();
		}

		if ((mask & KindedImport.STATIC) != 0 && parentContext != null)
		{
			this.context = parentContext;
			return;
		}

		if (!(parentContext instanceof Package))
		{
			markers.add(Markers.semanticError(this.position, "import.wildcard.invalid"));
			this.context = IDefaultContext.DEFAULT;
			return;
		}
		this.context = parentContext;
	}

	@Override
	public void resolve(MarkerList markers, IContext context, IImportContext parentContext, int mask)
	{
	}

	@Override
	public IImportContext asContext()
	{
		return this.context;
	}

	@Override
	public IImportContext asParentContext()
	{
		return null;
	}

	@Override
	public void writeData(DataOutput out) throws IOException
	{
		IImport.writeImport(this.parent, out);
	}

	@Override
	public void readData(DataInput in) throws IOException
	{
		this.parent = IImport.readImport(in);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.appendParent(prefix, buffer);
		buffer.append('_');
	}
}
