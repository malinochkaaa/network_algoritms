//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.example;

import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.core.Protocol;

public class IdleProtocolWithCoords implements Protocol, Linkable {
    private static final int DEFAULT_INITIAL_CAPACITY = 10;
    private static final String PAR_INITCAP = "capacity";
    protected Node[] neighbors;

    private float X;
    private float Y;

    public void setX(float x) {
        X = x;
    }

    public void setY(float y) {
        Y = y;
    }

    public float getX() {
        return X;
    }

    public float getY() {
        return Y;
    }
    protected int len;

    public IdleProtocolWithCoords(String var1) {
        this.neighbors = new Node[Configuration.getInt(var1 + "." + "capacity", 10)];
        this.len = 0;
    }

    public Object clone() {
        IdleProtocolWithCoords var1 = null;

        try {
            var1 = (IdleProtocolWithCoords) super.clone();
        } catch (CloneNotSupportedException var3) {
        }

        var1.neighbors = new Node[this.neighbors.length];
        System.arraycopy(this.neighbors, 0, var1.neighbors, 0, this.len);
        var1.len = this.len;
        return var1;
    }

    public boolean contains(Node var1) {
        for(int var2 = 0; var2 < this.len; ++var2) {
            if (this.neighbors[var2] == var1) {
                return true;
            }
        }

        return false;
    }

    public boolean addNeighbor(Node var1) {
        for(int var2 = 0; var2 < this.len; ++var2) {
            if (this.neighbors[var2] == var1) {
                return false;
            }
        }

        if (this.len == this.neighbors.length) {
            Node[] var3 = new Node[3 * this.neighbors.length / 2];
            System.arraycopy(this.neighbors, 0, var3, 0, this.neighbors.length);
            this.neighbors = var3;
        }

        this.neighbors[this.len] = var1;
        ++this.len;
        return true;
    }

    public Node getNeighbor(int var1) {
        return this.neighbors[var1];
    }

    public int degree() {
        return this.len;
    }

    public void pack() {
        if (this.len != this.neighbors.length) {
            Node[] var1 = new Node[this.len];
            System.arraycopy(this.neighbors, 0, var1, 0, this.len);
            this.neighbors = var1;
        }
    }

    public String toString() {
        if (this.neighbors == null) {
            return "DEAD!";
        } else {
            StringBuffer var1 = new StringBuffer();
            var1.append("len=" + this.len + " maxlen=" + this.neighbors.length + " [");

            for(int var2 = 0; var2 < this.len; ++var2) {
                var1.append(this.neighbors[var2].getIndex() + " ");
            }

            return var1.append("]").toString();
        }
    }

    public void onKill() {
        this.neighbors = null;
        this.len = 0;
    }
}
