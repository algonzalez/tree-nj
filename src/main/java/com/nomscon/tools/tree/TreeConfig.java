package com.nomscon.tools.tree;

import java.io.File;
import org.apache.commons.cli.*;

final class TreeConfig 
{
    private final static Options options;
    private final CommandLine cmdLine;

    private String startDirPath;
    
    static {
        options = new Options();
        defineOptions();
    }

    public TreeConfig(String[] args) throws UsageException {
        try {
            CommandLineParser parser = new PosixParser();
            cmdLine = parser.parse(options, args);
            
            String curDir = includeFullPath()
                    ? System.getProperty("user.dir")
                    : "."; 
            
            // TODO: limit to only one additional arg for the startDirPath
            String[] nonOptionArgs = cmdLine.getArgs();
            startDirPath = (nonOptionArgs.length == 0) 
                    ? curDir
                    : nonOptionArgs[0];
            File f = new File(startDirPath);
            // TODO: consider error if specified dir doesn't exist
            if (!f.exists())
                startDirPath = curDir;
        } catch (ParseException ex) {
            throw new UsageException(ex.getMessage(), ex);
        }
    }
    
    public String getStartDirPath() { return startDirPath; }
    
    public boolean excludeEmptyDirs() {
        return cmdLine.hasOption(AppOption.NO_EMPTY_DIRS_LONG);
    }

    public boolean includeAll() {
        return cmdLine.hasOption(AppOption.SHOW_ALL);
    }

    public boolean includeFileSize() {
        return cmdLine.hasOption(AppOption.INCLUDE_SIZE);
    }

    public boolean includeFileSizeInBytes() {
        return cmdLine.hasOption(AppOption.INCLUDE_SIZE_IN_BYTES);
    }

    public boolean includeFullPath() {
        return cmdLine.hasOption(AppOption.INCLUDE_FULL_PATH);
    }
    
    public boolean includeLastModifiedDate() {
        return cmdLine.hasOption(AppOption.INCLUDE_LAST_MODIFIED_DATE);
    }

    public boolean includeOnlyDirs() {
        return cmdLine.hasOption(AppOption.DIRS_ONLY)
                || cmdLine.hasOption(AppOption.DIRS_ONLY_LONG);
    }
    
    public boolean listDirsFirst() {
        return cmdLine.hasOption(AppOption.DIRS_FIRST_LONG);
    }
    
    public boolean needsHelp() {
        return cmdLine.hasOption(AppOption.HELP)
                || cmdLine.hasOption(AppOption.HELP_LONG);
    }
    
    public boolean needsVersion() {
        return cmdLine.hasOption(AppOption.VERSION_LONG);
    }
    public static void printUsage() {
        HelpFormatter optionsHelp = new HelpFormatter();
        optionsHelp.printHelp(" ", null, options, null, false);
    }
    
    public boolean skipIndent() {
        return cmdLine.hasOption(AppOption.NO_INDENT);
    }
    
    public boolean skipSummaryReport() {
        return cmdLine.hasOption(AppOption.NO_REPORT_LONG);
    }
    
    private static void defineOptions() {
        options.addOption("?", "help", false, "show this usage help");
        options.addOption(null, "version", false, "display tree version");
        
        options.addOption("a", null, false, "list all including hidden files");
        options.addOption("d", "dirsonly", false, "list only subdirectories, no files");
        options.addOption("D", null, false, "include last modified date for each file");
        options.addOption(null, "dirsfirst", false, "list directories before files");
        options.addOption("f", null, false, "add full path for each file");
        options.addOption("h", null, false, "include size for each file");
        options.addOption("i", false, "do not print lines and do not indent");
        options.addOption(null, "noreport", false, "omit summary at end of listing");
        options.addOption(null, "prune", false, "exclude empty directories");
        options.addOption("s", null, false, "include size in bytes for each file");
    }

    private final static class AppOption {
        public final static String HELP = "h";
        public final static String HELP_LONG = "help";
        public final static String DIRS_ONLY = "d";
        public final static String DIRS_ONLY_LONG = "dirsonly";
        public final static String DIRS_FIRST_LONG = "dirsfirst";
        public final static String INCLUDE_FULL_PATH = "f";
        public final static String INCLUDE_LAST_MODIFIED_DATE = "D";
        public final static String INCLUDE_SIZE = "h";
        public final static String INCLUDE_SIZE_IN_BYTES = "s";
        public final static String NO_EMPTY_DIRS_LONG = "prune";
        public final static String NO_INDENT = "i";
        public final static String NO_REPORT_LONG = "noreport";
        public final static String SHOW_ALL = "a";
        public final static String VERSION_LONG = "version";
    }
}

