package dyvilx.tools.compiler.parser.classes;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.consumer.IMemberConsumer;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IProperty;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.parser.type.TypeParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

public class FieldParser<T extends IDataMember> extends AbstractMemberParser implements Consumer<IType>
{
	protected static final int DECLARATOR = 0;
	protected static final int NAME       = 1;
	protected static final int TYPE       = 2;
	protected static final int VALUE      = 3;
	protected static final int PROPERTY   = 4;

	// Flags

	public static final int NO_VALUES     = 1;
	public static final int NO_PROPERTIES = 2;

	protected IMemberConsumer<T> consumer;
	private   int                flags;

	private T         dataMember;
	private IProperty property;

	private SourcePosition position;
	private Name           name;
	private IType          type = Types.UNKNOWN;

	public FieldParser(IMemberConsumer<T> consumer)
	{
		this.consumer = consumer;
		// this.mode = DECLARATOR;
	}

	public FieldParser(IMemberConsumer<T> consumer, AttributeList attributes)
	{
		super(attributes);
		this.consumer = consumer;
		// this.mode = DECLARATOR;
	}

	public FieldParser<T> withFlags(int flags)
	{
		this.flags |= flags;
		return this;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case DECLARATOR:
			switch (type)
			{
			case DyvilKeywords.CONST:
				this.attributes.addFlag(Modifiers.CONST);
				this.mode = NAME;
				return;
			case DyvilKeywords.LET:
				this.attributes.addFlag(Modifiers.FINAL);
				// Fallthrough
			case DyvilKeywords.VAR:
				this.mode = NAME;
				return;
			}

			if (this.parseAttribute(pm, token))
			{
				return;
			}
			// Fallthrough
		case NAME:
			if (!Tokens.isIdentifier(type))
			{
				pm.report(token, "field.identifier");
				return;
			}

			this.position = token.raw();
			this.name = token.nameValue();

			this.mode = TYPE;
			return;
		case TYPE:
			if (type == BaseSymbols.COLON)
			{
				// ... IDENTIFIER : TYPE ...
				pm.pushParser(new TypeParser(this));
				this.mode = VALUE;
				return;
			}
			// Fallthrough
		case VALUE:
			if (this.parseAttribute(pm, token))
			{
				return;
			}

			if ((this.flags & NO_VALUES) == 0 && type == BaseSymbols.EQUALS)
			{
				// definitely a field
				final T field = this.initField();
				if ((this.flags & NO_PROPERTIES) != 0)
				{
					this.mode = END;
					pm.pushParser(new ExpressionParser(field));
				}
				else
				{
					this.mode = PROPERTY;
					pm.pushParser(new ExpressionParser(field).withFlags(ExpressionParser.IGNORE_CLOSURE));
				}
				return;
			}
			// Fallthrough
		case PROPERTY:
			if ((this.flags & NO_PROPERTIES) == 0 && type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				// either a standalone property or a field property
				pm.pushParser(new PropertyBodyParser(this.initProperty()), true);
				this.mode = END;
				return;
			}
			// Fallthrough
		case END:
			if (this.property != null)
			{
				this.consumer.addProperty(this.property);
			}
			else
			{
				// for fields without values or properties, the dataMember field has not beed initialized yet
				this.consumer.addDataMember(this.initField());
			}
			pm.popParser(type != Tokens.EOF);
		}
	}

	private IProperty initProperty()
	{
		final IProperty prop;
		if (this.dataMember == null)
		{
			prop = this.consumer.createProperty(this.position, this.name, this.type, this.attributes);
			this.property = prop;
		}
		else
		{
			prop = this.dataMember.createProperty();
		}
		return prop;
	}

	private T initField()
	{
		if (this.dataMember != null)
		{
			return this.dataMember;
		}
		return this.dataMember = this.consumer.createDataMember(this.position, this.name, this.type, this.attributes);
	}

	@Override
	public void accept(IType type)
	{
		this.type = type;
	}
}
