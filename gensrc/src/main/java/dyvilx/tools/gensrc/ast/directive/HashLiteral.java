package dyvilx.tools.gensrc.ast.directive;

import dyvil.annotation.internal.NonNull;
import dyvil.source.position.SourcePosition;

public class HashLiteral extends ProcessedText
{
	public HashLiteral(@NonNull SourcePosition position)
	{
		super(position, "#");
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append("##");
	}
}
