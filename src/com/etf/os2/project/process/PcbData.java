package com.etf.os2.project.process;

public class PcbData {

	private long estimatedNextExecutionTime;
	private long startedLastRunAt;

	public long getEstimatedExecutionTime() {
		return estimatedNextExecutionTime;
	}

	public void setEstimatedExecutionTime(long t) {
		estimatedNextExecutionTime = t;
	}

	public void setStartTime(long t) {
		startedLastRunAt = t;
	}

	public long getStartTime() {
		return startedLastRunAt;
	}

	private int currentPriority;

	public void setCurrentPriority(int c) {
		currentPriority = c;
	}

	public int getCurrentPriority() {
		return currentPriority;
	}

	private long currentBurstExecutionTime;
	private long timeOfLastGet, timeOfLastPut;

	public long getCurrentBurstExecutionTime() {
		return currentBurstExecutionTime;
	}

	public void setCurrentBurstExecutionTime(long l) {
		currentBurstExecutionTime = l;
	}

	public long getTimeOfLastGet() {
		return timeOfLastGet;
	}

	public void setTimeOfLastGet(long l) {
		timeOfLastGet = l;
	}

	public long getTimeOfLastPut() {
		return timeOfLastPut;
	}

	public void setTimeOfLastPut(long l) {
		timeOfLastPut = l;
	}
}
