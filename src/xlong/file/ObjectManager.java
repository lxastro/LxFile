package xlong.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import xlong.file.object.Leaf;
import xlong.file.object.Container;
import xlong.file.object.Tree;
import xlong.file.operator.Operator;
import xlong.util.CompressionUtil;

/** 
 * Manager to manage objects.
 * Can only have one instance.
 */
public final class ObjectManager extends Manager implements Serializable {

	/** for serialization. */
	private static final long serialVersionUID = -6743983541821444460L;
	
	/** current objectManager. */
	private static ObjectManager manager = null;
	
	/** name of the objectManager. */
	private static final String NAME = "objectManager";
	
	/** map to record fathers and correspond roots of a object.*/
	private TreeMap<String, TreeMap<String, Set<String>>> fatherMap;
	
	/** map to record rootName and rootChecksum. */
	private TreeMap<String, String> rootMap;
	
	/** the current position. */
	private static Container curPos;
	
	/** the current root. */
	private static Container curRoot;
	
	/** the stack to record history positions. */
	private static Stack<Container> posStack;
	
	/** the stack to record history roots. */
	private static Stack<Container> rootStack;
	
	/** the stack to record history operations. */
	private static Stack<String[]> operationStack;
	
	/** the pool of loaded container. */
	private static TreeMap<String, Container> containerPool;
	
	/** private constructor.
	 */
	private ObjectManager() {
		fatherMap = new TreeMap<String, TreeMap<String, Set<String>>>();
		rootMap = new TreeMap<String, String>();
		recover();
	}
	
	/**
	 * recover when load.
	 */
	private void recover() {
		setCurPos(null);
		setCurRoot(null);
		posStack = new Stack<Container>();
		rootStack = new Stack<Container>();
		operationStack = new Stack<String[]>();
		containerPool = new TreeMap<String, Container>();
	}
	
	/**
	 * Get the manager path of given name.
	 * @return the path
	 */
	public static Path getPath() {
		Path path = Paths.get(getManagerDir(), NAME);
		return path;
	}
	
	/**
	 * load a manager to current manager from file.
	 * if the manager doen't exist, create a new manager.
	 * @return success of not
	 */
	public static boolean loadManager() {
		Path filePath = getPath();
		if (Files.exists(filePath)) {
			byte[] bytesArray;
			try {
				bytesArray = CompressionUtil.decompressToByteArray(filePath);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}	      
	
	        ObjectManager om = null;
	        try {
	        	ByteArrayInputStream bi = new ByteArrayInputStream(bytesArray);
	        	ObjectInputStream oi = new ObjectInputStream(bi);  
				om = (ObjectManager) oi.readObject();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				return false;
			}
	        om.recover();
	        manager = om;
		} else {
			manager = new ObjectManager();
		}
        return true;
	}
	
	/** save the current manager.
	 * 
	 * @return success or not
	 */
	public static boolean saveManager() {
		if (manager == null) {
			return false;
		}
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(bs);
			os.writeObject(manager);
	        byte[] bytesArray = bs.toByteArray();
			Path outFilePath = getPath();
			if (Files.exists(outFilePath)) {
				Files.delete(outFilePath);
			}
        	Files.createDirectories(outFilePath.getParent());
			CompressionUtil.compressByteArray(bytesArray, outFilePath);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
        
        return true;	
	}
	
