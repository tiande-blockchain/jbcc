package cn.tdchain.jbcc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import cn.tdchain.Trans;
import cn.tdchain.Trans.TransStatus;
import cn.tdchain.jbcc.rpc.RPCResult;

/**
 * @Description: 批量提交交易集合，一个批处理的交易集合中有且只能有一个key，key不能有重复，要么一起成功要么一起失败。
 * @author xiaoming
 * @date:上午10:10:47
 */
public class BatchTrans <T extends Trans> {
	/**
	 * @Description: 当前批处理的生命计数器，每参与一次共识计数器+1
	 */
	private int index = 1;
	
	/**
	 * @Description: 当前批处理的唯一标识，交易hash list的消息merkle—root
	 */
	private String id = null; 
	
	/**
	 * @Description: 标记着客户端id
	 */
	private String connectionId;
	
	/**
	 * @Description: 保存着一次批处理的信息，一般如果共识有异常时，错误信息会被保存到此属性上。
	 */
	private String msg;
	
	/**
	 * @Description: 创建时间戳
	 */
	private Long timeStamp;
	
	/**
	 * @Description: 保存着一次批处理的状态，一般情况下此状态就是批处理中所有交易的状态，默认是就绪状态，如果共识成功状态则会变成succes
	 */
	private Trans.TransStatus status = Trans.TransStatus.prep;
	
	/**
	 * @Description: 保存着当前交易集合的全部key，一个Batch交易集合不能有重复的key
	 */
	private HashSet<String> keySet = new HashSet<String>();
	
	/**
	 * @Description: 保存着当前交易集合的全部交易对象，交易对象不能重复
	 */
	private HashSet<T> transSet = new HashSet<T>();
	
	public BatchTrans() {
		this.timeStamp = System.currentTimeMillis();
	}
	
	/**
	 * @Description: 添加一笔交易到批处理中
	 * @param t
	 * @throws
	 */
	public void addTransToBatch(T t) {
		//交易自身验证是否合法，非法则抛出异常
		t.check();
		
		//验证批处理中是否已经存在该交易的key？已经存在则抛出异常
		if(keySet.contains(t.getKey()) || transSet.contains(t)) {
			throw new BatchTransException("batch trans exist key:" + t.getKey());
		}
		
		//将交易放入批处理中
		transSet.add(t);
		keySet.add(t.getKey());
	}
	
	/**
	 * @Description: 将一个交易集合一次性提交到批处理中
	 * @param transList
	 * @throws
	 */
	public void addTransToBatch(List<T> transList) {
		for(int i = 0; transList != null && i < transList.size(); i++) {
			T t = transList.get(i);
			this.addTransToBatch(t);
		}
	}
	
	/**
	 * @Description: 从批处理中获取交易hash list
	 * @return
	 * @throws
	 */
	public List<String> hashListfromBatch(){
		List<String> hashList = new ArrayList<String>(this.keySet.size() + 3);
		for(Trans t:this.transSet) {
			if(t != null) {
				hashList.add(t.getHash());
			}
		}
		return hashList;
	}
	
	/**
	 * @Description: 获取批处理大小
	 * @return
	 * @throws
	 */
	public int sizeFromBatch() {
		return this.transSet.size();
	}
	
	/**
	 * @Description: key是否有重合的？有返回true  没有返回false
	 * @param keySet
	 * @return
	 * @throws
	 */
	public boolean isExistKeys(HashSet keySet) {
		return this.keySet.contains(keySet);
	}
	
	/**
	 * @Description: 批处理自身检查，如果有问题抛异常。
	 * @throws
	 */
	public void check() {
		if(this.sizeFromBatch() == 0) {
			throw new BatchTransException("batch size is zero");
		}
		
		//自身状态必须是就绪状态
		if(this.status != Trans.TransStatus.prep) {
			throw new BatchTransException("status is not prep");
		}
		
		// id
		String t_id = MerkleUtil.getMerkleRoot(this.hashListfromBatch());
		if(this.id == null) {
			this.id = t_id;
		}else {
			if(!t_id.equals(this.id)) {
				//批处理的摘要不一致，验证失败。
				throw new BatchTransException("check this batch id is fail");
			}
		}
		
		HashSet<String> t_keySet = new HashSet<String>();
		// transSet不能有重复的key交易
		for(Trans t:this.transSet) {
			if(t == null) {
				throw new BatchTransException("trans is null");
			}
			
			//交易自身检查
			t.check();
			
			if(t_keySet.contains(t.getKey())) {
				//已经包含有该key
				throw new BatchTransException("repeated key:" + t.getKey());
			}else {
				//新的key
				t_keySet.add(t.getKey());
			}
		}
	}
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public HashSet<String> getKeySet() {
		return keySet;
	}

	public void setKeySet(HashSet<String> keySet) {
		this.keySet = keySet;
	}

	public HashSet<T> getTransSet() {
		return transSet;
	}

	/**
	 * 该方法只能在系统反序列化的时候时候
	 * @deprecated
	 * @param transSet
	 */
	public void setTransSet(HashSet<T> transSet) {
		this.transSet = transSet;
	}

	public String getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}
	
	public String toJsonString() {
		return JSONObject.toJSONString(this);
	}
	
	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Trans.TransStatus getStatus() {
		return status;
	}

	public void setStatus(Trans.TransStatus status) {
		this.status = status;
	}
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @Description: 批量设置批处理中全部交易的msg
	 * @param message
	 * @throws
	 */
	public void batchSetMsg(String message) {
		this.setMsg(message);
		
		for(Trans t:this.transSet) {
			if(t != null) {
				t.setMsg(message);
			}
		}
	}

	/**
	 * @Description: 批量设置批处理中全部交易的 status
	 * @param failed
	 * @throws
	 */
	public void batchSetStatus(TransStatus status) {
		this.setStatus(status);
		for(Trans t:this.transSet) {
			if(t != null) {
				t.setStatus(status);
			}
		}
	}

	/**
	 * @Description: 只获取当前batch的唯一一笔交易
	 * @return
	 * @throws
	 */
	public Trans oneTrans() {
		for(Trans t: this.transSet) {
			return t;
		}
		return null;
	}
	
	/**
	 * @Description: 获取批处理中的全部key数组
	 * @return
	 * @throws
	 */
	public String[] keyToArray() {
		String[] keys = new String[this.keySet.size()];
		this.keySet.toArray(keys);
		return keys;
	}
	
	
//	public static void main(String[] args) {
//		Trans t1 = new Trans();
//		t1.setKey("xiaoming");
//		t1.setData("{}");
//		
//		Trans t2 = new Trans();
//		t2.setKey("xiaoming2");
//		t2.setData("{}");
//		
//		BatchTrans<Trans> b = new BatchTrans<Trans>();
//		b.addTransToBatch(t1);
//		b.addTransToBatch(t2);
//		
//		b.check();
//		System.out.println(b.getId());
//		System.out.println(b.sizeFromBatch());
//		System.out.println(b.hashListfromBatch().size());
//		
//		
//		System.out.println(b.toJsonString());
//		
//		BatchTrans<Trans> b_new = JSON.parseObject(b.toJsonString(), new TypeReference<BatchTrans<Trans>>(){});
//		System.out.println(b_new.getTransSet().size());
//	}
	
}
