package xlong.file.operator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import xlong.file.ObjectManager;
import xlong.file.object.Leaf;
import xlong.file.object.Tree;

/** Import file or directory.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public final class Import extends Operator {

	
	@Override
	public boolean operate(final ObjectManager om, final String[] args) {
		if (args.length == 3) {
			System.out.println("Import " + args[1]);
			Path path = Paths.get(args[1]);
			String rootName = args[2];
			if (!ObjectManager.checkRootName(rootName)) {
				return false;
			}
			if (Files.isDirectory(path)) {
				try {
					Tree tree = Tree.create(path);
					tree.save();
					//System.out.println(tree.toString());
					ObjectManager.putRelation(tree);
					ObjectManager.putRoot(tree, rootName);	
					ObjectManager.traceContainer(tree);
					ObjectManager.changeRoot(tree);
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			} else if (Files.exists(path)) {
				try {
					Leaf leaf = Leaf.create(path);
					leaf.save();
					//System.out.println(leaf.toString());
					ObjectManager.putRelation(leaf);
					ObjectManager.putRoot(leaf, rootName);
					ObjectManager.traceContainer(leaf);
					ObjectManager.changeRoot(leaf);
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}				

			}
			return true;
		} else {
			return false;
		}
	}

}
