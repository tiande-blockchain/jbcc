/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc;

import cn.tdchain.Block;
import cn.tdchain.Trans;
import cn.tdchain.TransHead;
import cn.tdchain.cipher.Cipher;
import cn.tdchain.cipher.CipherException;
import cn.tdchain.cipher.Key;
import cn.tdchain.cipher.rsa.RsaUtil;
import cn.tdchain.cipher.utils.HashCheckUtil;
import cn.tdchain.jbcc.net.Net;
import cn.tdchain.jbcc.net.info.Node;
import cn.tdchain.jbcc.net.nio.NioNet;
import cn.tdchain.jbcc.rpc.*;
import cn.tdchain.tdmsp.Msp;
import cn.tdchain.tdmsp.util.Sha1Util;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.tdchain.jbcc.ConnectionFactory.DEFAULT_TIMEOUT;
import static cn.tdchain.jbcc.ConnectionFactory.MAX_TRANS_COUNT;
import static cn.tdchain.jbcc.ConnectionFactory.MAX_TRANS_HISTORY_COUNT;
import static cn.tdchain.jbcc.TransUtil.HASH_LENGTH;

/**
 * function：description
 * datetime：2019-03-27 13:07
 * author：warne
 */
public class Connection {
    /**
     * 获取client的角色
     *
     * @return
     */
    public String getOuRole() {
        if (StringUtils.isBlank(key.getLocalCertBase64String())) {
            throw new RuntimeException("connection init failed");
        }
        X509Certificate clientCert = Msp.base64StringToCert(key.getLocalCertBase64String());
        String organizationName = Msp.getOrganizationName(clientCert);
        return organizationName;
    }

    /**
     * 获取client的连接账户
     *
     * @return
     */
    public String getOuAccount() {
        if (StringUtils.isBlank(key.getLocalCertBase64String())) {
            throw new RuntimeException("connection init failed");
        }
        X509Certificate clientCert = Msp.base64StringToCert(key.getLocalCertBase64String());
        String clientPubliKey = RsaUtil.getPublicKey(clientCert.getPublicKey());
        return Sha1Util.sha1(clientPubliKey);
    }

    /**
     * Description: 添加一笔交易
     *
     * @param trans
     * @return TransHead
     */
    public Result<TransHead> addTrans(Trans trans) {
        //# 判断交易null
        if (trans == null) {
            return nullRpcResult("trans is null");
        }

        BatchTrans<Trans> batch = new BatchTrans<>();
        batch.setConnectionId(this.connectionId);
        batch.addTransToBatch(trans);

        Result<BatchTrans<TransHead>> rpcResult = addBatchTrans(batch);
        BatchTrans<TransHead> batchTrans = rpcResult.getEntity();
        Result<TransHead> result = new Result<>();
        result.setEntity(batchTrans.oneTrans());
        return result;
    }

    /**
     * Description: 批量交易
     *
     * @param batch
     * @return BatchTrans<TransHead>
     */
    public Result<BatchTrans<TransHead>> addBatchTrans(BatchTrans<Trans> batch) {
        //# 判断批量交易null
        if (batch == null || (batch.getTransSet() == null || batch.getTransSet().size() == 0)) {
            return nullRpcResult("batch is empty ");
        }
        batch.setConnectionId(this.connectionId);
        batch.check();

        RPCMessage msg = getMessage();
        msg.setMessageId(batch.getId());
        msg.setMsg(batch.toJsonString());
        msg.setTargetType(RPCMessage.TargetType.TX_WAIT);

        // 异步将交易发送给请求搬运工
        net.request(msg);

        // 异步等待交易返回
        RPCBatchResult batchResult = net.resphone(msg.getMessageId(), 12000);// 12秒超时，如果共识超时也能知道交易是否成功

        if (batchResult.isFail()) {
            return batchResult.getResult();
        }
        List<Result<BatchTrans<TransHead>>> list = batchResult.buildList(new TypeReference<BatchTrans<TransHead>>() {
        });
        Optional<Result<BatchTrans<TransHead>>> first = list.stream().filter(r -> r.isSuccess()).findFirst();
        if (first.isPresent()) {
            return first.get();
        }
        return list.get(0);

    }

