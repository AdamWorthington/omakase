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

package com.salesforce.omakase.parser.refiner;

import com.google.common.collect.ImmutableList;
import com.salesforce.omakase.ast.RawSyntax;
import com.salesforce.omakase.ast.atrule.AtRule;
import com.salesforce.omakase.ast.declaration.Declaration;
import com.salesforce.omakase.ast.declaration.value.FunctionValue;
import com.salesforce.omakase.ast.selector.Selector;
import com.salesforce.omakase.broadcast.Broadcaster;
import com.salesforce.omakase.broadcast.QueryableBroadcaster;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for {@link Refiner}.
 *
 * @author nmcwilliams
 */
@SuppressWarnings("JavaDoc")
public class RefinerTest {
    @Test
    public void customAtRuleRefinement() {
        AtRuleStrategy strategy = new AtRuleStrategy();
        Refiner refiner = new Refiner(new QueryableBroadcaster(), ImmutableList.<RefinerStrategy>of(strategy));
        refiner.refine(new AtRule(5, 5, "t", new RawSyntax(1, 1, "t"), new RawSyntax(1, 1, "t"), refiner));
        assertThat(strategy.called).isTrue();
    }

    @Test
    public void testMultipleCustomAtRule() {
        AtRuleStrategyFalse strategy1 = new AtRuleStrategyFalse();
        AtRuleStrategy strategy2 = new AtRuleStrategy();
        Refiner refiner = new Refiner(new QueryableBroadcaster(), ImmutableList.<RefinerStrategy>of(strategy1, strategy2));
        refiner.refine(new AtRule(5, 5, "t", new RawSyntax(1, 1, "t"), new RawSyntax(1, 1, "t"), refiner));
        assertThat(strategy1.called).isTrue();
        assertThat(strategy2.called).isTrue();
    }

    @Test
    public void standardAtRuleRefinement() {
        Refiner refiner = new Refiner(new QueryableBroadcaster());
        AtRule ar = new AtRule(5, 5, "t", new RawSyntax(1, 1, "t"), new RawSyntax(1, 1, "t"), refiner);
        refiner.refine(ar); // no errors
    }

    @Test
    public void customSelectorRefinement() {
        SelectorStrategy strategy = new SelectorStrategy();
        Refiner refiner = new Refiner(new QueryableBroadcaster(), ImmutableList.<RefinerStrategy>of(strategy));
        refiner.refine(new Selector(new RawSyntax(1, 1, "p"), refiner));
        assertThat(strategy.called).isTrue();
    }

    @Test
    public void testMultipleCustomSelector() {
        SelectorStrategyFalse strategy1 = new SelectorStrategyFalse();
        SelectorStrategy strategy2 = new SelectorStrategy();
        Refiner refiner = new Refiner(new QueryableBroadcaster(), ImmutableList.<RefinerStrategy>of(strategy1, strategy2));
        refiner.refine(new Selector(new RawSyntax(1, 1, "p"), refiner));
        assertThat(strategy1.called).isTrue();
        assertThat(strategy2.called).isTrue();
    }

    @Test
    public void standardSelectorRefinement() {
        Refiner refiner = new Refiner(new QueryableBroadcaster());
        Selector selector = new Selector(new RawSyntax(1, 1, "p"), refiner);
        refiner.refine(selector);
        assertThat(selector.isRefined()).isTrue();
    }

    @Test
    public void customDeclarationRefinement() {
        DeclarationStrategy strategy = new DeclarationStrategy();
        Refiner refiner = new Refiner(new QueryableBroadcaster(), ImmutableList.<RefinerStrategy>of(strategy));
        refiner.refine(new Declaration(new RawSyntax(5, 5, "color"), new RawSyntax(5, 5, "red"), refiner));
        assertThat(strategy.called).isTrue();
    }

