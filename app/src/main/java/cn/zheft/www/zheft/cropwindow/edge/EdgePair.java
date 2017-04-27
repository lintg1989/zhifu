package cn.zheft.www.zheft.cropwindow.edge;

/**
 * Created by Lin on 2017/4/21.
 */

public class EdgePair {
    // Member Variables ////////////////////////////////////////////////////////

    public Edge primary;
    public Edge secondary;

    // Constructor /////////////////////////////////////////////////////////////

    public EdgePair(Edge edge1, Edge edge2) {
        primary = edge1;
        secondary = edge2;
    }
}
