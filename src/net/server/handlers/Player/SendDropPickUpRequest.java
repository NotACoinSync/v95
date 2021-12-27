package net.server.handlers.Player;

import java.awt.Point;
import java.util.Arrays;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.RSSkill;
import client.autoban.AutobanFactory;
import client.autoban.AutobanManager;
import client.inventory.ItemFactory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.FeatureSettings;
import constants.GameConstants;
import constants.ItemConstants;
import net.AbstractMaplePacketHandler;
import net.server.world.MaplePartyCharacter;
import scripting.item.ItemScriptManager;
import server.ItemData;
import server.ItemInformationProvider;
import server.ItemInformationProvider.scriptedItem;
import server.MapleInventoryManipulator;
import server.maps.MapleMapItem;
import server.maps.objects.MapleMapObject;
import server.maps.objects.MapleMapObjectType;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.AriantArena;
import tools.packets.WvsContext;
import tools.packets.CField.DropPool;

public class SendDropPickUpRequest extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(final SeekableLittleEndianAccessor slea, final MapleClient c) {
        MapleCharacter player = c.getPlayer();
        AutobanManager abm = player.getAutobanManager();
        byte FieldKey = slea.readByte();
        int timestamp = slea.readInt(); // TODO: Readd
        abm.setTimestamp(0, timestamp, 3);
        Point playerPosition = slea.readPos();
        int DropID = slea.readInt();
        int CliCrc = slea.readInt(); // Check CRC

