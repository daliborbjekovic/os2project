package com.etf.os2.project.scheduler;

import java.util.ArrayList;
import java.util.Iterator;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.PcbData;

public class SJFScheduler extends Scheduler {

	private ArrayList<Pcb> readyQueue;
	private double alpha;
	private boolean preemptive;

	public SJFScheduler(double alpha, boolean preemptive) {
		if (alpha < 0 || alpha > 1) {
			java.lang.System.err.println("Averaging coeficient out of bounds \n");
			java.lang.System.exit(-5);
		}
		readyQueue = new ArrayList<>(0);
		this.alpha = alpha;
		this.preemptive = preemptive;
	}

	private void putInPriorityQueue(Pcb pcb) {
		int i = 0;
		Iterator<Pcb> iter = readyQueue.iterator();
		while (iter.hasNext()) {
			if (iter.next().getPcbData().getEstimatedExecutionTime() > pcb.getPcbData().getEstimatedExecutionTime()) {
				readyQueue.add(i, pcb);
				return;
			}
			i++;
		}
		readyQueue.add(pcb);

	}

	private Pcb getFirstFromPriorityQueueWithAffinity(int cpuID) {
		Iterator<Pcb> iter = readyQueue.iterator();
		Pcb potential, next;

		if (iter.hasNext())
			potential = iter.next();
		else
			return null;

		if (potential.getAffinity() == cpuID) {
			iter.remove();
			return potential;
		}
		while (iter.hasNext() && (next = iter.next()).getPcbData().getEstimatedExecutionTime() == potential.getPcbData()
				.getEstimatedExecutionTime()) {
			if (next.getAffinity() == cpuID) {
				iter.remove();
				return next;
			}
		}
		return readyQueue.remove(0);
	}

	@Override
	public synchronized Pcb get(int cpuId) {
		// return getFirstFromPriorityQueueWithAffinity(cpuId);
		Pcb ret = getFirstFromPriorityQueueWithAffinity(cpuId);
		if (ret != null && preemptive)
			ret.getPcbData().setStartTime(Pcb.getCurrentTime());
		return ret;
	}

	@Override
	public synchronized void put(Pcb pcb) {
		if (pcb.getPreviousState() == Pcb.ProcessState.CREATED) {
			pcb.setPcbData(new PcbData());
			pcb.getPcbData().setEstimatedExecutionTime(50);
		} else if (pcb.getPreviousState() == Pcb.ProcessState.BLOCKED
				|| pcb.getPreviousState() == Pcb.ProcessState.RUNNING) {
			updateEstimatedExectuionTime(pcb);

		}
		if (preemptive && pcb.getPreviousState()!=Pcb.ProcessState.RUNNING) {
			Pcb potential = null;
			long potentialsTimeLeftFromRunning = -1;
			for (int i = 0; i < Pcb.RUNNING.length; i++) {
				if (Pcb.RUNNING[i] == Pcb.IDLE) {
					putInPriorityQueue(pcb);
					return;
				}
			}
			for (int i = 0; i < Pcb.RUNNING.length; i++) {
				Pcb p = Pcb.RUNNING[i];

				long timeLeftFromRunningP = p.getPcbData().getEstimatedExecutionTime()
						- (Pcb.getCurrentTime() - p.getPcbData().getStartTime());

				if (timeLeftFromRunningP > pcb.getPcbData().getEstimatedExecutionTime()) {
					if (potential == null
							|| (potential != null && potentialsTimeLeftFromRunning < timeLeftFromRunningP)) {
						potential = p;
						potentialsTimeLeftFromRunning = timeLeftFromRunningP;
					} else if (potential != null && potentialsTimeLeftFromRunning == timeLeftFromRunningP
							&& pcb.getAffinity() == i) {
						potential = p;
					}
				}

			}
			if (potential != null)
				potential.preempt();
		}
		putInPriorityQueue(pcb);
	}

	private long updateEstimatedExectuionTime(Pcb pcb) {
		double up = alpha * pcb.getExecutionTime() + (1 - alpha) * pcb.getPcbData().getEstimatedExecutionTime();
		pcb.getPcbData().setEstimatedExecutionTime((long) up);

		return pcb.getPcbData().getEstimatedExecutionTime();
	}

}
