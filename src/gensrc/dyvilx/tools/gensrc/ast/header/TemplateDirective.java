package dyvilx.tools.gensrc.ast.header;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.parsing.ASTNode;

public class TemplateDirective implements ASTNode
{
	private SourcePosition position;
	private List<Name> names = new ArrayList<>();

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

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append("#template(");
		this.names.toString(buffer, "", ", ", "");
		buffer.append(')');
	}
}
