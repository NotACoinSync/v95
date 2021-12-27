package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import client.MapleCharacter;
import client.MapleClient;
import server.maps.objects.AbstractMapleMapObject;
import server.maps.objects.MapleMapObjectType;
import tools.packets.PacketHelper;
import tools.packets.WvsContext;
import tools.packets.CField.MiniRoomBaseDlg;
import tools.packets.CField.userpool.UserCommon;

public class MapleMiniGame extends AbstractMapleMapObject {

    private MapleCharacter owner;
    private MapleCharacter visitor;
    private String GameType = null;
    private int[] piece = new int[250];
    private List<Integer> list4x3 = new ArrayList<>();
    private List<Integer> list5x4 = new ArrayList<>();
    private List<Integer> list6x5 = new ArrayList<>();
    private String description;
    private String password = null;
    private int currentTurn = 2;
    private int omokSetType;
    private int firstslot = 0;
    private int visitorpoints = 0;
    private int ownerpoints = 0;
    private int matchestowin = 0;
    private boolean isReady = false;

    public MapleMiniGame(MapleCharacter owner, String description) {
        this.owner = owner;
        this.description = description;
    }

    public boolean hasFreeSlot() {
        return visitor == null;
    }

    public boolean isOwner(MapleCharacter c) {
        return owner.equals(c);
    }

    public void addVisitor(MapleCharacter challenger) {
        visitor = challenger;
        if (GameType.equals("omok")) {
            this.getOwner().getClient().announce(PacketHelper.getMiniGameNewVisitor(challenger, 1));
            this.getOwner().getMap().announce(UserCommon.MiniRoomBalloon.addOmokBox(owner, 2, 0));
        }
        if (GameType.equals("matchcard")) {
            this.getOwner().getClient().announce(PacketHelper.getMatchCardNewVisitor(challenger, 1));
            this.getOwner().getMap().announce(UserCommon.MiniRoomBalloon.addMatchCardBox(owner, 2, 0));
        }
    }

    public void removeVisitor(MapleCharacter challenger) {
        if (visitor.getId() == challenger.getId()) {
            visitor = null;
            this.getOwner().getClient().announce(PacketHelper.getMiniGameRemoveVisitor());
            if (GameType.equals("omok")) {
                this.getOwner().getMap().announce(UserCommon.MiniRoomBalloon.addOmokBox(owner, 1, 0));
            }
            if (GameType.equals("matchcard")) {
                this.getOwner().getMap().announce(UserCommon.MiniRoomBalloon.addMatchCardBox(owner, 1, 0));
            }
        }
    }

    public boolean isVisitor(MapleCharacter challenger) {
        return visitor == challenger;
    }

    public void broadcastToVisitor(final byte[] packet) {
        if (visitor != null) {
            visitor.getClient().announce(packet);
        }
    }

    public void setFirstSlot(int type) {
        firstslot = type;
    }

    public int getFirstSlot() {
        return firstslot;
    }

    public void setPassword(String pass) {
        password = pass;
    }

    public String getPassword() {
        return password;
    }

    public void setOwnerPoints() {
        ownerpoints++;
        if (ownerpoints + visitorpoints == matchestowin) {
            if (ownerpoints == visitorpoints) {
                this.broadcast(PacketHelper.getMatchCardTie(this));
            } else if (ownerpoints > visitorpoints) {
                this.broadcast(PacketHelper.getMatchCardOwnerWin(this));
            } else {
                this.broadcast(PacketHelper.getMatchCardVisitorWin(this));
            }
            ownerpoints = 0;
            visitorpoints = 0;
        }
    }

    public void setVisitorPoints() {
        visitorpoints++;
        if (ownerpoints + visitorpoints == matchestowin) {
            if (ownerpoints > visitorpoints) {
                this.broadcast(PacketHelper.getMiniGameOwnerWin(this));
            } else if (visitorpoints > ownerpoints) {
                this.broadcast(PacketHelper.getMiniGameVisitorWin(this));
            } else {
                this.broadcast(PacketHelper.getMiniGameTie(this));
            }
            ownerpoints = 0;
            visitorpoints = 0;
        }
    }

    public void setMatchesToWin(int type) {
        matchestowin = type;
    }

    public void setOmokSetType(int omokSetType) {
        this.omokSetType = omokSetType;
    }

    public int getOmokSetType() {
        return omokSetType;
    }

    public void setGameType(String game) {
        GameType = game;
        if (game.equals("matchcard")) {
            if (matchestowin == 6) {
                for (int i = 0; i < 6; i++) {
                    list4x3.add(i);
                    list4x3.add(i);
                }
            } else if (matchestowin == 10) {
                for (int i = 0; i < 10; i++) {
                    list5x4.add(i);
                    list5x4.add(i);
                }
            } else {
                for (int i = 0; i < 15; i++) {
                    list6x5.add(i);
                    list6x5.add(i);
                }
            }
        }
    }

    public String getGameType() {
        return GameType;
    }

    public void shuffleList() {
        if (matchestowin == 6) {
            Collections.shuffle(list4x3);
        } else if (matchestowin == 10) {
            Collections.shuffle(list5x4);
        } else {
            Collections.shuffle(list6x5);
        }
    }

