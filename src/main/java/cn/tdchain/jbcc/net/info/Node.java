/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.net.info;

import com.alibaba.fastjson.JSONObject;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Description: 节点实体对象描述
 *
 * @author xiaoming
 * 2019年4月18日
 */
public class Node implements Comparable<Node> {
    /**
     * Description: 节点的唯一标识，一般使用本地ip作为唯一标识。
     */
    private String id;

    /**
     * Description: 节点对外的公网ip，可能有多个。
     */
    private HashSet<String> publicIPList = new HashSet<String>();

    /**
     * Description: 节点本机ip集合 包含127.0.0.1
     */
    private HashSet<String> privateIPList = new HashSet<String>();

    /**
     * Description: 缓存配置的 iptables
     */
    private String[] iptables = null;

    /**
     * Description: 节点启动时间戳
     */
    private Long start;

    /**
     * Description: 节点死忙时间
     */
    private Long deadTime;

    /**
     * Description: 节点最近活跃时间，如果最近活跃时间超过12秒则将自己设置成死忙状态
     */
    private Long lastActiveTime;

    /**
     * Description: 节点状态，在线、或者死忙等状态。
     */
    private NodeStatus status;

    /**
     * Description: 节点类型，创世节点还是，后面新加进来的追随节点。
     */
    private NodeType type;

    /**
     * Description: 对外公网ip，用户配置，如果都是内网则可以为null，系统会自动使用内网ip
     */
    private String publicIP;

    /**
     * Description: 节点信誉值，初始值为50.
     */
    private Double reputation;//节点信誉值,初始化默认为50.信誉好的可累加,造成超时的节点会被消减信誉值.

    private Resource resource;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Node() {
    }

    public Node(String[] iptables) {
        if (iptables != null) {
            this.iptables = iptables;
        }
    }

    /**
     * Description: 初始化节点对象，一般在节点启动的时候就执行，而且只能执行一次。
     *
     * @param publicIP
     */
    public void init(String publicIP) {
        initPrivateIPList();

        initId();

        //
        if (publicIP != null && publicIP.length() > 0 && !"null".equals(publicIP)) {
            this.setPublicIP(publicIP);
        }

        this.setStatus(Node.NodeStatus.PRE);//就绪状态
        this.setType(NodeType.FOLLOW_NODE);//默认时跟随节点
        this.setStart(System.currentTimeMillis());
        this.setLastActiveTime(System.currentTimeMillis());
        this.setReputation(50D);//初始化信誉值
    }

