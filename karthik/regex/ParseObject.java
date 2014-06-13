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

import karthik.regex.dataStructures.Tree;

/**
 *
 * @author karthik
 */
class ParseObject {
    /* object returned by parser
     The tree is not created unless debug_create_tree flag in Pattern object
     is set to true.
     It is false by default so normally only the TransitionTable gets created
     */

    private Tree<RegexToken> parseTree;
    private TransitionTable parseNFA;

    ParseObject(Tree<RegexToken> pt, TransitionTable pn) {
        parseTree = pt;
        parseNFA = pn;
    }

    Tree<RegexToken> getTree() {
        return parseTree;
    }

    TransitionTable get_transition_matrix() {
        return parseNFA;
    }

}
