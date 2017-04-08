import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

public class TrieNode {
	
	// stores all relevant variables
	private char c;
	private boolean hasChild;
	private Map<Character, TrieNode> children = new HashMap<>();
	private TrieNode parent;
	// determines if a character is the end of a word or not, so "fish" and "fishing" register as two words
	private boolean isWord = false;
	
	// for creating the root
	public TrieNode() {
		
	}
	
	// for creating children
	public TrieNode(char c, TrieNode p) {
		this.c = c;
		this.parent = p;
	}
	
	public void insert(String str) {
		// if the prefix is a word on its own
		if (str.length() == 0) {
			isWord = true;
			return;
		}
		this.hasChild = true;
		char firstLetter = str.charAt(0);
		// go down the tree if there is a child with that letter
		if (children.containsKey(firstLetter)) {
			children.get(firstLetter).insert(str.substring(1));
		} else {
			// otherwise, add a new trienode and recurse onto that
			TrieNode t = new TrieNode(firstLetter, this);
			children.put(firstLetter, t);
			t.insert(str.substring(1));
		}
	}
	
	public void search (ArrayList<String> strings) {
		// adds each word to the passed list of words
		if (this.isWord) {
			strings.add(this.toString());
		}
		// if it has a child, recurse into each child
		if (this.hasChild) {
			for (Map.Entry<Character, TrieNode> entry : children.entrySet()) {
				entry.getValue().search(strings);
			}
		}
	}
	
	// getters n' setters
	
	public String toString() {
		String str = "";
		str = str + Character.toString(this.c);
		if (this.parent != null && parent.getChar() != Character.UNASSIGNED) {
			str = parent.toString() + str;
		}
		return str;
	}
	
	public boolean hasChild() {
		return this.hasChild;
	}
	
	public void setHasChild(boolean b) {
		this.hasChild = b;
	}
	
	public boolean isWord() {
		return this.isWord;
	}
	
	public void isWord(boolean b) {
		this.isWord = b;
	}
	
	public char getChar() {
		return this.c;
	}
	
	public Map<Character, TrieNode> getChildren() {
		return this.children;
	}
	
	public TrieNode getParent() {
		return this.parent;
	}
}
