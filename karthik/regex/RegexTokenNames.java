/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package karthik.regex;

/**
 *
 * @author karthik
 */

// identifiers used by the parser in Pattern class

 enum RegexTokenNames {
    CHAR, CHAR_CLASS, WHITESPACE, DOT, DIGIT, WORD, 
    NONDIGIT, NONWORD,NONWHITESPACE,
    TAB, NEWLINE, FORMFEED, CARR_RETURN, 
    BRACE, STAR, PLUS, QUESTION, 
    OR, AND, EPSILON, 
    GROUP, LOOKAHEAD, LOOKBEHIND, 
    WORD_BOUNDARY, NON_WORD_BOUNDARY, 
    STRING_START, STRING_END, LINE_START, LINE_END, JAVA_Z_STRING_END,
    BACKREFERENCE
}
