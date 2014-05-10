/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package karthik.regex;


/**
 *
 * @author karthik Parser for Regular expression Grammar described below
 * <RE>             ::= <simple-RE><union>
 * <union>          ::=	<OR><RE>|<eps>
 * <simple-RE>      ::= <basic-RE><simple-1>
 * <concat>         ::= <eps>|<basic-RE><concat>
 * <simple-1>       ::=	<concat> |<eps>
 * <basic-RE>       ::=	<elementary-RE><quantifier?>
 * <quantifier?>    ::=	<opType.QUANTIFIER> | <eps>
 * <elementary-RE>  ::=	<group> | <back_reference> | <opType.NONE>
 * <group>          ::= <type.GROUP> | <lookaround>
 * <lookaround>     ::= <type.LOOKAHEAD> | <type.LOOKBEHIND>
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import karthik.regex.dataStructures.Stack;
import karthik.regex.dataStructures.Tree;

public class Pattern {

    private RegexToken[] tokens;
    private int tokCtr;
    private Stack<Tree<RegexToken>> debug_tree_stack;  // used only in debugging
    private Stack<TransitionTable> matcherStack;
    
    private static boolean LOG = false;
    private static boolean debug_create_tree = false;  // enables debug tree creation when true
    
    private static int groupID = 0;   // static counter to generate unique group ID
    
     // groupIDList maintains group ID path of parser instances that recursively called this instance
    private List<Integer> groupIDList; 
    private CharSequence regex = "";  // regex String being parsed by this pattern instance
        
    
    private Pattern(CharSequence inputString) throws TokenizerException {        
        this(new Tokenizer(inputString).tokenize(), null);        
        regex = inputString;
    }
    
    private Pattern(CharSequence inputString, final List<Integer> IDList) throws TokenizerException {        
        this(new Tokenizer(inputString).tokenize(), IDList);        
        regex = inputString;
    }
    
    Pattern(final RegexToken[] tok, final List<Integer> IDList){
        this(tok, IDList, true);
    }
    
    private Pattern(final RegexToken[] tok, final List<Integer> IDList, boolean addID) {
        tokens = tok;
        tokCtr = 0;
        debug_tree_stack = new Stack<>();
        matcherStack = new Stack<>();
        
        // set up list of submatch group ID's to which tokens read by this parser instance belong
        groupIDList = new ArrayList<>();     
        if(IDList != null)
            groupIDList.addAll(IDList);
        
        if((addID) && (!groupIDList.contains(groupID)))
                groupIDList.add(groupID);
        
        if(LOG)
            System.out.println("Created parser with group IDs "
                    + Arrays.toString(groupIDList.toArray()));
    }

    public static Matcher compile(CharSequence s) throws ParserException, TokenizerException{
        groupID = 0;
        Pattern p = new Pattern(s);
        ParseObject parseObj = p.parse();                
        return new Matcher(parseObj.get_transition_matrix());
    }
    
    static TransitionTable get_trans_table(CharSequence inputString,  final List<Integer> IDList) 
            throws ParserException, TokenizerException{
        // this function is used only when simulator is processing back references 
        Pattern p = new Pattern(new Tokenizer(inputString).tokenize(), IDList, false);          
        ParseObject parseObj = p.parse();        
        return parseObj.get_transition_matrix();
    }

    public static void debug_display_tree(String s) throws ParserException, TokenizerException{
        // used only for debugging purposes to display the parse tree
        System.out.println("Creating debug tree");
        groupID = 0;
        debug_create_tree = true;
        Pattern p = new Pattern(s);
        ParseObject parseObj = p.parse();
        parseObj.getTree().traverse();
        debug_create_tree = false;
    }
    
    private RegexToken getNextToken() throws ParserException {
        if (tokCtr < tokens.length) {
            return tokens[tokCtr++];
        }

        throw new ParserException("Unexpected end of input when reading tokens");
    }

    private RegexToken getCurrentToken() {
        if (tokCtr < tokens.length) {
            return tokens[tokCtr];
        }

        return null;
    }

    public ParseObject parse() throws ParserException {
            
        if(RE()){
            TransitionTable transitions = matcherStack.pop();             
            // above stores number of capturing groups in transition matrix
            return new ParseObject(debug_tree_stack.pop(), transitions);                                   
        }        
        
        throw new ParserException("Regex could not be parsed");
    }

    private boolean RE() throws ParserException {
        if(getCurrentToken() == null)
            return false;
        
        if (simpleRE() && union() && getCurrentToken() == null) {            
            return true;
        }

        return false;
    }

    private boolean union() throws ParserException {
        if(getCurrentToken() == null)
            return true;
        
        if (getCurrentToken().getType() == RegexTokenNames.OR) {
            getNextToken();
            if (RE()) {
                // pop two items and push OR node with those two items
                if (debug_create_tree) {
                    Tree<RegexToken> right = debug_tree_stack.pop();
                    Tree<RegexToken> left = debug_tree_stack.pop();
                    debug_tree_stack.push(new Tree<RegexToken>(new RegexToken(RegexTokenNames.OR), left, right));
                }
                TransitionTable n2 = matcherStack.pop();
                TransitionTable n1 = matcherStack.pop();
                matcherStack.push(n1.union(n2));
                return true;
            } else {
                return false;
            }
        }

        return true; //handles union ::= <epsilon>
    }

    private boolean simpleRE() throws ParserException {
        if(getCurrentToken() == null)
            return false;
        
        if (basicRE() && simple1()) {
            return true;
        }

        return false;
    }

    private boolean concat() throws ParserException {
        if(getCurrentToken() == null)
            return true;
        
        if (basicRE() && concat()) {
            // pop two items, push concat node with those two
            // only called after basicRE so there will always be two items on debug_tree_stack to push
            // if regex is properly formed
            if(debug_create_tree){
            Tree<RegexToken> right = debug_tree_stack.pop();
            Tree<RegexToken> left = debug_tree_stack.pop();            
            debug_tree_stack.push(new Tree<RegexToken>(new RegexToken(RegexTokenNames.AND), left, right));
            }
            TransitionTable n2 = matcherStack.pop();
            TransitionTable n1 = matcherStack.pop();
            matcherStack.push(n1.concat(n2));
            return true;
        }

        return true; //eps
    }

    private boolean simple1() throws ParserException {
        if(getCurrentToken() == null)
            return true;
                
        if (concat()) {
            return true;
        }

        return true; // for eps;
    }

    private boolean basicRE() throws ParserException {
        if(getCurrentToken() == null)
            return false;
        
        if (elemRE() && quantifier()) {
            // nothing to push or pop
            return true;
        }

        return false;
    }

    private boolean quantifier() throws ParserException {
        RegexToken current = getCurrentToken();
        
        if(current == null)
            return true;
        
        if (current.getOpType() == RegexToken.OpTypes.QUANTIFIER) {
            // pop one item and push subtree with quantifier operator
            if(debug_create_tree){
            Tree<RegexToken> left = debug_tree_stack.pop();
            debug_tree_stack.push(new Tree<RegexToken>(current, left, null));
            }
            TransitionTable trans_table = matcherStack.pop();            
            switch(current.getType()){
                case STAR:
                    matcherStack.push(trans_table.star());
                    break;
                case QUESTION:
                    matcherStack.push(trans_table.question());
                    break;
                case PLUS:
                    matcherStack.push(trans_table.plus());
                    break;
                case BRACE:
                    matcherStack.push(trans_table.brace(((BraceRegexToken) current).min, 
                            ((BraceRegexToken) current).max));
                    break;                
                default:
                    throw new ParserException("Reached unknown unary operator" + current.toString());                    
            }
            getNextToken();
            return true;
        }

        return true;   // handles epsilon case
    }

    private boolean elemRE() throws ParserException {
        RegexToken current = getCurrentToken();
        if(current == null)
            return false;
        
        if(group())
            return true;
        
        if(back_reference())
            return true;
        
        if (current.getOpType() == RegexToken.OpTypes.NONE) {
            // push node with elem expression
            current.addGroupIDList(groupIDList);
            
            if(debug_create_tree)
                debug_tree_stack.push(new Tree<>(current, null, null));
            matcherStack.push(new TransitionTable(current));
            getNextToken();
            return true;
        }
        return false;
    }
    
    private boolean group() throws ParserException {         
        TransitionTable group_transition_matrix;        
        RegexTokenNames currentType;
        
        RegexToken current = getCurrentToken();
        currentType = current.getType();
        if (currentType == RegexTokenNames.GROUP){
            // create new Pattern instance to process the group subtree
            groupID++;
            current.addGroupIDList(groupIDList);
            group_transition_matrix = ((GroupRegexToken) current).createTransitionMatrix();
            if(group_transition_matrix == null)  // regex in group was invalid
                return false;

            matcherStack.push(group_transition_matrix);
            getNextToken();
            
            if(debug_create_tree){                            
                Tree<RegexToken> groupTree = ((GroupRegexToken) current).debug_create_tree();
                debug_tree_stack.push(new Tree<>
                        (new RegexToken(currentType), groupTree, null));
            }
            
            return true;
        }
        
        if(lookaround())
            return true;
        
         return false;
    }
    
    private boolean lookaround() throws ParserException
        {
        RegexTokenNames currentType;

        RegexToken current = getCurrentToken();
        currentType = current.getType();
        if (currentType == RegexTokenNames.LOOKAHEAD)
            {
            ((LookaheadRegexToken) current).createMatcher(groupIDList);

            if (debug_create_tree)
                {
                debug_tree_stack.push(new Tree<>(current, null, null));
                }
            matcherStack.push(new TransitionTable(current));
            getNextToken();
            return true;
            }

        if (currentType == RegexTokenNames.LOOKBEHIND)
            {
            ((LookbehindRegexToken) current).createMatcher(groupIDList);

            if (debug_create_tree)                
                debug_tree_stack.push(new Tree<>(current, null, null));
                
            matcherStack.push(new TransitionTable(current));
            getNextToken();
            return true;

            }
        return false;
        }
    
    private boolean back_reference() throws ParserException{
        RegexToken current = getCurrentToken();        
        
        if(current.type == RegexTokenNames.BACKREFERENCE){
            // back reference number has to be to a group that has aready been captured
            // i.e. can't have /3 if there have only been 2 groups so far
            
            int backrefID = ((BackReferenceRegexToken) current).getBackRefID();
            if(backrefID > groupID)
                throw new ParserException("Regex cannot be parsed: Back Reference to group ID " + 
                        backrefID + " but there have only been " + groupID + " groups so far");
            
            // and can't have /1 inside group 1 either
            if(groupIDList.contains(backrefID))
                throw new ParserException("Regex cannot be parsed: Back Reference to group ID " + 
                        backrefID + " in group " + backrefID);
            
            current.addGroupIDList(groupIDList);
            if(LOG)
                System.out.println("in back_reference() - added " + current.toString());
            
            if(debug_create_tree)
                debug_tree_stack.push(new Tree<>(current, null, null));
            matcherStack.push(new TransitionTable(current));
            getNextToken();
            return true;
        }
        return false;
        }
    }
