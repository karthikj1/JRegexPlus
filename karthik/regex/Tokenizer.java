/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package karthik.regex;

import java.util.ArrayList;

/**
 *
 * @author karthik
 */
class Tokenizer {

    /**
     * @param args the command line arguments
     */
    private int indexPos;
    private CharSequence inputString;
    private int len;
    private char INTERSECTIONCHAR = '&';    
    private char NEGATIONCHAR = '^';

    Tokenizer(CharSequence s) {
        indexPos = 0;
        inputString = s;        
        len = inputString.length();
    }

    void setIntersectionChar(char c)
    {
        INTERSECTIONCHAR = c;
    }
    
    
    void setNegationChar(char c)
    {
        NEGATIONCHAR = c;
    }
    
    RegexToken[] tokenize() throws TokenizerException{
        
        ArrayList<RegexToken> tokenList = new ArrayList<>();
        int numTokens = recParse(tokenList, false);
        return tokenList.toArray(new RegexToken[numTokens]);
    }
    
    private int recParse(ArrayList<RegexToken> tokens, boolean inGroup) throws TokenizerException{
        // inGroup is true if the parser is inside a group enclosed by parentheses
        char currentChar;
        int numTokens = 0;
        
        try {            
            while (indexPos < len) {
                currentChar = inputString.charAt(indexPos++);
                numTokens++;
                switch (currentChar) {
                    case '\\':
                        currentChar = inputString.charAt(indexPos++);
                        handleEscapeChars(tokens, currentChar);
                        break;

                    case '[':
                        indexPos--;
                        tokens.add(parseCharClass());
                        break;

                    case '(':
                        if(inputString.charAt(indexPos) == '?')
                            tokens.add(handleLookaround());
                        else
                            tokens.add(parseGroup());
                        break;
                    case ')':
                        if(inGroup)
                            return --numTokens;
                        // ) treated like ordinary character when not preceded by matching left parentheses
                        tokens.add(new RegexToken(RegexTokenNames.CHAR, currentChar));
                        break;                            
                    case '{':
                        tokens.add(parseBraces());
                        break;                    
                    case '.':
                        tokens.add(new RegexToken(RegexTokenNames.DOT));
                        break;
                    case '|':
                        tokens.add(new RegexToken(RegexTokenNames.OR));
                        break;
                    
                    case '?':
                        if(is_lazy_quantifier())
                             tokens.add(new RegexToken(RegexTokenNames.LAZY_QUESTION));
                        else
                             tokens.add(new RegexToken(RegexTokenNames.QUESTION));
                        break;    
                    case '+':
                        if(is_lazy_quantifier())
                             tokens.add(new RegexToken(RegexTokenNames.LAZY_PLUS));
                        else
                            tokens.add(new RegexToken(RegexTokenNames.PLUS));
                        break;
                        
                    case '*':
                        if(is_lazy_quantifier())
                             tokens.add(new RegexToken(RegexTokenNames.LAZY_STAR));
                        else
                            tokens.add(new RegexToken(RegexTokenNames.STAR));
                        break;
                    case '^':
                        tokens.add(new BoundaryRegexToken(RegexTokenNames.LINE_START));
                        break;    
                    case '$':
                        tokens.add(new BoundaryRegexToken(RegexTokenNames.LINE_END));
                        break;    
                    default:
                        tokens.add(new RegexToken(RegexTokenNames.CHAR, currentChar));
                }                
            
            }  // while
            if((indexPos == len) && inGroup)
                throw new TokenizerException("Expected ) but reached end of string unexpectedly");
            
            return numTokens;
        } // try
        catch (StringIndexOutOfBoundsException siobe) {
            throw new TokenizerException("Reached unexpected end of string at "
                    + indexPos + " - " + siobe.getMessage());
        }
        
    }
    
    private boolean is_lazy_quantifier(){
        if(inputString.charAt(indexPos) == '?'){
            indexPos++;
            return true;
        }
        return false;
    }
    
