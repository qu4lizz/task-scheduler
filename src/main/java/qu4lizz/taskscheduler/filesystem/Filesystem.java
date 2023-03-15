package qu4lizz.taskscheduler.filesystem;

import jdk.incubator.foreign.*;
import jnr.ffi.*;
import ru.serce.jnrfuse.FuseStubFS;

import java.util.*;

public class Filesystem extends FuseStubFS {
    private static List<String> directories = new ArrayList<>();
    private static List<String> files = new ArrayList<>();
    private static Map<String, String> filesContent = new HashMap<>();
    private static String[] args;
    private static ResourceScope rsScope = null;
    public Filesystem() {
        System.load("/usr/lib/libfuse3.so.3");

        args = new String[]{"-f", "-d", "/mnt/fuse_test/"};

        try (var scope = ResourceScope.newSharedScope()) {
            rsScope = scope;
            var arguments = Arrays.stream(args).map(s -> CLinker.toCString(s, scope)).toArray(MemorySegment[]::new);
            var allocator = SegmentAllocator.ofScope(scope);
            var argumentCount = args.length;
            var argumentSpace = allocator.allocateArray(CLinker.C_POINTER, arguments);

        }
    }

    public static boolean isDir(String path) {
       return directories.contains(path);
    }

    public static void addFile(String filename) {
        files.add(filename);
        filesContent.put(filename,"");
    }

    public static boolean isFile(String path) {
        return files.contains(path);
    }
}
