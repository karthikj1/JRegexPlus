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
            int backref_token_row, int backref_token_col){
        
        return new TransitionTable(match_string, groupIDList, parent_table, backref_token_row, backref_token_col);
    }
    
    private TransitionTable(String match_string, List<Integer> groupIDList, TransitionTable parent_table, 
            int backref_token_row, int backref_token_col){
        // creates transition table to match match_string
        // used to create table quickly when matching backreferences
        // since we don't need to go through the full Pattern.compile process for a simple text string
        RegexToken token;
        Map<Integer, Matchable> m;
        contains_backref = false;
        int len = match_string.length();
        List<Map<Integer, Matchable>> temp_list = new ArrayList<>(len + 1);
        
        EndBackRefRegexToken end_backref_token = new EndBackRefRegexToken(0, len);
        
        for(int r = 0; r < len; r++){
            token = new RegexToken(RegexTokenNames.CHAR, match_string.charAt(r));
            token.addGroupIDList(groupIDList);
            m = new HashMap<>();
            m.put(r + 1, token);
            temp_list.add(m);
        }
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
 
        setTransition(len, backref_token_col + backref_states, end_backref_token);
        
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
    
    TransitionTable plus(Integer quantGroupID){
        // plus implemented as a+ = a.a*
        
        TransitionTable temp = this.clone();
        expand_table(1);
        setTransition(finish, getNumStates() - 1, new QuantifierToken(RegexTokenNames.PLUS, quantGroupID, true));
        finish = getNumStates() - 1;
        
        // convert temp into temp.star
        // done here instead of calling star method because quantifiers are different
        int oldFinish = temp.getFinish();
        int oldStart = temp.getStart();        
        temp.expand_table(1);
        temp.finish = temp.getNumStates() - 1;
   
        temp.setTransition(oldStart, temp.finish, new QuantifierToken(RegexTokenNames.PLUS, quantGroupID, false));
        temp.setTransition(oldFinish, temp.finish, new QuantifierToken(RegexTokenNames.PLUS, quantGroupID, false));
        temp.setTransition(oldFinish, oldStart, new QuantifierToken(RegexTokenNames.PLUS, quantGroupID, true)); 
   
        // concat temp now that it has been made into star
        concat(temp);
        temp = null;
        
        return this;
    }
    
    TransitionTable star(Integer quantGroupID){
        // same as question but with one e-transition more and with quantifier markers instead of eps
        int oldFinish = getFinish();
        int oldStart = getStart();        
        expand_table(1);
        finish = getNumStates() - 1;
        
        setTransition(start, finish, new QuantifierToken(RegexTokenNames.STAR, quantGroupID, false));
        setTransition(oldFinish, finish, new QuantifierToken(RegexTokenNames.STAR, quantGroupID, false));
        setTransition(oldFinish, oldStart, new QuantifierToken(RegexTokenNames.STAR, quantGroupID, true)); 
        
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
    
   TransitionTable brace(final int min,final int max, Integer quantGroupID){ 
        // max = 0 implies max is infinity       
               
       TransitionTable temp;
       
        if ((min == 0) && (max == 0)){
            star(quantGroupID);
            return this;
        }         
            
        temp = this.clone();
        temp.expand_table(1);
        temp.setTransition(temp.finish, temp.getNumStates() - 1, 
                new QuantifierToken(RegexTokenNames.BRACE, quantGroupID, true));
        temp.finish = temp.getNumStates() - 1;
        
        if(min == 0)
           question();                   

        expand_table(1);  // add finish state but don't set it as finish state yet
        int brace_finish = getNumStates() - 1;        
        
        if(min == 1){
          setTransition(finish, brace_finish, eps);
        }
        
        expand_table(1);
        setTransition(finish, getNumStates() - 1, new QuantifierToken(RegexTokenNames.BRACE, quantGroupID, true));
        finish = getNumStates() - 1;
        for(int r = 2; r <= min; r++)
            concat(temp);        
            
        if(max == 0){   // n1 is repeated min or more times, no upper limit
            concat(temp.star(quantGroupID));
            return this;
        }
        
        // sets transition after min repeats
        setTransition(finish, brace_finish, new QuantifierToken(RegexTokenNames.BRACE, quantGroupID, false));
        
        for(int r = min + 1; r <= max; r++){
            // n1 is repeated between min and max times               
            // set e-transitions so that NFA state can transition to finish
            // anywhere between min and max times
            concat(temp);
            setTransition(getFinish(), brace_finish, new QuantifierToken(RegexTokenNames.BRACE, quantGroupID, false));
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
