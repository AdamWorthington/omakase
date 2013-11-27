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

package com.salesforce.omakase.broadcast.emitter;

import com.google.common.base.Objects;
import com.salesforce.omakase.error.ErrorManager;
import com.salesforce.omakase.util.As;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Metadata class to wrap the details around a subscription method. For internal use only.
 *
 * @author nmcwilliams
 */
final class Subscription implements Comparable<Subscription> {
    @SuppressWarnings("StaticNonFinalField") private static int counter;

    private final SubscriptionPhase phase;
    private final Object subscriber;
    private final Method method;
    private final Integer number;

    Subscription(SubscriptionPhase phase, Object subscriber, Method method) {
        this.phase = phase;
        this.subscriber = subscriber;
        this.method = method;
        this.number = ++counter;
    }

    /**
     * Gets the {@link SubscriptionPhase} this subscription takes part in.
     *
     * @return The {@link SubscriptionPhase}.
     */
    public SubscriptionPhase phase() {
        return phase;
    }

    /**
     * Gets the method for the subscription.
     *
     * @return The method.
     */
    public Method method() {
        return method;
    }

    /**
     * Invokes the subscription method.
     *
     * @param event
     *     The event object (e.g., syntax instance).
     * @param em
     *     The {@link ErrorManager} instance to use for validation methods.
     */
    public void deliver(Object event, ErrorManager em) {
        try {
            if (phase == SubscriptionPhase.VALIDATE) {
                method.invoke(subscriber, event, em);
            } else {
                method.invoke(subscriber, event);
            }
        } catch (IllegalArgumentException e) {
            throw new SubscriptionException("Invalid arguments for subscription method", e);
        } catch (IllegalAccessException e) {
            throw new SubscriptionException("Subscription method is not accessible", e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) throw (RuntimeException)e.getCause();
            throw new SubscriptionException("A problem was encountered while invoking the subscription method", e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(subscriber, method);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Subscription) {
            Subscription other = (Subscription)object;
            // must be same instance of the same class (identity)
            // this keeps plugins that are registered twice only stored once in the Emitter
            return subscriber == other.subscriber && Objects.equal(this.method, other.method);
        }
        return false;
    }

    @Override
    public String toString() {
        return As.string(this).fields().toString();
    }

    @Override
    public int compareTo(Subscription o) {
        return number.compareTo(o.number);
    }
}
