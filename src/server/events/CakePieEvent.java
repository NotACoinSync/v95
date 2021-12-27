package server.events;

public class CakePieEvent extends MapleEvents {
    
    private int nFieldID, nItemID, nPercentage;
    private byte uCount, pHead, pTail, nWinnerTeam;

    public CakePieEvent() {
        super();
        this.uCount = 0;
        this.pHead = 0;
        this.pTail = 0;
        this.nFieldID = 0;
        this.nItemID = 0;
        this.nPercentage = 0;
        this.nWinnerTeam = 0;
    }

    public byte getCount() {
        return uCount;
    }

    public byte getHead() {
        return pHead;
    }

    public byte getTail() {
        return pTail;
    }

    public int getFieldID() {
        return nFieldID;
    }

    public int getItemID() {
        return nItemID;
    }

    public int getPercentage() {
        return nPercentage;
    }

    public byte getWinnerTeam() {
        return nWinnerTeam;
    }
}