    /**
     * Description: 根据块高度查询块信息
     *
     * @param height
     * @return Block<Trans>
     */
    public Result<Block> getBlock(Long height) {

        Map<String, String> command = new HashMap<>();
        command.put("height", height.toString());
        RPCMessage msg = getMessage();
        msg.setMessageId(UUID.randomUUID().toString());
        msg.setCommand(command);
        msg.setTargetType(RPCMessage.TargetType.GET_BLOCK);

        net.request(msg);
        RPCBatchResult batchResult = net.resphone(msg.getMessageId(), 10000);
        if (batchResult.isFail()) {
            return batchResult.getResult();
        }
        List<Result<Block>> list = batchResult.buildList(new TypeReference<Block>() {
        });
        Optional<Result<Block>> first = list.stream().filter(r -> r.isSuccess()).findFirst();
        if (first.isPresent()) {
            return first.get();
        }
        return list.get(0);
    }


    /**
     * Description: 根据height查询block header
     *
     * @param height
     * @return Block
     */
    public Result<Block> getBlockHeaderByHeight(Long height) {
        Map<String, String> command = new HashMap<String, String>();
        command.put("height", height.toString());

        RPCMessage msg = getMessage();
        msg.setCommand(command);
        msg.setMessageId(UUID.randomUUID().toString());
        msg.setTargetType(RPCMessage.TargetType.GET_BLOCK_HEADER);

        net.request(msg);
        //resphonse
        RPCBatchResult batchResult = net.resphone(msg.getMessageId(), 10000);
        if (batchResult.isFail()) {
            return batchResult.getResult();
        }
        List<Result<Block>> list = batchResult.buildList(new TypeReference<Block>() {
        });
        Optional<Result<Block>> first = list.stream().filter(r -> r.isSuccess()).findFirst();
        if (first.isPresent()) {
            return first.get();
        }
        return list.get(0);

    }


    /**
     * Description: 查询最大块信息
     *
     * @return Block<Trans>
     */
    public Result<Block> getMaxBlock() {
        return getBlock(-1L);
    }

    /**
     * Description: 根据hash查询交易信息
     *
     * @param hash
     * @return Trans
     */
    public Result<Trans> getTransByHash(String hash) {
        Result<Trans> result = new Result<>();
        if (!HashCheckUtil.hashCheck(hash) || hash.length() <= HASH_LENGTH) {
            result.setMsg("invalid param");
            result.setStatus(RPCResult.StatusType.fail);
            return result;
        }
        Map<String, String> command = new HashMap<>();
        command.put("hash", hash);
        RPCMessage msg = getMessage();
        msg.setMessageId(UUID.randomUUID().toString());
        msg.setCommand(command);
        msg.setTargetType(RPCMessage.TargetType.GET_TRANS_HASH);
        net.request(msg);
        RPCBatchResult batchResult = net.resphone(msg.getMessageId(), 3000);
        if (batchResult.isFail()) {
            return batchResult.getResult();
        }
        List<Result<Trans>> rpcResults = batchResult.buildList(new TypeReference<Trans>() {
        });
        Optional<Result<Trans>> first = rpcResults.stream().filter(r -> r.isSuccess()).findFirst();
        if (first.isPresent()) {
            return first.get();
        }
        return rpcResults.get(0);
    }

    /**
     * Description: 根据hashlist查询交易
     *
     * @param hashList
     * @return List<Trans>
     */
    public Result<List<Trans>> getTransListByHashList(List<String> hashList) {

        Result<List<Trans>> result = new Result<>();
        if (CollectionUtils.isEmpty(hashList)) {
            result.setMsg("invalid param");
            return result;
        }
        if (hashList.size() > MAX_TRANS_COUNT) {
            result.setMsg("hashList is too large [ less than or equal to \" + MAX_TRANS_COUNT + \" ] ");
        }
        Map<String, String> command = new HashMap<>();
        command.put("hashList", JSONObject.toJSONString(hashList));

        RPCMessage msg = getMessage();
        msg.setMessageId(UUID.randomUUID().toString());//交易hash标识唯一消息
        msg.setCommand(command);
        msg.setTargetType(RPCMessage.TargetType.GET_TRANS_LIST);

        net.request(msg);
        RPCBatchResult batchResult = net.resphone(msg.getMessageId(), 6000);
        if (batchResult.isFail()) {
            return batchResult.getResult();
        }
        List<Result<List<Trans>>> rpcResults = batchResult.buildList(new TypeReference<List<Trans>>() {
        });
        Map<RPCResult.StatusType, List<Result<List<Trans>>>> fresult = rpcResults.stream().collect(Collectors.groupingBy(Result::getStatus));
        List<Result<List<Trans>>> success = fresult.get(RPCResult.StatusType.succes);
        List<Result<List<Trans>>> fail = fresult.get(RPCResult.StatusType.fail);
        int succescount = success == null ? 0 : success.size();
        int failcount = fail == null ? 0 : fail.size();
        if (succescount >= failcount) {
            return success.get(0);
        }
        return fail.get(0);
    }

