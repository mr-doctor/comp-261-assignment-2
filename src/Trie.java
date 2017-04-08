import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Trie {
	private TrieNode root;

	public Trie() {
		this.root = new TrieNode();
	}
	
	public void insert(String str) {
		root.insert(str);
	}
	
	public ArrayList<String> search(String prefix) {
		TrieNode t = root;
		// adds the at the index letter to the arraylist
		for (int i=0; i<prefix.length(); i++) {
			t = t.getChildren().get(prefix.charAt(i));
			if (t == null) {
				return new ArrayList<String>();
			}
		}
		ArrayList<String> names = new ArrayList<>();
		// searches for the names using an empty list. The search will populate the list
		t.search(names);
		return names;
	}
	
}
