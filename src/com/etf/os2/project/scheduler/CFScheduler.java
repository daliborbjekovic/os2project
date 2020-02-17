package com.etf.os2.project.scheduler;

import java.util.ArrayList;
import java.util.Iterator;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.PcbData;

public class CFScheduler extends Scheduler {

	private ArrayList<Pcb> readyPQueue;

	public CFScheduler() {
		readyPQueue = new ArrayList<>();
	}

	@Override
	public Pcb get(int cpuId) {
		Pcb ret = getFirstFromPriorityQueueWithAffinity(cpuId);
		if (ret != null) {
			ret.getPcbData().setTimeOfLastGet(Pcb.getCurrentTime());
			ret.setTimeslice((ret.getPcbData().getTimeOfLastGet() - ret.getPcbData().getTimeOfLastPut())
					/ Pcb.getProcessCount());
		}
		return ret;
	}

	@Override
	public void put(Pcb pcb) {
		if (pcb.getPreviousState() == Pcb.ProcessState.CREATED)
			pcb.setPcbData(new PcbData());
		pcb.getPcbData().setTimeOfLastPut(Pcb.getCurrentTime());
		if (pcb.getPreviousState() == Pcb.ProcessState.BLOCKED || pcb.getPreviousState() == Pcb.ProcessState.CREATED) {
			pcb.getPcbData().setCurrentBurstExecutionTime(0);
		} else if (pcb.getPreviousState() == Pcb.ProcessState.RUNNING) {
			pcb.getPcbData().setCurrentBurstExecutionTime(pcb.getPcbData().getCurrentBurstExecutionTime()
					+ pcb.getPcbData().getTimeOfLastPut() - pcb.getPcbData().getTimeOfLastGet());
		}
		putInPriorityQueue(pcb);
	}

	private void putInPriorityQueue(Pcb pcb) {
		int i = 0;
		Iterator<Pcb> iter = readyPQueue.iterator();
		while (iter.hasNext()) {
			if (iter.next().getPcbData().getCurrentBurstExecutionTime() > pcb.getPcbData()
					.getCurrentBurstExecutionTime()) {
				readyPQueue.add(i, pcb);
				return;
			}
			i++;
		}
		readyPQueue.add(pcb);

	}

	private Pcb getFirstFromPriorityQueueWithAffinity(int cpuID) {
		Iterator<Pcb> iter = readyPQueue.iterator();
		Pcb potential, next;

		if (iter.hasNext())
			potential = iter.next();
		else
			return null;

		if (potential.getAffinity() == cpuID) {
			iter.remove();
			return potential;
		}
		while (iter.hasNext() && (next = iter.next()).getPcbData().getCurrentBurstExecutionTime() == potential
				.getPcbData().getCurrentBurstExecutionTime()) {
			if (next.getAffinity() == cpuID) {
				iter.remove();
				return next;
			}
		}
		return readyPQueue.remove(0);
	}

}
