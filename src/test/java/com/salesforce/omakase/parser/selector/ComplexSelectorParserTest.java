/**
 * ADD LICENSE
 */
package com.salesforce.omakase.parser.selector;

import static com.salesforce.omakase.util.Templates.withExpectedResult;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.salesforce.omakase.Message;
import com.salesforce.omakase.ast.Syntax;
import com.salesforce.omakase.ast.selector.*;
import com.salesforce.omakase.parser.AbstractParserTest;
import com.salesforce.omakase.parser.ParserException;
import com.salesforce.omakase.util.Templates.SourceWithExpectedResult;

/**
 * Unit tests for {@link ComplexSelectorParser}.
 * 
 * @author nmcwilliams
 */
@SuppressWarnings("javadoc")
public class ComplexSelectorParserTest extends AbstractParserTest<ComplexSelectorParser> {

    @Override
    public List<String> invalidSources() {
        return ImmutableList.of("$anc", "    ", "\n\n\n", "");
    }

    @Override
    public List<String> validSources() {
        // QE_TEST add more use cases
        return ImmutableList.of(
            "#id",
            ".class",
            "p",
            "a:link",
            "a:hover",
            "div:hover",
            "::selection",
            ":hover",
            "*",
            "*#id",
            "*.class",
            "*:hover",
            "*:before",
            "*::before",
            ".class1.class2.class3.class4",
            "#id#id2#id3",
            // "p[foo]",
            // "a[foo=\"bar\"]",
            // "a[foo~=\"bar\"]",
            // "a[foo^=\"bar\"]",
            // "a[foo$=\"bar\"]",
            // "a[foo*=\"bar\"]",
            // "a[foo|=\"en\"]",
            // "input[type=\"search\"]::-webkit-search-cancel-button",
            ":root",
            " #id.class:first-child",
            ".page .home > .child #id:hover .button .inner + span",
            ".page .home > .child #id:hover .button .inner + span",
            "      ul.gallery li:last-child",
            ".panel-primary > .panel-heading",
            ".ABCCLASS P",
            "  .col-lg-push-0",
            ".table caption + thead tr:first-child th",
            ".table > thead > tr > td.active",
            ".table-hover > tbody > tr > td.warning:hover",
            "  .table-responsive  >  .table-bordered > thead > tr > th:last-child",
            // "fieldset[disabled] input[type=\"checkbox\"]",
            // "fieldset[disabled] .btn-info:focus",
            // ".btn-group > .btn:first-child:not(:last-child):not(.dropdown-toggle)",
            // ".btn-group > .btn-group:not(:first-child):not(:last-child) > .btn",
            ".input-group-btn:first-child > .dropdown-toggle"
            );
    }

    @Override
    public List<SourceWithExpectedResult<Integer>> validSourcesWithExpectedEndIndex() {
        return ImmutableList.of(
            withExpectedResult(".class .class2", 14),
            withExpectedResult(".class.class2", 13),
            withExpectedResult(".class-abc-abc", 14),
            withExpectedResult(".claz#id", 8));
    }

    @Override
    public boolean allowedToTrimLeadingWhitespace() {
        return true;
    }

    @Test
    @Override
    public void matchesExpectedBroadcastCount() {
        List<ParseResult<Integer>> results = parseWithExpected(ImmutableList.of(
            withExpectedResult("#id", 1),
            withExpectedResult(".class1.class2.class3.class4", 4),
            withExpectedResult("#id.class:first-child", 3),
            withExpectedResult(".page .home > .child #id:hover .button .inner + span", 14),
            withExpectedResult(".table-hover  >  tbody > tr > td.warning:hover", 9),
            withExpectedResult(".ABCCLASS P", 3),
            withExpectedResult("div:hover", 2),
            withExpectedResult(".input-group-btn:first-child > .dropdown-toggle", 4),
            withExpectedResult("      ul.gallery li:last-child", 5),
            withExpectedResult("*::before", 2)));

        for (ParseResult<Integer> result : results) {
            assertThat(result.broadcasted)
                .describedAs(result.stream.toString())
                .hasSize(result.expected);
        }
    }

