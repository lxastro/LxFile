package xlong.file.operator;

import xlong.file.ObjectManager;

/**
 * The abstract father of all operators.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public abstract class Operator {
	/**
	 * do operation on object manager.
	 * @param om the ObjectManager
	 * @param args arguments
	 * @return success or not
	 */
	public abstract boolean operate(ObjectManager om, String[] args);

}
