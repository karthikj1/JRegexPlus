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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import karthik.regex.Matchable;
import karthik.regex.MatcherException;
import karthik.regex.Pattern;

/**
 *
 * @author karthik
 */
class RegexToken implements Matchable {

    public enum OpTypes {

        QUANTIFIER, BINARY, NONE;
    }
    protected char c;
    protected RegexTokenNames type;
    protected List<Integer> groupIDList = new ArrayList<>();
    protected int flags;

    protected RegexToken() {
    }

    RegexToken(RegexTokenNames itemType, char ch, int flags) {
        this(itemType, flags);
        c = ch;
    }

    RegexToken(RegexTokenNames itemType, int flags) {
        type = itemType;
        this.flags = flags;
    }

    public List<Integer> getGroupID() {
        return groupIDList;
    }

    List<Integer> addGroupIDList(List<Integer> newGroupID) {
        groupIDList = newGroupID;

        return groupIDList;
    }

    public RegexTokenNames getType() {
        return type;
    }

    public OpTypes getOpType() {
        switch (getType()) {
            case STAR:
            case PLUS:
            case QUESTION:
            case BRACE:
            case LAZY_STAR:
            case LAZY_PLUS:
            case LAZY_QUESTION:
            case LAZY_BRACE:
                return OpTypes.QUANTIFIER;
            case OR:
            case AND:
                return OpTypes.BINARY;
            default:
                return OpTypes.NONE;
        }
    }

    public boolean isEpsilon() {
        return (type == RegexTokenNames.EPSILON);
    }

    public boolean isBoundaryOrLookaround() {
        // is_boundary defaults to false unless overridden by subclass
        return false;
    }

    public boolean isQuantifier() {
        return (getOpType() == OpTypes.QUANTIFIER);
    }

    public boolean isBackReference() {
        // isBackReference defaults to false unless overridden by subclass
        return false;
    }

    public int getFlags() {
        return flags;
    }

    public boolean matches(final CharSequence search_string, final int pos) throws MatcherException {
        char current_char = search_string.charAt(pos);
        switch (getType()) {
            case CHAR:
                return match_char(current_char);
            case DIGIT:
                return Character.isDigit(current_char);
            case WHITESPACE:
                return Character.isWhitespace(current_char);
            case WORD:
                return (Character.isLetterOrDigit(current_char) || current_char == '_');
            case NONDIGIT:
                return !Character.isDigit(current_char);
            case NONWHITESPACE:
                return !Character.isWhitespace(current_char);
            case NONWORD:
                return !(Character.isLetterOrDigit(current_char) || current_char == '_');
            case DOT:
                return match_dot(current_char);
            case TAB:
                return (current_char == '\t');
            case NEWLINE:
                return (current_char == '\n');
            case FORMFEED:
                return (current_char == '\f');
            case CARR_RETURN:
                return (current_char == '\r');
            case EPSILON:
            default:
                return false;
        }

    }

    private boolean match_char(char current_char) {
        if ((flags & Pattern.CASE_INSENSITIVE) == 0) // case-sensitive match
            return (current_char == c);
        // else do case-insensitive match
        return (Character.toLowerCase(c) == Character.toLowerCase(current_char));
    }

    private boolean match_dot(char current_char) {
        if ((flags & Pattern.DOTALL) == 0) // DOT does not match newline
            return (current_char != '\n');
        // otherwise DOT matches anything
        return true;
    }

    protected String groupID_toString() {
        if (groupIDList != null)
            return "GROUPID: " + Arrays.toString(groupIDList.toArray());
        else
            return "GROUPID: null";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("");
        if (getType() == RegexTokenNames.CHAR)
            sb.append(getType().name() + ":" + c + " ");
        else
            sb.append(getType().name() + " ");
        sb.append(groupID_toString() + " ");
        return sb.toString();
    }
}
