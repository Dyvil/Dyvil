package dyvil.tools.compiler.ast.classes;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;

public class TraitMetadata extends InterfaceMetadata
{
	public TraitMetadata(IClass theClass)
	{
		super(theClass);
	}

	@Override
	protected void processField(IField field, MarkerList markers)
	{
		this.processMember(field, markers);

		if (!field.hasModifier(Modifiers.STATIC) || !field.hasModifier(Modifiers.FINAL))
		{
			field.getModifiers().addIntModifier(Modifiers.STATIC | Modifiers.FINAL);
			markers.add(Markers.semanticWarning(field.getPosition(), "trait.field.warning", field.getName()));
		}
	}
}
