/*
 * Copyright (C) 2014 karthik
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package karthik.regex;

import karthik.regex.MatcherException;
import karthik.regex.Pattern;

/**
 *
 * @author karthik
 */
class BoundaryRegexToken extends RegexToken {

    private boolean multiline_enabled;

    BoundaryRegexToken(RegexTokenNames itemType, int flags) {
        super(itemType, flags);
        multiline_enabled = ((flags & Pattern.MULTILINE) != 0);
    }

    public boolean isBoundaryOrLookaround() {
        return true;
    }

    private boolean isWordChar(Character c) {
        if (c == null)
            return false;
        return (Character.isLetterOrDigit(c) || (c == '_'));
    }

    public boolean matches(final CharSequence search_string, final int pos) throws MatcherException {
        switch (getType()) {
            case WORD_BOUNDARY:
                return matchesWordBoundary(search_string, pos);
            case NON_WORD_BOUNDARY:
                return !matchesWordBoundary(search_string, pos);
            case STRING_START:
                return (pos == 0);
            case STRING_END:
                return (pos == search_string.length());
            case JAVA_Z_STRING_END:
                return matchesJavaZStringEnd(search_string, pos);
            case LINE_START:
                return matchesLineStart(search_string, pos);
            case LINE_END:
                return matchesLineEnd(search_string, pos);
            default:
                return false;
        }
    }

    private boolean matchesWordBoundary(final CharSequence search_string,
        final int pos) {

        boolean wb;

        Character current = (pos < search_string.length()) ? search_string.
            charAt(pos) : null;
        Character prev = (pos > 0) ? search_string.charAt(pos - 1) : null;

        // start of string and end of string do not count as word boundaries
        if ((current == null) || (prev == null))
            return false;

        // returns XOR of current and prev types        
        wb = isWordChar(current) ^ isWordChar(prev);
        return wb;
    }

    private boolean matchesLineStart(final CharSequence search_string,
        final int pos) {

        Character prev = (pos > 0) ? search_string.charAt(pos - 1) : null;

        // start of string counts as start of line
        if ((prev == null) || (multiline_enabled && (prev == '\n')))
            return true;
        else
            return false;
    }

    private boolean matchesLineEnd(final CharSequence search_string,
        final int pos) {

        Character next = (pos < search_string.length()) ? search_string.charAt(
            pos) : null;

        // end of string counts as end of line
        if ((next == null) || (multiline_enabled && (next == '\n')))
            return true;
        else
            return false;
    }

    private boolean matchesJavaZStringEnd(final CharSequence search_string,
        final int pos) {

        // matches \Z boundary character in Java - either String end or final line terminator
        Character next = (pos < search_string.length()) ? search_string.charAt(
            pos) : null;

        // end of string matches
        if (next == null)
            return true;

        // final line terminator also matches for this boundary matcher
        if ((next == '\n') && (pos == search_string.length() - 1))
            return true;

        return false;
    }

    public String toString() {
        return getType().name() + " ";
    }
}
