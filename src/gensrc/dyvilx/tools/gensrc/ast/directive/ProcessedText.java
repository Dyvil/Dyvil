package dyvilx.tools.gensrc.ast.directive;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.StringInterpolationExpr;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.expression.constant.StringValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.gensrc.ast.GenSrcValue;
import dyvilx.tools.parsing.lexer.CharacterTypes;
import dyvilx.tools.parsing.marker.MarkerList;

public class ProcessedText implements GenSrcValue
{
	protected String text;

	// Metadata
	protected SourcePosition position;

	public ProcessedText(String value)
	{
		this.text = value;
	}

	public ProcessedText(SourcePosition position, String text)
	{
		this.position = position;
		this.text = text;
	}

	@Override
	public int valueTag()
	{
		return PROCESSED_TEXT;
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

	public String getText()
	{
		return this.text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		final int startLine = this.position.startLine();
		final int startColumn = this.position.startColumn();
		final StringInterpolationExpr parts = new StringInterpolationExpr();

		final String text = this.text;
		final int length = text.length();
		int prev = 0;

		for (int startIndex = 0; startIndex < length; )
		{
			final int c = text.codePointAt(startIndex);

			// advance to an identifier character
			if (!Character.isJavaIdentifierStart(c))
			{
				startIndex += Character.charCount(c);
				continue;
			}

			final int endIndex = identifierEnd(text, startIndex + 1, length);
			final String key = text.substring(startIndex, endIndex);
			final IDataMember field = context.resolveField(Name.fromRaw(key));

			if (field != null && (field.isLocal() || field.hasModifier(Modifiers.PUBLIC)))
			{
				// append contents before this identifier
				parts.append(new StringValue(text.substring(prev, startIndex)));

				final SourcePosition position = SourcePosition
					                                .apply(startLine, startColumn + startIndex, startColumn + endIndex);
				parts.append(new FieldAccess(position, null, field));

				// advance to the end of the identifier
				prev = endIndex;
				startIndex = endIndex;
				continue;
			}

			startIndex += Character.charCount(c);
		}

		if (prev != length)
		{
			parts.append(new StringValue(text.substring(prev, length)));
		}

		return new WriteCall(parts.resolve(markers, context));
	}

	private static int identifierEnd(String text, int start, int end)
	{
		while (start < end)
		{
			final int cp = text.codePointAt(start);
			if (!CharacterTypes.isIdentifierPart(cp))
			{
				return start;
			}

			start += Character.charCount(cp);
		}
		return end;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
	}

	@Override
	public IValue foldConstants()
	{
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append(this.text);
	}
}
