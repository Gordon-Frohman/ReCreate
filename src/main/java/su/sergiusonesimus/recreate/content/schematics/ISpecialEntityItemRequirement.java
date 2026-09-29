package su.sergiusonesimus.recreate.content.schematics;

public interface ISpecialEntityItemRequirement {

    default ItemRequirement getRequiredItems() {
        return ItemRequirement.INVALID;
    }

}
