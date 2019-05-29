/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc.rpc.io.eclc;

import cn.tdchain.cipher.Key;
import cn.tdchain.cipher.rsa.AesUtil;
import cn.tdchain.cipher.rsa.RsaUtil;
import cn.tdchain.jbcc.SoutUtil;
import cn.tdchain.jbcc.net.ConnectionCount;
import cn.tdchain.jbcc.rpc.BlackList;
import cn.tdchain.tdmsp.Msp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.cert.X509Certificate;
import java.util.UUID;

/**
 * 对等口令长连接协议：拥有相同口令的A和B可以建立长连接的socket连接。
 * 协议目的：防止恶意程序经过公网开放的ip和端口对区块链进行长连接攻击，也就意味着拥有口令的节点才能真正和其他节点进行建立网络连接。提升区块链网络安全能力/防止ddos攻击/提升网络传输效率。
 *  
 * 协议步骤：
 * 1. A用户用口令“123”对明文“ABC”进行加密，并得到密文“@A”发送给B用户
 * 2. B用户收到A寄来的密文，同时进行解密得出明文“ABC”，此时B用户用自己id口令“123”对明文“BCD”进行加密得出密文“@B”
 * B用户同时将“ABC”和“@B”返回给A用户
 * 3. A用户收到B的响应对比返回的明文是“ABC”，同时也要对B发过来的密文进行解密得出明文“BCD”，最后将明文“BCD”发送给B用户。
 * 4. B用户收到A用户发送过来的明文是“BCD”和自己在上步骤使用的明文是一致的，此时协议完成，A和B可以建立socket长连接。
 *
 * @author xiaoming
 */
public class EclcServerSocket extends ServerSocket {
    private Key key = null;
    private String passwd = "1234567890123456";
    private long timeOut = 5000;

    public EclcServerSocket(int port, String passwd, Key key) throws IOException {
        super(port);
        this.passwd = passwd;
        this.key = key;
    }

    public EclcServerSocket(int port, int backlog, InetAddress bindAddr, String passwd, Key key) throws IOException {
        super(port, backlog, bindAddr);
        this.passwd = passwd;
        this.key = key;
    }

    public EclcServerSocket(int port, int backlog, String passwd, Key key) throws IOException {
        super(port, backlog);
        this.passwd = passwd;
        this.key = key;
    }

    public ClientSocket accept2() throws IOException {
        ClientSocket clientSocket = null;
        Socket client = super.accept();
        if (client.isConnected()) {
            if (BlackList.isBlackListByHost(client.getLocalAddress().getHostAddress())) {
                // 已经在黑名单列表中拒绝连接,错误的token会被放进黑名单
                if (client != null) {
                    client.close();
                }
                throw new EclcException("Eclc  failed! this host is a blacklist.");
            }


            OutputStreamWriter os = new OutputStreamWriter(client.getOutputStream());
            BufferedWriter bwrite = new BufferedWriter(os);
            InputStreamReader ir = new InputStreamReader(client.getInputStream());
            BufferedReader bread = new BufferedReader(ir);

            // 1. 等待客户端发来密文
            Result result1 = new Result();
            try {
                waitLine(result1, bread);
            } catch (Exception e) {
                // add to black list
                BlackList.addBlackListByHost(client.getLocalAddress().getHostAddress());
                if (client != null) {
                    client.close();
                }
                throw new EclcException(e.getMessage());
            }

            String c_data = AesUtil.decrypt(result1.line, passwd);
            String s_data = UUID.randomUUID().toString();
            String s_r_data = c_data + ";" + AesUtil.encrypt(s_data, passwd);

            // 2.写回客户端
            bwrite.write(s_r_data);
            bwrite.newLine();
            bwrite.flush();

            // 3. 阻塞等待客户端的最后响应
            Result result2 = new Result();
            try {
                waitLine(result2, bread);
            } catch (Exception e) {
                // add to black list
                BlackList.addBlackListByHost(client.getLocalAddress().getHostAddress());
                if (client != null) {
                    client.close();
                }
                throw new EclcException(e.getMessage());
            }
            if (s_data.equals(result2.line)) {

                //接收客户端发送的证书，验证证书是否是合法证书，如果是则提取公钥，否则端口连接。
                String keyAndConnId = bread.readLine();
                String[] splits = keyAndConnId.split(";");
                String clientCertBase64Str = splits[0];
                String connectId = splits[1];
                if (clientCertBase64Str == null || clientCertBase64Str.length() == 0) {
                    //null证书
                    throw new EclcException("Eclc  failed! null certificate.");
                } else {
                    X509Certificate clientCert = Msp.base64StringToCert(clientCertBase64Str);
                    boolean leg = Msp.validateCert(Msp.base64StringToCert(key.getRootCertBase64String()), clientCert);//验证客户端证书是否合法?
                    if (leg) {
                        //合法证书,提取公钥。
                        String clientPubliKey = RsaUtil.getPublicKey(clientCert.getPublicKey());
                        clientSocket = new ClientSocket(client, clientPubliKey);
                        if (SoutUtil.isOpenSout())
                            System.out.println(connectId + "::::::::::::" + clientCertBase64Str);
                        String orgName = Msp.getOrganizationName(clientCert);
                        if (ConnectionCount.newInstance().checkSingle(connectId, clientPubliKey)) {
                            ConnectionCount.newInstance().handleConnection(connectId, clientPubliKey, orgName, null);
                        } else {
                            throw new EclcException("Eclc shake  failed! Illegal certificate. params");
                        }
                        //发送本地server端的公钥给客户端
                        bwrite.write(this.key.getPublicKey());
                        bwrite.newLine();
                        bwrite.flush();

                        //允许建立长连接
                        client.setSoTimeout(0);
                    } else {
                        //非法证书
                        throw new EclcException("Eclc  failed! Illegal certificate.");
                    }
                }


            } else {
                // add to black list
                BlackList.addBlackListByHost(client.getLocalAddress().getHostAddress());
                if (client != null) {
                    client.close();
                }
                throw new EclcException("Eclc  failed! passwd is not agreement.");
            }

            return clientSocket;
        } else {
            throw new EclcException("Eclc  failed! client is not connection.");
        }

    }


    private void waitLine(Result result, BufferedReader bread) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result.line = bread.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        /* wait a result line */
        long start = System.currentTimeMillis();
        while ((System.currentTimeMillis() - start) < timeOut) {
            if (result.line != null && result.line.length() > 0) {
                break;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }

        if (result.line == null) {
            throw new EclcException("read time out.");
        }
    }

    class Result {
        public String line;
    }

}
