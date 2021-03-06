package eod.warObject.character.concrete.blue;

import eod.Gameboard;
import eod.Party;
import eod.Player;
import eod.card.abstraction.summon.SummonCard;
import eod.card.concrete.summon.SecureGuardBotSummon;
import eod.param.AttackParam;
import eod.param.PointParam;
import eod.warObject.CanAttack;
import eod.warObject.Damageable;
import eod.warObject.Status;
import eod.warObject.WarObject;
import eod.warObject.character.abstraction.Machine;

import java.awt.*;
import java.util.ArrayList;

import static eod.effect.EffectFunctions.RequestRegionalAttack;

public class SecureGuardBot extends Machine {
    private CanAttack attacker;
    public SecureGuardBot(Player player) {
        super(player, 4, 4, Party.BLUE);
    }

    @Override
    public String getName() {
        return "維安警備機械";
    }

    @Override
    public SummonCard getSummonCard() {
        SummonCard c = new SecureGuardBotSummon();
        c.setPlayer(player);
        return c;
    }

    @Override
    public CanAttack getAttacker() {
        return attacker;
    }

    @Override
    public void attack(){
        RequestRegionalAttack(player, attack).from(this).to(getAttackRange());
    }

    @Override
    public ArrayList<Damageable> attack(ArrayList<Point> targets, AttackParam param) {
        int hp = param.hp;
        ArrayList<Damageable> affected = new ArrayList<>();
        Gameboard gameboard = player.getBoard();
        for(Point p:targets) {
            try {
                Damageable target = gameboard.getObjectOn(p.x, p.y);
                if(param.realDamage) {
                    target.damage(hp);
                } else {
                    target.attacked(this, hp);
                }
                affected.add(target);
                ((WarObject) target).addStatus(Status.ATTACKED);
            } catch (IllegalArgumentException e) {
                System.out.println(e.toString());
            }
        }
        return affected;
    }

    @Override
    public ArrayList<Point> getAttackRange() {
        PointParam param = new PointParam();
        param.range = 1;
        return player.getBoard().getSurrounding(this.position, param);
    }
}
