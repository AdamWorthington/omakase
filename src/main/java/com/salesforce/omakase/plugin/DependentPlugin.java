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

package com.salesforce.omakase.plugin;

import com.salesforce.omakase.PluginRegistry;
import com.salesforce.omakase.plugin.basic.AutoRefiner;
import com.salesforce.omakase.plugin.basic.SyntaxTree;

/**
 * A {@link Plugin} that have dependencies on other {@link Plugin}s.
 *
 * @author nmcwilliams
 */
public interface DependentPlugin extends Plugin {
    /**
     * This method will be called just before source code processing begins.
     * <p/>
     * The main purpose of this method is to allow you to specify a dependency on and/or configure another {@link Plugin}. In many
     * cases a dependency on {@link SyntaxTree} or {@link AutoRefiner} is required. See the comments on {@link Plugin} for more
     * details.
     * <p/>
     * The order in which this will be invoked (between plugins) is the same order that the {@link Plugin} was registered.
     *
     * @param registry
     *     The {@link PluginRegistry} instance.
     *
     * @see PluginRegistry#require(Class)
     * @see PluginRegistry#require(Class, com.google.common.base.Supplier)
     * @see PluginRegistry#retrieve(Class)
     */
    void dependencies(PluginRegistry registry);
}
