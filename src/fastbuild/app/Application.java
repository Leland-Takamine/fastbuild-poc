package fastbuild.app;

import exopackage.SystemClassLoaderAdder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initExo();
    }

    private void initExo() {
        List<File> dexJars = new ArrayList<>();
        dexJars.addAll(getDexJars("/data/local/tmp/fastbuild"));
        dexJars.addAll(getDexJars("/data/local/tmp/exopackage/" + getPackageName() + "/secondary-dex"));
        SystemClassLoaderAdder.installDexJars(getClassLoader(), dexJars);
    }

    private static List<File> getDexJars(String dexDirPath) {
        File dexDir = new File(dexDirPath);
        return Arrays.asList(dexDir.listFiles());
    }
}
