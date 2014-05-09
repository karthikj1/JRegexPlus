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
class GroupRegexToken extends RegexToken{
    private RegexToken[] tokArray;
    
    GroupRegexToken(RegexToken[] tokArr){
        tokArray = tokArr;
        type = RegexTokenNames.GROUP;        
    }        
 
    TransitionTable createTransitionMatrix() throws ParserException{
        TransitionTable groupMatrix;
        groupMatrix = new Pattern(tokArray, groupIDList).parse().get_transition_matrix();
        return groupMatrix;
    }
    
    Tree<RegexToken> debug_create_tree() throws ParserException{
        Tree<RegexToken> groupTree;
        groupTree = new Pattern(tokArray, groupIDList).parse().getTree();
        return groupTree;
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer("GROUPTOKEN" + ": (");
        for (RegexToken r: tokArray)
            sb.append(r.toString() + " ");
        
        sb.append(") ");
        sb.append(groupID_toString() + " ");
        return sb.toString();
    }    
}
