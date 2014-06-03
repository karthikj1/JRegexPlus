/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package karthik.regex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author karthrowk
 */
class TransitionTable implements Cloneable
    {
    private int start, finish;
    private final EpsClass eps = EpsClass.getEpsClass(); 
    private boolean contains_backref = false;

    private List<Map<Integer, Matchable>> trans_table_list = new ArrayList<>();

    private TransitionTable(int numStates) {
        expand_table(numStates);
    }

    TransitionTable(Matchable token)
        {
            elementary(token);
            contains_backref = token.isBackReference();
        }
    
    static TransitionTable get_expanded_backref_table(String match_string, List<Integer> groupIDList, TransitionTable parent_table, 
            int backref_token_col){
        
        return new TransitionTable(match_string, groupIDList, parent_table, backref_token_col);
    }
    
    private TransitionTable(String match_string, List<Integer> groupIDList, TransitionTable parent_table, 
            int backref_token_col){
        /* creates transition table to match match_string - used to create table quickly when matching backreferences
           since we don't need to go through the full Pattern.compile process for a simple text string
           In reality, it just puts in one row with a token that matches the full match_string
           and another row with the end_backref token that will transition back to the original table
           when the back ref string has matched
                */
        RegexToken backref_string_token;
        Map<Integer, Matchable> m;
        contains_backref = false;
        
        List<Map<Integer, Matchable>> temp_list = new ArrayList<>(2);
        
        EndBackRefRegexToken end_backref_token = new EndBackRefRegexToken(0, 1);
        
        backref_string_token = new BackRefString_RegexToken(match_string, end_backref_token);
        backref_string_token.addGroupIDList(groupIDList);
    
        m = new HashMap<>();
        m.put(1, backref_string_token);
        temp_list.add(m);

        temp_list.add(new HashMap<Integer, Matchable>());  // add the empty finish state
        
        trans_table_list = temp_list;
        
        // now insert parent table into this table
        int n1States = parent_table.getNumStates();
        int backref_states = getNumStates();
        Matchable tok;
        
        expand_table(n1States);
        contains_backref = parent_table.contains_backref();
        for (int i = 0; i < n1States; i++)
            for (int j : parent_table.getKeySet(i))
                {
                tok = parent_table.getTransition(i, j);
                setTransition(i + backref_states, j + backref_states, tok);
                }
 
        setTransition(1, backref_token_col + backref_states, end_backref_token);
        setTransition(1, 0, eps);
        
        start = parent_table.start + backref_states;
        finish = parent_table.finish + backref_states;        
    }
        
    TransitionTable get_table_with_backref_expansion_removed(EndBackRefRegexToken end_backref_token){
        // removes states from expanded back reference string
        
        int start_index = end_backref_token.getStartRow(); // assumes this is 0 for now
        int end_index = end_backref_token.getEndRow();
        
        int len = getNumStates();
        int backref_states = end_index + 1 - start_index;
        TransitionTable transMatrix = new TransitionTable(len - backref_states);
        
        Map<Integer, Matchable> m;
        Matchable tok;
        List<Map<Integer, Matchable>> temp_list = new ArrayList<>(len - backref_states);
                
        // first copy the transitions to a new list exluding the states from the expanded backref 
        // and pointing to the correct state numbers
        for (Integer row = backref_states; row < len; row++)
            {
            m = new HashMap<>();
            temp_list.add(m);
            for (Integer col : this.getKeySet(row))
                {
                tok = getTransition(row, col);
                m.put(col - backref_states, tok);
                }
            }

        transMatrix.trans_table_list = temp_list;
        transMatrix.start = start - backref_states;
        transMatrix.finish = finish - backref_states;
        transMatrix.contains_backref = contains_backref;
        
        return transMatrix;
    }
   
    int getStart() {
        return start;
    }

    int getFinish() {
        return finish;
    }     
        
    Matchable getTransition(int row, int col)
        {
        return trans_table_list.get(row).get(col);
        }

    Matchable setTransition(int row, int col, Matchable tok)
        {
        removeTransition(row, col);
        trans_table_list.get(row).put(col, tok);
        return tok;
        }

    private void removeTransition(int row, int col){
        Map<Integer, Matchable> rowMap = trans_table_list.get(row);
        rowMap.remove(col);        
    }
    
    Set<Integer> getKeySet(int row){
        return trans_table_list.get(row).keySet();
    }
    
    private void expand_table(int num_states_to_add){
        //trans_table_list.ensureCapacity(num_states_to_add);
        
        for(int r = 0; r < num_states_to_add; r++)
            trans_table_list.add(new HashMap<Integer, Matchable>());
    }
    
    int getNumStates()
        {
        return trans_table_list.size();
        }
    
    TransitionTable elementary(final Matchable token){
       expand_table(2);
       
       start = 0;
       finish = 1;
       setTransition(0, 1, token);       
       return this;
    }
    
    TransitionTable concat(final TransitionTable n2)
        {
        Matchable token;

        int starting_num_states = getNumStates();
        int n2States = n2.getNumStates();
        int oldFinish = getFinish();
        int n2Start = n2.getStart();
        int oldn2finish = n2.getFinish();
        contains_backref = contains_backref | n2.contains_backref;

        expand_table(n2States - 1);
        // clone matrix from TransitionTable n2 to new matrix
        // copying is done in 9 cases to cover rows before n2 start_index row, n2 start_index row
        // and rows after n2 start_index row and same 3 divisions for columns
        
        for (int i = 0; i < n2Start; i++)
            for (int j = 0; j < n2Start; j++)
                {
                token = n2.getTransition(i, j);
                if (token != null)
                    setTransition(starting_num_states + i, starting_num_states + j, token);
                }

        for (int i = 0; i < n2Start; i++)
            {
            token = n2.getTransition(i, n2Start);
            if (token != null)
                setTransition(starting_num_states + i, oldFinish, token);
            }

        for (int i = 0; i < n2Start; i++)
            for (int j = n2Start + 1; j < n2States; j++)
                {
                token = n2.getTransition(i, j);
                if (token != null)
                    setTransition(starting_num_states + i, starting_num_states - 1 + j, token);
                }

        for (int j = 0; j < n2Start; j++)
            {
            token = n2.getTransition(n2Start, j);
            if (token != null)
                setTransition(oldFinish, starting_num_states + j, token);
            }

        token = n2.getTransition(n2Start, n2Start);
        if (token != null)
            setTransition(oldFinish, oldFinish, token);

        for (int j = n2Start + 1; j < n2States; j++)
            {
            token = n2.getTransition(n2Start, j);
            if (token != null)
                setTransition(oldFinish, starting_num_states - 1 + j, token);
            }

        for (int i = n2Start + 1; i < n2States; i++)
            for (int j = 0; j < n2Start; j++)
                {
                token = n2.getTransition(i, j);
                if (token != null)
                    setTransition(starting_num_states - 1 + i, starting_num_states + j, token);
                }

        for (int i = n2Start + 1; i < n2States; i++)
            {
            token = n2.getTransition(i, n2Start);
            if (token != null)
                setTransition(starting_num_states - 1 + i, oldFinish, token);
            }

        for (int i = n2Start + 1; i < n2States; i++)
            for (int j = n2Start + 1; j < n2States; j++)
                {
                token = n2.getTransition(i, j);
                if (token != null)
                    setTransition(starting_num_states - 1 + i, starting_num_states - 1 + j, token);
                }

        if (oldn2finish < n2Start)
            finish = starting_num_states + oldn2finish;

        if (oldn2finish > n2Start)
            finish = starting_num_states + oldn2finish - 1;

        if (oldn2finish == n2Start)
            finish = oldFinish;

        return this;
        }
    
    TransitionTable union(final TransitionTable n2){        
        Matchable token;
        
        Integer starting_num_states = getNumStates();
        Integer n2States = n2.getNumStates();
        contains_backref = contains_backref | n2.contains_backref;
        Integer oldStart = start;
        Integer oldFinish = finish;
  
        // clone matrix from TransitionTable n2 to new matrix
        expand_table(n2States + 2);
        for(Integer i = 0; i < n2States; i++)
            for(Integer j : n2.getKeySet(i)){
                 token = n2.getTransition(i,j);             
                 setTransition(starting_num_states + i, starting_num_states + j,token);
            }
        
        start = getNumStates() - 2;
        finish = getNumStates() - 1;
        // set e-transitions for new start_index state
         setTransition(start, oldStart, eps);
         setTransition(start, n2.getStart() + starting_num_states, eps);        
        
      // set e-transition to go to new finish state
         setTransition(oldFinish, finish, eps);
         setTransition(n2.getFinish() + starting_num_states, finish, eps);     
                
        return this;
    }
    
    TransitionTable plus(Integer quantGroupID, Integer uniqueID){
        // plus implemented as a+ = a.a*
        RegexTokenNames tok_type = (uniqueID == Pattern.GREEDY_ID) ? RegexTokenNames.PLUS : RegexTokenNames.LAZY_PLUS;
        TransitionTable temp = this.clone();
        expand_table(1);
        setTransition(finish, getNumStates() - 1, new QuantifierToken(tok_type, quantGroupID, true, uniqueID));
        finish = getNumStates() - 1;
        
        // convert temp into temp.star
        // done here instead of calling star method because quantifiers are different
        int oldFinish = temp.getFinish();
        int oldStart = temp.getStart();        
        temp.expand_table(1);
        temp.finish = temp.getNumStates() - 1;
        
        temp.setTransition(oldStart, temp.finish, new QuantifierToken(tok_type, quantGroupID, false, uniqueID));
        temp.setTransition(oldFinish, temp.finish, new QuantifierToken(tok_type, quantGroupID, false, uniqueID));
        temp.setTransition(oldFinish, oldStart, new QuantifierToken(tok_type, quantGroupID, true, uniqueID)); 
   
        // concat temp now that it has been made into star
        concat(temp);
        temp = null;
        
        return this;
    }
    
    TransitionTable star(Integer quantGroupID, Integer uniqueID){
        // same as question but with one e-transition more and with quantifier markers instead of eps
        RegexTokenNames tok_type = (uniqueID == Pattern.GREEDY_ID) ? RegexTokenNames.STAR : RegexTokenNames.LAZY_STAR;
        int oldFinish = getFinish();
        int oldStart = getStart();        
        expand_table(1);
        finish = getNumStates() - 1;
        
        setTransition(start, finish, new QuantifierToken(tok_type, quantGroupID, false, uniqueID));
        setTransition(oldFinish, finish, new QuantifierToken(tok_type, quantGroupID, false, uniqueID));
        setTransition(oldFinish, oldStart, new QuantifierToken(tok_type, quantGroupID, true, uniqueID)); 
        
       return this;
    }
    
    
    TransitionTable question(){
        
        int oldFinish = getFinish();
        
        expand_table(1);
        finish = getNumStates() - 1;
        // set e-transitions for new start_index state
                
        setTransition(start, finish, eps);        
        
        // set e-transition to go to new finish state
        setTransition(oldFinish, finish, eps);    
                        
        return this;
    }
    
    TransitionTable lazy_question(Integer quantGroupID, Integer uniqueID)
        {
        //TODO: implement lazy question quantifier
        question();
        make_lazy(quantGroupID, uniqueID, RegexTokenNames.LAZY_QUESTION);
        return this;
        }

    TransitionTable lazy_star(Integer quantGroupID, Integer uniqueID)
        {
        //TODO: implement lazy question quantifier
        star(quantGroupID, uniqueID);
        make_lazy(quantGroupID, uniqueID, RegexTokenNames.LAZY_STAR);
        return this;
        }

    TransitionTable lazy_plus(Integer quantGroupID, Integer uniqueID)
        {
        //TODO: implement lazy question quantifier
        plus(quantGroupID, uniqueID);
        make_lazy(quantGroupID, uniqueID, RegexTokenNames.LAZY_PLUS);
        return this;
        }

    private void make_lazy(Integer quantGroupID, Integer uniqueID, RegexTokenNames tok_type){
        /* called by star, lazy or question methods to add an extra transition that
           converts them into their lazy equivalent - operates on this TransitionTable       
        */       
        int oldStart = getStart();        

        // set first start quantifier so that the lazy quantifier counts the number of chars it eats
        expand_table(1);
        start = getNumStates() - 1;
        setTransition(start, oldStart, new QuantifierToken(tok_type, quantGroupID, true, uniqueID));                
    }     
    
   TransitionTable brace(final int min,final int max, Integer quantGroupID, Integer uniqueID){ 
        // max = 0 implies max is infinity       
               
       TransitionTable temp;
       RegexTokenNames tok_type = (uniqueID == Pattern.GREEDY_ID) ? RegexTokenNames.BRACE : RegexTokenNames.LAZY_BRACE;
       
        if ((min == 0) && (max == 0)){
            if(uniqueID == Pattern.GREEDY_ID)
                star(quantGroupID, uniqueID);
            else
                lazy_star(quantGroupID, uniqueID);
            return this;
        }         
            
        temp = this.clone();
        temp.expand_table(1);
        temp.setTransition(temp.finish, temp.getNumStates() - 1, 
                new QuantifierToken(tok_type, quantGroupID, true, uniqueID));
        temp.finish = temp.getNumStates() - 1;
        
        if(min == 0){
           if(uniqueID == Pattern.GREEDY_ID) 
               question();
            else    
               lazy_question(quantGroupID, uniqueID);
        }
        
        if(max == 1)  // max = 1 means this is just question operator
            return this; 
        
        expand_table(1);  // add finish state but don't set it as finish state yet
        int brace_finish = getNumStates() - 1;        
        
        if(min == 1){
          setTransition(finish, brace_finish, eps);
        }
        
        expand_table(1);
        setTransition(finish, getNumStates() - 1, new QuantifierToken(tok_type, quantGroupID, true, uniqueID));
        finish = getNumStates() - 1;
        for(int r = 2; r <= min; r++)
            concat(temp);        
            
        if(max == 0){   // n1 is repeated min or more times, no upper limit
            if(uniqueID == Pattern.GREEDY_ID)
                concat(temp.star(quantGroupID, uniqueID));
            else
                concat(temp.lazy_star(quantGroupID, uniqueID));            
            return this;
        }
        
        // sets transition after min repeats
        setTransition(finish, brace_finish, new QuantifierToken(tok_type, quantGroupID, false, uniqueID));
        
        for(int r = min + 1; r <= max; r++){
            // n1 is repeated between min and max times               
            // set e-transitions so that NFA state can transition to finish
            // anywhere between min and max times
            concat(temp);
            setTransition(getFinish(), brace_finish, new QuantifierToken(tok_type, quantGroupID, false, uniqueID));
        }
        finish = brace_finish;
        return this;
    }
    
    TransitionTable get_transposed_table()
        {
        int numStates = getNumStates();
        TransitionTable transposeMatrix = new TransitionTable(numStates);
        Matchable token;

        for (int transMatrixRow = 0; transMatrixRow < numStates; transMatrixRow++)
            for (int col : getKeySet(transMatrixRow))
                {
                token = this.getTransition(transMatrixRow, col);
                transposeMatrix.setTransition(col, transMatrixRow, token);
                }
                
        transposeMatrix.start = this.getFinish();
        transposeMatrix.finish = this.getStart();
        transposeMatrix.contains_backref = this.contains_backref;
        
        return transposeMatrix;
        }

    
    public TransitionTable clone()
        {
        Matchable token;
        try
            {
            TransitionTable newTable = (TransitionTable) super.clone();            
            newTable.start = start;
            newTable.finish = finish;
            newTable.trans_table_list = new ArrayList<>();
            newTable.contains_backref = contains_backref;
            
            int numStates = getNumStates();
            newTable.expand_table(numStates);
            
            for (int transMatrixRow = 0; transMatrixRow < numStates; transMatrixRow++)
                for (Integer col : getKeySet(transMatrixRow))
                    {
                    token = this.getTransition(transMatrixRow, col);                    
                    newTable.setTransition(transMatrixRow, col, token);
                    }

            return newTable;
            } catch (CloneNotSupportedException cnse)
            {
            // can only get here if an error in cloning the transition table
            throw new Error("Error when cloning transition table");
            }
        }

    public String toString()
        {
        Matchable tok;
        int r = 0;
        StringBuilder sb = new StringBuilder("\r\n");

        for (Map<Integer, Matchable> transMatrixRows : trans_table_list)
            {
            sb.append("Row " + r++ + ":; ");
            for (int i : transMatrixRows.keySet())
                {
                tok = transMatrixRows.get(i);
                if (tok == null)   // should not get to this branch since map will only contain valid tokens
                    {
                    sb.append(i + ":");
                    sb.append(transMatrixRows.containsKey(i) ? "contains null; " : "empty; ");
                    } else
                    sb.append(i + ":" + tok.toString()).append("; ");
                }
            sb.append("\r\n");
            }
        return sb.toString();

        }

    boolean contains_backref()
        {
        return contains_backref;
        }

    }