    /**
     * Description: 根据key维度查询交易
     *
     * @param key
     * @return Trans
     */
    public Result<Trans> getNewTransByKey(String key) {
        Result<Trans> result = new Result<>();
        if (StringUtils.isBlank(key)) {
            result.setMsg("key is blank");
            return result;
        }
        key = key.trim();
        if (SQLCheckUtil.checkSQLError(key)) { //可能存在sql注入的key
            result.setMsg("key is invalid");
            return result;
        }
        Map<String, String> command = new HashMap<>();
        command.put("key", key);
        RPCMessage msg = getMessage();
        msg.setMessageId(UUID.randomUUID().toString());//交易hash标识唯一消息
        msg.setCommand(command);
        msg.setTargetType(RPCMessage.TargetType.GET_TRANS_KEY);

        net.request(msg);
        RPCBatchResult batchResult = net.resphone(msg.getMessageId(), 3000);
        if (batchResult.isFail()) {
            return batchResult.getResult();
        }
        List<Result<Trans>> rpcResults = batchResult.buildList(new TypeReference<Trans>() {
        });

        Map<RPCResult.StatusType, List<Result<Trans>>> fresult = rpcResults.stream().collect(Collectors.groupingBy(Result::getStatus));
        List<Result<Trans>> success = fresult.get(RPCResult.StatusType.succes);
        List<Result<Trans>> fail = fresult.get(RPCResult.StatusType.fail);
        int succescount = success == null ? 0 : success.size();
        int failcount = fail == null ? 0 : fail.size();
        if (succescount >= failcount) {
            return success.get(0);
        }
        return fail.get(0);
    }

    /**
     * Description:根据交易类type查询交易
     * 根据key维度查询交易
     *
     * @return
     */
    public Result<Trans> getSystemNewTrans() {
        RPCMessage msg = getMessage();
        msg.setMessageId(UUID.randomUUID().toString());//交易hash标识唯一消息
        msg.setTargetType(RPCMessage.TargetType.GET_SYSTEM_TRANS_KEY);

        net.request(msg);
        RPCBatchResult batchResult = net.resphone(msg.getMessageId(), 3000);
        if (batchResult.isFail()) {
            return batchResult.getResult();
        }
        List<Result<Trans>> rpcResults = batchResult.buildList(new TypeReference<Trans>() {
        });
        Optional<Result<Trans>> first = rpcResults.stream().filter(r -> r.getStatus() == RPCResult.StatusType.succes).findFirst();
        if (first.isPresent()) {
            return first.get();
        }
        return rpcResults.get(0);
    }

    /**
     * 根据交易类type查询交易
     *
     * @param type
     * @return List<Trans>
     */
    public Result<List<Trans>> getTransListByType(String type) {
        Result<List<Trans>> result = new Result<>();
        if (StringUtils.isBlank(type)) {
            result.setMsg("type is blank");
            return result;
        }
        if (HashCheckUtil.illegalCharacterCheck(type) || type == null || type.length() > 45) {
            result.setMsg("type have Illegal character or length too long.");
            return result;
        }
        Map<String, String> command = new HashMap<>();
        command.put("type", type);
        RPCMessage msg = getMessage();
        msg.setMessageId(UUID.randomUUID().toString());
        msg.setCommand(command);
        msg.setTargetType(RPCMessage.TargetType.GET_TRANS_LIST_BY_TYPE);
        net.request(msg);
        RPCBatchResult batchResult = net.resphone(msg.getMessageId(), 6000);
        if (batchResult.isFail()) {
            return batchResult.getResult();
        }
        List<Result<List<Trans>>> rpcResults = batchResult.buildList(new TypeReference<List<Trans>>() {
        });
        Map<RPCResult.StatusType, List<Result<List<Trans>>>> fresult = rpcResults.stream().collect(Collectors.groupingBy(Result::getStatus));
        List<Result<List<Trans>>> success = fresult.get(RPCResult.StatusType.succes);
        List<Result<List<Trans>>> fail = fresult.get(RPCResult.StatusType.fail);
        int succescount = success == null ? 0 : success.size();
        int failcount = fail == null ? 0 : fail.size();
        if (succescount >= failcount) {
            return success.get(0);
        }
        return fail.get(0);
    }

