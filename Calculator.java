//Anurag Kalra ID 260631195
package a3;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
/* ACADEMIC INTEGRITY STATEMENT
 * 
 * By submitting this file, we state that all group members associated
 * with the assignment understand the meaning and consequences of cheating, 
 * plagiarism and other academic offenses under the Code of Student Conduct 
 * and Disciplinary Procedures (see www.mcgill.ca/students/srr for more information).
 * 
 * By submitting this assignment, we state that the members of the group
 * associated with this assignment claim exclusive credit as the authors of the
 * content of the file (except for the solution skeleton provided).
 * 
 * In particular, this means that no part of the solution originates from:
 * - anyone not in the assignment group
 * - Internet resources of any kind.
 * 
 * This assignment is subject to inspection by plagiarism detection software.
 * 
 * Evidence of plagiarism will be forwarded to the Faculty of Science's disciplinary
 * officer.
 */


/**
 * Main class for the calculator: creates the GUI for the calculator 
 * and responds to presses of the "Enter" key in the text field 
 * and clicking on the button. You do not need to understand or modify 
 * the GUI code to complete this assignment. See instructions below the line
 * BEGIN ASSIGNMENT CODE
 * 
 * @author Martin P. Robillard 26 February 2015
 *
 */
@SuppressWarnings("serial")
public class Calculator extends JFrame implements ActionListener
{
	private static final Color LIGHT_RED = new Color(214,163,182);
	
	private final JTextField aText = new JTextField(40);
	
