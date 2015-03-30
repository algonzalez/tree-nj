package com.nomscon.tools.tree;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Arrays;

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
        
        printTree();
        return OK;    
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