    @Test
    @Override
    public void matchesExpectedBroadcastContent() {
        GenericParseResult result = Iterables
            .getOnlyElement(parse("*.page .home > .child #id:hover .button .inner + span:before"));

        List<Syntax> broadcasted = result.broadcasted;
        assertThat(broadcasted.get(0)).isInstanceOf(UniversalSelector.class);
        assertThat(broadcasted.get(1)).isInstanceOf(ClassSelector.class);
        assertThat(broadcasted.get(2)).isInstanceOf(Combinator.class);
        assertThat(broadcasted.get(3)).isInstanceOf(ClassSelector.class);
        assertThat(broadcasted.get(4)).isInstanceOf(Combinator.class);
        assertThat(broadcasted.get(5)).isInstanceOf(ClassSelector.class);
        assertThat(broadcasted.get(6)).isInstanceOf(Combinator.class);
        assertThat(broadcasted.get(7)).isInstanceOf(IdSelector.class);
        assertThat(broadcasted.get(8)).isInstanceOf(PseudoClassSelector.class);
        assertThat(broadcasted.get(9)).isInstanceOf(Combinator.class);
        assertThat(broadcasted.get(10)).isInstanceOf(ClassSelector.class);
        assertThat(broadcasted.get(11)).isInstanceOf(Combinator.class);
        assertThat(broadcasted.get(12)).isInstanceOf(ClassSelector.class);
        assertThat(broadcasted.get(13)).isInstanceOf(Combinator.class);
        assertThat(broadcasted.get(14)).isInstanceOf(TypeSelector.class);
        assertThat(broadcasted.get(15)).isInstanceOf(PseudoElementSelector.class);
    }

    @Test
    public void mustParseFullStream() {
        List<GenericParseResult> parse = parse(ImmutableList.of(
            "#id",
            ".class",
            "p",
            "a:link",
            "a:hover",
            "div:hover",
            "::selection",
            ":hover",
            "*",
            "*#id",
            "*.class",
            "*:hover",
            "*:before",
            "*::before",
            ".class1.class2.class3.class4",
            "#id#id2#id3",
            // "p[foo]",
            // "a[foo=\"bar\"]",
            // "a[foo~=\"bar\"]",
            // "a[foo^=\"bar\"]",
            // "a[foo$=\"bar\"]",
            // "a[foo*=\"bar\"]",
            // "a[foo|=\"en\"]",
            // "input[type=\"search\"]::-webkit-search-cancel-button",
            ":root",
            " #id.class:first-child",
            ".page .home > .child #id:hover .button .inner + span",
            ".page .home > .child #id:hover .button .inner + span",
            "      ul.gallery li:last-child",
            ".panel-primary > .panel-heading",
            ".ABCCLASS P",
            "  .col-lg-push-0",
            ".table caption + thead tr:first-child th",
            ".table > thead > tr > td.active",
            ".table-hover > tbody > tr > td.warning:hover",
            "  .table-responsive  >  .table-bordered > thead > tr > th:last-child",
            // "fieldset[disabled] input[type=\"checkbox\"]",
            // "fieldset[disabled] .btn-info:focus",
            // ".btn-group > .btn:first-child:not(:last-child):not(.dropdown-toggle)",
            // ".btn-group > .btn-group:not(:first-child):not(:last-child) > .btn",
            ".input-group-btn:first-child > .dropdown-toggle"
            ));

        for (GenericParseResult result : parse) {
            assertThat(result.stream.eof()).isTrue();
        }
    }

    @Test
    public void errorIfUniversalNotLast() {
        exception.expect(ParserException.class);
        exception.expectMessage(Message.NAME_SELECTORS_NOT_ALLOWED.message());
        parse(".class*");
    }

    @Test
    public void errorsIfNestedComments() {
        exception.expect(ParserException.class);
        exception.expectMessage(Message.COMMENTS_NOT_ALLOWED.message());
        parse(".class/*test*/.class");
    }
}
