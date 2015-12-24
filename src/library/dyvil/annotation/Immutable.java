package dyvil.annotation;

import dyvil.annotation.Mutating;
import dyvil.collection.*;

/**
 * Marker interface that marks types as <i>immutable</i>. This interface is
 * primarily designed to be used in conjunction with the {@link Mutating}
 * annotation, which allows compile-time checking mutating accesses to immutable
 * types, especially {@link Collection collections}.
 *
 * @author Clashsoft
 * @version 1.0
 * @see Mutating
 * @see Collection
 * @see ImmutableCollection
 * @see ImmutableList
 * @see ImmutableMap
 * @see ImmutableMatrix
 */
public @interface Immutable
{
}
