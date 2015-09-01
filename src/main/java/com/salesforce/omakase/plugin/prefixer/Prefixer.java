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

package com.salesforce.omakase.plugin.prefixer;

import com.salesforce.omakase.PluginRegistry;
import com.salesforce.omakase.SupportMatrix;
import com.salesforce.omakase.ast.atrule.AtRule;
import com.salesforce.omakase.ast.declaration.Declaration;
import com.salesforce.omakase.ast.declaration.FunctionValue;
import com.salesforce.omakase.ast.declaration.KeywordValue;
import com.salesforce.omakase.ast.selector.PseudoElementSelector;
import com.salesforce.omakase.broadcast.annotation.Rework;
import com.salesforce.omakase.plugin.DependentPlugin;
import com.salesforce.omakase.plugin.basic.AutoRefiner;
import com.salesforce.omakase.plugin.validator.StandardValidation;

import static com.salesforce.omakase.data.Browser.*;

/**
 * This experimental plugin automagically handles vendor prefixing of css property names, function values, at-rules and
 * selectors.
 * <p/>
 * Which vendor prefixes are actually used depends of the browser versions that you want to support. You can manually specify
 * specific browser versions, last N versions, or just the latest browser version by utilizing the {@link SupportMatrix} returned
 * by the {@link #support()} method. In most cases however the default set of browser versions to support is adequate, as achieved
 * by using the {@link #defaultBrowserSupport()} constructor method.
 * <p/>
 * Browser and prefix data is seamlessly handled via updates from the caniuse.com data. To update to the latest data, see the
 * readme file titled "Scripts".
 * <p/>
 * This plugin integrates well with existing CSS. If all required prefixes are already present then nothing will be changed by
 * default. You can optionally have unnecessary prefixes removed via the {@link #prune(boolean)} method. You can also optionally
 * have all existing vendor-prefixed declarations rearranged to be <em>before</em> the unprefixed version via the {@link
 * #rearrange(boolean)} method.
 * <p/>
 * This doesn't automatically refine declarations or other refinables to check if they might need prefixes. This will only handle
 * prefixes if declarations, at-rules, and selectors have already been refined. If you want to ensure that every thing gets
 * checked then register an {@link AutoRefiner}, and call {@link AutoRefiner#all()}, or ensure that a {@link StandardValidation}
 * plugin instance is registered. Both of these must be registered before this plugin. See the main readme doc for more
 * information.
 * <p/>
 * <b>Important:</b> This is an <em>Experimental</em> plugin. Some rare and uncommon usages of prefixed values, property names,
 * selectors or at-rules may not currently work correctly. Please check the list of what's actually supported in the readme or
 * {@code prefix-info.yaml} file.
 * <p/>
 * Even if a prefix is supported, there may still be a few edge cases where a particular prefixable value is not automatically
 * handled. If you are using bleeding-edge syntax or prefixable features in a non-typical way then please double check the CSS
 * output for the proper behavior.
 * <p/>
 * Also note that some very old browser versions utilizing non-standard syntax may not currently be handled. For example, the
 * legacy linear-gradient syntax is not currently handled because several browser versions have passed since it was last used.
 * <p/>
 * Example usage:
 * <pre><code>
 *     Prefixer prefixing = Prefixer.customBrowserSupport();
 *     prefixing.support().last(Browser.FIREFOX, 3);
 *     prefixing.support().last(Browser.CHROME, 2);
 *     prefixing.support().latest(Browser.SAFARI);
 *     prefixing.support().browser(Browser.IE, 9);
 *     prefixing.support().browser(Browser.IE, 10);
 *     prefixing.prune(true);
 *     prefixing.rearrange(true);
 * <p/>
 *     AutoRefiner refinement = new AutoRefiner().all();
 * <p/>
 *     Omakase.source(cssSource).use(refinement).use(prefixing).process();
 * </code></pre>
 * <p/>
 * In some cases at-rules scoped by a prefixed name may have non-applicable prefixes added. For example:
 * <pre><code>
 * &#64;keyframes animation {
 *     from { transform: rotate(0deg) }
 *     from { transform: rotate(360deg) }
 * }
 * </code></pre>
 * after prefixing could result in
 * <p/>
 * <pre><code>
 * &#64;-webkit-keyframes animation {
 *     from { -webkit-transform: rotate(0deg); -ms-transform: rotate(0deg); transform: rotate(0deg) }
 *     from { -webkit-transform: rotate(360deg); -ms-transform: rotate(360deg); transform: rotate(360deg) }
 * }
 * </code></pre>
 * <p/>
 * Notice the {@code -ms-transform} is most likely unnecessary as it is within a {@code -webkit-} prefixed at-rule. The {@link
 * PrefixCleaner} plugin can be utilized to remove such prefixed declarations inside of prefixed at-rules. The plugin must be
 * registered <em>after</em> this one:
 * <pre><code>
 *     Omakase.source(input)
 *         .add(Prefixer.defaultBrowserSupport())
 *         .add(PrefixPruner.prunePrefixedAtRules())
 *         .process()
 * </code></pre>
 *
 * @author nmcwilliams
 */