    private CharClassRegexToken parseCharClass()
            throws TokenizerException {
        CharClassRegexToken tokenChain;

        boolean negated = false;

        char currentChar;
        try {
            currentChar = inputString.charAt(indexPos++);
            if (currentChar != '[') {
                throw new TokenizerException("[ expected but not found at start of char class");
            }

            currentChar = inputString.charAt(indexPos++);

            if (currentChar == NEGATIONCHAR){
                    negated = true;
                    tokenChain = new CharClassRegexToken("", negated);
            }
            else if(currentChar == '['){
                    indexPos--;
                    tokenChain = new CharClassRegexToken("", negated);
                    tokenChain.add(parseCharClass(), true);
            }
            else {
                tokenChain = new CharClassRegexToken("", negated); 
                indexPos--;
            }

            
            while ((currentChar = inputString.charAt(indexPos++)) != ']'){

                if (currentChar == '\\') {                    
                    currentChar = inputString.charAt(indexPos++);
                    switch (currentChar) {
                        case 'd':
                            tokenChain.add(new CharClassRegexToken("0123456789", false), true);
                            break;
                        case 'D':
                            tokenChain.add(new CharClassRegexToken("0123456789", true), true);
                            break;                        
                        case 'w':
                            tokenChain.add(new CharClassRegexToken(makeWordCharClass(), false), true);
                            break;               
                        case 'W':
                            tokenChain.add(new CharClassRegexToken(makeWordCharClass(), true), true);
                            break;               
                            
                        case 's':
                            tokenChain.add(new CharClassRegexToken("\\n\\r\\f\\b \\t", false), true);                            
                            break;
                        case 'S':
                            tokenChain.add(new CharClassRegexToken("\\n\\r\\f\\b \\t", true), true);                            
                            break;
                        case '\\':
                        case '[':
                        case ']':
                        case '&':                            
                            tokenChain.append(currentChar);
                            break;
                    }
                    continue;
                }
                // union of nested character classes 
                if(currentChar == '['){
                    indexPos--;
                    tokenChain.add(parseCharClass(), true);
                    continue;                    
                }
                
                if (currentChar == INTERSECTIONCHAR) {
                    if (inputString.charAt(indexPos) == INTERSECTIONCHAR) {
                       // code here to handle && in character class
                        // && *must* be followed by a character class enclosed in []
                        indexPos++;
                        tokenChain.add(parseCharClass(), false);
                        continue;
                    }

                }
                if (currentChar == '-') {  // fill in char range  
                    char start = inputString.charAt(indexPos - 2);
                    char end = inputString.charAt(indexPos++);

                    for (int i = start + 1; i <= end; i++) {
                        tokenChain.append((char) i);
                    }
                    continue;
                }
                // character is a normal character so just add it to the chain
                tokenChain.append(currentChar);
            }
            if(currentChar != ']')
                throw new TokenizerException("Expected ] at end of character class at " + indexPos);           
            return tokenChain;

        } catch (StringIndexOutOfBoundsException siobe) {
            throw new TokenizerException("Reached unexpected end of string - "
                    + siobe.getMessage());
            // replace with TokenizerException
        }
    }