    public int getCardId(int slot) {
        int cardid;
        if (matchestowin == 6) {
            cardid = list4x3.get(slot - 1);
        } else if (matchestowin == 10) {
            cardid = list5x4.get(slot - 1);
        } else {
            cardid = list6x5.get(slot - 1);
        }
        return cardid;
    }

    public int getMatchesToWin() {
        return matchestowin;
    }

    public void broadcast(final byte[] packet) {
        if (owner.getClient() != null && owner.getClient().getSession() != null) {
            owner.getClient().announce(packet);
        }
        broadcastToVisitor(packet);
    }

    public void chat(MapleClient c, String chat) {
        broadcast(MiniRoomBaseDlg.getPlayerShopChat(c.getPlayer(), chat, isOwner(c.getPlayer())));
    }

    public void sendOmok(MapleClient c, int type) {// type is the omok set you are using
        c.announce(PacketHelper.getMiniGame(c, this, isOwner(c.getPlayer()), type));
    }

    public void sendMatchCard(MapleClient c, int type) {
        c.announce(PacketHelper.getMatchCard(c, this, isOwner(c.getPlayer()), type));
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public MapleCharacter getVisitor() {
        return visitor;
    }

    public void setPiece(int move1, int move2, int type, MapleCharacter chr) {
        if (type <= 0 || type > 2) {
            // chr.getAutobanManager().addPoint(AutobanFactory.PACKET_EDIT, "Invalid omok piece.");
            System.out.println("Invalid omok piece");
            chr.getClient().announce(WvsContext.enableActions());
            return;
        }
        if (currentTurn != type) {
            System.out.println("Type: " + type + " CurrentTurn: " + currentTurn);
            chr.getClient().announce(WvsContext.enableActions());
            return;
        }
        /*if(idToPiece[type] != chr.getId()){
			System.out.println("incorrect piece type for id");
			chr.getClient().announce(CWvsContext.enableActions());
			return;
		}*/
        if (move1 > 14 || move2 > 14) {
            // chr.getAutobanManager().addPoint(AutobanFactory.PACKET_EDIT, "Invalid omok location.");
            System.out.println("invalid omok location.");
            chr.getClient().announce(WvsContext.enableActions());
            return;
        }
        int slot = move2 * 15 + move1 + 1;
        if (piece[slot] == 0) {
            piece[slot] = type;
            this.broadcast(PacketHelper.getMiniGameMoveOmok(this, move1, move2, type));
            setCurrentTurn(type == 2 ? 1 : 2);
            for (int y = 0; y < 15; y++) {
                for (int x = 0; x < 15; x++) {
                    if (searchCombo(x, y, type)) {
                        if (this.isOwner(chr)) {
                            this.broadcast(PacketHelper.getMiniGameOwnerWin(this));
                            this.setCurrentTurn(2);
                        } else {
                            this.broadcast(PacketHelper.getMiniGameVisitorWin(this));
                            this.setCurrentTurn(1);
                        }
                        for (int y2 = 0; y2 < 15; y2++) {
                            for (int x2 = 0; x2 < 15; x2++) {
                                int slot2 = (y2 * 15 + x2 + 1);
                                piece[slot2] = 0;
                            }
                        }
                    }
                }
            }
            for (int y = 0; y < 15; y++) {
                for (int x = 4; x < 15; x++) {
                    if (searchCombo2(x, y, type)) {
                        if (this.isOwner(chr)) {
                            this.broadcast(PacketHelper.getMiniGameOwnerWin(this));
                            this.setCurrentTurn(2);
                        } else {
                            this.broadcast(PacketHelper.getMiniGameVisitorWin(this));
                            this.setCurrentTurn(1);
                        }
                        for (int y2 = 0; y2 < 15; y2++) {
                            for (int x2 = 0; x2 < 15; x2++) {
                                int slot2 = (y2 * 15 + x2 + 1);
                                piece[slot2] = 0;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean searchCombo(int x, int y, int type) {
        int slot = y * 15 + x + 1;
        for (int i = 0; i < 5; i++) {
            if (piece[slot + i] == type) {
                if (i == 4) {
                    return true;
                }
            } else {
                break;
            }
        }
        for (int j = 15; j < 17; j++) {
            for (int i = 0; i < 5; i++) {
                if (piece[slot + i * j] == type) {
                    if (i == 4) {
                        return true;
                    }
                } else {
                    break;
                }
            }
        }
        return false;
    }

    private boolean searchCombo2(int x, int y, int type) {
        int slot = y * 15 + x + 1;
        for (int j = 14; j < 15; j++) {
            for (int i = 0; i < 5; i++) {
                if (piece[slot + i * j] == type) {
                    if (i == 4) {
                        return true;
                    }
                } else {
                    break;
                }
            }
        }
        return false;
    }

    public String getDescription() {
        return description;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        this.isReady = ready;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(int currentTurn) {
        this.currentTurn = currentTurn;
    }

    @Override
    public void sendDestroyData(MapleClient client) {
    }

    @Override
    public void sendSpawnData(MapleClient client) {
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.MINI_GAME;
    }

    @Override
    public MapleMiniGame clone() {
        return null;
    }
}
