package com.village.portal.complaint.dto.response;

public class VoteResponse {
    private boolean voted;        // true = vote added, false = vote removed
    private int newSupportCount;

    public VoteResponse(boolean voted, int newSupportCount) {
        this.voted = voted;
        this.newSupportCount = newSupportCount;
    }
    public boolean isVoted()            { return voted; }
    public int getNewSupportCount()     { return newSupportCount; }
}