    /**
     * 根据account 和key查询历史
     *
     * @param account
     * @param key
     * @param query
     * @return
     */
    public Result<RPCPage<Trans>> getTransHistoryByAccountAndKey(String account, String key, RPCPageQuery query) {
        Result<RPCPage<Trans>> result = new Result<>();
        if (StringUtils.isBlank(account)) {
            result.setMsg("account is blank");
            return result;
        }
        if (StringUtils.isBlank(key)) {
            result.setMsg("key is blank");
            return result;
        }
        if (query == null) {
            return nullRpcResult("query params is necessary");
        }
        if (query.getLimit() == null) {
            query.setLimit(20l);
        }
        if (query.getLimit() > 100) {
            return nullRpcResult("query count is limit to 100");
        }
        if (query.getOffset() == null) {
            query.setOffset(0l);
        }
        if (SQLCheckUtil.checkSQLError(key)) {
            result.setMsg("key is sql limit");
            return result;
        }
        Map<String, String> command = new HashMap<>();
        command.put("account", account);
        command.put("key", key);
        command.put("queryPage", JSON.toJSONString(query));
        RPCMessage msg = getMessage();
        msg.setMessageId(UUID.randomUUID().toString());//交易hash标识唯一消息
        msg.setCommand(command);
        msg.setTargetType(RPCMessage.TargetType.GET_ACCOUNT_KEY_TRANS_HISTORY);

        net.request(msg);
        RPCBatchResult batchResult = net.resphone(msg.getMessageId(), 8000);
        if (batchResult.isFail()) {
            return batchResult.getResult();
        }
        List<Result<RPCPage<Trans>>> rpcResults = batchResult.buildList(new TypeReference<RPCPage<Trans>>() {
        });
        Map<RPCResult.StatusType, List<Result<RPCPage<Trans>>>> fresult = rpcResults.stream().collect(Collectors.groupingBy(Result::getStatus));
        List<Result<RPCPage<Trans>>> success = fresult.get(RPCResult.StatusType.succes);
        List<Result<RPCPage<Trans>>> fail = fresult.get(RPCResult.StatusType.fail);
        int succescount = success == null ? 0 : success.size();
        int failcount = fail == null ? 0 : fail.size();
        if (succescount >= failcount) {
            return success.get(0);
        }
        return fail.get(0);
    }


    /**
     * Description:根据key维度查询交易历史
     *
     * @param key
     * @param startIndex
     * @param endIndex
     * @return List<Trans>
     */
    public Result<List<Trans>> getTransHistoryByKey(String key, int startIndex, int endIndex) {
        Result<List<Trans>> result = new Result<>();
        if (StringUtils.isBlank(key)) {
            result.setMsg("key is blank");
            return result;
        }
        if (startIndex < 0 || startIndex > endIndex || ((endIndex - startIndex) >= MAX_TRANS_HISTORY_COUNT)) {
            result.setMsg("startIndex or endIndex error ;endIndex - startIndex not >=" + MAX_TRANS_HISTORY_COUNT);
            return result;
        }
        if (SQLCheckUtil.checkSQLError(key)) {
            result.setMsg("key is sql limit");
            return result;
        }
        Map<String, String> command = new HashMap<>();
        command.put("key", key);
        command.put("startIndex", startIndex + "");
        command.put("endIndex", endIndex + "");
        RPCMessage msg = getMessage();
        msg.setMessageId(UUID.randomUUID().toString());//交易hash标识唯一消息
        msg.setCommand(command);
        msg.setTargetType(RPCMessage.TargetType.GET_TRANS_HISTORY);
        net.request(msg);
        RPCBatchResult batchResult = net.resphone(msg.getMessageId(), 8000);
        if (batchResult.isFail()) {
            return batchResult.getResult();
        }

        List<Result<List<Trans>>> rpcResults = batchResult.buildList(new TypeReference<List<Trans>>() {
        });
        Map<RPCResult.StatusType, List<Result<List<Trans>>>> fresult = rpcResults.stream().collect(Collectors.groupingBy(Result::getStatus));
        List<Result<List<Trans>>> success = fresult.get(RPCResult.StatusType.succes);
        List<Result<List<Trans>>> fail = fresult.get(RPCResult.StatusType.fail);
        int succescount = success == null ? 0 : success.size();
        int failcount = fail == null ? 0 : fail.size();
        if (succescount >= failcount) {
            return success.get(0);
        }
        return fail.get(0);
    }

