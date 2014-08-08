package xlong.file.object;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * A tree object contains a map of nicknames to tree checksum
 * and contains a map of nicknames to leaf checksum
 * <p>
 * A tree is similar to a directory.
 * It can contain several other containers.
 * A subtree is another tree object.
 * Both the leafs and subtrees are identified by its SHA-1 checksum.
 * Both the leafs and subtrees have their nickname in this tree.
 * These name may be different in different tree objects.
 * Nicknames of leafs cannot duplicate.
 * Nicknames of trees cannot duplicate.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public final class Tree extends Container {
	
	/** for serialization. */
	private static final long serialVersionUID = -8205593088522114084L;
	
	/** for indent. */
	private static final String INDENT = "    ";
	
	/** the map of leaf nicknames to leaf checksums. */
	private TreeMap<String, String> leafChecksums;
	
	/** the map of tree nicknames to tree checksums. */
	private TreeMap<String, String> treeChecksums;
	
	/** the map of leaf nicknames to leaf objects. */
	private transient TreeMap<Path, Leaf> leafs;
	/** the map of tree nicknames to tree objects. */
	private transient TreeMap<Path, Tree> trees;
	
	/**
	 * Default constructor. Initialize maps.
	 */
	private Tree() {
		leafChecksums = new TreeMap<>();
		treeChecksums = new TreeMap<>();
		leafs = new TreeMap<>();
		trees = new TreeMap<>();
		setType(Object.TREE);
	}	
	
	/**
	 * Get leafs map.
	 * @return leafs map
	 */
	public TreeMap<Path, Leaf> getLeafs() {
		return leafs;
	}
	
	/**
	 * Get trees map.
	 * @return trees map
	 */
	public TreeMap<Path, Tree> getTrees() {
		return trees;
	}
	
	/**
	 * Get the object with give nickname path.
	 * If exist both tree and leaf, return leaf.
	 * If not exist return null.
	 * @param path the nickname path.
	 * @return the backup object
	 */
	public Container get(final Path path) {
		if (path.getNameCount() == 1) {
			if (leafs.containsKey(path)) {
				return leafs.get(path);
			}
			if (trees.containsKey(path)) {
				return trees.get(path);
			}
			return null;
		}
		Path first = path.getName(0);
		if (trees.containsKey(first)) {
			return trees.get(first).
					get(first.relativize(path));
		}
		return null;
	}
	
	/**
	 * Create a tree contains given directory and save.
	 * 
	 * @param dirPath the directory to contain
	 * @return the tree. If fail, return null.
	 * @throws IOException if an I/O error occurs
	 */
	public static Tree create(final Path dirPath) 
			throws IOException {
		System.out.println("Create Tree for " + dirPath);
		Tree tree = null;
		if (Files.isDirectory(dirPath)) {
			tree = new Tree();
			DirectoryStream<Path> paths = Files.newDirectoryStream(dirPath);
			for (Path p:paths) {
				if (Files.isDirectory(p)) {
					Tree subtree = Tree.create(p);
					tree.add(subtree, dirPath.relativize(p));
				} else {
					Leaf leaf = Leaf.create(p);
					tree.add(leaf, dirPath.relativize(p));
				}
            }
		}
		return tree;
	}
	
	/**
	 * Add a new leaf to this tree.
	 * If the path already exist, fail.
	 * 
	 * @param leaf the leaf to add
	 * @param path the nickname path of the leaf to add, can be a path.
	 * @return totally success or not
	 */
	private boolean add(final Leaf leaf, final Path path) {
		boolean flag = true;
		if (path.getNameCount() == 1) {
			if (leafs.containsKey(path)) {
				flag = false;
			} else {
				leafs.put(path, leaf);
			}	
		} else {
			Path first = path.getName(0);
			if (!trees.containsKey(first)) {
				add(new Tree(), first);
			}
			if (!trees.get(first).add(leaf, first.relativize(path))) {
				flag = false;
			}
		}
		return flag;
	}
	
	/**
	 * Add a new tree to this tree.
	 * If the path already exist, fail.
	 * 
	 * @param tree the tree to add
	 * @param path the nickname path of the tree to add, can be a path.
	 * @return success or not
	 */
	private boolean add(final Tree tree, final Path path) {
		boolean flag = true;
		if (path.getNameCount() == 1) {
			if (trees.containsKey(path)) {
				flag = false;
			} else {
				trees.put(path, tree);
			}
		} else {
			Path first = path.getName(0);
			if (!trees.containsKey(first)) {
				add(new Tree(), first);
			}
			if (!trees.get(first).add(tree, first.relativize(path))) {
				flag = false;
			}	
		}
		return flag;
	}
	
	/**
	 * Delete a leaf.
	 * @param path the nickname path of the leaf to delete, can be a path.
	 * @return success or not
	 */
	public boolean deleteLeaf(final Path path) {
		if (path.getNameCount() == 1) {
			if (leafs.containsKey(path)) {
				leafs.remove(path);
				return true;
			}
			return false;
		}
		Path first = path.getName(0);
		if (trees.containsKey(first)) {
			return trees.get(first).
					deleteLeaf(first.relativize(path));
		}
		return false;
	}
	
	/**
	 * Delete a tree.
	 * @param path the nickname path of the tree to delete, can be a path.
	 * @return success or not
	 */
	public boolean deleteTree(final Path path) {
		if (path.getNameCount() == 1) {
			if (trees.containsKey(path)) {
				trees.remove(path);
				return true;
			}
			return false;
		}
		Path first = path.getName(0);
		if (trees.containsKey(first)) {
			return trees.get(first).
					deleteTree(first.relativize(path));
		}
		return false;
	}
	
	@Override
	public boolean restore(final Path outFileDir) 
			throws IOException {
		System.out.println("Restore Tree to " + outFileDir);
		boolean flag = true;
		Files.createDirectories(outFileDir);
		for (Entry<Path, Leaf> en:leafs.entrySet()) {
			Leaf leaf = en.getValue();
			Path nick = en.getKey();
			if (!leaf.restore(outFileDir.resolve(nick))) {
				flag = false;
			}
		}
		for (Entry<Path, Tree> en:trees.entrySet()) {
			Tree tree = en.getValue();
			Path nick = en.getKey();
			if (!tree.restore(outFileDir.resolve(nick))) {
				flag = false;
			}
		}
		return flag;
	}
	
	/**
	 * Converts tree to string.
	 * @return the string
	 */
	@Override
	public String toString() {
		return "Tree " + getChecksum() + "\n";
	}	
	
	/**
	 * Get the string representation of leafs and their names.
	 * @param depth the depth of the leaf
	 * @return the string contains leafs and their names
	 */
	public String listLeafs(final int depth) {
		String s = "";
		String dp = "";
		for (int i = 0; i < depth; i++) {
			dp += INDENT;
		}
		for (Entry<Path, Leaf> en:leafs.entrySet()) {
			Leaf leaf = en.getValue();
			Path nick = en.getKey();
			s += dp + nick + ": " + leaf.toString();
		}
		return s;
	}
	
	/**
	 * Get the string representation of leafs and their names.
	 * @return the string contains leafs and their names
	 */
	public String listLeafs() {
		return listLeafs(0);
	}
	
	/**
	 * Get the string representation of trees and their names.
	 * @param depth the depth of the tree
	 * @return the string contains trees and their names
	 */
	public String listTrees(final int depth) {
		String s = "";
		String dp = "";
		for (int i = 0; i < depth; i++) {
			dp += INDENT;
		}
		for (Entry<Path, Tree> en:trees.entrySet()) {
			Tree tree = en.getValue();
			Path nick = en.getKey();
			s += dp + nick + ": " + tree.toString();
		}
		return s;
	}

	/**
	 * Get the string representation of trees and their names.
	 * @return the string contains trees and their names
	 */
	public String listTrees() {
		return listTrees(0);
	}

	/**
	 * Get the String contains both leafs and trees and their names.
	 * @param depth the depth of the tree
	 * @return the String contains both leafs and trees and their names
	 */
	public String list(final int depth) {
		return listLeafs(depth) + listTrees(depth);
	}
	
	/**
	 * Get the String contains both leafs and trees and their names.
	 * @return the String contains both leafs and trees and their names
	 */
	public String list() {
		return toString() + listLeafs(1) + listTrees(1);
	}
	
	/**
	 * Get the String contains leafs and leafs in subtrees and their names.
	 * @param depth the depth of the tree
	 * @return the String contains leafs and leafs in subtrees and their names
	 */	
	public String listAll(final int depth) {
		String s = listLeafs(depth);
		String dp = "";
		for (int i = 0; i < depth; i++) {
			dp += INDENT;
		}
		for (Entry<Path, Tree> en:trees.entrySet()) {
			Tree tree = en.getValue();
			Path nick = en.getKey();
			s += dp + nick + ": " + tree.toString();
			s += dp + "{\n";
			s += tree.listAll(depth + 1);
			s += dp + "}\n";
		}		
		return s;
	}
	
	/**
	 * Get the String contains leafs and leafs in subtrees and their names.
	 * @return the String contains leafs and leafs in subtrees and their names
	 */	
	public String listAll() {
		String s = toString();
		s += "{\n";
		s += listAll(1);
		s += "}\n";
		return s;
	}

	@Override
	public boolean recover() {
		trees = new TreeMap<>();
		for (Entry<String, String> en:treeChecksums.entrySet()) {
			String checksum = en.getValue();
			String nick = en.getKey();
			try {
				Tree tree = (Tree) Tree.load(checksum);
				if (tree == null) {
					return false;
				}
				trees.put(Paths.get(nick), tree);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		leafs = new TreeMap<>();
		for (Entry<String, String> en:leafChecksums.entrySet()) {
			String checksum = en.getValue();
			String nick = en.getKey();
			try {
				Leaf leaf = (Leaf) Leaf.load(checksum);
				if (leaf == null) {
					return false;
				}
				leafs.put(Paths.get(nick), leaf);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}	
		return true;
	}

	@Override
	public void backup() throws IOException {
		treeChecksums = new TreeMap<>();
		for (Entry<Path, Tree> en:trees.entrySet()) {
			Path path = en.getKey();
			Tree tree = en.getValue();
			tree.save();
			treeChecksums.put(path.toString(), tree.getChecksum());
		}	
		leafChecksums  = new TreeMap<>();
		for (Entry<Path, Leaf> en:leafs.entrySet()) {
			Path path = en.getKey();
			Leaf leaf = en.getValue();
			leaf.save();
			leafChecksums.put(path.toString(), leaf.getChecksum());
		}	
	}
}
