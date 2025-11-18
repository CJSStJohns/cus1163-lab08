import java.io.*;
import java.util.*;

public class MemoryAllocationLab {

    // ================================
    // Memory Block Class (Provided)
    // ================================
    private static class MemoryBlock {
        int start;
        int size;
        String processName; // null = free

        MemoryBlock(int start, int size, String processName) {
            this.start = start;
            this.size = size;
            this.processName = processName;
        }

        boolean isFree() {
            return processName == null;
        }

        int end() {
            return start + size - 1;
        }
    }

    // ================================
    // Global Variables
    // ================================
    private static ArrayList<MemoryBlock> memory = new ArrayList<>();
    private static int totalMemory = 0;

    private static int successfulAllocations = 0;
    private static int failedAllocations = 0;

    // ================================
    // TODO 1 & TODO 2 - Process Requests
    // ================================
    private static void processRequests(String filename) {

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

            // Read total memory size
            totalMemory = Integer.parseInt(br.readLine().trim());

            System.out.println("========================================");
            System.out.println("Memory Allocation Simulator (First-Fit)");
            System.out.println("========================================\n");

            System.out.println("Reading from: " + filename);
            System.out.println("Total Memory: " + totalMemory + " KB");
            System.out.println("----------------------------------------\n");
            System.out.println("Processing requests...\n");

            // Initialize memory with one large free block
            memory.add(new MemoryBlock(0, totalMemory, null));

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(" ");

                if (parts[0].equals("REQUEST")) {
                    String process = parts[1];
                    int size = Integer.parseInt(parts[2]);
                    allocate(process, size);
                } else if (parts[0].equals("RELEASE")) {
                    String process = parts[1];
                    deallocate(process);
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    // ================================
    // TODO 2A - First-Fit Allocation
    // ================================
    private static void allocate(String process, int size) {
        for (int i = 0; i < memory.size(); i++) {
            MemoryBlock block = memory.get(i);

            // Must be free & large enough
            if (block.isFree() && block.size >= size) {

                // Split block if larger
                if (block.size > size) {
                    int remaining = block.size - size;

                    // Allocate part of it
                    block.size = size;
                    block.processName = process;

                    // Create leftover block
                    MemoryBlock leftover =
                        new MemoryBlock(block.start + size, remaining, null);
                    memory.add(i + 1, leftover);

                } else {
                    // Exact fit
                    block.processName = process;
                }

                System.out.println("REQUEST " + process + " " + size + " KB → SUCCESS");
                successfulAllocations++;
                return;
            }
        }

        // Not enough free space
        System.out.println("REQUEST " + process + " " + size + " KB → FAILED (insufficient memory)");
        failedAllocations++;
    }

    // ================================
    // Deallocate
    // ================================
    private static void deallocate(String process) {

        for (int i = 0; i < memory.size(); i++) {
            MemoryBlock block = memory.get(i);

            if (!block.isFree() && block.processName.equals(process)) {
                block.processName = null;

                System.out.println("RELEASE " + process + " → SUCCESS");

                // Optional - Helps reduce fragmentation
                mergeAdjacentBlocks();
                return;
            }
        }

        System.out.println("RELEASE " + process + " → FAILED (not found)");
    }

    // ================================
    // Bonus: Merge Adjacent Free Blocks
    // ================================
    private static void mergeAdjacentBlocks() {
        for (int i = 0; i < memory.size() - 1; i++) {
            MemoryBlock a = memory.get(i);
            MemoryBlock b = memory.get(i + 1);

            if (a.isFree() && b.isFree()) {
                a.size += b.size;
                memory.remove(i + 1);
                i--;
            }
        }
    }

    // ================================
    // Final Statistics (Provided)
    // ================================
    private static void displayStatistics() {

        System.out.println("\n========================================");
        System.out.println("Final Memory State");
        System.out.println("========================================");

        int blockNum = 1;
        int allocated = 0;
        int freeMem = 0;
        int freeBlocks = 0;
        int largestFree = 0;

        for (MemoryBlock b : memory) {
            String status = (b.isFree())
                    ? "FREE (" + b.size + " KB)"
                    : b.processName + " (" + b.size + " KB) - ALLOCATED";

            System.out.printf("Block %d: [%d-%d]   %s\n",
                    blockNum++, b.start, b.end(), status);

            if (b.isFree()) {
                freeMem += b.size;
                freeBlocks++;
                if (b.size > largestFree) largestFree = b.size;
            } else {
                allocated += b.size;
            }
        }

        double allocatedPct = (allocated * 100.0) / totalMemory;
        double freePct = (freeMem * 100.0) / totalMemory;
        double fragmentation = (freeMem - largestFree) * 100.0 / totalMemory;

        System.out.println("\n========================================");
        System.out.println("Memory Statistics");
        System.out.println("========================================");
        System.out.printf("Total Memory:           %d KB\n", totalMemory);
        System.out.printf("Allocated Memory:       %d KB (%.2f%%)\n", allocated, allocatedPct);
        System.out.printf("Free Memory:            %d KB (%.2f%%)\n", freeMem, freePct);
        System.out.printf("Number of Processes:    %d\n", (int) memory.stream().filter(b -> !b.isFree()).count());
        System.out.printf("Number of Free Blocks:  %d\n", freeBlocks);
        System.out.printf("Largest Free Block:     %d KB\n", largestFree);
        System.out.printf("External Fragmentation: %.2f%%\n\n", fragmentation);

        System.out.printf("Successful Allocations: %d\n", successfulAllocations);
        System.out.printf("Failed Allocations:     %d\n", failedAllocations);
        System.out.println("========================================");
    }

    // ================================
    // Main (Provided)
    // ================================
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java MemoryAllocationLab <inputfile>");
            return;
        }

        processRequests(args[1]);
        displayStatistics();
    }
}