    /**
     * Description:获取当前区块链的连接数
     *
     * @return int
     */
    public Result<Integer> getConnectionCount() {
        RPCMessage msg = getMessage();
        msg.setMessageId(UUID.randomUUID().toString());//交易hash标识唯一消息
        msg.setTargetType(RPCMessage.TargetType.GET_CONNECTION_COUNT);
        net.request(msg);
        RPCBatchResult batchResult = net.resphone(msg.getMessageId(), 8000);
        if (batchResult.isFail()) {
            return batchResult.getResult();
        }
        List<Result<Integer>> list = batchResult.buildList(new TypeReference<Integer>() {
        });
        Optional<Result<Integer>> first = list.stream().filter(r -> r.isSuccess()).findFirst();
        Integer count;
        if (first.isPresent()) {
            count = connectionCountByResultList(list);
            Result<Integer> r = first.get();
            r.setEntity(count);
            return r;
        }
        return list.get(0);
    }

    /**
     * Description:开启事务
     *
     * @param keys
     * @return boolean
     */
    public boolean startTransaction(String[] keys) {
        if (keys != null && keys.length > 0) {
            //获取事务对象
            Transaction transaction = new Transaction(keys);
            //将事务提交到事务池，如果成功当前现在则锁住所有关于key的添加操作，在事务池中最多保留事务对象到stop time时间
            return ManagerTransactionPool.register(transaction, 6000);
        } else {
            return false;
        }
    }

    /**
     * Description:关闭事务
     *
     * @param keys
     */
    public void stopTransaction(String[] keys) {
        if (keys != null && keys.length > 0) {
            //获取事务对象
            Transaction transaction = new Transaction(keys);
            ManagerTransactionPool.destroy(transaction);
        }
    }

    /**
     * Description:查询交易总数量
     *
     * @return Long
     */
    public Result<Long> getTotalTransCount() {
        Long totalTransCount = 0L;
        RPCMessage msg = getMessage();
        msg.setMessageId(UUID.randomUUID().toString());
        msg.setTargetType(RPCMessage.TargetType.GET_TOTAL_TRANS_COUNT);

        net.request(msg);
        RPCBatchResult batchResult = net.resphone(msg.getMessageId(), 5000);
        if (batchResult.isFail()) {
            return batchResult.getResult();
        }
        List<Result<Long>> list = batchResult.buildList(new TypeReference<Long>() {
        });
        Optional<Result<Long>> first = list.stream().filter(r -> r.isSuccess()).findFirst();
        if (first.isPresent()) {
            return first.get();
        }
        return list.get(0);
    }

    /**
     * Description: 获取当前系统全部节点的node对象集合
     *
     * @return List<Node>
     */
    public List<Node> getBlockChainNodeStatus() {
        List<Node> nodes = net.getNodes();
        return nodes;
    }

    public <T> Result<T> nullRpcResult(String msg) {
        Result<T> nullRpcResult = new Result<>();
        nullRpcResult.setStatus(RPCResult.StatusType.fail);
        nullRpcResult.setMsg(msg);
        return nullRpcResult;
    }

