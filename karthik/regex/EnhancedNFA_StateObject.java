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
class EnhancedNFA_StateObject
    {
    TransitionTable trans_table;
    Enhanced_Path_to_State_List states;
    EClose_Cache eclose_cache;

    EnhancedNFA_StateObject(TransitionTable t, Enhanced_Path_to_State_List m, EClose_Cache cache)
        {
            trans_table = t;
            states = m;
            eclose_cache = cache;
        }       
        
    }
