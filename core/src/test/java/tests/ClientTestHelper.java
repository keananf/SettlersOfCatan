package tests;

import board.Edge;
import board.Node;
import client.ClientGame;
import enums.Colour;
import protocol.BoardProtos;
import protocol.BuildProtos;
import protocol.EnumProtos;

public class ClientTestHelper extends TestHelper
 {
     ClientGame clientGame;

    public void processRoadEvent(Edge edge, Colour col)
    {
        // Set up request
        BuildProtos.RoadProto.Builder req = BuildProtos.RoadProto.newBuilder();
        BoardProtos.EdgeProto e = edge.toEdgeProto();
        BuildProtos.PointProto p1 = e.getP1(), p2 = e.getP2();
        req.setP1(p1);
        req.setP2(p2);
        req.setPlayerId(Colour.toProto(col));

        clientGame.setTurn(col);
        clientGame.processRoad(req.build());
    }

    public void processSettlementEvent(Node node, Colour col)
    {
        // Set up request
        BuildProtos.BuildingProto.Builder req = BuildProtos.BuildingProto.newBuilder();
        BoardProtos.NodeProto n = node.toProto();
        req.setP(n.getP());
        req.setPlayerId(Colour.toProto(col));
        req.setType(EnumProtos.BuildingTypeProto.SETTLEMENT);

        clientGame.setTurn(col);
        clientGame.processNewBuilding(req.build());
    }
}