    public Result<RPCPage<TransHead>> getNewTransByAccount(String account, RPCPageQuery query) {
        if (account == null) {
            return nullRpcResult("account is null");
        }
        if (query == null) {
            return nullRpcResult("query params is necessary");
        }
        if (query.getLimit() == null) {
            query.setLimit(20l);
        }
        if (query.getLimit() > 100) {
            return nullRpcResult("query count is limit to 100");
        }
        if (query.getOffset() == null) {
            query.setOffset(0l);
        }
        if (query.getSort() == null) {
            query.setSort(true);
        }
        RPCMessage msg = getMessage();
        msg.setMessageId(UUID.randomUUID().toString());
        msg.setTargetType(RPCMessage.TargetType.GET_NEW_TRANS_LIST);

        Map<String, String> command = new HashMap<>();
        command.put("account", account);
        command.put("queryPage", JSON.toJSONString(query));
        msg.setCommand(command);

        net.request(msg);

        RPCBatchResult batchResult = net.resphone(msg.getMessageId(), 5000);
        System.out.println(JSON.toJSONString(batchResult));
        if (batchResult.isFail()) {
            return batchResult.getResult();
        }
        List<Result<RPCPage<TransHead>>> list = batchResult.buildList(new TypeReference<RPCPage<TransHead>>() {
        });
        Optional<Result<RPCPage<TransHead>>> first = list.stream().filter(r -> r.isSuccess()).findFirst();
        if (first.isPresent()) {
            return first.get();
        }
        System.out.println(JSON.toJSONString(list));
        return list.get(0);
    }

    /**
     * @return String 获取当前connection对象的id
     */
    public String getId() {
        return this.connectionId;
    }


    /**
     * Description: 客户端构造器 ， 默认超时为 DEFAULT_TIMEOUT
     *
     * @param ipArr
     * @param port
     * @param token
     * @param ksPath
     * @param ksPasswd
     */
    protected Connection(String[] ipArr, int port, String token, String ksPath, String ksPasswd) {
        this(ipArr, port, token, DEFAULT_TIMEOUT, ksPath, ksPasswd);
    }

    /**
     * @param ipTable
     * @param port
     * @param key
     * @param token
     * @param cipher
     */
    protected Connection(String[] ipTable, int port, Key key, String token, Cipher cipher) {
        this.key = key;
        this.cipher = cipher;
        /**
         * @Description: 根据初始化的iptable长度计算最小吻合指数。
         */
        this.minSucces = PBFT.getMinByCount(ipTable.length);

        //开启net网络
        openNet(ipTable, port, token, cipher);

        //异步定时同步在线nodes
        asynAskNodes(DEFAULT_TIMEOUT);
    }

    /**
     * DEFAULT_TIMEOUT
     *
     * @param ipArr
     * @param port
     * @param token
     * @param timeout
     * @param ksPath
     * @param ksPasswd
     */
    protected Connection(String[] ipArr, int port, String token, long timeout, String ksPath, String ksPasswd) {

        this.minSucces = PBFT.getMinByCount(ipArr.length);

        //# 准备证书
        readyCert(ksPath, ksPasswd);

        //# 开启网络
        openNet(ipArr, port, token);

        //# 同步节点
        asynAskNodes(timeout);
    }

    protected RPCMessage getMessage() {
        RPCMessage msg = new RPCMessage();
        msg.setSender(this.connectionId);
        return msg;
    }


