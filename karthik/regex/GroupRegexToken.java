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

import karthik.regex.ParserException;
import karthik.regex.Pattern;
import karthik.regex.TransitionTable;
import karthik.regex.dataStructures.Tree;

/**
 *
 * @author karthik
 */
class GroupRegexToken extends RegexToken {

    private RegexToken[] tokArray;
    private CharSequence group_name = "";

    GroupRegexToken(RegexToken[] tokArr) {
        tokArray = tokArr;
        type = RegexTokenNames.GROUP;
    }

    GroupRegexToken(RegexToken[] tokArr, CharSequence name) {
        this(tokArr);
        group_name = name;
    }

    CharSequence get_group_name() {
        return group_name;
    }

    TransitionTable createTransitionMatrix() throws ParserException {
        TransitionTable groupMatrix;
        groupMatrix = new Pattern(tokArray, groupIDList).parse().
            get_transition_matrix();
        return groupMatrix;
    }

    Tree<RegexToken> debug_create_tree() throws ParserException {
        Tree<RegexToken> groupTree;
        groupTree = new Pattern(tokArray, groupIDList).parse().getTree();
        return groupTree;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("GROUPTOKEN " + group_name + ": (");
        for (RegexToken r : tokArray)
            sb.append(r.toString() + " ");

        sb.append(") ");
        sb.append(groupID_toString() + " ");
        return sb.toString();
    }
}
