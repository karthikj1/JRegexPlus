/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package karthik.regex;

import java.util.Map;

/**
 *
 * @author karthik
 */
class NFA_StateObject
    {
    TransitionTable trans_table;
    Map<Integer, Path_to_State> states;

    NFA_StateObject(TransitionTable t, Map<Integer, Path_to_State> m)
        {
            trans_table = t;
            states = m;
        }       
    
    }