    private void handleEscapeChars(ArrayList<RegexToken> tokens, char c) throws TokenizerException {
        switch (c) {
            case 'd':
                tokens.add(new RegexToken(RegexTokenNames.DIGIT));
                break;
            case 'D':
                tokens.add(new RegexToken(RegexTokenNames.NONDIGIT));
                break;
            case 'w':
                tokens.add(new RegexToken(RegexTokenNames.WORD));
                break;
            case 'W':
                tokens.add(new RegexToken(RegexTokenNames.NONWORD));
                break;
            case 's':
                tokens.add(new RegexToken(RegexTokenNames.WHITESPACE));
                break;
            case 'S':
                tokens.add(new RegexToken(RegexTokenNames.NONWHITESPACE));
                break;
            case 'n':
                tokens.add(new RegexToken(RegexTokenNames.NEWLINE));
                break;
            case 'r':
                tokens.add(new RegexToken(RegexTokenNames.CARR_RETURN));
                break;
            case 'f':
                tokens.add(new RegexToken(RegexTokenNames.FORMFEED));
                break;           
            case 'b':
                tokens.add(new BoundaryRegexToken(RegexTokenNames.WORD_BOUNDARY));
                break;                
            case 'B':
                tokens.add(new BoundaryRegexToken(RegexTokenNames.NON_WORD_BOUNDARY));
                break;        
            case 'A':
                tokens.add(new BoundaryRegexToken(RegexTokenNames.STRING_START));
                break;        
            case 'z':
                tokens.add(new BoundaryRegexToken(RegexTokenNames.STRING_END));
                break;                        
            case 'Z':
                tokens.add(new BoundaryRegexToken(RegexTokenNames.JAVA_Z_STRING_END));
                break;                        
            case 'p':
                tokens.add(handlePosix(false));
                break;
            case 'P':
                tokens.add(handlePosix(true));
                break;
            case 'x': 
                tokens.add(processHexDigits());
                break;
            case '0': 
                tokens.add(processOctalDigits());
                break;            
            case '{': case '\\': case '*': case '+': case '.' : case '[' :
            case '(':
                tokens.add(new RegexToken(RegexTokenNames.CHAR, c));
                break;    
            case '1':case '2':case '3':case '4':case '5':case '6':case '7':
                case '8':case '9':
                    tokens.add(new BackRefRegexToken(Character.getNumericValue(c)));
                    break;
            default:
                throw new TokenizerException("Expected control character but found "
                        + "\\" + c + " at index position " + indexPos);
        }
    }
    
    private RegexToken processHexDigits() throws TokenizerException{
        // allows only two digit hex number in the form \xhh and returns the character 0xhh
        int hexNumber;
        StringBuffer hexString = new StringBuffer("");
        try{
        hexString.append(inputString.charAt(indexPos++));        
        hexString.append(inputString.charAt(indexPos++));        
        hexNumber = Integer.parseInt(hexString.toString(), 16);
        return new RegexToken(RegexTokenNames.CHAR, (char) hexNumber);
        }
        catch (NumberFormatException nfe){
            throw new TokenizerException("Expected 2 digit hexadecimal number but found" 
                    + hexString + " " + nfe.getMessage());
        }
        catch(StringIndexOutOfBoundsException siobe){
            throw new TokenizerException("Reached unexpected end of string while processing hexadecimal "
                    + "escape character " + siobe.getMessage());
        }        
    }
    
    
    private RegexToken processOctalDigits() throws TokenizerException{
        // allows only three digit hex number in the form \xmnn and returns the character 0xmnn
        // mnn must be less than 256
        
        int octalNumber;
        StringBuffer octalString = new StringBuffer("");
        
        try{
        octalString.append(inputString.charAt(indexPos++));        
        octalString.append(inputString.charAt(indexPos++));        
        octalString.append(inputString.charAt(indexPos++));  
        octalNumber = Integer.parseInt(octalString.toString(), 8);
        if(octalNumber > 255)
            throw new TokenizerException("Expected octal number less than \0377 but found " + octalString);
        
        return new RegexToken(RegexTokenNames.CHAR, (char) octalNumber);
        }
        catch (NumberFormatException nfe){
            throw new TokenizerException("Expected 3 digit octal number but found" 
                    + octalString + " " + nfe.getMessage());
        }
        catch(StringIndexOutOfBoundsException siobe){
            throw new TokenizerException("Reached unexpected end of string while processing octal escape character: " 
                        + siobe.getMessage());
        }        
    }
    
 private CharClassRegexToken handlePosix(final boolean isNegated) throws TokenizerException{     
     char currentChar;
     StringBuffer posix_class_name = new StringBuffer("");
     
            try {
            currentChar = inputString.charAt(indexPos++);
            if (currentChar != '{') {
                throw new TokenizerException("{ expected but not found at start of posix class");
            }

            currentChar = inputString.charAt(indexPos++);
            while(currentChar != '}')    {                
                posix_class_name.append(currentChar);
                currentChar = inputString.charAt(indexPos++);
            }
            
            // create char classes from POSIX class names
            return NamedCharClassTokenFactory.getCharClassToken(posix_class_name.toString(), isNegated);
         
        } catch (StringIndexOutOfBoundsException siobe) {
            throw new TokenizerException("Reached unexpected end of string while processing POSIX class. "
                    + " Possibly missing closing } - "
                    + siobe.getMessage());            
        }
     
 }
    private String makeWordCharClass()
        {
        StringBuffer sb = new StringBuffer("");
        for(int c = 'a'; c <= 'z'; c++){
            sb.append((char) c);
            sb.append(Character.toUpperCase((char) c));
        }
        sb.append('_');
        return sb.toString();
        }
    
