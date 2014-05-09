/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    
    ParseObject(Tree<RegexToken> pt, TransitionTable pn){
        parseTree = pt;
        parseNFA = pn;
    }
    
    Tree<RegexToken> getTree(){
        return parseTree;
    }
    
    TransitionTable get_transition_matrix(){
        return parseNFA;
    }    
     
}
