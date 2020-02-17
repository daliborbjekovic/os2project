package com.etf.os2.project.scheduler;

import com.etf.os2.project.process.Pcb;

public abstract class Scheduler {
	public abstract Pcb get(int cpuId);

	public abstract void put(Pcb pcb);

	public static Scheduler createScheduler(String[] args) {
		String scheduler = args[0];
		if (scheduler.equals("SJFS")) {
			if (args.length < 3) {
				java.lang.System.out.println("Not enough arguments \n");
				java.lang.System.exit(-99);
			}
			return new SJFScheduler(Double.parseDouble(args[1]), Integer.parseInt(args[2]) == 1);
		} else if (scheduler.equals("CFS")) {
			return new CFScheduler();
		} else if (scheduler.equals("MFQS")) {
			if (args.length < 2) {
				java.lang.System.out.println("Not enough arguments \n");
				java.lang.System.exit(-98);
			}
			int numOfQueues = Integer.parseInt(args[1]);
			if (args.length < 2 + numOfQueues) {
				java.lang.System.out.println("Not enough arguments \n");
				java.lang.System.exit(-97);
			}
			long[] qs = new long[numOfQueues];
			for (int i = 0; i < numOfQueues; i++)
				qs[i] = Long.parseLong(args[i + 2]);
			return new MFQScheduler(numOfQueues, qs);

		} else {
			java.lang.System.out.println("Invalid scheduler type\n");
			java.lang.System.exit(-100);
		}
		return null;

		/*
		 * long [] qs= new long[3]; qs[0]= 30; qs[1]=60; qs[2]=90; //return new
		 * MFQScheduler(3, qs); //return new SJFScheduler(0.5,false); return new
		 * CFScheduler();
		 */
	}

}