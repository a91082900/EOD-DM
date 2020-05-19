package eod.warObject.character.abstraction.other;

import eod.Party;
import eod.Player;
import eod.warObject.character.abstraction.Character;

public abstract class Ghost extends Character {

    public Ghost(Player player, int hp) {
        super(player, hp, 0, Party.RED);
        // TODO: ask Spacezipper about the details of Ghost
    }

    @Override
    public String getName() {
        return "亡靈";
    }

    @Override
    public void attack() {
        // TODO
    }
}