	public Calculator()
	{
		setTitle("COMP 250 Calculator");
		setLayout(new GridLayout(2, 1));
		setResizable(false);
		add(aText);
		JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				aText.setText("");		
				aText.requestFocusInWindow();
			}
		});
		add(clear);
		
		aText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
		aText.addActionListener(this);

		aText.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void removeUpdate(DocumentEvent e)
			{
				aText.getHighlighter().removeAllHighlights();	
			}
			
			@Override
			public void insertUpdate(DocumentEvent e)
			{
				aText.getHighlighter().removeAllHighlights();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e)
			{
				aText.getHighlighter().removeAllHighlights();
			}
		});
		
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}
	
	/**
	 * Run this main method to start the calculator
	 * @param args Not used.
	 */
	public static void main(String[] args)
	{
		new Calculator();
	}
	
	/* 
	 * Responds to events by the user. Do not modify this method.
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( aText.getText().contains("="))
		{
			aText.setText("");		
		}
		else
		{
			Queue<Token> expression = processExpression(aText.getText());
			if( expression != null )
			{
				for(Token t : expression) {
					System.out.println("RPN Queue " + t.toString() + " " + aText.getText().substring(t.getStart(),t.getEnd()+1));
				}
				
				String result = evaluateExpression(expression, aText.getText());
				if( result != null )
				{
					aText.setText(aText.getText() + " = " + result);  
				}
			}

		}
	}
	
	/**
	 * Highlights a section of the text field with a color indicating an error.
	 * Any change to the text field erase the highlights.
	 * Call this method in your own code to highlight part of the equation to 
	 * indicate an error.
	 * @param pBegin The index of the first character to highlight.
	 * @param pEnd The index of the first character not to highlight.
	 */
	public void flagError( int pBegin, int pEnd )
	{
		assert pEnd > pBegin;
		try
		{
			aText.getHighlighter().addHighlight(pBegin, pEnd, new DefaultHighlighter.DefaultHighlightPainter(LIGHT_RED));
		}
		catch(BadLocationException e)
		{
			
		}
	}
	
	/******************** BEGIN ASSIGNMENT CODE ********************/
	
	/**
	 * Tokenizes pExpression (see Tokenizer, below), and 
	 * returns a Queue of Tokens that describe the original 
	 * mathematical expression in reverse Polish notation (RPN).
	 * Flags errors and returns null if the expression
	 * a) contains any symbol except spaces, digits, round parentheses, or operators (+,-,*,/)
	 * b) contains unbalanced parentheses
	 * c) contains invalid combinations of parentheses and numbers, e.g., 2(3)(4)
	 * 
	 * @param pExpression A string.
	 * @return The tokenized expression transformed in RPN
	 */
	private Queue<Token> processExpression(String pExpression)
	{
		//makes new instance of Tokenizer class tz
		Tokenizer tz = new Tokenizer();
		try {
			tz.tokenize(pExpression);
		}
		catch (InvalidExpressionException e) {
			flagError(e.getPosition(),e.getPosition()+1);
			return null;
		}
		
		//12+34+56
		//12+(34+56)
		//a +   b
		//12+(34 56+)
		// 12 34 56 + + 
		
		// 123 + 456
		// 123 456 +
		
		
		//Initializes Queue of type Token called RPN
		Queue<Token> RPN = new LinkedList<Token>();
		//Initializes Stack of type Token called stack
		Stack<Token> stack = new Stack<Token>();
		
		for (Token t : tz) {
			//makes String called curTokenString with token from pExpression
			String curTokenString = pExpression.substring(t.getStart(), t.getEnd()+1);
			//line below used for debugging
			System.out.println("llt " + t.toString() + " Tkn " + curTokenString);
			//if an operator
			if (curTokenString.equals("+") || 
				curTokenString.equals("-") ||
				curTokenString.equals("*") ||
				curTokenString.equals("/")) {
				//while stack is not empty and the token is an operator
				while(!stack.empty() && isOperator(pExpression.substring(stack.peek().getStart(),stack.peek().getEnd()+1)))  {
					if (OperPrec(curTokenString) < OperPrec(pExpression.substring(stack.peek().getStart(),stack.peek().getEnd()+1))) {
						// precedence is higher then pop from stack and add to RPN
						Token popTkn = stack.pop();
						System.out.println("RPN " + popTkn.toString()  + " " + pExpression.substring(popTkn.getStart(),popTkn.getEnd()+1));
						RPN.add(popTkn);
						continue;
					}
					break;
				}
				stack.push(t);
			}
			else if (curTokenString.equals("(")) {
				// Push the Open Parentheses
				stack.push(t);
			}
			else if (curTokenString.equals(")")) {
				// Keep popping back until you hit the open parentheses
				while(!stack.empty() && !pExpression.substring(stack.peek().getStart(), stack.peek().getEnd()+1).equals("(")) {
					Token popTkn = stack.pop();
					System.out.println("RPN " + popTkn.toString()  + " " + pExpression.substring(popTkn.getStart(),popTkn.getEnd()+1));
					RPN.add(popTkn);
				}
				// Do not print the popped open parentheses
				if (stack.empty()) {
					// TODO need to figure out position of closing parentheses
					flagError(0,1);
					return null;
				}
				else {
					stack.pop();
				}
				// The close parentheses is also not added to RPN
			}
			else {
				//add to top of RPN
				System.out.println("RPN " + t.toString() + " " + pExpression.substring(t.getStart(),t.getEnd()+1));
				RPN.add(t);
			}
		}
		
		while(!stack.empty()) {
			Token peekTkn = stack.peek();
			if (pExpression.substring(peekTkn.getStart(),peekTkn.getEnd()+1).equals("(")) 
			{
				flagError(peekTkn.getStart(),peekTkn.getStart()+1);
				return null;
			}
			Token popTkn = stack.pop();
			System.out.println("RPN " + popTkn.toString() + " " + pExpression.substring(popTkn.getStart(),popTkn.getEnd()+1));
			RPN.add(popTkn);
		}
			
		return RPN;
	}
	
	//method to check if TknStr is an operator {+.-,*,/}
	private boolean isOperator(String TknStr) {
		//if an operator, return true
		if (TknStr.equals("+") ||
			TknStr.equals("-") ||
			TknStr.equals("*") ||
			TknStr.equals("/")) 
			return true;
		//else, return false
		else
			return false;
	}
	
	//Function to find operation precedence
	private int OperPrec(String TknStr) {
		//if adding or subtracting, precedence 0
		if (TknStr.equals("+") ||
			TknStr.equals("-")) 
			return 0;
		//if (TknStr.equals("*") ||
		//TknStr.equals("/"))
		//else, precedence 10. This is essentially for multiplication and division
		else
			return 10;
	}
	
	/**
	 * Assumes pExpression is a Queue of tokens produced as the output of processExpression.
	 * Evaluates the answer to the expression. The division operator performs a floating-point 
	 * division. 
	 * Flags errors and returns null if the expression is an invalid RPN expression e.g., 1+-
	 * @param pExpression The expression in RPN
	 * @return A string representation of the answer)
	 */
	private String evaluateExpression(Queue<Token> pExpression, String pExprString)
	{
		// Hint return String.format("%s", <YOUR ANSWER>);
		
		// 1+2-3 infix
		// 123-+ rpn
		
		//Stack evalStack which holds Float values
		Stack<Float> evalStack = new Stack<Float>();
		
		for (Token t : pExpression) {
			//for all Tokens t in pExpression
			String curTokenString = pExprString.substring(t.getStart(), t.getEnd()+1);
			//initializing operand 1, operand 2, and result floats
			float oper1;
			float oper2;
			float res;
			
			if (isOperator(curTokenString)) {
				//this flags the error if there aren't two numbers in the evalStack before doing the operation. 
				//For example if the rpn sequence is 2 + 3 (invalid) 
				if (evalStack.size() < 2) {
					//flags error and returns null
					flagError(t.getStart(),t.getStart()+1);
					return null;
				}
			}
			//switch-case tester for curTokenString. This actually does the operations
			//in each case, the top two operands are popped and saved to oper1 and oper2
			switch(curTokenString) {
			//multiplication
			case "*":
				oper1 = evalStack.pop();
				oper2 = evalStack.pop();
				res = oper2*oper1;
				evalStack.push(res);
				break;
			//division
			case "/":
				oper1 = evalStack.pop();
				oper2 = evalStack.pop();
				res = oper2/oper1;
				evalStack.push(res);
				break;
			//subtraction
			case "-":
				oper1 = evalStack.pop();
				oper2 = evalStack.pop();
				res = oper2 - oper1;
				evalStack.push(res);
				break;
			//addition
			case "+":
				oper1 = evalStack.pop();
				oper2 = evalStack.pop();
				res = oper1 + oper2;
				evalStack.push(res);
				break;
			default :
				float operand = Float.parseFloat(curTokenString);
				evalStack.push(operand);
			}
		}
		//if evalStack is empty, return null and flagError on (0,1)
		if (evalStack.empty()) {
			flagError(0,1);
			return null;
		}
		//returns the answer, formatted
		return String.format("Your answer %s", evalStack.pop());  
	}
}

