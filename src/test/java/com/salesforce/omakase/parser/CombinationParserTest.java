/**
 * ADD LICENSE
 */
package com.salesforce.omakase.parser;

import com.salesforce.omakase.broadcaster.QueryableBroadcaster;
import com.salesforce.omakase.parser.declaration.KeywordValueParser;
import com.salesforce.omakase.parser.declaration.NumericalValueParser;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for {@link CombinationParser}.
 *
 * @author nmcwilliams
 */
@SuppressWarnings("JavaDoc")
public class CombinationParserTest {
    @Test
    public void parsesEither() {
        CombinationParser c = new CombinationParser(new KeywordValueParser(), new NumericalValueParser());
        assertThat(c.parse(new Stream("red"), new QueryableBroadcaster())).isTrue();
        assertThat(c.parse(new Stream("3px"), new QueryableBroadcaster())).isTrue();
        assertThat(c.parse(new Stream("!"), new QueryableBroadcaster())).isFalse();
    }
}
