package su.sergiusonesimus.recreate.compat.nei;

import codechicken.nei.api.IConfigureNEI;
import su.sergiusonesimus.recreate.Tags;

public class CreateNEI implements IConfigureNEI {

    @Override
    public void loadConfig() {}

    @Override
    public String getName() {
        return "Create NEI Plugin";
    }

    @Override
    public String getVersion() {
        return Tags.VERSION;
    }
}
