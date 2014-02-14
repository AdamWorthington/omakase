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

package com.salesforce.omakase.ast.declaration;

import com.google.common.base.Optional;
import com.salesforce.omakase.broadcast.annotation.Description;
import com.salesforce.omakase.broadcast.annotation.Subscribable;

import static com.salesforce.omakase.broadcast.BroadcastRequirement.REFINED_DECLARATION;

/**
 * A {@link PropertyValueMember} within a {@link PropertyValue} representing a single segment of the {@link Declaration} value.
 * <p/>
 * For example, in <code>margin: 3px 5px</code>, there are two terms, <code>3px</code> and <code>5px</code>.
 *
 * @author nmcwilliams
 */
@Subscribable
@Description(value = "a single segment of a property value", broadcasted = REFINED_DECLARATION)
public interface Term extends PropertyValueMember {
    Optional<Declaration> declaration();
}
