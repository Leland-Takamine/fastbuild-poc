package fastbuild;

import android.app.Application;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Fastbuild {

    private Fastbuild() {}

    public static void init(Application application) {
        List<File> dexJars = new ArrayList<>();
        dexJars.addAll(getDexJars("/data/local/tmp/fastbuild"));
        dexJars.addAll(getDexJars("/data/local/tmp/exopackage/" + application.getPackageName() + "/secondary-dex"));
        SystemClassLoaderAdder.installDexJars(application.getClassLoader(), dexJars);
    }

    private static List<File> getDexJars(String dexDirPath) {
        File dexDir = new File(dexDirPath);
        return Arrays.asList(dexDir.listFiles());
    }
}