/**
 * Use this class as the root class of a hierarchy of token classes
 * that can represent the following types of tokens:
 * a) Integers (e.g., "123" "4", or "345") Negative numbers are not allowed as inputs
 * b) Parentheses '(' or ')'
 * c) Operators '+', '-', '/', '*' Hint: consider using the Comparable interface to support
 * comparing operators for precedence
 */
class Token
{
	private int aStart;//This is the start index of the token
	private int aEnd;// This is the end index of the token
	
	/**
	 * @param pStart The index of the first character of this token
	 * in the original expression.
	 * @param pEnd The index of the last character of this token in
	 * the original expression
	 */
	public Token( int pStart, int pEnd )
	{
		aStart = pStart;
		aEnd = pEnd;  
	}
	
	public int getStart()
	{
		return aStart;
	}
	
	public int getEnd()
	{
		return aEnd;
	}
	
	public String toString()
	{
		return "{" + aStart + "," + aEnd + "}";
	}
}

/**
 * Partial implementation of a tokenizer that can convert any valid string
 * into a stream of tokens, or detect invalid strings. Do not change the signature
 * of the public methods, but you can add private helper methods. The signature of the
 * private methods is there to help you out with basic ideas for a design (it is strongly 
 * recommended to use them). Hint: consider making your Tokenizer an Iterable<Token>
 */