        MapleMapObject object = player.getMap().getMapObject(DropID);
        if (object == null) {
            return;
        }
        if (object instanceof MapleMapItem) {
            MapleMapItem mapItem = (MapleMapItem) object;
            pickUpItem(c, null, object, mapItem, playerPosition, false);
        }
        c.announce(WvsContext.enableActions());
    }

    static boolean useItem(final MapleClient c, final int id) {
        if (id / 1000000 == 2) {
            ItemInformationProvider ii = ItemInformationProvider.getInstance();
            if (ii.getItemData(id).consumeOnPickup) {
                if (id > 2022430 && id < 2022434) {
                    for (MapleCharacter mc : c.getPlayer().getMap().getCharacters()) {
                        if (mc.getParty() == c.getPlayer().getParty()) {
                            ii.getItemData(id).itemEffect.applyTo(mc);
                        }
                    }
                } else {
                    ii.getItemData(id).itemEffect.applyTo(c.getPlayer());
                }
                return true;
            }
        }
        return false;
    }

    public void pickUpItem(MapleClient c, MaplePet pet, MapleMapObject ob, MapleMapItem mapitem, Point pos, boolean itemVac) {
        boolean usedPet = pet != null;
        MapleCharacter player = c.getPlayer();

        if (!FeatureSettings.LOOTING) {
            c.announce(WvsContext.enableActions());
            return;
        }

        if (ob.getVisibleTo() >= 0 && (ob.getVisibleTo() != player.getId())) {
            return;
        }

        if (mapitem.getItemId() == 4031865 // Nexon Game Card - 100 points
                || mapitem.getItemId() == 4031866 // Nexon Game Card - 250 points
                || mapitem.getMeso() > 0
                || (mapitem.getMeso() <= 0 && ItemInformationProvider.getInstance().getItemData(mapitem.getItemId()).consumeOnPickup)
                || c.getPlayer().canHoldItem(mapitem.getItem())) {
            if (player.getMapId() > 980000000 || (player.getMapId() > 925020000 && player.getMapId() < 925033600)) { // MCPQ, Dojo
                if (mapitem.getItemId() == 2022163 || mapitem.getItemId() == 2022433) { // Party All Cure Potion
                    if (player.isInParty()) {
                        for (MaplePartyCharacter pchr : player.getParty().getMembers()) {
                            if (pchr.isOnline()) {
                                MapleCharacter pChrI = pchr.getPlayerInChannel();
                                if (pChrI != null) {
                                    pChrI.dispelDebuffs();
                                }
                            }
                        }
                    }
                }
            }

            if ((player.getMapId() > 209000000 && player.getMapId() < 209000016) || (player.getMapId() >= 990000500 && player.getMapId() <= 990000502)) {// happyville trees and guild PQ
                if (!mapitem.isPlayerDrop() || mapitem.getDropper().getObjectId() == c.getPlayer().getObjectId()) {
                    if (mapitem.getMeso() > 0) {
                        player.gainMeso(mapitem.getMeso(), true, true, false);
                        if (usedPet) {
                            player.getMap().announce(DropPool.LeaveField(mapitem.getObjectId(), DropPool.PickedUpByPet, player.getId(), player.getPetIndex(pet)), mapitem.getPosition());
                        } else {
                            player.getMap().announce(DropPool.LeaveField(mapitem.getObjectId(), DropPool.PickedUpByUser, player.getId()), mapitem.getPosition());
                        }
                        player.getMap().removeMapObject(ob);
                        mapitem.setPickedUp(true);
                    } else if (c.getPlayer().canHoldItem(mapitem.getItem())) {
                        MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), false);
                        if (usedPet) {
                            player.getMap().announce(DropPool.LeaveField(mapitem.getObjectId(), DropPool.PickedUpByPet, player.getId(), player.getPetIndex(pet)), mapitem.getPosition());
                        } else {
                            player.getMap().announce(DropPool.LeaveField(mapitem.getObjectId(), DropPool.PickedUpByUser, player.getId()), mapitem.getPosition());
                        }
                        player.getMap().removeMapObject(ob);
                        mapitem.setPickedUp(true);
                    } else {
                        return;
                    }
                } else {
                    c.announce(WvsContext.InventoryOperationFull());
                    c.announce(WvsContext.OnMessage.getShowInventoryFull());
                    return;
                }
                return;
            }

            synchronized (mapitem) {
                ItemInformationProvider ii = ItemInformationProvider.getInstance();
                if (mapitem.isPickedUp()) {
                    c.announce(WvsContext.InventoryOperationFull());
                    c.announce(WvsContext.OnMessage.getShowInventoryFull());
                    return;
                }
                // if player drop > 5 second wait before other people can loot(dropper ignores)
                // if mob drop && hasn't been 900ms(item drop animation) > cancel
                // if mob drop && you aren't in a party with killer/aren't killer && hasn't been 10 seconds > cancel
                // if looter || mob killer == ironman && owner != partyid/chrid > cancel
                boolean dropOwner = mapitem.getOwner() == player.getId() || mapitem.getOwner() == player.getPartyId();
                long timeSinceDropped = System.currentTimeMillis() - mapitem.getDropTime();

                if (mapitem.isPlayerDrop()) {
                    if (timeSinceDropped < 5000 && !dropOwner) {
                        return;// drop cooldown
                    }
                } else {
                    if (timeSinceDropped < 900) {
                        return;// animation cooldown
                    }
                    if (!dropOwner && mapitem.getDropType() != 2) {
                        if (timeSinceDropped < 10000) {
                            return;// 10 seconds before other players can steal your loot.
                        }
                    }
                }

                if ((mapitem.isIronMan() || player.isIronMan()) && !dropOwner) {
                    return;// Iron man check
                }

                if (mapitem.getQuest() > 0 && !player.needQuestItem(mapitem.getQuest(), mapitem.getItemId())) {
                    if (!usedPet) {
                        c.announce(WvsContext.OnMessage.showItemUnavailable());
                    }
                    return;
                }

                if (usedPet) {
                    if (mapitem.getDropper() == c.getPlayer()) {
                        return;
                    }
                    //
                    if (mapitem.getMeso() <= 0 && player.getInventory(MapleInventoryType.EQUIPPED).findById(1812001) == null) {
                        return;
                    } else if (mapitem.getMeso() > 0 && player.getInventory(MapleInventoryType.EQUIPPED).findById(1812000) == null) {
                        return;
                    }
                }

                if (mapitem.getMeso() <= 0 && mapitem.getItem() != null) {
                    if (!mapitem.isPlayerDrop()) {
                        if (player.canHoldItem(mapitem.getItem())) {
                            player.gainRSSkillExp(RSSkill.Capacity, 1);
                        }
                    }
                    if (mapitem.getItem() != null) {
                        if (mapitem.getItemId() == 4031530 || mapitem.getItemId() == 4031865 || mapitem.getItemId() == 4031531 || mapitem.getItemId() == 4031866) {
                            int nx = mapitem.getItemId() == 4031530 || mapitem.getItemId() == 4031865 ? 100 : 250;
                            nx *= mapitem.getItem().getQuantity();
                            player.getCashShop().gainCash(GameConstants.MAIN_NX_TYPE, nx);
                            player.dropMessage(MessageType.TITLE, nx + " NX");
                            if (usedPet) {
                                player.getMap().announce(DropPool.LeaveField(mapitem.getObjectId(), DropPool.PickedUpByPet, player.getId(), player.getPetIndex(pet)), mapitem.getPosition());
                            } else {
                                player.getMap().announce(DropPool.LeaveField(mapitem.getObjectId(), DropPool.PickedUpByUser, player.getId()), mapitem.getPosition());
                            }
                            player.getMap().removeMapObject(ob);
                            mapitem.setPickedUp(true);
                            return;
                        }
                        if (!mapitem.isPlayerDrop()) {
                            if (mapitem.getItemId() == 4000514 || mapitem.getItemId() == 4032473) {
                                int exp = mapitem.getItemId() == 4000514 ? 4 : 8;
                                exp *= mapitem.getItem().getQuantity();
                                player.gainRSSkillExp(RSSkill.Prayer, exp);
                                if (usedPet) {
                                    player.getMap().announce(DropPool.LeaveField(mapitem.getObjectId(), DropPool.PickedUpByPet, player.getId(), player.getPetIndex(pet)), mapitem.getPosition());
                                } else {
                                    player.getMap().announce(DropPool.LeaveField(mapitem.getObjectId(), DropPool.PickedUpByUser, player.getId()), mapitem.getPosition());
                                }
                                player.getMap().removeMapObject(ob);
                                mapitem.setPickedUp(true);
                                return;
                            }
                        }
                        if (!mapitem.getItem().getLog().contains("Legendary") && !mapitem.getItem().getLog().contains("Rare")) {
                            if (player.getAutoSell() && player.getClient().checkEliteStatus()) {
                                if (!ItemConstants.AUTO_SELL_BLACKLIST.contains(mapitem.getItemId())) {
                                    if (player.isItemAutoSellable(mapitem.getItemId())) {
                                        int price = ii.getItemData(mapitem.getItemId()).wholePrice;
                                        price *= 0.85;
                                        if (player.getMeso() + price > 0) {
                                            player.gainMeso(price, true);
                                            mapitem.setPickedUp(true);
                                            if (usedPet) {
                                                player.getMap().announce(DropPool.LeaveField(mapitem.getObjectId(), DropPool.PickedUpByPet, player.getId(), player.getPetIndex(pet)), mapitem.getPosition());
                                            } else {
                                                player.getMap().announce(DropPool.LeaveField(mapitem.getObjectId(), DropPool.PickedUpByUser, player.getId()), mapitem.getPosition());
                                            }
                                            player.getMap().removeMapObject(ob);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!itemVac) {
                    final double distance = pos.distanceSq(mapitem.getPosition());
                    if (distance > 5000 && (mapitem.getMeso() > 0 || mapitem.getItemId() != 4001025)) {
                        player.getAutobanManager().addPoint(AutobanFactory.ITEM_VAC, "Item Vac " + distance + " distance.");
                    } else if (distance > 640000.0) {
                        player.getAutobanManager().addPoint(AutobanFactory.ITEM_VAC, "Item Vac " + distance + " distance.");
                    }
                }

                if (usedPet && player.getInventory(MapleInventoryType.EQUIPPED).findById(1812007) != null) {
                    for (int i : pet.getExceptionList()) {
                        if ((mapitem.getItem() != null && mapitem.getItem().getItemId() == i) || (mapitem.getMeso() > 0 && i == 2147483647)) {
                            return;
                        }
                    }
                }

                if (mapitem.getMeso() > 0) {
                    if (player.isInParty()) {
                        int mesosamm = mapitem.getMeso();
                        if (mesosamm > 50000 * player.getStats().getMesoRate()) {
                            mesosamm = 50000;
                        }
                        double partynum = 0;
                        for (MaplePartyCharacter partymem : player.getParty().getMembers()) {
                            if (partymem.isOnline() && partymem.getMapId() == player.getMap().getId() && partymem.getChannel() == c.getChannel()) {
                                partynum++;
                            }
                        }
                        for (MaplePartyCharacter partymem : player.getParty().getMembers()) {
                            if (partymem.isOnline() && partymem.getMapId() == player.getMap().getId()) {
                                MapleCharacter somecharacter = c.getChannelServer().getPlayerStorage().getCharacterById(partymem.getId());
                                if (somecharacter != null) {
                                    int meso = (int) (mesosamm / partynum);
                                    player.battleAnaylsis.addMeso(meso);
                                    somecharacter.gainMeso(meso, true, true, false);
                                }
                            }
                        }
                    } else {
                        player.battleAnaylsis.addMeso(mapitem.getMeso());
                        player.gainMeso(mapitem.getMeso(), true, true, false);
                    }
                } else if (mapitem.getItem().getItemId() / 10000 == 243) {
                    scriptedItem info = ii.getItemData(mapitem.getItem().getItemId()).scriptedItem;
                    if (info.runOnPickup()) {
                        ItemScriptManager ism = ItemScriptManager.getInstance();
                        String scriptName = info.getScript();
                        if (ism.scriptExists(scriptName)) {
                            ism.getItemScript(c, scriptName);
                        }
                    } else {
                        if (!c.getPlayer().canHoldItem(mapitem.getItem())) {
                            return;
                        } else {
                            MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true, false);
                            ItemFactory.updateItemOwner(player, mapitem.getItem(), ItemFactory.INVENTORY);
                        }
                    }
                } else if (useItem(c, mapitem.getItem().getItemId())) {
                    if (mapitem.getItem().getItemId() / 10000 == 238) {
                        player.getMonsterBook().addCard(c, mapitem.getItem().getItemId());
                    }
                } else if (c.getPlayer().canHoldItem(mapitem.getItem())) {
                    MapleInventoryManipulator.addFromDrop(c, mapitem.getItem(), true, false);
                    ItemFactory.updateItemOwner(player, mapitem.getItem(), ItemFactory.INVENTORY);
                } else if (mapitem.getItem().getItemId() == 4031868) {
                    player.getMap().announce(AriantArena.UserScore(false, player.getName(), player.getItemQuantity(4031868, false)));
                } else {
                    c.announce(WvsContext.enableActions());
                    return;
                }

                if (mapitem.getMeso() <= 0) {
                    ItemData data = ItemInformationProvider.getInstance().getItemData(mapitem.getItemId());
                    if (data.onlyOnePickup) {
                        // Loop through drops on the map, and expire all of the other only one pickup items.
                        player.getMap().getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.ITEM)).stream().filter((mmo) -> {
                            if (mmo instanceof MapleMapItem) {
                                if (mmo.getObjectId() != ob.getObjectId() && ItemInformationProvider.getInstance().getItemData(((MapleMapItem) mmo).getItemId()).onlyOnePickup) {
                                    return true;
                                }
                            }
                            return false;
                        }).forEach(mmo -> {
                            if (mmo instanceof MapleMapItem) {
                                ((MapleMapItem) mmo).setPickedUp(true);
                                player.getMap().announce(DropPool.LeaveField(mmo.getObjectId(), 0, 0));
                                player.getMap().removeMapObject(mmo);
                            }
                        });
                    }
                }

                mapitem.setPickedUp(true);

                if (usedPet) {
                    player.getMap().announce(DropPool.LeaveField(mapitem.getObjectId(), DropPool.PickedUpByPet, player.getId(), player.getPetIndex(pet)), mapitem.getPosition());
                } else {
                    player.getMap().announce(DropPool.LeaveField(mapitem.getObjectId(), DropPool.PickedUpByUser, player.getId()), mapitem.getPosition());
                }

                player.getMap().removeMapObject(ob);
            }
        }
    }
}
