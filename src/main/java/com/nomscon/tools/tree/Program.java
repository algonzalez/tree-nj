package com.nomscon.tools.tree;

public class Program {
    public static void main(String[] args) {
//        args = new String[] {
//            "--version"
//        };
        Tree tree = new Tree();
        int result = tree.run(args);
        System.exit(result);
    }
    
// TODO: handle args to:
// - include/exclude hidden files
// - control how deep to go on the sub dirs
// - use drawing characters

// see: http://mama.indstate.edu/users/ice/tree/tree.1.html
// -L level - MAX depth of the directory tree
// -P pattern - file name match filter
// -I pattern - file name ignore filter
// --prune - exclude empty dirs

// # Future
// -X - output as XML
// HTML outpug (--nolinks, -T for title)
// -D - print last modified date
// -s - Print the size of each file in bytes along with the name.
// -h - Print the size of each file but in a more human readable way, e.g. appending a size letter for kilobytes (K), megabytes (M), gigabytes (G), terabytes (T), petabytes (P) and exabytes (E).
// -Q - Quote the names of files in double quotes.
// --filelimit # - Do not descend directories that contain more than # entries.
// --timefmt format - Prints (implies -D) and formats the date according to the format string which uses the strftime(3) syntax.
// -o filename - Send output to filename.
}
