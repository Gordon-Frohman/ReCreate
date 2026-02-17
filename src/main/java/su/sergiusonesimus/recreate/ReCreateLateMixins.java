package su.sergiusonesimus.recreate;

import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

import su.sergiusonesimus.recreate.zmixin.Mixins;

@LateMixin
public class ReCreateLateMixins implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.recreate.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        return Mixins.getLateMixins(loadedMods);
    }
}
