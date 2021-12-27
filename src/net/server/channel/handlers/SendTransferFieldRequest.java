package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.MapleInventoryType;
import java.net.InetAddress;
import java.net.UnknownHostException;
import net.AbstractMaplePacketHandler;
import server.MapleInventoryManipulator;
import server.MaplePortal;
import server.MapleTrade;
import server.maps.MapleMap;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.userpool.UserLocal;
import tools.packets.ClientSocket;
import tools.packets.EffectPacket;
import tools.packets.Field;
import tools.packets.WvsContext;

public final class SendTransferFieldRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player.isBanned() || player == null) {
            c.disconnect(true, false);
            return;
        }
        if (player.getTrade() != null) {
            MapleTrade.cancelTrade(player);
        }
        if (iPacket.available() == 0) { // Cash Shop :)
            if (!player.getCashShop().isOpened()) {
                c.disconnect(false, false);
                return;
            }
            String[] socket = c.getChannelServer().getIP().split(":");
            player.saveToDB();
            c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
            try {
                c.announce(ClientSocket.MigrateCommand(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
            } catch (UnknownHostException ex) {
                Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
            }
        } else {
            if (player.getCashShop().isOpened()) {
                c.disconnect(false, false);
                return;
            }
            try {
                byte type = iPacket.readByte(); // 1 = from dying 0 = regular portals
                int dwTargetField = iPacket.readInt();
                String startwp = iPacket.readMapleAsciiString();
                boolean useSafetyCharm = iPacket.readByte() > 0;
                byte days = iPacket.readByte();
                byte times = iPacket.readByte();
                MaplePortal portal = player.getMap().getPortal(startwp);
                if (dwTargetField != -1 && !player.isAlive()) {
                    boolean executeStandardPath = true;
                    if (player.getEventInstance() != null) {
                        executeStandardPath = player.getEventInstance().revivePlayer(player, useSafetyCharm);
                    }
                    if (executeStandardPath) {
                        MapleMap to = player.getMap();
                        if (useSafetyCharm && player.getItemQuantity(5510000, false) > 0) {
                            MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, 5510000, 1, true, true);
                            player.announce(EffectPacket.Local.ProtectOnDieItemUse(useSafetyCharm, days, times, 5510000));
                            player.setHp(player.getMaxHp());
                            player.setMp(player.getMaxMp());
                        } else {
                            player.cancelAllBuffs();
                            to = player.getMap().getReturnMap();
                            player.setStance(0);
                            player.setHp(50);
                        }
                        player.changeMap(to, to.getPortal(0));
                    }
                } else if (dwTargetField != -1) {// Thanks celino for saving me some time (:
                    final int divi = player.getMapId() / 100;
                    boolean warp = false;
                    if (divi == 0) {
                        if (dwTargetField == 10000) {
                            warp = true;
                        } else {
                            AutobanFactory.PACKET_EDIT.alert(player, "Tried to use black map warpout functions. Divi: " + divi + " targetid: " + dwTargetField);
                        }
                    } else if (divi == 20100) {
                        if (dwTargetField == 104000000) {
                            c.announce(UserLocal.SetDirectionMode(false));
                            c.announce(UserLocal.SetStandAloneMode(false));
                            warp = true;
                        } else {
                            AutobanFactory.PACKET_EDIT.alert(player, "Tried to use black map warpout functions. Player Map: " + player.getMapId() + " Divi: " + divi + " targetid: " + dwTargetField);
                        }
                    } else if (divi == 9130401) { // Only allow warp if player is already in Intro map, or else = hack
                        if (dwTargetField == 130000000 || dwTargetField / 100 == 9130401) { // Cygnus introduction
                            warp = true;
                        } else {
                            AutobanFactory.PACKET_EDIT.alert(player, "Tried to use black map warpout functions. Player Map: " + player.getMapId() + " Divi: " + divi + " targetid: " + dwTargetField);
                        }
                    } else if (divi == 9140900) { // Aran Introduction
                        if (dwTargetField == 914090011 || dwTargetField == 914090012 || dwTargetField == 914090013 || dwTargetField == 140090000) {
                            warp = true;
                        } else {
                            AutobanFactory.PACKET_EDIT.alert(player, "Tried to use black map warpout functions. Player Map: " + player.getMapId() + " Divi: " + divi + " targetid: " + dwTargetField);
                        }
                    } else if (divi / 10 == 1020) { // Adventurer movie clip Intro
                        if (dwTargetField == 1020000) {
                            warp = true;
                        } else {
                            AutobanFactory.PACKET_EDIT.alert(player, "Tried to use black map warpout functions. Player Map: " + player.getMapId() + " Divi: " + divi + " targetid: " + dwTargetField);
                        }
                    } else if (divi / 10 >= 980040 && divi / 10 <= 980045) {
                        if (dwTargetField == 980040000) {
                            warp = true;
                        } else {
                            AutobanFactory.PACKET_EDIT.alert(player, "Tried to use black map warpout functions. Player Map: " + player.getMapId() + " Divi: " + divi + " targetid: " + dwTargetField);
                        }
                    } else if (divi == 1060200) { // Mushroom Castle.
                        if (player.getMapId() == 106020001 && dwTargetField == 106020000) {
                            warp = true;
                        } else {
                            AutobanFactory.PACKET_EDIT.alert(player, "Tried to use black map warpout functions. Player Map: " + player.getMapId() + " Divi: " + divi + " targetid: " + dwTargetField);
                        }
                    } else if (divi == 1060205) { // Mushroom Castle.
                        if (player.getMapId() == 106020502 && dwTargetField == 106020501) {
                            warp = true;
                        } else {
                            AutobanFactory.PACKET_EDIT.alert(player, "Tried to use black map warpout functions. Player Map: " + player.getMapId() + " Divi: " + divi + " targetid: " + dwTargetField);
                        }
                    } else if (divi == 9000901) { // Promise Dragon shit
                        switch (dwTargetField) {
                            case 100030100:
                                warp = true;
                                break;
                            case 900010200:
                                warp = true;
                                break;
                            case 900020200:
                                warp = true;// Piglet shit
                                break;
                            case 900020110:
                                warp = true;// Dragon egg
                                break;
                            case 900090100:
                                warp = true;
                                break;
                            case 900090200:
                                warp = true;
                                break;
                            default:
                                AutobanFactory.PACKET_EDIT.alert(player, "Tried to use black map warpout functions. Player Map: " + player.getMapId() + " Divi: " + divi + " targetid: " + dwTargetField);
                                break;
                        }
                    } else {
                        MapleMap to = c.getChannelServer().getMap(dwTargetField);
                        if (to != null) {
                            player.changeMap(to, to.getPortal(0));
                            return;
                        }
                    }
                    if (warp) {
                        final MapleMap to = c.getChannelServer().getMap(dwTargetField);
                        player.changeMap(to, to.getPortal(0));
                    }
                }
                if (portal != null && !portal.getPortalStatus()) {
                    c.announce(Field.TransferFieldReqIgnored(1));
                    c.announce(WvsContext.enableActions());
                    return;
                }
                if (player.getMapId() == 109040004) {
                    player.getFitness().resetTimes();
                }
                if (player.getMapId() == 109030003 || player.getMapId() == 109030103) {
                    player.getOla().resetTimes();
                }
                if (portal != null) {
                    if (portal.getPosition().distanceSq(player.getPosition()) > 400000) {
                        AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a portal far away.");
                        c.announce(WvsContext.enableActions());
                        return;
                    }
                    portal.enterPortal(c);
                } else {
                    c.announce(WvsContext.enableActions());
                }
                player.getStats().recalcLocalStats(player);
            } catch (Exception e) {
                Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
            }
        }
    }
}
