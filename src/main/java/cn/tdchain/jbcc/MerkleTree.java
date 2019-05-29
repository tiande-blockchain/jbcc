/*
 * Copyright (c) 2017 Beijing Tiande Technology Co., Ltd.
 * All Rights Reserved.
 */
package cn.tdchain.jbcc;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import cn.tdchain.cipher.rsa.Sha256Util;



/**
 * Merkle Utility.
 * @author xiaoming
 * 2019年4月18日
 */
public class MerkleTree {

    public static final int MAGIC_HDR = 0xcdaace99;
    public static final int INT_BYTES = 4;
    public static final int LONG_BYTES = 8;
    public static final byte LEAF_SIG_TYPE = 0x0;
    public static final byte INTERNAL_SIG_TYPE = 0x01;

    private List<String> leafSigs;
    private BinaryNode root;
    private int depth;
    private int nnodes;

    /**
     * Use this constructor to create a MerkleUtil from a list of leaf signatures. The Merkle tree
     * is built from the bottom up.
     * 
     * @param leafSignatures List<String>
     */
    public MerkleTree(List<String> leafSignatures) {
        constructTree(leafSignatures);
    }

    /**
     * Use this constructor when you have already constructed the tree of Nodes (from
     * deserialization).
     * @param treeRoot
     * @param numNodes
     * @param height
     * @param leafSignatures
     */
    public MerkleTree(BinaryNode treeRoot, int numNodes, int height,
            List<String> leafSignatures) {
        root = treeRoot;
        nnodes = numNodes;
        depth = height;
        leafSigs = leafSignatures;
    }

    /**
     * Serialization format:
     * (magicheader:int)(numnodes:int)[(nodetype:byte)(siglength:int)(signature:[]byte)].
     * @return byte[]
     */
    public byte[] serialize() {
        int magicHeaderSz = INT_BYTES;
        int nnodesSz = INT_BYTES;
        int hdrSz = magicHeaderSz + nnodesSz;

        int typeByteSz = 1;
        int siglength = INT_BYTES;

        int parentSigSz = LONG_BYTES;
        int leafSigSz = leafSigs.get(0).getBytes(StandardCharsets.UTF_8).length;

        // some of the internal nodes may use leaf signatures (when "promoted")
        // so ensure that the ByteBuffer overestimates how much space is needed
        // since ByteBuffer does not expand on demand
        int maxSigSz = leafSigSz;
        if (parentSigSz > maxSigSz) {
            maxSigSz = parentSigSz;
        }

        int spaceForNodes = (typeByteSz + siglength + maxSigSz) * nnodes;

        int cap = hdrSz + spaceForNodes;
        ByteBuffer buf = ByteBuffer.allocate(cap);

        buf.putInt(MAGIC_HDR).putInt(nnodes); // header
        serializeBreadthFirst(buf);

        // the ByteBuf allocated space is likely more than was needed
        // so copy to a byte array of the exact size necesssary
        byte[] serializedTree = new byte[buf.position()];
        buf.rewind();
        buf.get(serializedTree);
        return serializedTree;
    }

    /**
     * Serialization format after the header section:
     * [(nodetype:byte)(siglength:int)(signature:[]byte)].
     * 
     * @param buf ByteBuffer
     */
    void serializeBreadthFirst(ByteBuffer buf) {
        Queue<BinaryNode> q = new ArrayDeque<BinaryNode>((nnodes / 2) + 1);
        q.add(root);

        while (!q.isEmpty()) {
            BinaryNode nd = q.remove();
            buf.put(nd.type).putInt(nd.sig.length).put(nd.sig);

            if (nd.left != null) {
                q.add(nd.left);
            }
            if (nd.right != null) {
                q.add(nd.right);
            }
        }
    }

    /**
     * Create a tree from the bottom up starting from the leaf signatures.
     * 
     * @param signatures
     */
    void constructTree(List<String> signatures) {
        if (signatures.size() <= 1) {
            throw new IllegalArgumentException(
                    "Must be at least two signatures to construct a Merkle tree");
        }

        leafSigs = signatures;
        nnodes = signatures.size();
        List<BinaryNode> parents = bottomLevel(signatures);
        nnodes += parents.size();
        depth = 1;

        while (parents.size() > 1) {
            parents = internalLevel(parents);
            depth++;
            nnodes += parents.size();
        }

        root = parents.get(0);
    }