class Tokenizer implements Iterable<Token>
{
	/**
	 * Converts pExpression into a sequence of Tokens that are retained in
	 * a data structure in this class that can be made available to other objects.
	 * Every call to tokenize should clear the structure and start fresh.
	 * White spaces are tolerated but should be ignored (not converted into tokens).
	 * The presence of any illegal character should raise an exception.
	 * 
	 * @param pExpression An expression to tokenize. Can contain invalid characters.
	 * @throws InvalidExpressionException If any invalid character is detected or if parentheses
	 * are misused as operators.
	 */
	
	//int variables used to create tokens
	private int curPos;
	private int startPos;
	private int endPos;
	private boolean inNum;
	//LinkedList of type Token called llt
	private LinkedList<Token> llt;
	public void tokenize(String pExpression) throws InvalidExpressionException
	{
		//inNum initialized to false
		inNum = false;
		llt = new LinkedList();
		
		// loop thru pExpression using curPos = current position		
		for (curPos = 0; curPos < pExpression.length(); curPos++) {
			//makes char c for each char in loop through pExpression and does consume()
			char c = pExpression.charAt(curPos);
			consume(c);
		}
		//if last char in pExpression is a number, creates token with startPos and endPos and adds to list
		if (inNum) {
			endPos = curPos - 1;
			llt.add(new Token(startPos, endPos));
		}
	}
	
	private void consume(char pChar) throws InvalidExpressionException
	{
		// Consume a single character in the input expression and deals with
		// it appropriately.
		
		//test cases for pChar
		switch (pChar) {
			//operator or parentheses
			case '(': case ')' :
			case '+': case '-' : case '*': case '/' :
				//if previous pChar was a number, create and add token to llt with startPos and endPos as parameters
				if (inNum) {
					endPos = curPos - 1;
					llt.add(new Token(startPos, endPos));
				}
				//makes inNum false
				inNum = false;
				//adds token with curPos as both parameters
				llt.add(new Token(curPos, curPos));
				break;
			//number
			case '0' : case '1' : case '2' : case '3' : case '4' : case '5' :
			case '6' : case '7' : case '8' : case '9' :
				//if previous pChar was not a number, switch to true and change startPos value
				if (inNum == false) {
					inNum = true;
					startPos = curPos;
				}
				break;
			//space
			case ' ' :
				//if previous pChar is a number, make token and add to llt
				if (inNum) {
					endPos = curPos - 1;
					llt.add(new Token(startPos, endPos));
				}
				inNum = false;
				break;
			//for any other pChar, throw InvalidExpressionException
			default :
				throw new InvalidExpressionException(curPos);
		}	
		
		//parentheses, digits, operators, whitespace(a single blank),  
	}
	
	/**
	 * Detects if parentheses are misused
	 * @throws InvalidExpressionException
	 */
	private void validate() throws InvalidExpressionException
	{
		// An easy way to detect if parentheses are misused is 
		// to look for any opening parenthesis preceded by a token that
		// is neither an operator nor an opening parenthesis, and for any
		// closing parenthesis that is followed by a token that is
		// neither an operator nor a closing parenthesis. Don't check for
		// unbalanced parentheses here, you can do it in processExpression
		// directly as part of the Shunting Yard algorithm.
		// Call this method as the last statement in tokenize.
	}


	@Override
	//iterator
	public Iterator<Token> iterator() {
		return llt.iterator();
	}
}




/**
 * Thrown by the Tokenizer if an expression is detected to be invalid.
 * You don't need to modify this class.
 */
@SuppressWarnings("serial")
class InvalidExpressionException extends Exception
{
	private int aPosition;
	
	public InvalidExpressionException( int pPosition )
	{
		aPosition = pPosition;
	}
	
	public int getPosition()
	{
		return aPosition;
	}
}