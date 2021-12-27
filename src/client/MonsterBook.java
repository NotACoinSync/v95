package client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import tools.DatabaseConnection;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.userpool.UserLocal;
import tools.packets.CField.userpool.UserRemote;
import tools.packets.EffectPacket;
import tools.packets.WvsContext;

public final class MonsterBook {

    private int specialCard;
    private int normalCard = 0;
    private int bookLevel = 1;
    private Map<Integer, Integer> cards = new LinkedHashMap<>();

    public void addCard(final MapleClient c, final int cardid) {
        c.getPlayer().getMap().announce(c.getPlayer(), EffectPacket.Remote.MonsterBookCardGet(c.getPlayer().getId()));
        for (Entry<Integer, Integer> all : cards.entrySet()) {
            if (all.getKey() == cardid) {
                if (all.getValue() > 4) {
                    c.announce(WvsContext.MonsterBook.SetCard(true, cardid, all.getValue()));
                    c.getPlayer().getCashShop().gainCash(4, 1000);
                    c.getPlayer().dropMessage(MessageType.TITLE, 1000 + " NX");
                } else {
                    all.setValue(all.getValue() + 1);
                    c.announce(WvsContext.MonsterBook.SetCard(false, cardid, all.getValue()));
                    c.announce(EffectPacket.Local.MonsterBookCardGet());
                    calculateLevel();
                }
                return;
            }
        }
        cards.put(cardid, 1);
        c.announce(WvsContext.MonsterBook.SetCard(false, cardid, 1));
        c.announce(EffectPacket.Local.MonsterBookCardGet());
        calculateLevel();
        saveCards(c.getPlayer().getId());
    }

    private void calculateLevel() {
        bookLevel = (int) Math.max(1, Math.sqrt((normalCard + specialCard) / 5));
    }

    public int getBookLevel() {
        return bookLevel;
    }

    public Map<Integer, Integer> getCards() {
        return cards;
    }

    public int getTotalCards() {
        return specialCard + normalCard;
    }

    public int getNormalCard() {
        return normalCard;
    }

    public int getSpecialCard() {
        return specialCard;
    }

    public void loadCards(final int charid) throws SQLException {
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT cardid, level FROM monsterbook WHERE charid = ? ORDER BY cardid ASC")) {
            ps.setInt(1, charid);
            try (ResultSet rs = ps.executeQuery()) {
                int cardid, level;
                while (rs.next()) {
                    cardid = rs.getInt("cardid");
                    level = rs.getInt("level");
                    if (cardid / 1000 >= 2388) {
                        specialCard++;
                    } else {
                        normalCard++;
                    }
                    cards.put(cardid, level);
                }
            }
        }
        calculateLevel();
    }

    public void saveCards(final int charid) {
        if (cards.isEmpty()) {
            return;
        }
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("DELETE FROM monsterbook WHERE charid = ?");
            ps.setInt(1, charid);
            ps.execute();
            ps.close();
            boolean first = true;
            StringBuilder query = new StringBuilder();
            for (Entry<Integer, Integer> all : cards.entrySet()) {
                if (first) {
                    query.append("INSERT INTO monsterbook VALUES (");
                    first = false;
                } else {
                    query.append(",(");
                }
                query.append(charid);
                query.append(", ");
                query.append(all.getKey());
                query.append(", ");
                query.append(all.getValue());
                query.append(")");
            }
            ps = con.prepareStatement(query.toString());
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
        }
    }
}
