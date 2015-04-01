package com.nomscon.tools.tree;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Stack;

final class Tree {
    private final int OK = 0;
    private final int ERROR = 1;
    
    private int dirCount = 0;
    private int fileCount = 0;
    
    private TreeConfig config = null;
    private boolean isLastTopLevelDir = false;

    public int run(String[] args)
    {
        try {
            config = new TreeConfig(args);
        } catch (UsageException ex) {
            printError(ex.getMessage());
            return ERROR;
        }
        
        if (config.needsHelp()) {
            printHelp();
            return OK;
        }
        if (config.needsVersion()) {
            Log.println(AppInfo.getVersion());
            return OK;
        }
        
        //printTree();
        
        // TODO: try with walkFileTree
        
        EnumSet<FileVisitOption> options;
        // if (config.followSymLinks
        options = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        // else
        options = EnumSet.noneOf(FileVisitOption.class);

        PrintTreeFileVisitor visitor = new PrintTreeFileVisitor();
        String startDirPath = config.getStartDirPath();
        File baseDir = new File(startDirPath);
        
        // TODO: fix tildes in path
        
        Path startDir = Paths.get("c:\\dev\\java");
        
        try {
            Files.walkFileTree(startDir, options, Integer.MAX_VALUE, visitor);
        } catch (Exception e) {
            println(e.toString());
        }
        
        if (!config.skipSummaryReport()) {
            println();
            printSummary();
            int dCount = visitor.getTotalDirCount();
            int fCount = visitor.getTotalFileCount();
            println(" %s director%s%s", 
                dCount, 
                (dCount == 1) ? "y" : "ies",
                config.includeOnlyDirs()
                        ? ""
                        : String.format(", %s file%s",
                                fCount,
                                (fCount == 1) ? "" : "s"));

        }

        return OK;
    }
    
    static void print(Object value) { System.out.print(value.toString()); }
    static void print(String value) { System.out.print(value); }
    static void print(String format, Object... args) {
        System.out.print(String.format(format, args));
    }

    static void println() { System.out.println(); }
    static void println(Object value) { System.out.println(value.toString()); }
    static void println(String value) { System.out.println(value); }
    static void println(String format, Object... args) {
        System.out.println(String.format(format, args));
    }
    
    private class DirInfo {
        private int curDirCount = 0;
        private final int subDirCount;
        
        public DirInfo(int subDirCount) {
            curDirCount = 1;    // TODO: or 0 and then increment in code ???
            this.subDirCount = subDirCount;
        }
        
        public void incrDirCount() { curDirCount++; }
        
        public boolean isLastDir() {
            return curDirCount == subDirCount;
        }
    }
    
    private class PrintTreeFileVisitor extends SimpleFileVisitor<Path> {
        private int curDepth = 0;
        private int dirFileCount = 0;
        
        private int totalDirCount = 0;
        private int totalFileCount = 0;
        
        private File[] files;

        private Stack<DirInfo> subDirInfoStack = new Stack<>();
        
        private void printIndent(boolean isLast) {
            int lastDepthIndex = curDepth - 1;
            for (int i = 0; i < curDepth; i++) {
                print((i != lastDepthIndex)
                        ? "|   "
                        : isLast
                                ? "`-- "
                                : "|-- ");
            }
        }
        
        public int getTotalDirCount() { return totalDirCount; }
        public int getTotalFileCount() { return totalFileCount; }
        
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
            throws IOException
        {
            if (curDepth > 0) {
                printIndent(false);     // TODO: determine last dir
                println(dir.getFileName() + File.separator);
                totalDirCount++;
            }
            curDepth++;
            files = dir.toFile().listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isFile();
                }
            });
            dirFileCount = files.length;
            // TODO: determine if sort is needed