    /**
     * Description: 初始化本节点的id，使用本机内网ipv4 作为id
     */
    private void initId() {
        String localHostName = "";
        try {
            localHostName = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        this.id = localIP() + "_" + localHostName;//使用非127.0.0.1的内网ip作为唯一标识,后面加上localHostName防止ip冲突
    }

    private String localIP() {
        Iterator<String> localIP_I = this.privateIPList.iterator();
        while (localIP_I.hasNext()) {
            String local_ip = localIP_I.next();
            if (!"127.0.0.1".equals(local_ip)) {
                return local_ip;
            }
        }

        return null;
    }

    /**
     * Description: 初始化本机全部内网ip
     */
    private void initPrivateIPList() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface networkInterface;
            Enumeration<InetAddress> inetAddresses;
            InetAddress inetAddress;
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    inetAddress = inetAddresses.nextElement();
                    if (inetAddress != null && inetAddress instanceof Inet4Address) {
//						System.out.println("ip=" + inetAddress.getHostAddress() + " isMCNodeLocal=" + inetAddress.+ " isMCSiteLocal=" + inetAddress.isMCSiteLocal() + " isMCLinkLocal=" + inetAddress.isMCLinkLocal() + " isMCOrgLocal=" + inetAddress.isMCOrgLocal() + " isMCGlobal=" + inetAddress.isMCGlobal() + " isMulticastAddress=" + inetAddress.isMulticastAddress());
                        // 内网IPV4
                        String localip = inetAddress.getHostAddress();
                        this.privateIPList.add(localip);
                    }
                    if (!inetAddress.isSiteLocalAddress() && !inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().indexOf(":") == -1) {
                        // 外网IP
                        String netip = inetAddress.getHostAddress();
                        this.publicIPList.add(netip);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * Description: 节点发布TCP服务的ip，使用此ip可以和节点建立连接，首先考虑使用公网ip，如果没有公网ip则选择使用内网ip。
     *
     * @return String
     */
    public String serverIP() {
        if (this.publicIP != null && this.publicIP.length() > 0 && !"null".equals(this.publicIP)) {
            //使用用户配置的对外公网ip
            return this.publicIP;
        }

        String server = null;
        if (this.publicIPList.size() > 0) {
            Iterator<String> p_ip_i = this.publicIPList.iterator();
            while (p_ip_i.hasNext()) {
                String p_ip = p_ip_i.next();
                if (p_ip != null && p_ip.length() > 0) {
                    server = p_ip;//获取公网ip
                    break;
                }
            }

        } else {
            //返回内网ip
            server = localIP();
        }

        return server;
    }

    /**
     * Description: 获取当前节点的全部ip数组
     *
     * @return String[]
     */
    public String[] privateIps() {
        String[] ips = new String[this.privateIPList.size()];
        this.privateIPList.toArray(ips);
        return ips;
    }


    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public Double getReputation() {
        return reputation;
    }

    public void setReputation(Double reputation) {
        this.reputation = reputation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashSet<String> getPublicIPList() {
        return publicIPList;
    }

    public void setPublicIPList(HashSet<String> publicIPList) {
        this.publicIPList = publicIPList;
    }

    public HashSet<String> getPrivateIPList() {
        return privateIPList;
    }

    public void setPrivateIPList(HashSet<String> privateIPList) {
        this.privateIPList = privateIPList;
    }

    public Long getDeadTime() {
        return deadTime;
    }

    public void setDeadTime(Long deadTime) {
        this.deadTime = deadTime;
    }

    public String[] getIptables() {
        return iptables;
    }

    public void setIptables(String[] iptables) {
        this.iptables = iptables;
    }

    public String getPublicIP() {
        return publicIP;
    }

    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }


    public Long getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(Long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }


    /**
     * Description: 节点类型CREATION_NODE:创世节点,就是iptable配置里面的节点     FOLLOW_NODE:追随节点,就是后来加入链的节点.本不在iptable配置中.
     *
     * @author xiaoming
     * 2019年4月18日
     */
    public enum NodeType {
        CREATION_NODE, FOLLOW_NODE
    }

    /**
     * Description:节点状态.DIE:节点死亡状态(网络断开了,或者已经下线了的)      PRE:就绪状态     ASYN:节点正在同步中(在线的,但是不能参与共识节拍)     METRONOMER:节拍者(参与了共识节拍的节点,具有leader权限发布节拍器)
     *
     * @author xiaoming
     * 2019年4月18日
     */
    public enum NodeStatus {
        DIE, PRE, ASYN, METRONOMER
    }

    /**
     * Description: 节点角色 light:轻节点，只能同步交易。   heavy：重节点，参与共识。
     *
     * @author xiaoming
     * 2019年4月18日
     */
    public enum Role {
        light, heavy
    }

    @Override
    public int compareTo(Node o) {
        //信誉值越大越排在前面，Node.start创建时间越早越排在前面。如果时间差异再3秒内则按照信誉值排序，如果差异超过3秒则按照时间排序
        Long count = this.start - o.getStart();
        if (count > 10000 || count < -10000) {
            //差异超过10秒
            if (this.start == -1L) {
                return -1;
            } else if (this.start < o.getStart()) {
                return -1;
            } else if (this.start > o.getStart()) {
                return 1;
            } else {
                return 0;
            }
        } else {
            if (this.reputation > o.reputation) {
                return -1;
            } else if (this.reputation < o.reputation) {
                return 1;
            } else {
                return 0;
            }
        }

    }

    /**
     * Description: 降低信誉，每次减 1
     */
    public void reduceReputation() {
        synchronized (reputation) {
            this.reputation--;
        }
    }

    public String toJSONString() {
        return JSONObject.toJSONString(this);
    }

    /**
     * Description: 添加公网ip
     *
     * @param publicIP
     */
    public void addPublicIp(String publicIP) {
        this.publicIPList.add(publicIP);
    }

    /**
     * Description: 检测识别该ip是否是本届点的ip
     *
     * @param ip
     * @return boolean
     */
    public boolean isLocalIp(String ip) {
        //优先检查
        boolean flag = this.publicIPList.contains(ip);
        if (!flag) {
            flag = this.privateIPList.contains(ip);
        }

        return flag;
    }

    //# +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /**
     * //# 扩展参数名称
     */
    //# +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    /**
     * Description: //# 节点名称，来自于配置文件tdbc.system.node.name
     */
    private String name;
    /**
     * Description:  //# 经度
     */
    private String longitude;
    /**
     * Description:   //# 纬度
     */
    private String latitude;
    /**
     * Description:   //# 节点所在地址
     */
    private String location;

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    // --------------------------------------------REPUTATION_REQ-----------------------------------------
    private Double addReputationDefaultValue = 0.01D;
    public void addReputation(){
        synchronized (reputation) {
            this.reputation += addReputationDefaultValue;
        }
    }

    public static class Resource{
        /* 可使用内存. */
        private long totalMemory;

        /* 剩余内存. */
        private long freeMemory;

        /* 操作系统. */
        private String osName;

        /* 已使用的物理内存. */
        private long usedMemory;

        /* 线程总数. */
        private int totalThread;

        /* cpu使用率. */
        private double cpuRatio;

        /* 系统磁盘总量 */
        private long totalDisk;

        /* 系统空闲磁盘 */
        private long freeDisk;

        /* 已使用磁盘 */
        private long usedDisk;

        public long getTotalMemory() {
            return totalMemory;
        }

        public void setTotalMemory(long totalMemory) {
            this.totalMemory = totalMemory;
        }

        public long getFreeMemory() {
            return freeMemory;
        }

        public void setFreeMemory(long freeMemory) {
            this.freeMemory = freeMemory;
        }

        public String getOsName() {
            return osName;
        }

        public void setOsName(String osName) {
            this.osName = osName;
        }

        public long getUsedMemory() {
            return usedMemory;
        }

        public void setUsedMemory(long usedMemory) {
            this.usedMemory = usedMemory;
        }

        public int getTotalThread() {
            return totalThread;
        }

        public void setTotalThread(int totalThread) {
            this.totalThread = totalThread;
        }

        public double getCpuRatio() {
            return cpuRatio;
        }

        public void setCpuRatio(double cpuRatio) {
            this.cpuRatio = cpuRatio;
        }

        public long getTotalDisk() {
            return totalDisk;
        }

        public void setTotalDisk(long totalDisk) {
            this.totalDisk = totalDisk;
        }

        public long getFreeDisk() {
            return freeDisk;
        }

        public void setFreeDisk(long freeDisk) {
            this.freeDisk = freeDisk;
        }

        public long getUsedDisk() {
            return usedDisk;
        }

        public void setUsedDisk(long usedDisk) {
            this.usedDisk = usedDisk;
        }
    }

}