	/** 
	 * get the container with given checksum.
	 * if not exist return null.
	 * @param checksum the checksum
	 * @return the object
	 */
	public static Container getContainer(final String checksum) {
		if (getContainerPool().containsKey(checksum)) {
			return getContainerPool().get(checksum);
		} else {
			Container con;
			try {
				con = Container.load(checksum);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			return con;
		}
	}
	
	/**
	 * put a son-father relation.
	 * @param son the son
	 * @param father the father
	 * @param root the relation
	 */
	public static void putRelation(final String son, final String father, final String root) {
		TreeMap<String, TreeMap<String, Set<String>>> farMap = getFatherMap();
		if (!farMap.containsKey(son)) {
			farMap.put(son, new TreeMap<String, Set<String>>());
		}
		TreeMap<String, Set<String>> fars = farMap.get(son);
		if (!fars.containsKey(father)) {
			fars.put(father, new TreeSet<String>());
		}
		fars.get(father).add(root);
	}
	
	/**
	 * put relation for a leaf.
	 * @param leaf the leaf
	 * @param root the root
	 */
	public static void putRelation(final Leaf leaf, final String root) {
		putRelation(leaf.getBlobChecksum(), leaf.getChecksum(), root);
	}
	
	/**
	 * put relation for a leaf.
	 * @param leaf the leaf
	 */
	public static void putRelation(final Leaf leaf) {
		putRelation(leaf, leaf.getChecksum());
	}
	
	/**
	 * put relation for a tree.
	 * @param tree the tree
	 * @param root the root
	 */
	public static void putRelation(final Tree tree, final String root) {
		for (Leaf leaf:tree.getLeafs().values()) {
			putRelation(tree.getChecksum(), leaf.getChecksum(), root);
			putRelation(leaf, root);
		}
		for (Tree subTree:tree.getTrees().values()) {
			putRelation(tree.getChecksum(), subTree.getChecksum(), root);
			putRelation(subTree, root);
		}
	}
	
	/**
	 * put relation for a tree.
	 * @param tree the tree
	 */
	public static void putRelation(final Tree tree) {
		putRelation(tree, tree.getChecksum());
	}
	
	/**
	 * check rootName is available or not.
	 * @param rootName the rootName
	 * @return available or not
	 */
	public static boolean checkRootName(final String rootName) {
		return !getRootMap().containsKey(rootName);
	}
	
	/**
	 * put a new root. Please check root name first.
	 * @param root the root
	 * @param rootName the name of the root
	 */
	public static void putRoot(final Container root, final String rootName) {
		getRootMap().put(rootName, root.getChecksum());
	}
	
	/**
	 * change to new root.
	 * @param root the new root
	 */
	public static void changeRoot(final Container root) {
		setCurPos(root);
		setCurRoot(root);
		getPosStack().push(root);
		getRootStack().push(root);
	}
	
	/**
	 * change to new position.
	 * @param root the new position
	 */
	public static void changePos(final Container root) {
		setCurPos(root);
		getPosStack().push(root);
	}
	
	/**
	 * list all root.
	 * @return the root list string.
	 */
	public static String listRoot() {
		String s = "";
		for (Entry<String, String> en:getRootMap().entrySet()) {
			s = s + en.getKey() + " : " + en.getValue() + "\n";
		}
		return s;
	}
	/**
	 * list all son father relations.
	 * @return the relation string.
	 */
	public static String listFather() {
		String s = "";
		String i1 = "    ";
		for (Entry<String, TreeMap<String, Set<String>>> en:getFatherMap().entrySet()) {
			s = s + en.getKey() + ":\n";
			TreeMap<String, Set<String>> fathers = en.getValue();
			for (Entry<String, Set<String>> father:fathers.entrySet()) {
				s = s + i1 + father.getKey() + " ( ";
				for (String root:father.getValue()) {
					s = s + root + " ";
				}
				s = s + ")\n";
			}
		}
		return s;
	}
	
	/** 
	 * add single container into container pool.
	 * @param con the container
	 */
	public static void traceSingleContainer(final Container con) {
		getContainerPool().put(con.getChecksum(), con);
	}
	
	/** 
	 * add containers into container pool.
	 * @param tree the tree
	 */
	public static void traceContainer(final Tree tree) {
		for (Leaf leaf:tree.getLeafs().values()) {
			traceSingleContainer(leaf);
		}
		for (Tree subTree:tree.getTrees().values()) {
			traceSingleContainer(subTree);
			traceContainer(subTree);
		}
	}
	
	/** 
	 * add containers into container pool.
	 * @param leaf the leaf
	 */
	public static void traceContainer(final Leaf leaf) {
		traceSingleContainer(leaf);
	}
	
	/**
	 * do operate on current manager.
	 * @param args arguments
	 * @return success or not.
	 */
	public static boolean operate(final String[] args) {
		if (args == null) {
			return false;
		}
		int n = args.length;
		if (n < 1) {
			return false;
		}
		try {
			Operator o = (Operator) Class.forName("xlong.file.operator." + args[0]).newInstance();
			if (!o.operate(manager, args)) {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @return the curPos
	 */
	public static Container getCurPos() {
		return curPos;
	}

	/**
	 * @param newCurPos the curPos to set
	 */
	public static void setCurPos(final Container newCurPos) {
		curPos = newCurPos;
	}

	/**
	 * @return the curRoot
	 */
	public static Container getCurRoot() {
		return curRoot;
	}

	/**
	 * @param newCurRoot the curRoot to set
	 */
	public static void setCurRoot(final Container newCurRoot) {
		curRoot = newCurRoot;
	}

	/**
	 * @return the posStack
	 */
	public static Stack<Container> getPosStack() {
		return posStack;
	}

	/**
	 * @return the rootStack
	 */
	public static Stack<Container> getRootStack() {
		return rootStack;
	}

	/**
	 * @return the fatherMap
	 */
	public static TreeMap<String, TreeMap<String, Set<String>>> getFatherMap() {
		return manager.fatherMap;
	}

	/**
	 * @return the operationStack
	 */
	public static Stack<String[]> getOperationStack() {
		return operationStack;
	}

	/**
	 * @return the containerPool
	 */
	public static TreeMap<String, Container> getContainerPool() {
		return containerPool;
	}

	/**
	 * @return the rootMap
	 */
	public static TreeMap<String, String> getRootMap() {
		return manager.rootMap;
	}
}
