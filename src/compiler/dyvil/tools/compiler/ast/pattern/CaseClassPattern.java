package dyvil.tools.compiler.ast.pattern;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.parameter.IParameterList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class CaseClassPattern extends Pattern implements IPatternList
{
	protected IType type;
	protected IPattern[] patterns = new IPattern[2];
	protected int patternCount;

	// Metadata
	private IMethod[] getterMethods;
	private IType[]   paramTypes;
	
	public CaseClassPattern(ICodePosition position)
	{
		this.position = position;
	}

	public CaseClassPattern(ICodePosition position, IType type)
	{
		this.position = position;
		this.type = type;
	}

	@Override
	public int getPatternType()
	{
		return CASE_CLASS;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public IPattern withType(IType type, MarkerList markers)
	{
		if (!this.isType(type))
		{
			return null;
		}
		
		final IClass caseClass = this.type.getTheClass();
		if (caseClass == null)
		{
			return this; // skip
		}

		final IParameterList parameters = caseClass.getParameterList();
		final int paramCount = parameters.size();
		if (this.patternCount != paramCount)
		{
			final Marker marker = Markers.semantic(this.position, "pattern.class.count", this.type.toString());
			marker.addInfo(Markers.getSemantic("pattern.class.count.pattern", this.patternCount));
			marker.addInfo(Markers.getSemantic("pattern.class.count.class", paramCount));
			markers.add(marker);
			return this;
		}

		this.paramTypes = new IType[paramCount];
		
		for (int i = 0; i < paramCount; i++)
		{
			final IPattern pattern = this.patterns[i];
			final IParameter param = parameters.get(i);

			if (param.getAccessLevel() != Modifiers.PUBLIC)
			{
				this.checkMethodAccess(markers, caseClass, param, i, paramCount, pattern);
			}

			final IType paramType = param.getInternalType().getConcreteType(type);
			this.paramTypes[i] = paramType;

			final IPattern typedPattern = pattern.withType(paramType, markers);

			if (typedPattern == null)
			{
				final Marker marker = Markers.semantic(this.position, "pattern.class.type", param.getName());
				marker.addInfo(Markers.getSemantic("pattern.type", pattern.getType()));
				marker.addInfo(Markers.getSemantic("classparameter.type", paramType));
				markers.add(marker);
			}
			else
			{
				this.patterns[i] = typedPattern;
			}
		}

		if (Types.isExactType(type, this.type))
		{
			// No additional type check required
			return this;
		}
		return new TypeCheckPattern(this, type, this.type);
	}

	public void checkMethodAccess(MarkerList markers, IClass caseClass, IParameter param, int paramIndex, int paramCount, IPattern pattern)
	{
		final IMethod accessMethod = IContext.resolveMethod(caseClass, null, param.getName(), null); // find by name
		if (accessMethod != null)
		{
			if (this.getterMethods == null)
			{
				this.getterMethods = new IMethod[paramCount];
			}

			this.getterMethods[paramIndex] = accessMethod;
		}
		else
		{
			Marker marker = Markers.semanticError(pattern.getPosition(), "pattern.class.access", param.getName());
			marker.addInfo(Markers.getSemantic("pattern.class.access.type", caseClass.getFullName()));
			markers.add(marker);
		}
	}
	
	@Override
	public int patternCount()
	{
		return 0;
	}
	
	@Override
	public void setPattern(int index, IPattern pattern)
	{
		this.patterns[index] = pattern;
	}
	
	@Override
	public void addPattern(IPattern pattern)
	{
		final int index = this.patternCount++;
		if (index >= this.patterns.length)
		{
			final IPattern[] temp = new IPattern[index + 1];
			System.arraycopy(this.patterns, 0, temp, 0, this.patterns.length);
			this.patterns = temp;
		}
		this.patterns[index] = pattern;
	}
	
	@Override
	public IPattern getPattern(int index)
	{
		return this.patterns[index];
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		for (int i = 0; i < this.patternCount; i++)
		{
			final IDataMember field = this.patterns[i].resolveField(name);
			if (field != null)
			{
				return field;
			}
		}
		return null;
	}
	
	@Override
	public IPattern resolve(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
		
		for (int i = 0; i < this.patternCount; i++)
		{
			this.patterns[i] = this.patterns[i].resolve(markers, context);
		}
		
		return this;
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, int varIndex, IType matchedType, Label elseLabel)
			throws BytecodeException
	{
		varIndex = IPattern.ensureVar(writer, varIndex, matchedType);

		final IClass caseClass = this.type.getTheClass();
		final IParameterList parameters = caseClass.getParameterList();
		final int lineNumber = this.getLineNumber();

		for (int i = 0; i < this.patternCount; i++)
		{
			if (this.patterns[i].getPatternType() == WILDCARD)
			{
				// Skip wildcard patterns
				continue;
			}

			// Load the instance
			writer.visitVarInsn(Opcodes.ALOAD, varIndex);
			matchedType.writeCast(writer, this.type, lineNumber);

			final IMethod method = this.getterMethods[i];
			final IType targetType = this.paramTypes[i];
			final IType memberType;

			if (method != null)
			{
				memberType = method.getType();

				method.writeInvoke(writer, null, EmptyArguments.INSTANCE, ITypeContext.DEFAULT, lineNumber);
			}
			else
			{
				final IDataMember field = parameters.get(i);
				memberType = field.getType();

				field.writeGet(writer, null, lineNumber);
			}

			memberType.writeCast(writer, targetType, lineNumber);
			// Check the pattern
			this.patterns[i].writeInvJump(writer, -1, targetType, elseLabel);
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
		Formatting.appendSeparator(buffer, "parameters.open_paren", '(');

		Util.astToString(prefix, this.patterns, this.patternCount, Formatting.getSeparator("parameters.separator", ','),
		                 buffer);

		if (Formatting.getBoolean("parameters.close_paren.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(')');
	}
}
