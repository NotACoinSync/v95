package server.partyquest;

import java.util.Date;

public class PartyQuestRankRecord {

    public String pq;
    public int tries;
    public int completed;
    public byte completeRate; // completed / tries
    public int fastestTime; // in seconds
    public Date fastestDate; // the date in which fastestTime occurs (based on pq end date)
    public String rank;

    public PartyQuestRankRecord(String pq, int tries, int completed, byte completeRate, int fastestTime, Date fastestDate, String rank) {
        this.pq = pq;
        this.tries = tries;
        this.completed = completed;
        this.completeRate = completeRate;
        this.fastestTime = fastestTime;
        this.fastestDate = fastestDate;
        this.rank = rank;
    }

    public PartyQuestRankRecord() {
    }
}
