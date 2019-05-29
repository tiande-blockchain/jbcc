/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc;

import org.apache.commons.lang3.StringUtils;

/**
 * function：
 * datetime：2019-03-27 10:03
 * author：warne
 */
public class ConnectionFactory {

    /**
     * Description: 获取连接器实例
     *
     * @return Connection
     */
    public Connection getConnection() {
        return Singleton.INSTANCE.getConnection();
    }

    private static ConnectionFactory getFactory(ConnectionConfig connectionConfig) {
        if (connectionConfig == null)
            throw new ConnectionFactory.ConnectionException("connection config is null ");

        ConnectionFactory.connectionConfig = connectionConfig;
        validateConfig();

        return Singleton.INSTANCE.getFactory();
    }

    public static class ConnectionConfig {
        /**
         * node ip array
         */
        private String[] iptables;
        /**
         * access port
         * <p>
         * defalut port is 18088
         */
        private int port;
        /**
         * access token
         */
        private String token;

        /**
         * request timeout
         * timeout <=0 mean no limit ,but system max timeout is DEFAULT_MAX_TIMEOUT
         */
        private long timeout;
        /**
         * cert path
         * eg:
         * /usr/local/rsa_tiande.pfx
         */
        private String keystorePtah;
        /**
         * password of open cert
         */
        private String keystorePassword;
        private boolean soutSwitch = false;

        public boolean getSoutSwitch() {
            return soutSwitch;
        }

        public String[] getIptables() {
            return iptables;
        }

        public int getPort() {
            return port;
        }

        public String getToken() {
            return token;
        }

        public long getTimeout() {
            return timeout;
        }

        public String getKeystorePtah() {
            return keystorePtah;
        }

        public String getKeystorePassword() {
            return keystorePassword;
        }

        ConnectionConfig(String[] iptables, int port, String token, long timeout, String keystorePtah, String keystorePassword, boolean soutSwitch) {
            this.iptables = iptables;
            this.port = port;
            this.token = token;
            this.timeout = timeout;
            this.keystorePtah = keystorePtah;
            this.keystorePassword = keystorePassword;
            this.soutSwitch = soutSwitch;
        }

        public static ConnectionConfig.Builder builder() {
            return new ConnectionConfig.Builder();
        }

        public static class Builder {
            private String[] iptables;
            private int port = DEFAULT_PORT;
            private String token;
            private long timeout = DEFAULT_TIMEOUT;
            private String keystorePtah;
            private String keystorePassword;
            private boolean soutSwitch;

            Builder() {
            }

            public ConnectionConfig.Builder iptables(String[] iptables) {
                this.iptables = iptables;
                return this;
            }

            public ConnectionConfig.Builder port(int port) {
                this.port = port;
                return this;
            }

            public ConnectionConfig.Builder token(String token) {
                this.token = token;
                return this;
            }

            public ConnectionConfig.Builder timeout(long timeout) {
                if (timeout <= 0) {
                    this.timeout = DEFAULT_MAX_TIMEOUT;
                } else {
                    this.timeout = timeout;
                }
                return this;
            }

            public ConnectionConfig.Builder keystorePtah(String keystorePtah) {
                this.keystorePtah = keystorePtah;
                return this;
            }

            public ConnectionConfig.Builder showPrint() {
                this.soutSwitch = true;
                return this;
            }

            public ConnectionConfig.Builder keystorePassword(String keystorePassword) {
                this.keystorePassword = keystorePassword;
                return this;
            }

            public ConnectionFactory build() {
                ConnectionConfig connectionConfig = new ConnectionConfig(this.iptables, this.port, this.token, this.timeout, this.keystorePtah, this.keystorePassword, soutSwitch);
                return ConnectionFactory.getFactory(connectionConfig);
            }
        }
    }

    private ConnectionFactory(ConnectionConfig connectionConfig) {
        ConnectionFactory.connectionConfig = connectionConfig;
    }

    /**
     * 单例
     */
    private enum Singleton {
        INSTANCE;
        Connection singletonConnection;
        ConnectionFactory factory;

        Singleton() {
            factory = new ConnectionFactory(connectionConfig);
            singletonConnection = new Connection(ipTables, port, token, timeout, keystorePath, keystorePasswd);
        }

        public Connection getConnection() {
            return singletonConnection;
        }

        public ConnectionFactory getFactory() {
            return factory;
        }
    }

    static class ConnectionException extends RuntimeException {
        public ConnectionException() {
            super();
        }

        public ConnectionException(String msg) {
            super(msg);
        }
    }

    public static void validateConfig() {
        if (connectionConfig == null)
            throw new ConnectionException("connection config is null ");

        ipTables = connectionConfig.getIptables();
        if (ipTables == null || ipTables.length == 0 || ipTables[0] == null || ipTables[0].length() == 0) {
            throw new ConnectionException("iptables connfig error, please check iptables ");
        }

        port = connectionConfig.getPort();
        //# 1025~65534
        if (port < 1025 || port > 65534)
            throw new ConnectionException("port error. [1025~65534] ");

        token = connectionConfig.getToken();
        if (StringUtils.isBlank(token))
            throw new ConnectionException("token is empty ");

        keystorePath = connectionConfig.getKeystorePtah();
        if (StringUtils.isBlank(keystorePath))
            throw new ConnectionException("keystore path is empty ");

        keystorePasswd = connectionConfig.getKeystorePassword();
        if (StringUtils.isBlank(keystorePasswd))
            throw new ConnectionException("keystore passwd is empty ");

        //# 超时时间设置为负数或者0表示最大接受的超时为DEFAULT_TIMEOUT
        timeout = connectionConfig.getTimeout();

        //# 是否打印 System.out.print
        soutSwitch = connectionConfig.getSoutSwitch();
    }

    protected static String[] ipTables;
    protected static int port;
    //# 访问天德云区块链的token, 注册成功之后即可获取有效token
    protected static String token;
    //# 访问天德云区块链时需要的证书,注册成功之后即可下载证书,keystorePath为证书存放路径
    protected static String keystorePath;
    //# keystore文件的密码
    protected static String keystorePasswd;
    //# request timeout
    protected static long timeout;
    //# 是否打印 System.out.print
    public static boolean soutSwitch = Boolean.FALSE;

    public final static int DEFAULT_TIMEOUT = 3000;
    public final static int DEFAULT_PORT = 18088;
    public final static int DEFAULT_MAX_TIMEOUT = 15000;
    protected static ConnectionConfig connectionConfig;

    //# 全局变量
    public static final int MAX_TRANS_COUNT = 100; //# 批量交易最大交易量
    public static final int MAX_TRANS_HISTORY_COUNT = 29;  //# 查询历史交易最大量
}