public final class Prefixer implements DependentPlugin {
    // at-rule handlers
    private static final Handler<AtRule> STANDARD_AT_RULE = new HandleAtRule();

    // pseudo element selector handlers
    private static final Handler<PseudoElementSelector> STANDARD_PSEUDO = new HandlePseudoElement();
    private static final Handler<PseudoElementSelector> PLACEHOLDER = new HandlePlaceholder();

    // declaration handlers
    private static final Handler<Declaration> STANDARD_PROPERTY = new HandleProperty();
    private static final Handler<Declaration> TRANSITION = new HandleTransition();
    private static final Handler<Declaration> FLEX_ORDER = new HandleFlexOrder();
    private static final Handler<Declaration> FLEX_WRAP = new HandleFlexWrap();

    // keyword handlers
    private static final Handler<KeywordValue> FLEX = new HandleFlexValue();

    // function handlers
    private static final Handler<FunctionValue> STANDARD_FUNCTION = new HandleFunction();

    private final SupportMatrix support;
    private boolean rearrange;
    private boolean prune;

    /** private constructor -- use one of the constructor methods to create instances */
    private Prefixer(SupportMatrix support) {
        this.support = support == null ? new SupportMatrix() : support;
    }

    /**
     * Gets the {@link SupportMatrix} instance which can be used to indicate which browser versions are supported.
     *
     * @return The {@link SupportMatrix} instance.
     */
    public SupportMatrix support() {
        return support;
    }

    /**
     * Whether this plugin should rearranged the declarations/at-rules so that the unprefixed version always comes last. Default
     * is false.
     * <p/>
     * This only works for at-rules if the at-rules are contiguous.
     *
     * @param rearrange
     *     Whether we should moved the unprefixed version last.
     *
     * @return this, for chaining.
     */
    public Prefixer rearrange(boolean rearrange) {
        this.rearrange = rearrange;
        return this;
    }

    /**
     * Gets whether prefixed declarations can be rearranged.
     *
     * @return True if prefixed declarations can be rearrange.
     */
    public boolean rearrange() {
        return rearrange;
    }

    /**
     * Whether we should remove prefixed declarations/at-rules if they are not required for the supported browser versions.
     * Default is false.
     * <p/>
     * This only works for at-rules if the at-rules are contiguous.
     *
     * @param prune
     *     Whether we should remove unnecessary prefixed declarations.
     *
     * @return this, for chaining.
     */
    public Prefixer prune(boolean prune) {
        this.prune = prune;
        return this;
    }

    /**
     * Gets whether unnecessary prefixed declarations can be pruned.
     *
     * @return If unnecessary prefixed declarations can be pruned.
     */
    public boolean prune() {
        return prune;
    }

    @Override
    public void dependencies(PluginRegistry registry) {
        if (registry.retrieve(PrefixCleaner.class).isPresent()) {
            String msg = "The %s plugin should be registered AFTER the %s plugin";
            throw new IllegalStateException(String.format(msg, PrefixCleaner.class.getSimpleName(), Prefixer.class.getSimpleName()));
        }
    }

