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
    private int flags = Pattern.DOTALL;

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
        recParse(tokenList, false);
        return tokenList.toArray(new RegexToken[1]);
    }
    
    private void recParse(ArrayList<RegexToken> tokens, boolean inGroup) throws TokenizerException{
        // inGroup is true if the parser is inside a group enclosed by parentheses
        char currentChar;
        
        try {            
            while (indexPos < len) {
                currentChar = inputString.charAt(indexPos++);                
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
                        if(inputString.charAt(indexPos) == '?'){
                            RegexToken lookaround_tok = handleLookaround_and_flags();
                            if(lookaround_tok != null)
                                tokens.add(lookaround_tok);
                            // null token means it was a flag so nothing to do
                        }
                        else
                            tokens.add(parseGroup());
                        break;
                    case ')':
                        if(inGroup)
                            return;
                        // ) treated like ordinary character when not preceded by matching left parentheses
                        tokens.add(new RegexToken(RegexTokenNames.CHAR, currentChar, flags));
                        break;                            
                    case '{':
                        tokens.add(parseBraces());
                        break;                    
                    case '.':
                        tokens.add(new RegexToken(RegexTokenNames.DOT, flags));
                        break;
                    case '|':
                        tokens.add(new RegexToken(RegexTokenNames.OR, flags));
                        break;
                    
                    case '?':
                        if(is_lazy_quantifier())
                             tokens.add(new RegexToken(RegexTokenNames.LAZY_QUESTION, flags));
                        else
                             tokens.add(new RegexToken(RegexTokenNames.QUESTION, flags));
                        break;    
                    case '+':
                        if(is_lazy_quantifier())
                             tokens.add(new RegexToken(RegexTokenNames.LAZY_PLUS, flags));
                        else
                            tokens.add(new RegexToken(RegexTokenNames.PLUS, flags));
                        break;
                        
                    case '*':
                        if(is_lazy_quantifier())
                             tokens.add(new RegexToken(RegexTokenNames.LAZY_STAR, flags));
                        else
                            tokens.add(new RegexToken(RegexTokenNames.STAR, flags));
                        break;
                    case '^':
                        tokens.add(new BoundaryRegexToken(RegexTokenNames.LINE_START, flags));
                        break;    
                    case '$':
                        tokens.add(new BoundaryRegexToken(RegexTokenNames.LINE_END, flags));
                        break;    
                    default:
                        tokens.add(new RegexToken(RegexTokenNames.CHAR, currentChar, flags));
                }                
            
            }  // while
            if((indexPos == len) && inGroup)
                throw new TokenizerException("Expected ) but reached end of string unexpectedly");
            
            return;
        } // try
        catch (StringIndexOutOfBoundsException siobe) {
            throw new TokenizerException("Reached unexpected end of string at "
                    + indexPos + " - " + siobe.getMessage());
        }
        
    }
    
    private boolean is_lazy_quantifier(){
        if(indexPos >= inputString.length())
            return false;
        
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
                    tokenChain = new CharClassRegexToken("", negated, flags);
            }
            else if(currentChar == '['){
                    indexPos--;
                    tokenChain = new CharClassRegexToken("", negated, flags);
                    tokenChain.add(parseCharClass(), true);
            }
            else {
                tokenChain = new CharClassRegexToken("", negated, flags); 
                indexPos--;
            }

            
            while ((currentChar = inputString.charAt(indexPos++)) != ']'){

                if (currentChar == '\\') {                    
                    currentChar = inputString.charAt(indexPos++);
                    switch (currentChar) {
                        case 'd':
                            tokenChain.add(new CharClassRegexToken('0', '9', false, flags), true);
                            break;
                        case 'D':
                            tokenChain.add(new CharClassRegexToken('0','9', true, flags), true);
                            break;                        
                        case 'w':
                            tokenChain.add(makeWordCharClass(false), true);
                            break;               
                        case 'W':
                            tokenChain.add(makeWordCharClass(true), true);
                            break;               
                            
                        case 's':
                            tokenChain.add(new CharClassRegexToken("\\n\\r\\f\\b \\t", false, flags), true);                            
                            break;
                        case 'S':
                            tokenChain.add(new CharClassRegexToken("\\n\\r\\f\\b \\t", true, flags), true);                            
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
                    if (Character.compare(start, end) > 0)
                        throw new TokenizerException("Invalid character class " + start 
                        + "-" + end + " at index " + indexPos + ". End character is lower than starting character");                    
                        tokenChain.append(start, end);
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
                tokens.add(new RegexToken(RegexTokenNames.DIGIT, flags));
                break;
            case 'D':
                tokens.add(new RegexToken(RegexTokenNames.NONDIGIT, flags));
                break;
            case 'w':
                tokens.add(new RegexToken(RegexTokenNames.WORD, flags));
                break;
            case 'W':
                tokens.add(new RegexToken(RegexTokenNames.NONWORD, flags));
                break;
            case 's':
                tokens.add(new RegexToken(RegexTokenNames.WHITESPACE, flags));
                break;
            case 'S':
                tokens.add(new RegexToken(RegexTokenNames.NONWHITESPACE, flags));
                break;
            case 'n':
                tokens.add(new RegexToken(RegexTokenNames.NEWLINE, flags));
                break;
            case 'r':
                tokens.add(new RegexToken(RegexTokenNames.CARR_RETURN, flags));
                break;
            case 'f':
                tokens.add(new RegexToken(RegexTokenNames.FORMFEED, flags));
                break;           
            case 'b':
                tokens.add(new BoundaryRegexToken(RegexTokenNames.WORD_BOUNDARY, flags));
                break;                
            case 'B':
                tokens.add(new BoundaryRegexToken(RegexTokenNames.NON_WORD_BOUNDARY, flags));
                break;        
            case 'A':
                tokens.add(new BoundaryRegexToken(RegexTokenNames.STRING_START, flags));
                break;        
            case 'z':
                tokens.add(new BoundaryRegexToken(RegexTokenNames.STRING_END, flags));
                break;                        
            case 'Z':
                tokens.add(new BoundaryRegexToken(RegexTokenNames.JAVA_Z_STRING_END, flags));
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
                tokens.add(new RegexToken(RegexTokenNames.CHAR, c, flags));
                break;    
            case 'k':
                tokens.add(new BackRefRegexToken(handleNamedBackReference()));
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
    
    private CharSequence handleNamedBackReference() throws TokenizerException{
        if(inputString.charAt(indexPos++) != '<')
            throw new TokenizerException("Expected < after \\k but did not find it at " + indexPos);
        
        return get_group_name(inputString.charAt(indexPos++));
    }
    
    private RegexToken processHexDigits() throws TokenizerException{
        // allows only two digit hex number in the form \xhh and returns the character 0xhh
        int hexNumber;
        StringBuffer hexString = new StringBuffer("");
        try{
        hexString.append(inputString.charAt(indexPos++));        
        hexString.append(inputString.charAt(indexPos++));        
        hexNumber = Integer.parseInt(hexString.toString(), 16);
        return new RegexToken(RegexTokenNames.CHAR, (char) hexNumber, flags);
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
        
        return new RegexToken(RegexTokenNames.CHAR, (char) octalNumber, flags);
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
            return NamedCharClassTokenFactory.getCharClassToken(posix_class_name.toString(), isNegated, flags);
         
        } catch (StringIndexOutOfBoundsException siobe) {
            throw new TokenizerException("Reached unexpected end of string while processing POSIX class. "
                    + " Possibly missing closing } - "
                    + siobe.getMessage());            
        }
     
 }
    private CharClassRegexToken makeWordCharClass(boolean isNegated)
        {
        CharClassRegexToken tok = new CharClassRegexToken('a', 'z', isNegated, flags);
        tok.append('A','Z');
        tok.append('_');
        return tok;
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
    
    private RegexToken parseGroup(CharSequence... group_name) throws TokenizerException
        {
        // only called when ( has been found so assumes it is inside a group
        ArrayList<RegexToken> groupTokenList = new ArrayList<>();
        recParse(groupTokenList, true);

        if (group_name.length > 0)  // it is a named group
            return new GroupRegexToken(groupTokenList.toArray(new RegexToken[1]), group_name[0]);
        else
            return new GroupRegexToken(groupTokenList.toArray(new RegexToken[1]));
        }
    
    private RegexToken handleLookaround_and_flags() throws TokenizerException{
        // called when (? has been found and pointer is on the ?
        // also handles named groups and flags
        
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
                        // this must be a named capturing group then
                        CharSequence group_name = get_group_name(current);
                        return parseGroup(group_name);                        
                }
                break;
            default:  // might be some flag
                parseFlags(current);
                return null;
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
    
    private void parseFlags(char currentChar) throws TokenizerException
        {
        // called when (? and a character other than lookaround or named group is found
        boolean switch_off = false;
        try
            {

            if (currentChar == '-')
                {
                switch_off = true;
                currentChar = inputString.charAt(indexPos++);
                }
            while (currentChar != ')')
                {
                switch (currentChar)
                    {
                    case 'i':
                        flags = (switch_off) ? flags & (~Pattern.CASE_INSENSITIVE) : flags | Pattern.CASE_INSENSITIVE;
                        break;
                    case 'm':
                        flags = (switch_off) ? flags & (~Pattern.MULTILINE) : flags | Pattern.MULTILINE;
                        break;
                    case 's':
                        flags = (switch_off) ? flags & (~Pattern.DOTALL) : flags | Pattern.DOTALL;
                        break;
                    default:
                        throw new TokenizerException("Unrecognized flag character " + currentChar
                                + " at index " + indexPos);
                    }
                currentChar = inputString.charAt(indexPos++);
                }
            } catch (StringIndexOutOfBoundsException siobe)
            {
            throw new TokenizerException("Reached unexpected end of string while looking for flag characters - "
                    + siobe.getMessage());
            }
        }
    
    private CharSequence get_group_name(char c) throws TokenizerException
        {
        // called with indexPos on first letter of group name
        StringBuilder sb = new StringBuilder("").append(c);
        try
            {
            char currentChar = inputString.charAt(indexPos++);
            while (currentChar != '>')
                {
                sb.append(currentChar);
                currentChar = inputString.charAt(indexPos++);
                }
            return sb;
            } catch (StringIndexOutOfBoundsException siobe)
            {
            throw new TokenizerException("Reached unexpected end of string at while parsing"
                    + " group name - "
                    + siobe.getMessage());
            }
        }

    private CharSequence find_group_end(int paren_count) throws TokenizerException{
        // called from within handleLookaround_and_flags to capture regex pattern string in the lookaround group
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
