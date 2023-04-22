/**
 * andrewId: qinlinj
 * author: Justin Jia
 */

public enum CardType {
    HERO("HERO"),
    MINION("MINION"),
    SPELL("SPELL"),
    ENCHANTMENT("ENCHANTMENT"),
    WEAPON("WEAPON"),
    HERO_POWER("HERO POWER");

    private final String name;

    CardType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