    private BraceRegexToken parseBraces() throws TokenizerException{
        // only called when { is found so assumes it is inside a brace pair
        char currentChar;
        int lower = 0, upper = 0;
        try {

            while (Character.isDigit(currentChar = inputString.charAt(indexPos++))) 
                lower = 10 * lower + Character.getNumericValue(currentChar);
            
            upper = lower;            
            if (currentChar == ',') {
                upper = 0;
            }
            if (currentChar == '}')
                {
                if (is_lazy_quantifier())
                     return new BraceRegexToken(lower, lower, true);
                else

                    return new BraceRegexToken(lower, lower, false);
                }
            while (Character.isDigit(currentChar = inputString.charAt(indexPos++))) {
                upper = 10 * upper + Character.getNumericValue(currentChar);
            }
            if (currentChar != '}') {
                throw new TokenizerException("Expected } but found " + currentChar);
            }
        } catch (StringIndexOutOfBoundsException siobe) {
            throw new TokenizerException("Reached unexpected end of string - "
                    + siobe.getMessage());
        }
        if((upper < lower) && (upper !=0))
            throw new TokenizerException("Upper limit in braces must be greater than lower");
        
        if(is_lazy_quantifier())
            return new BraceRegexToken(lower, upper, true);
        else
            return new BraceRegexToken(lower, upper, false);        
    }   
    
    private RegexToken parseGroup() throws TokenizerException{
        // only called when ( has been found so assumes it is inside a group
        ArrayList<RegexToken> groupTokenList = new ArrayList<>();
        int i = recParse(groupTokenList, true);
        
        return new GroupRegexToken(groupTokenList.toArray(new RegexToken[i]));
    }
    
    private RegexToken handleLookaround() throws TokenizerException{
        // called when (? has been found and pointer is on the ?
        indexPos++;
        boolean lookahead = true, positive = true;
        try{
        char current = inputString.charAt(indexPos++);
        switch(current){
            case ':':
                // non-capturing group treated same as capturing group for now
                return parseGroup();                
            case '=':                
                lookahead = true;
                positive = true;
                break;
            case '!':                
                lookahead = true;
                positive = false;
                break;               
            case '<':    
                lookahead = false;
                current = inputString.charAt(indexPos++);
                switch(current){
                    case '=':                
                        positive = true;
                        break;
                    case '!':                
                        positive = false;
                        break; 
                    default:
                        throw new TokenizerException("Expected = or ! but found " 
                                + current + " at " + indexPos);            
                }
        }
        }
         catch (StringIndexOutOfBoundsException siobe) {
            throw new TokenizerException("Reached unexpected end of string - "
                    + siobe.getMessage());
        }
        if(lookahead)
            return new LookaheadRegexToken(new Tokenizer(find_group_end(1)).tokenize(), positive);
        else
            return new LookbehindRegexToken(new Tokenizer(find_group_end(1)).tokenize(), positive);        
    }
    
    private CharSequence find_group_end(int paren_count) throws TokenizerException{
        // called from within handleLookaround to capture regex pattern string in the lookaround group
        // paren_count must be non-zero and represents the number of open parentheses
        // at current indexPos
        
        int start = indexPos;
        char current;
               
        while((indexPos < len) && (paren_count != 0)){
                current = inputString.charAt(indexPos++);
                switch(current){
                    case '(':
                        paren_count++;
                        break;
                    case ')':
                        paren_count--;
                        break;
                    default:
                        // do nothing
                }
            }
        
            if(paren_count != 0 )
                throw new TokenizerException("Expected ) but reached unexpected end of string at "
                    + indexPos);
            return inputString.subSequence(start, indexPos - 1);
        
    }
}
