package tests;

import board.Edge;
import board.Hex;
import board.Node;
import client.ClientGame;
import enums.Colour;
import enums.DevelopmentCardType;
import enums.ResourceType;
import game.build.City;
import game.build.Road;
import game.build.Settlement;
import protocol.BoardProtos;
import protocol.BuildProtos;
import protocol.EnumProtos;

import java.util.Map;

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

        clientGame.getPlayer().grantResources(Road.getRoadCost());
        clientGame.setTurn(col);
        try
        {
            clientGame.processRoad(req.build());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void processSettlementEvent(Node node, Colour col, EnumProtos.BuildingTypeProto type)
    {
        // Set up request
        BuildProtos.BuildingProto.Builder req = BuildProtos.BuildingProto.newBuilder();
        BoardProtos.NodeProto n = node.toProto();
        req.setP(n.getP());
        req.setPlayerId(Colour.toProto(col));
        req.setType(type);
        Map<ResourceType, Integer> grant = type.equals(EnumProtos.BuildingTypeProto.CITY)
                                        ? City.getCityCost() : Settlement.getSettlementCost();

        clientGame.getPlayer().grantResources(grant);
        clientGame.setTurn(col);
        try
        {
            clientGame.processNewBuilding(req.build(), false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

     public void processPlayKnightEvent(Hex h, Colour player)
     {
         // Make a move robber event
         BuildProtos.PointProto.Builder p = BuildProtos.PointProto.newBuilder();
         p.setX(h.getX());
         p.setY(h.getY());

         clientGame.getPlayer().grantResources(DevelopmentCardType.getCardCost());
         clientGame.setTurn(player);
         try
         {
             clientGame.recordDevCard(Colour.toProto(player));
             clientGame.processPlayedDevCard(EnumProtos.DevelopmentCardProto.KNIGHT);
             clientGame.moveRobber(p.build());
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
     }

     public void processPlayedDevCard(EnumProtos.DevelopmentCardProto type, Colour player)
     {
         clientGame.getPlayer().grantResources(DevelopmentCardType.getCardCost());
         clientGame.setTurn(player);
         try
         {
             clientGame.recordDevCard(Colour.toProto(player));
             clientGame.processPlayedDevCard(type);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
     }
 }
