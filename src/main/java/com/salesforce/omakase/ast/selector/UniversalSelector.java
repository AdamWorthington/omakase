/**
 * ADD LICENSE
 */
package com.salesforce.omakase.ast.selector;

import static com.salesforce.omakase.emitter.SubscribableRequirement.REFINED_SELECTOR;

import java.io.IOException;

import com.salesforce.omakase.As;
import com.salesforce.omakase.ast.collection.AbstractGroupable;
import com.salesforce.omakase.emitter.Description;
import com.salesforce.omakase.emitter.Subscribable;
import com.salesforce.omakase.writer.StyleAppendable;
import com.salesforce.omakase.writer.StyleWriter;

/**
 * TESTME Represents the CSS universal selector, i.e., "*".
 * 
 * @author nmcwilliams
 */
@Subscribable
@Description(value = "universal selector segment", broadcasted = REFINED_SELECTOR)
public class UniversalSelector extends AbstractGroupable<SelectorPart> implements SelectorPart {
    /**
     * Constructs a new {@link UniversalSelector} instance.
     * 
     * @param line
     *            The line number.
     * @param column
     *            The column number.
     */
    public UniversalSelector(int line, int column) {
        super(line, column);
    }

    /**
     * TODO
     */
    public UniversalSelector() {
        super();
    }

    @Override
    public boolean isSelector() {
        return true;
    }

    @Override
    public boolean isCombinator() {
        return false;
    }

    @Override
    public SelectorPartType type() {
        return SelectorPartType.UNIVERSAL_SELECTOR;
    }

    @Override
    protected SelectorPart self() {
        return this;
    }

    @Override
    public void write(StyleWriter writer, StyleAppendable appendable) throws IOException {
        appendable.append('*');
    }

    @Override
    public String toString() {
        return As.string(this)
            .indent()
            .add("position", super.toString())
            .toString();
    }
}
