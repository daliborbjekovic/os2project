package com.etf.os2.project.system;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.Process;
import com.etf.os2.project.process.ProcessStats;
import com.etf.os2.project.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class System {
    private final int processorNumber;
    private final List<Process> processes;
    private final List<Process> finishedProcess = new ArrayList<Process>();
    private final Cpu[] cpus;
    private final Scheduler scheduler;
    private long time = -1;

    private long numOfProccesses; //remove
    
    
    public System(Scheduler scheduler, int processorNumber, List<Process> processes) {
        this.processorNumber = processorNumber;
        this.processes = processes;
        this.scheduler = scheduler;
        
        numOfProccesses=processes.size();//remove

        Pcb.RUNNING = new Pcb[processorNumber];
        cpus = new Cpu[processorNumber];
        for (int i = 0; i < processorNumber; i++) {
            Pcb.RUNNING[i] = Pcb.IDLE;
            cpus[i] = new Cpu(i);
        }
    }

    public void work() {
        while (processes.size() > 0) {
            long nextActivation = getNextActivation();
            assert nextActivation != Long.MAX_VALUE;

            if (nextActivation > time) {
                time = nextActivation;
            } else {
                time++;
            }

            stepExecution();

            preempt();

            finishProcesses();
        }

        for (Process proc : finishedProcess) {
            proc.writeResults();
        }
        java.lang.System.out.println("Avrage response time is "+ProcessStats.AvrageResponseTime/numOfProccesses);
    }

    private void finishProcesses() {
        Iterator<Process> iter = processes.iterator();
        while (iter.hasNext()) {
            Process p = iter.next();
            if (p.isFinished()) {
                iter.remove();
                finishedProcess.add(p);
                Process.removeProcess();
            }
        }
    }

    private void preempt() {
        for (Cpu cpu : cpus) {
            cpu.tryToPreempt(scheduler);
        }
    }

    private void stepExecution() {
        for (Process p : processes) {
            p.step(scheduler, time);
        }

        for (Cpu cpu : cpus) {
            cpu.step(scheduler, time);
        }
    }

    private long getNextActivation() {
        long nextActivation = Long.MAX_VALUE;
        for (Process p : processes) {
            long next = p.getNextEventTime();
            if (next < nextActivation) {
                nextActivation = next;
            }
        }

        for (Cpu cpu : cpus) {
            long next = cpu.getNextEventTime();
            if (next < nextActivation) {
                nextActivation = next;
            }
        }
        return nextActivation;
    }
}
