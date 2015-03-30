package com.nomscon.tools.tree;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

final class AppInfo {

    final static Package pkg;
    final static Attributes mfAttribs;

    static {
        pkg = AppInfo.class.getPackage();

        String path = Program.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        Manifest mf = null;
        try {
            JarFile jar = new JarFile(path);  // or can give a File handle
            mf = jar.getManifest();
        } catch (IOException ex) {
            // TODO: log ?
        }
        mfAttribs = (mf != null) ? mf.getMainAttributes() : null;
    }

    public static String getCopyright() {
        return getAttributeValue("x-Copyright");
    }

    public static String getDescription() {
        return getAttributeValue("x-Description");
    }

    public static String getTitle() {
        return pkg.getImplementationTitle();
    }

    public static String getVendor() {
        return pkg.getImplementationVendor();
    }

    // TODO: return SemVer?
    public static String getVersion() {
        return pkg.getImplementationVersion();
    }

    private static String getAttributeValue(String key) {
        if (mfAttribs == null || !mfAttribs.containsKey(new Name(key))) {
            return "";
        }
        return mfAttribs.getValue(key);
    }
}
