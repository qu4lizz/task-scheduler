package qu4lizz.taskscheduler.resource;

import qu4lizz.taskscheduler.task.Task;
import qu4lizz.taskscheduler.utils.Graph;

import java.util.*;
import java.awt.*;
import java.lang.reflect.*;

public class Resource {
    private static Graph graph = new Graph();
    private String resourceName;
    private Integer ownerPriority = null;
    private Task owner = null;
    private PriorityQueue<Task> waitingForResource = new PriorityQueue<Task>((x, y) -> x.getPriority() - y.getPriority());

    public Resource(String resourceName) {
        this.resourceName = resourceName;
    }

    // Priority Ceiling Protocol
    public void tryLock(Task task) {
        boolean status = false;
        synchronized (this) {
            // resource is locked
            if (this.owner != null) {
                graph.addTransition(task, owner);
                // grana je usmjerena od zadatka koji ceka prema zadatku na koji ceka da oslobodi resurs
                waitingForResource.add(task);
                // inverzija prioriteta, prioritet manjeg zadatke se pamti i postavlja se na prioritet veceg onog koji zeli resurs
                if (task.getPriority() > owner.getPriority()) {
                    ownerPriority = owner.getPriority();
                    // a trenutnom vlasniku resursa dajemo veci prioritet
                    owner.setPriority(task.getPriority());
                }
                status = true;
            } // resurs nije zauzet
            else {
                this.owner = task;
            }
        }
        // da se izbjegne lock u lock-u, jer ako je resurs zauzet zadatak koji ga je trazio mora da ceka na resurs, slicno pauzi, wait
        if (status) {
            // zadatak zaustavlja izvrsavanje dok ne dobije resurs
            task.blockForResourse();
            // on postaje novi vlasnik resursa
            this.owner = task;
        }
    }

    public void unlock() {
        synchronized (this) {
            // ako je resurs zauzet, oslobodimo ga i damo ga sledecem zadatku ako ga ima
            if (owner != null) {
                // ako je != null znaci da se prioritet mijenjao i vratimo ga, zapamcen
                if (this.ownerPriority != null) {
                    this.owner.setPriority(this.ownerPriority); // vratimo zapamcenu vrijednost prioriteta
                    this.ownerPriority = null;
                }
                this.owner = null;
                // ako neko ceka na resurs, uzima se sledeci na redu
                if (!waitingForResource.isEmpty()) {
                    Task task = waitingForResource.poll();
                    // taj zadatak vise ne ceka, grana od njega ne postoji
                    graph.removeTransition(task);
                    // pozivamo handler da se zadatak nastavi
                    task.unblockForResource();
                }
            }
        }
    }
}

