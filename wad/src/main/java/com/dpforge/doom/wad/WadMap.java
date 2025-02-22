package com.dpforge.doom.wad;

public class WadMap {
    public final String name;

    public Thing[] things;
    public LineDef[] lineDefs;
    public SideDef[] sideDefs;
    public Vertex[] vertexes;
    public Seg[] segs;
    public SSector[] ssectors;
    public Node[] nodes;
    public Sector[] sectors;

    public WadMap(String name) {
        this.name = name;
    }
}