    @Test
    public void testMultipleCustomDeclaration() {
        DeclarationStrategyFalse strategy1 = new DeclarationStrategyFalse();
        DeclarationStrategy strategy2 = new DeclarationStrategy();
        Refiner refiner = new Refiner(new QueryableBroadcaster(), ImmutableList.<RefinerStrategy>of(strategy1, strategy2));
        refiner.refine(new Declaration(new RawSyntax(5, 5, "color"), new RawSyntax(5, 5, "red"), refiner));
        assertThat(strategy1.called).isTrue();
        assertThat(strategy2.called).isTrue();
    }

    @Test
    public void standardDeclarationRefinement() {
        Refiner refiner = new Refiner(new QueryableBroadcaster());
        Declaration declaration = new Declaration(new RawSyntax(5, 5, "color"), new RawSyntax(5, 5, "red"), refiner);
        refiner.refine(declaration);
    }

    @Test
    public void functionValueRefinement() {
        FunctionValueStrategy strategy = new FunctionValueStrategy();
        Refiner refiner = new Refiner(new QueryableBroadcaster(), ImmutableList.<RefinerStrategy>of(strategy));
        refiner.refine(new FunctionValue(1, 1, "test", "blah", refiner));
        assertThat(strategy.called).isTrue();
    }

    @Test
    public void testMultipleFunctionValue() {
        FunctionValueStrategyFalse strategy1 = new FunctionValueStrategyFalse();
        FunctionValueStrategy strategy2 = new FunctionValueStrategy();
        Refiner refiner = new Refiner(new QueryableBroadcaster(), ImmutableList.<RefinerStrategy>of(strategy1, strategy2));
        refiner.refine(new FunctionValue(1, 1, "test", "blah", refiner));
        assertThat(strategy1.called).isTrue();
        assertThat(strategy2.called).isTrue();
    }

    @Test
    public void standardFunctionValueRefinement() {
        Refiner refiner = new Refiner(new QueryableBroadcaster());
        refiner.refine(new FunctionValue(1, 1, "test", "blah", refiner)); // no errors
    }

    public static final class AtRuleStrategy implements AtRuleRefinerStrategy {
        boolean called;

        @Override
        public boolean refine(AtRule atRule, Broadcaster broadcaster, Refiner refiner) {
            called = true;
            return true;
        }
    }

    public static final class AtRuleStrategyFalse implements AtRuleRefinerStrategy {
        boolean called;

        @Override
        public boolean refine(AtRule atRule, Broadcaster broadcaster, Refiner refiner) {
            called = true;
            return false;
        }
    }

    public static final class SelectorStrategy implements SelectorRefinerStrategy {
        boolean called;

        @Override
        public boolean refine(Selector selector, Broadcaster broadcaster, Refiner refiner) {
            called = true;
            return true;
        }
    }

    public static final class SelectorStrategyFalse implements SelectorRefinerStrategy {
        boolean called;

        @Override
        public boolean refine(Selector selector, Broadcaster broadcaster, Refiner refiner) {
            called = true;
            return false;
        }
    }

    public static final class DeclarationStrategy implements DeclarationRefinerStrategy {
        boolean called;

        @Override
        public boolean refine(Declaration declaration, Broadcaster broadcaster, Refiner refiner) {
            called = true;
            return true;
        }
    }

    public static final class DeclarationStrategyFalse implements DeclarationRefinerStrategy {
        boolean called;

        @Override
        public boolean refine(Declaration declaration, Broadcaster broadcaster, Refiner refiner) {
            called = true;
            return false;
        }
    }

    public static final class FunctionValueStrategy implements FunctionValueRefinerStrategy {
        boolean called;

        @Override
        public boolean refine(FunctionValue functionValue, Broadcaster broadcaster, Refiner refiner) {
            called = true;
            return true;
        }
    }

    public static final class FunctionValueStrategyFalse implements FunctionValueRefinerStrategy {
        boolean called;

        @Override
        public boolean refine(FunctionValue functionValue, Broadcaster broadcaster, Refiner refiner) {
            called = true;
            return false;
        }
    }
}