//            if (dirFileCount > 0)
//                Arrays.sort(files);
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException
        {
            return FileVisitResult.CONTINUE;
        }
        
        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc)
            throws IOException
        {
            File f = file.toFile();
            boolean isDir = f.isDirectory();
            if (isDir) {
                totalDirCount++;
            } else {
                totalFileCount++;
            }

            printIndent(false);     // TODO: determine isLast
            println("%s - Failed: %s", file.getFileName(), exc.getClass().getSimpleName());
            
            return isDir ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
            throws IOException
        {
            printFiles();
            curDepth--;
            return FileVisitResult.CONTINUE;
            // println("[%d|%d|%d]post: %s", curDepth, totalFileCount, totalDirCount, dir.toFile().getName());
        }
        
        // TODO: can call if dirs of files first
        private void printFiles() {
            if (dirFileCount > 0) {
                for (int i = 0; i < dirFileCount; i++) {
                    File file = files[i];
                    printIndent(dirFileCount == (i + 1));
                    println(file.getName());
                }
                totalFileCount += dirFileCount;
            }
        }
    }
    
    private int getDirCount() {
        return dirCount;
    }

    private int getFileCount() {
        return fileCount;
    }

    private String getShortVersion(String versionString) {
        if (versionString == null || versionString.isEmpty())
            versionString = "0.0.0";
        // TODO: semver
        //Version semver = Version.valueOf(versionString);
        //return semver.getNormalVersion();
        return AppInfo.getVersion();
    }

    private int incDirCount(int incAmount) {
        if (incAmount < 0) {
            return dirCount;
        }
        return dirCount += incAmount;
    }

    private int incFileCount(int incAmount) {
        if (incAmount < 0) {
            return fileCount;
        }
        return fileCount += incAmount;
    }

    private void printDir(File dir, int level, boolean isLastDir) {
        int nextLevel = level + 1;
        if (level > 0) {
            incDirCount(1);
            if (!config.skipIndent()) {
                printIndent(level);
                Log.print(isLastDir ? "`-- " : "|-- ");
            }
            Log.println(config.includeFullPath() 
                    ? dir.getAbsolutePath()
                    : dir.getName());
        }
        
        if (!config.includeOnlyDirs()) {
            FileFilter fileFilter = (config.includeAll())
                    ? new IsFileFilter()
                    : new IsVisibleFileFilter();
            File[] files = dir.listFiles(fileFilter);
            if (files != null && files.length > 0) {
                Arrays.sort(files);
                int lastIndex = files.length - 1;
                for (int i = 0; i <= lastIndex; i++) {
                    incFileCount(1);
                    printFile(files[i], nextLevel, i == lastIndex);
                }
            }
        }
        
        // TODO: handle config.excludeEmptyDirs()
        // TODO: handle config.listDirsFirst();
        
        FileFilter dirFilter = (config.includeAll())
                ? new IsDirFilter()
                : new IsVisibleDirFilter();
        File[] subDirs = dir.listFiles(dirFilter);
        if (subDirs != null && subDirs.length > 0) {
            Arrays.sort(subDirs);
            int lastIndex = subDirs.length - 1;
            for (int i = 0; i <= lastIndex; i++) {
                if (nextLevel == 1 && i == lastIndex) {
                    isLastTopLevelDir = true;
                }
                printDir(subDirs[i], nextLevel, i == lastIndex);
            }
        }
    }

    private void printError(String errorMessage) {
        printHeader();
        if (errorMessage != null && !errorMessage.isEmpty()) {
            Log.println("ERROR: " + errorMessage);
            Log.println();
        }
        printUsage();
    }
    
    private void printFile(File file, int level, boolean isLast) {
        if (!config.skipIndent()) {
            printIndent(level);
            Log.print(isLast ? "`-- " : "|-- ");
        }
        Log.print(config.includeFullPath() 
                ? file.getAbsolutePath()
                : file.getName());
        
        if (config.includeLastModifiedDate())
            Log.print(" [" + new SimpleDateFormat("yyyy-MM-dd").format(file.lastModified()) + "]");
        if (config.includeFileSize()) {
            // TODO:
            Log.print(" " + file.length());
        } else if (config.includeFileSizeInBytes()) {
            // TODO: format with commas or localized period?
            Log.print(" " + file.length() + " bytes");
        }
        
        Log.println();
    }
    
    private void printHeader() {
        Log.println(AppInfo.getTitle() + " - Version " + getShortVersion(AppInfo.getVersion()));
        Log.println(AppInfo.getCopyright());
        Log.println(AppInfo.getDescription());
        Log.println();
    }
    
    private void printHelp() {
        printHeader();
        printUsage();
    }
    
    private void printIndent(int level) {
        if (level < 1) {
            return;
        }
        for (int i = 1; i < level; i++) {
            //Log.print((i == 1 && !isLastTopLevelDir) ? "| " : "   ");
            //Log.print((i == 1) ? "| " : "   ");
            Log.print("| ");
        }
    }

    private void printSummary() {
        Log.println(String.format(" %s director%s%s", 
                dirCount, 
                (dirCount == 1) ? "y" : "ies",
                config.includeOnlyDirs()
                        ? ""
                        : String.format(", %s file%s",
                                fileCount,
                                (fileCount == 1) ? "" : "s")));
    }

    private void printTree() {
        String startDirPath = config.getStartDirPath();
        File baseDir = new File(startDirPath);
        Log.println(baseDir.getName());
        
        if (baseDir.isDirectory()) {
            printDir(baseDir, 0, false);
        }
        
        if (!config.skipSummaryReport()) {
            Log.println();
            printSummary();
        }
    }

    private void printUsage() {
        TreeConfig.printUsage();
    }
    
    class IsDirFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            return pathname != null && pathname.isDirectory();
        }
    }
    
    // ------------------------------------------------------------
    
    class IsFileFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            return pathname != null && pathname.isFile();
        }
    }
    
    class IsVisibleDirFilter extends IsDirFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            return super.accept(pathname) && !pathname.isHidden();
        }
    }
    
    class IsVisibleFileFilter extends IsFileFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            return super.accept(pathname) && !pathname.isHidden();
        }
    }

}
