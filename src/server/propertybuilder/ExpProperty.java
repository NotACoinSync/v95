package server.propertybuilder;

import client.ExpGainType;

public class ExpProperty {

    public byte EventPercentage, PlayTimeHour,
            PartyBonusPercentage,
            QuestBonusRate, QuestBonusRemainCount,
            PartyBonusEventRate;
    public int gain,
            party,
            equip,
            cafe,
            rainbow,
            wedding,
            bonusEvent,
            PartyExpRingExp,
            CakePieEventBonus;
    public boolean show,
            inChat,
            white;
    // inChat = bOnQuest, white == bIsLastHit(Last hit has white, quest exp obv shows in chat)
    public ExpGainType type;
    public String logData;

    public ExpProperty(ExpGainType type) {
        this.type = type;
    }

    public ExpProperty gain(int gain) {
        this.gain = gain;
        return this;
    }

    public ExpProperty party(int party) {
        this.party = party;
        return this;
    }

    public ExpProperty equip(int equip) {
        this.equip = equip;
        return this;
    }

    public ExpProperty bonusEvent(int bonusEvent) {
        this.bonusEvent = bonusEvent;
        return this;
    }

    public ExpProperty cafe(int cafe) {
        this.cafe = cafe;
        return this;
    }

    public ExpProperty rainbow(int rainbow) {
        this.rainbow = rainbow;
        return this;
    }

    public ExpProperty wedding(int wedding) {
        this.wedding = wedding;
        return this;
    }

    public ExpProperty show() {
        this.show = true;
        return this;
    }

    public ExpProperty show(boolean show) {
        this.show = show;
        return this;
    }

    public ExpProperty inChat() {
        this.inChat = true;
        return this;
    }

    public ExpProperty inChat(boolean inChat) {
        this.inChat = inChat;
        return this;
    }

    public ExpProperty white() {
        this.white = true;
        return this;
    }

    public ExpProperty white(boolean white) {
        this.white = white;
        return this;
    }

    public ExpProperty logData(String logData) {
        if (this.logData != null && this.logData.length() > 0) {// incase
            this.logData += logData;
        } else {
            this.logData = logData;
        }
        return this;
    }

    @Override
    public ExpProperty clone() {
        return new ExpProperty(type).bonusEvent(bonusEvent).cafe(cafe).equip(equip).gain(gain).inChat(inChat).party(party).rainbow(rainbow).show(show).wedding(wedding).white(white);
    }
}
