package qu4lizz.taskscheduler.utils;

import qu4lizz.taskscheduler.exceptions.CycleException;

import java.util.HashMap;
import java.util.HashSet;

public class Graph<T> {
    private HashMap<T, HashSet<T>> graph = new HashMap<>();

    public void addTransition(T from, T to) {
        if (!graph.containsKey(from))
            graph.put(from, new HashSet<>());
        graph.get(from).add(to);
        if (isCyclic(from, new HashSet<>(), new HashSet<>())) {
            graph.get(from).remove(to);
            System.out.println("Deadlock");
        }
    }

    public void removeNode(T val) {
        graph.remove(val);
    }

    public boolean hasTransition(T from, T to) {
        return graph.containsKey(from) && graph.get(from).contains(to);
    }

    private boolean isCyclic(T node, HashSet<T> visited, HashSet<T> recursionStack) {
        if (!visited.contains(node)) {
            visited.add(node);
            recursionStack.add(node);
            if (graph.containsKey(node))
                for (var child : graph.get(node))
                    if (!visited.contains(child) && isCyclic(child, visited, recursionStack))
                        return true;
                    else if (recursionStack.contains(child))
                        return true;
        }
        recursionStack.remove(node);
        return false;
    }

}
