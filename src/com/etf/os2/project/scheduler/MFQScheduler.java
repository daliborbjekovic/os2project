package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.PcbData;

public class MFQScheduler extends Scheduler {

	public class List {
		public class Elem {
			public Pcb myPcb;
			public Elem nextElem;

			public Elem(Pcb p, Elem n) {
				myPcb = p;
				nextElem = n;
			}
		}

		private int size;
		private Elem head, tail;

		public List() {
			size = 0;
			head = null;
			tail = null;
		}

		public void addToEnd(Pcb p) {
			if (head == null) {
				head = tail = new Elem(p, null);
				size++;
			} else {
				Elem n = new Elem(p, null);
				tail.nextElem = n;
				tail = n;
				size++;
			}
		}

		public boolean isEmpty() {
			return size == 0;
		}

		public int size() {
			return size;
		}

		public Pcb getFirst() {
			if (head == null)
				return null;
			else {
				Pcb ret = head.myPcb;
				head = head.nextElem;
				if (head == null)
					tail = null;
				size--;
				return ret;
			}
		}

	}

	private boolean firstput;
	private List[][] listTable;
	private long[] quants;
	private int NumOfQueues, NumOfProcessors;

	public MFQScheduler(int n, long[] quants) {
		if (n < 0 || quants.length != n) {
			java.lang.System.err.println("Arguments invalid \n");
			java.lang.System.exit(-6);
		}
		for (int i = 0; i < n; i++)
			if (quants[i] <= 0) {
				java.lang.System.err.println("Arguments invalid\n");
				java.lang.System.exit(-7);
			}
		firstput = true;
		NumOfQueues = n;
		this.quants = new long[n];
		for (int i = 0; i < n; i++) {
			this.quants[i] = quants[i];
		}

	}

	@Override
	public Pcb get(int cpuId) {
		for (int i = 0; i < NumOfQueues; i++) {
			if (!listTable[i][cpuId].isEmpty())
				return listTable[i][cpuId].getFirst();
			int index = -1;
			int numOfElementsAtIndex = -1;
			for (int j = 0; j < NumOfProcessors; j++) {
				if (j == cpuId)
					continue;
				if (listTable[i][j].size() > numOfElementsAtIndex) {
					index = j;
					numOfElementsAtIndex = listTable[i][j].size();
				}
			}
			if (numOfElementsAtIndex > 0)
				return listTable[i][index].getFirst();
		}
		return null;
	}

	@Override
	public void put(Pcb pcb) {
		if (firstput) {
			firstput = false;
			listTable = new List[NumOfQueues][Pcb.RUNNING.length];
			for (int i = 0; i < NumOfQueues; i++)
				for (int j = 0; j < Pcb.RUNNING.length; j++)
					listTable[i][j] = new List();
			NumOfProcessors = Pcb.RUNNING.length;
		}
		if (pcb.getPreviousState() == Pcb.ProcessState.CREATED) {
			pcb.setPcbData(new PcbData());
			int newPriority = pcb.getPriority();
			if (newPriority > NumOfQueues - 1)
				newPriority = NumOfQueues - 1;
			if (newPriority < 0)
				newPriority = 0;
			
			pcb.getPcbData().setCurrentPriority(newPriority);
		} else if (pcb.getPreviousState() == Pcb.ProcessState.BLOCKED) {
			if (pcb.getPcbData().getCurrentPriority() > 0) {
				int newPriority = pcb.getPcbData().getCurrentPriority()-1;
				pcb.getPcbData().setCurrentPriority(newPriority);
			}
		} else if (pcb.getPreviousState() == Pcb.ProcessState.RUNNING) {
			if (pcb.getPcbData().getCurrentPriority() < NumOfQueues - 1) {
				int newPriority = pcb.getPcbData().getCurrentPriority() + 1;
				pcb.getPcbData().setCurrentPriority(newPriority);
			}
		}
		pcb.setTimeslice(quants[pcb.getPcbData().getCurrentPriority()]);
		listTable[pcb.getPcbData().getCurrentPriority()][pcb.getAffinity()].addToEnd(pcb);
	}

}
