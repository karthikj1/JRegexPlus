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
class NFA_StateObject
    {
    TransitionTable trans_table;
    Path_to_State_List states;

    NFA_StateObject(TransitionTable t, Path_to_State_List m)
        {
            trans_table = t;
            states = m;
        }       
    
    }
