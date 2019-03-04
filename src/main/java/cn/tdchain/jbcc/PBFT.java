package cn.tdchain.jbcc;
/**
 * @Description:
 * @author xiaoming
 * @date:上午10:14:34
 */
public class PBFT {
	/**
	 * @Description: 根据count获取拜占庭最小数
	 * @param count
	 * @return
	 * @throws
	 */
	public static int getMinByCount(int count) {
		int a = (count * 2) / 3;
		int b = (count * 2) % 3;
		if(b > 0) {
			a = a + 1;
		}
		return a;
	}
}
