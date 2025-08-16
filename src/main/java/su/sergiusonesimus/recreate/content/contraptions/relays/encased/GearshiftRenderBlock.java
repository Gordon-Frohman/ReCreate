package su.sergiusonesimus.recreate.content.contraptions.relays.encased;

import su.sergiusonesimus.recreate.AllModelTextures;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.AbstractShaftModel;

public class GearshiftRenderBlock extends SplitShaftRenderBlock {

    public GearshiftRenderBlock(int blockComplexRenderID) {
        super(blockComplexRenderID);
    }

    public AbstractShaftModel getUnlitModel() {
        return new EncasedShaftModel(AllModelTextures.GEARSHIFT);
    }

    public AbstractShaftModel getLitModel() {
        return new EncasedShaftModel(AllModelTextures.LIT_GEARSHIFT);
    }
}
