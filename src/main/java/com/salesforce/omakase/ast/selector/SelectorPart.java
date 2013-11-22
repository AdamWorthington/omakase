/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.salesforce.omakase.ast.selector;

import com.google.common.base.Optional;
import com.salesforce.omakase.ast.collection.Groupable;
import com.salesforce.omakase.broadcast.annotation.Description;
import com.salesforce.omakase.broadcast.annotation.Subscribable;

import static com.salesforce.omakase.broadcast.BroadcastRequirement.REFINED_SELECTOR;

/**
 * A {@link SimpleSelector}, {@link PseudoElementSelector}, or {@link Combinator}.
 *
 * @author nmcwilliams
 */
@Subscribable
@Description(value = "group interface for all selector segments", broadcasted = REFINED_SELECTOR)
public interface SelectorPart extends Groupable<Selector, SelectorPart> {
    /**
     * Gets the parent {@link Selector} instance. This is equivalent to {@link #parent()}.
     * <p/>
     * Take this CSS snippet for example:
     * <p/>
     * {@code .class > .class2 #id, p a}
     * <p/>
     * There are two {@link Selector}s, The first being {@code .class > .class2 #id}, the second being {@code p a}.
     * <p/>
     * For the {@link SelectorPart} {@code #id}, which is an {@link IdSelector}, calling this method will return the first {@link
     * Selector} (with {@code .class > .class2 #id}).
     *
     * @return The parent, or {@link Optional#absent()} if the parent is not specified.
     */
    Optional<Selector> parentSelector();

    /**
     * Gets the {@link SelectorPartType} of this {@link SelectorPart}. This is a delegate to {@link Groupable#parent()}.
     *
     * @return The {@link SelectorPartType}.
     */
    SelectorPartType type();

    /**
     * Gets the non-combinator {@link SelectorPart}s contiguous to this one.
     * <p/>
     * If this part is a {@link Combinator} then this method will return a collection of one, containing only this {@link
     * Combinator} instance itself.
     * <p/>
     * If this part is <em>not</em> a {@link Combinator} then this method will return all preceding and subsequent {@link
     * SelectorPart}s up until the first encountered {@link Combinator}. In other words, this will return all parts matching a
     * single element, such as multiple class selectors.
     * <p/>
     * For example, in this selector:
     * <pre><code>
     * .test1.test2#test3 .testA.testB#testC
     * </code></pre>
     * <p/>
     * If <code>this</code> is ".test2", this method will return the ".test1", ".test2", and "#test3" parts.
     *
     * @return The adjoining {@link SelectorPart}s.
     */
    Iterable<SelectorPart> adjoining();
}
