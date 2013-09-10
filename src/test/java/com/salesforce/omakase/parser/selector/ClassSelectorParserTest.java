/**
 * ADD LICENSE
 */
package com.salesforce.omakase.parser.selector;

import com.google.common.collect.ImmutableList;
import com.salesforce.omakase.ast.selector.ClassSelector;
import com.salesforce.omakase.parser.AbstractParserTest;
import com.salesforce.omakase.parser.ParserException;
import com.salesforce.omakase.test.util.Templates.SourceWithExpectedResult;
import org.junit.Test;

import java.util.List;

import static com.salesforce.omakase.test.util.Templates.withExpectedResult;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for {@link ClassSelectorParser}.
 *
 * @author nmcwilliams
 */
@SuppressWarnings({"JavaDoc", "SpellCheckingInspection"})
public class ClassSelectorParserTest extends AbstractParserTest<ClassSelectorParser> {
    @Override
    public List<String> invalidSources() {
        return ImmutableList.of(
            "#id",
            " .class",
            "cla.ss",
            "#a.class",
            "class");
    }

    @Override
    public List<String> validSources() {
        return ImmutableList.of(
            ".class",
            ".CLASS",
            "._class",
            ".c1ass",
            ".-class",
            "._NAMEname1_aAz234ABCdefafklsjfseufhuise____hfie");
    }

    @Override
    public List<SourceWithExpectedResult<Integer>> validSourcesWithExpectedEndIndex() {
        return ImmutableList.of(
            withExpectedResult(".class .class2", 6),
            withExpectedResult(".class.class2", 6),
            withExpectedResult(".class-abc-abc", 14),
            withExpectedResult(".claz#id", 5));
    }

    @Test
    @Override
    public void matchesExpectedBroadcastContent() {
        List<ParseResult<String>> results = parseWithExpected(
            withExpectedResult(".class", "class"),
            withExpectedResult(".CLASS", "CLASS"),
            withExpectedResult("._clasZ", "_clasZ"),
            withExpectedResult(".-class-abc", "-class-abc"),
            withExpectedResult(".claz1", "claz1"));

        for (ParseResult<String> result : results) {
            ClassSelector selector = result.broadcaster.findOnly(ClassSelector.class).get();
            assertThat(selector.name())
                .describedAs(result.stream.toString())
                .isEqualTo(result.expected);
        }
    }

    @Override
    public boolean allowedToTrimLeadingWhitespace() {
        return false;
    }

    @Test
    public void errorsIfInvalidClassNameAfterDot() {
        exception.expect(ParserException.class);
        exception.expectMessage("expected to find a valid class name");
        parse(".#class");
    }

    @Test
    public void errorsIfDotDot() {
        exception.expect(ParserException.class);
        exception.expectMessage("expected to find a valid class name");
        parse("..class");
    }

    @Test
    public void errorsIfDotNumber() {
        exception.expect(ParserException.class);
        exception.expectMessage("expected to find a valid class name");
        parse(".9class");
    }

    @Test
    public void errorsIfDashNumber() {
        exception.expect(ParserException.class);
        exception.expectMessage("expected to find a valid class name");
        parse(".-9class");
    }

    @Test
    public void errorsIfDashDash() {
        exception.expect(ParserException.class);
        exception.expectMessage("expected to find a valid class name");
        parse(".--class");
    }

    @Test
    public void errorsIfSpace() {
        exception.expect(ParserException.class);
        exception.expectMessage("expected to find a valid class name");
        parse(". class");
    }
}