    public int getNumNodes() {
        return nnodes;
    }

    public BinaryNode getRoot() {
        return root;
    }

    public int getHeight() {
        return depth;
    }

    /**
     * Constructs an internal level of the tree.
     * 
     * @param children List<BinaryNode>
     * @return List<BinaryNode>
     */
    List<BinaryNode> internalLevel(List<BinaryNode> children) {
        List<BinaryNode> parents = new ArrayList<BinaryNode>(
                children.size() / 2);

        for (int i = 0; i < children.size() - 1; i += 2) {
            BinaryNode child1 = children.get(i);
            BinaryNode child2 = children.get(i + 1);

            BinaryNode parent = constructInternalNode(child1, child2);
            parents.add(parent);
        }

        if (children.size() % 2 != 0) {
            BinaryNode child = children.get(children.size() - 1);
            BinaryNode parent = constructInternalNode(child, null);
            parents.add(parent);
        }

        return parents;
    }

    /**
     * Constructs the bottom part of the tree - the leaf nodes and their immediate parents. Returns
     * a list of the parent nodes.
     */
    List<BinaryNode> bottomLevel(List<String> signatures) {
        List<BinaryNode> parents = new ArrayList<BinaryNode>(
                signatures.size() / 2);

        for (int i = 0; i < signatures.size() - 1; i += 2) {
            BinaryNode leaf1 = constructLeafNode(signatures.get(i));
            BinaryNode leaf2 = constructLeafNode(signatures.get(i + 1));

            BinaryNode parent = constructInternalNode(leaf1, leaf2);
            parents.add(parent);
        }

        // if odd number of leafs, handle last entry
        if (signatures.size() % 2 != 0) {
            BinaryNode leaf = constructLeafNode(
                    signatures.get(signatures.size() - 1));
            BinaryNode parent = constructInternalNode(leaf, null);
            parents.add(parent);
        }

        return parents;
    }

    private BinaryNode constructInternalNode(BinaryNode child1,
                                             BinaryNode child2) {
        BinaryNode parent = new BinaryNode();
        parent.type = INTERNAL_SIG_TYPE;

        if (child2 == null) {
            parent.sig = child1.sig;
        } else {
            parent.sig = internalHash(child1.sig, child2.sig);
        }

        parent.left = child1;
        parent.right = child2;
        return parent;
    }

    private static BinaryNode constructLeafNode(String signature) {
        BinaryNode leaf = new BinaryNode();
        leaf.type = LEAF_SIG_TYPE;
        leaf.sig = signature.getBytes(StandardCharsets.UTF_8);
        return leaf;
    }

    private byte[] internalHash(byte[] leftChildSig, byte[] rightChildSig) {
        String leftSign = new String(leftChildSig, StandardCharsets.UTF_8);
        String rightSign = new String(rightChildSig, StandardCharsets.UTF_8);
        return Sha256Util.hash(leftSign + rightSign)
                .getBytes(StandardCharsets.UTF_8);
    }

    /**
     * The BinaryNode class should be treated as immutable, though immutable is not enforced in the
     * current design.
     * 
     * A BinaryNode knows whether it is an internal or leaf BinaryNode and its signature.
     * 
     * Internal Nodes will have at least one child (always on the left). Leaf Nodes will have no
     * children (left = right = null).
     */
    public static class BinaryNode {
        public byte type; // INTERNAL_SIG_TYPE or LEAF_SIG_TYPE
        public byte[] sig; // signature of the BinaryNode
        public BinaryNode left;
        public BinaryNode right;

        @Override
        public String toString() {
            String leftType = "<null>";
            String rightType = "<null>";
            if (left != null) {
                leftType = String.valueOf(left.type);
            }
            if (right != null) {
                rightType = String.valueOf(right.type);
            }
            return String.format(
                    "MerkleUtil.BinaryNode<type:%d, sig:%s, left (type): %s, right (type): %s>",
                    type, getBinaryNodeSig(), leftType, rightType);
        }

        public String getBinaryNodeSig() {
            return new String(sig, StandardCharsets.UTF_8);
        }
    }

}