    /**
     * Run the given list of prefix handlers on the instance in order. If a handler specifies that it is completely processed the
     * prefixes then subsequent handlers will not be run.
     *
     * @param instance
     *     The syntax unit.
     * @param handlers
     *     The prefix handlers.
     * @param <T>
     *     The type of syntax unit.
     */
    @SafeVarargs
    private final <T> void run(T instance, Handler<T>... handlers) {
        boolean finished = false;
        for (Handler<T> handler : handlers) {
            finished = handler.handle(instance, rearrange, prune, support);
            if (finished) return;
        }
    }

    /**
     * Subscription method - do not invoke directly.
     *
     * @param declaration
     *     The declaration instance.
     */
    @Rework
    public void declaration(Declaration declaration) {
        // don't automatically trigger refinement on every declaration just to check if a prefix is needed.
        if (!declaration.isRefined() || declaration.isPrefixed()) return; // skip stuff already prefixed
        run(declaration, TRANSITION, FLEX_ORDER, FLEX_WRAP, STANDARD_PROPERTY);
    }

    /**
     * Subscription method - do not invoke directly.
     *
     * @param function
     *     The function instance.
     */
    @Rework
    public void function(FunctionValue function) {
        run(function, STANDARD_FUNCTION);
    }

    /**
     * Subscription method - do not invoke directly.
     *
     * @param keyword
     *     The keyword instance.
     */
    @Rework
    public void keyword(KeywordValue keyword) {
        run(keyword, FLEX);
    }

    /**
     * Subscription method - do not invoke directly.
     *
     * @param atRule
     *     The atRule instance.
     */
    @Rework
    public void atRule(AtRule atRule) {
        // don't automatically trigger refinement on every at rule just to check if a prefix is needed.
        if (!atRule.isRefined() || atRule.name().charAt(0) == '-') return; // skip stuff already prefixed
        run(atRule, STANDARD_AT_RULE);
    }

    /**
     * Subscription method - do not invoke directly.
     *
     * @param selector
     *     The selector instance.
     */
    @Rework
    public void pseudoElementSelector(PseudoElementSelector selector) {
        if (selector.name().charAt(0) == '-') return; // skip stuff already prefixed
        run(selector, PLACEHOLDER, STANDARD_PSEUDO);
    }

    /**
     * Creates a new instance of the {@link Prefixer} plugin with default browser version support levels: IE7+, latest versions of
     * Safari, IE Mobile and Opera Mini, last 5 versions of Firefox and Chrome, last 4 versions of IOS Safari and last 3 versions
     * of Android Browser.
     *
     * @return The new {@link Prefixer} instance.
     *
     * @see #rearrange(boolean)
     * @see #prune(boolean)
     */
    public static Prefixer defaultBrowserSupport() {
        SupportMatrix support = new SupportMatrix()
            .last(IOS_SAFARI, 4)
            .last(FIREFOX, 5)
            .last(ANDROID, 3)
            .last(CHROME, 5)
            .browser(IE, 7)
            .browser(IE, 8)
            .browser(IE, 9)
            .browser(IE, 10)
            .browser(IE, 11)
            .latest(SAFARI)
            .latest(IE_MOBILE)
            .latest(OPERA_MINI);

        return new Prefixer(support);
    }

    /**
     * Creates a new instance of the {@link Prefixer} plugin with no default browser version support. Specify which versions to
     * support via the {@link #support()} method.
     *
     * @return The new {@link Prefixer} instance.
     *
     * @see #rearrange(boolean)
     * @see #prune(boolean)
     */
    public static Prefixer customBrowserSupport() {
        return new Prefixer(null);
    }

    /**
     * Creates a new instance of the {@link Prefixer} plugin using the given {@link SupportMatrix}.
     *
     * @param support
     *     Use this {@link SupportMatrix} instance.
     *
     * @return The new {@link Prefixer} instance.
     *
     * @see #rearrange(boolean)
     * @see #prune(boolean)
     */
    public static Prefixer customBrowserSupport(SupportMatrix support) {
        return new Prefixer(support);
    }
}