    private int connectionCountByResultList(List<Result<Integer>> rpcResultList) {
        if (CollectionUtils.isEmpty(rpcResultList)) {
            return 0;
        }
        List<Integer> list = new ArrayList<>();
        for (Result<Integer> result : rpcResultList) {
            try {
                //反序列化可能异常
                int count = result.getEntity();
                list.add(count);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 每个节点的连接数是异步刷新,未必完全一致
        if (list.size() >= this.minSucces || list.size() >= rpcResultList.size() / 2) {
            Map<Integer, Long> collect = list.stream().collect(Collectors.groupingBy(i -> i, Collectors.counting()));
            //{1,1,1,2} max = 3 || {1,2,1,2} max =2
            Long max = Collections.max(collect.values());
            //{1,1,1,2} count = 1 || {1,2,1,2} count =2
            List<Map.Entry<Integer, Long>> sameList = collect.entrySet().stream().filter(en -> en.getValue().longValue() == max.longValue()).collect(Collectors.toList());
            if (sameList.size() >= list.size() / 2) { // 取平均值
                int sum = list.stream().mapToInt(t -> t).sum();
                return sum / list.size();
            } else {
                return sameList.get(0).getKey();
            }
        }
        return 0;
    }

    private List<Trans> transListByResultList(List<Result> results) {
        List<Trans> transList = null;
        for (Result<List<Trans>> result : results) {
            if (stopNext(result)) {
                continue;
            }
            try {
                transList = result.getEntity();
                if (transList != null)
                    break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return transList;
    }

    /**
     * Description: 是否停止往下执行
     *
     * @param result
     * @return boolean
     */
    private boolean stopNext(Result result) {
        if (result == null) {
            return true;
        }
        return false;
    }

    /**
     * Description:开启网络连接
     *
     * @param ipArr
     * @param port
     * @param token
     */
    protected void openNet(String[] ipArr, int port, String token) {
        // 验证token不能为空
        if (StringUtils.isBlank(token)) {
            throw new ParameterException("token is empty ");
        }
        net = new NioNet(ipArr, port, cipher, token, key, connectionId);
        net.start();
        Long start = System.currentTimeMillis();
        while (true) {
            if (net.getTaskSize() >= net.getMinNodeSize()) {
                break;
            }
            if ((System.currentTimeMillis() - start) >= 20000) {
                // 连接超时
                net.stop();
                throw new ConnectionTimeOutException("Connection time out, iptables=" + StringUtils.join(ipArr, ", ") + ",port=" + port + ",token=" + token);
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }

        }
    }

    protected void openNet(String[] ipArr, int port, String token, Cipher cipher) {
        this.cipher = cipher;
        openNet(ipArr, port, token);
    }

    /**
     * Description:异步同步节点信息
     *
     * @param timeout
     */
    protected void asynAskNodes(long timeout) {
        //# 每隔3秒同步一次
        scheduledService.scheduleAtFixedRate(() -> {
            try {
                RPCMessage msg = getMessage();
                msg.setTargetType(RPCMessage.TargetType.GET_NODE_LIST);
                msg.setMessageId(UUID.randomUUID().toString());
                //异步提交请求
                net.request(msg);
                //resphonse
                List<Node> nodes = new ArrayList<>();
                RPCBatchResult batchResult = net.resphone(msg.getMessageId(), timeout);
                if (batchResult.isFail()) {
                    batchResult.getResult(); // 抛出异常？
                    return;
                }
                List<Result<List<Node>>> rpcResultList = batchResult.buildList(new TypeReference<List<Node>>() {
                });
                for (Result<List<Node>> result : rpcResultList) {
                    if (stopNext(result)) {
                        continue;
                    }
                    List<Node> nodeList = result.getEntity();
                    if (nodeList != null && nodeList.size() > 0) {
                        if (nodeList.size() > nodes.size()) {
                            nodes = nodeList;
                        }
                    }
                }
                //获取在线的节点添加到net中
                if (nodes != null && nodes.size() > 0) {
                    for (Node node : nodes) {
                        if (SoutUtil.isOpenSout())
                            System.out.println("copy node id:" + node.getId() + " status=" + node.getStatus());
                        net.addNodeToNodes(node);
                    }
                }
                Thread.sleep(1); //# 释放权限
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 3, TimeUnit.SECONDS);

    }

    /**
     * Description:准备证书
     *
     * @param ksPath
     * @param ksPasswd
     */
    protected void readyCert(String ksPath, String ksPasswd) {
        try {
            String alias = Msp.ORGANIZATION_ALIAS;
            // 读取key store
            KeyStore keyStore = Msp.getKeyStore(ksPath, ksPasswd);

            String privateKey = cipher.getPrivateKeyStringByKeyStore(keyStore, ksPasswd, alias);
            String publicKey = cipher.getPublicKeyStringByStore(keyStore, ksPasswd, alias);

            //获取本地证书,该证书必须时由root证书颁发，否则无法与server建立连接。
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
            String certBase64String = Msp.certToBase64String(cert);

            key.setPrivateKey(privateKey); //缓存私钥字符串
            key.setPublicKey(publicKey); // 缓存公钥字符串
            key.setLocalCertBase64String(certBase64String); //缓存证书base64字符串
        } catch (Exception e) {
            throw new CipherException("get private key by key store error:" + e.getMessage());
        }
    }


    private int minSucces = 1;
    protected Key key = new Key();
    protected Net net;
    private String connectionId = UUID.randomUUID().toString();
    protected Cipher cipher = new Cipher();

    protected final static ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();

}

