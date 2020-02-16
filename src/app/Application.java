package app;

import fastbuild.Fastbuild;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fastbuild.init(this);
    }
}
